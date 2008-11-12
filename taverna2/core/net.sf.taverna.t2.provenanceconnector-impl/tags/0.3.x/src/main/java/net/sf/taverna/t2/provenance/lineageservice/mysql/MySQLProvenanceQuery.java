package net.sf.taverna.t2.provenance.lineageservice.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResult;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.provenance.lineageservice.LineageSQLQuery;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.utils.Arc;
import net.sf.taverna.t2.provenance.lineageservice.utils.DBconnections;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceProcessor;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;
import net.sf.taverna.t2.provenance.lineageservice.utils.VarBinding;

import org.jdom.Document;
import org.jdom.Element;



/**
 * @author paolo
 *
 */
public class MySQLProvenanceQuery implements ProvenanceQuery {

	Connection dbConn = null;
	static boolean useDB = true;

	public void setConnection(Connection openConnection) {
		this.dbConn = openConnection;
	}

	
	public MySQLProvenanceQuery() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {


		DBconnections DBconn = new DBconnections();

		if (useDB) {
			dbConn = DBconn.openConnection();
			System.out.println("successfully opened DB connection");
		}
	}


	/**
	 * implements a set of query constraints of the form var = value into a WHERE clause
	 * @param q0
	 * @param queryConstraints
	 * @return
	 */
	private String addWhereClauseToQuery(String q0, Map<String,String> queryConstraints, boolean terminate) {

		// complete query according to constraints
		StringBuffer q = new StringBuffer(q0);

		boolean first = true;
		if (queryConstraints != null && queryConstraints.size() > 0) {
			q.append(" where ");

			for (Entry<String,String> entry:queryConstraints.entrySet()) {
				if (!first) {
					q.append(" and ");
				}
				q.append(" "+entry.getKey()+" = \""+ entry.getValue()+ "\" ");
				first = false;
			}
		}
		if (terminate) q.append(";");

		return q.toString();
	}


	private String addOrderByToQuery(String q0, List<String> orderAttr, boolean terminate) {

		// complete query according to constraints
		StringBuffer q = new StringBuffer(q0);

		boolean first = true;
		if (orderAttr != null && orderAttr.size() > 0) {
			q.append(" ORDER BY ");

			int i=1;
			for (String attr: orderAttr) {
				q.append(attr);
				if (i++ < orderAttr.size()) q.append(",");
			}
		}
		if (terminate) q.append(";");

		return q.toString();
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#getVars(java.util.Map)
	 */
	public List<Var> getVars(Map<String,String> queryConstraints) throws SQLException {

		List<Var>  result = new ArrayList<Var>();

		String q0 = "SELECT * FROM Var V ";

		String q = addWhereClauseToQuery(q0, queryConstraints, true);

		Statement stmt;
		stmt = dbConn.createStatement();

		//System.out.println("getVars: executing query\n"+q.toString());

		boolean success = stmt.execute(q.toString());

		if (success) {
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				Var aVar = new Var();

				aVar.setWfInstanceRef(rs.getString("WfInstanceRef"));

				if (rs.getInt("inputOrOutput") == 1) {
					aVar.setInput(true);					
				} else {
					aVar.setInput(false);
				}	
				aVar.setPName(rs.getString("pnameRef"));
				aVar.setVName(rs.getString("varName"));
				aVar.setType(rs.getString("type"));
				aVar.setTypeNestingLevel(rs.getInt("nestingLevel"));
				aVar.setActualNestingLevel(rs.getInt("actualNestingLevel"));
				aVar.setANLset((rs.getInt("anlSet") == 1 ? true : false));

				result.add(aVar);

			}
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#getInputVars(java.lang.String, java.lang.String)
	 */
	public List<Var> getInputVars(String pname, String wfInstanceId) throws SQLException {

		// get (var, proc) from Var  to see if it's input/output
		Map<String, String>  varQueryConstraints = new HashMap<String, String>();

		varQueryConstraints.put("V.wfInstanceRef", wfInstanceId);
		varQueryConstraints.put("V.pnameRef", pname);  
		varQueryConstraints.put("V.inputOrOutput", "1");

		return getVars(varQueryConstraints);		
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#getOutputVars(java.lang.String, java.lang.String)
	 */
	public List<Var> getOutputVars(String pname, String wfInstanceId) throws SQLException {

		// get (var, proc) from Var  to see if it's input/output
		Map<String, String>  varQueryConstraints = new HashMap<String, String>();

		varQueryConstraints.put("V.wfInstanceRef", wfInstanceId);
		varQueryConstraints.put("V.pnameRef", pname);  
		varQueryConstraints.put("V.inputOrOutput", "0");

		return getVars(varQueryConstraints);		
	}



	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#getArcs(java.util.Map)
	 */
	public List<Arc>   getArcs(Map<String,String> queryConstraints) throws SQLException {

		List<Arc> result = new ArrayList<Arc>();

		String q0 = "SELECT * FROM Arc A ";

		String q = addWhereClauseToQuery(q0, queryConstraints, true);

		Statement stmt;
		stmt = dbConn.createStatement();

		//System.out.println("getArcs: executing query\n"+q.toString());

		boolean success = stmt.execute(q.toString());

		if (success) {
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				Arc aArc = new Arc();

				aArc.setWfInstanceRef(rs.getString("WfInstanceRef"));
				aArc.setSourcePnameRef(rs.getString("sourcePNameRef"));
				aArc.setSourceVarNameRef(rs.getString("sourceVarNameRef"));
				aArc.setSinkPnameRef(rs.getString("sinkPNameRef"));
				aArc.setSinkVarNameRef(rs.getString("sinkVarNameRef"));

				result.add(aArc);

			}
		}

		return result;

	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#getWFInstanceIDs()
	 */
	public List<String> getWFInstanceIDs() throws SQLException {

		List<String> result = new ArrayList<String>();

		String q = "SELECT instanceID FROM WfInstance order by timestamp desc;";

		Statement stmt;
		stmt = dbConn.createStatement();

		boolean success = stmt.execute(q);

		if (success) {
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				result.add(rs.getString("instanceID"));

			}
		}

		return result;
	}



	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#getProcBindings(java.util.Map)
	 */
	public List<ProcBinding> getProcBindings(Map<String, String> constraints) throws SQLException {

		List<ProcBinding> result = new ArrayList<ProcBinding>();

		String q = "SELECT * FROM ProcBinding PB ";

		q = addWhereClauseToQuery(q, constraints, true);

		Statement stmt;
		stmt = dbConn.createStatement();

		System.out.println("getProcBindings: executing: "+q);

		boolean success = stmt.execute(q);
		//System.out.println("result: "+success);

		if (success) {
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				ProcBinding pb = new ProcBinding();

				pb.setActName(rs.getString("actName"));
				pb.setExecIDRef(rs.getString("execIDRef"));
				pb.setIterationVector(rs.getString("iteration"));
				pb.setPNameRef(rs.getString("pnameRef"));

				result.add(pb);

			}
		}
		return result;


	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#getVarBindings(java.util.Map)
	 */
	public List<VarBinding>  getVarBindings(Map<String, String> constraints) throws SQLException {

		List<VarBinding> result = new ArrayList<VarBinding>();

		String q = "SELECT * FROM VarBinding VB join Var V "+
		"on (VB.varNameRef = V.varName and  VB.wfInstanceRef = V.wfInstanceRef and VB.PNameRef =  V.PNameRef) ";

		q = addWhereClauseToQuery(q, constraints, true);

		Statement stmt;
		stmt = dbConn.createStatement();

		System.out.println("executing: "+q);

		boolean success = stmt.execute(q);
		//System.out.println("result: "+success);

		if (success) {
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				VarBinding vb = new VarBinding();

				vb.setVarNameRef(rs.getString("varNameRef"));
				vb.setWfInstanceRef(rs.getString("wfInstanceRef"));
				vb.setValue(rs.getString("value"));
				vb.setCollIDRef(rs.getString("collIdRef"));
				vb.setIterationVector(rs.getString("iteration"));
				vb.setPNameRef(rs.getString("PNameRef"));
				vb.setPositionInColl(rs.getInt("positionInColl"));

				result.add(vb);

			}
		}
		return result;
	}	



	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#getProcessorsIncomingLinks(java.lang.String)
	 */
	public Map<String, Integer> getProcessorsIncomingLinks(String wfInstanceRef) throws SQLException {

		Map<String, Integer> result = new HashMap<String, Integer>();

		// get all processors and init their incoming links to 0
		String q = "SELECT pName "+
		"FROM Processor "+
		"WHERE wfInstanceRef = \""+wfInstanceRef+"\" ;";

		Statement stmt;
		stmt = dbConn.createStatement();

		System.out.println("executing: "+q);

		boolean success = stmt.execute(q);

		System.out.println("query executed");

		if (success) {
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				result.put(rs.getString("pName"), new Integer(0));
			}
		}

		// exclude processors connected to inputs -- those have 0 predecessors for our purposes
		// and we add them later
		q = "SELECT sinkPNameRef, count(*) as cnt "+
		"FROM Arc "+
		"WHERE wfInstanceRef = \""+wfInstanceRef+"\" "+
		"AND sourcePNameRef <> \"_INPUT_\" "+
		"GROUP BY sinkPNameRef;";

		stmt = dbConn.createStatement();

		System.out.println("executing: "+q);

		success = stmt.execute(q);

		System.out.println("query executed");

		if (success) {
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				result.put(rs.getString("sinkPNameRef"), new Integer(rs.getInt("cnt")));
			}
		}

		return result;
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#getSuccVars(java.lang.String, java.lang.String, java.lang.String)
	 */
	public List<Var> getSuccVars(String pName, String vName, String wfInstanceRef) throws SQLException {

		List<Var> result = new ArrayList<Var>();

		String q = "SELECT v.*   "+
		"FROM Arc a JOIN Var v ON a.sinkPNameRef = v.pnameRef "+
		"AND  a.sinkVarNameRef = v.varName "+
		"AND a.wfInstanceRef = v.wfInstanceRef "+
		"WHERE a.wfInstanceRef = \""+wfInstanceRef+"\" "+
		"AND sourceVarNameRef = \""+vName+"\" "+
		"AND sourcePNameRef = \""+pName+"\";";

		Statement stmt;
		stmt = dbConn.createStatement();

		System.out.println("executing: "+q);

		boolean success = stmt.execute(q);
		// System.out.println("result: "+success);

		if (success) {
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {

				Var aVar = new Var();

				aVar.setWfInstanceRef(rs.getString("WfInstanceRef"));

				if (rs.getInt("inputOrOutput") == 1) {
					aVar.setInput(true);					
				} else {
					aVar.setInput(false);
				}	
				aVar.setPName(rs.getString("pnameRef"));
				aVar.setVName(rs.getString("varName"));
				aVar.setType(rs.getString("type"));
				aVar.setTypeNestingLevel(rs.getInt("nestingLevel"));
				aVar.setActualNestingLevel(rs.getInt("actualNestingLevel"));
				aVar.setANLset((rs.getInt("anlSet") == 1 ? true : false));

				result.add(aVar);

			}
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#getSuccProcessors(java.lang.String, java.lang.String)
	 */
	public List<String> getSuccProcessors(String pName, String wfInstanceRef) throws SQLException {

		List<String> result = new ArrayList<String>();

		String q = "SELECT distinct sinkPNameRef "+
		"FROM Arc "+
		"WHERE wfInstanceRef = \""+wfInstanceRef+"\" "+
		"AND sourcePNameRef = \""+pName+"\";";

		Statement stmt;
		stmt = dbConn.createStatement();

		System.out.println("executing: "+q);

		boolean success = stmt.execute(q);
		// System.out.println("result: "+success);

		if (success) {
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				result.add(rs.getString("sinkPNameRef"));				
			}
		}
		return result;
	}




	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#getProcessors(java.util.Map)
	 */
	public List<ProvenanceProcessor>  getProcessors(Map<String, String> constraints) throws SQLException {

		List<ProvenanceProcessor> result = new ArrayList<ProvenanceProcessor>();

		String q = "SELECT * FROM Processor P";

		q = addWhereClauseToQuery(q, constraints, true);

		Statement stmt;
		stmt = dbConn.createStatement();

		System.out.println("executing: "+q);

		boolean success = stmt.execute(q);
		// System.out.println("result: "+success);

		if (success) {
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				ProvenanceProcessor proc = new ProvenanceProcessor();

				proc.setPname(rs.getString("pname"));
				proc.setType(rs.getString("type"));
				proc.setWfInstanceRef(rs.getString("wfInstanceRef"));

				result.add(proc);

			}
		}
		return result;
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#simpleLineageQuery(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public LineageSQLQuery simpleLineageQuery(
			String wfInstance,
			String pname,
			String vname,
			String iteration)  {

		LineageSQLQuery lq = new LineageSQLQuery();

		// base query
		String q1 = "SELECT * FROM VarBinding VB JOIN Var V on "+
		"VB.wfInstanceRef = V.wfInstanceRef and VB.PNameRef = V.pnameRef and VB.varNameRef = V.varName ";

		// constraints:
		Map<String, String>  lineageQueryConstraints = new HashMap<String, String>();

		lineageQueryConstraints.put("VB.wfInstanceRef", wfInstance);
		lineageQueryConstraints.put("VB.PNameRef", pname);
		if (iteration != null ) { lineageQueryConstraints.put("VB.iteration", iteration); }

		q1= addWhereClauseToQuery(q1, lineageQueryConstraints, false);  // false: do not terminate query

		// add order by clause
		List<String> orderAttr = new ArrayList<String>();
		orderAttr.add("varNameRef");
		orderAttr.add("iteration");

		q1 = addOrderByToQuery(q1, orderAttr, true);

		System.out.println("generated query: \n"+q1);

		lq.setSQLQuery(q1);

		return lq;
	}



	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#lineageQueryGen(java.lang.String, java.lang.String, java.util.Map, net.sf.taverna.t2.provenance.lineageservice.utils.Var, java.lang.String)
	 */
	public LineageSQLQuery lineageQueryGen(String wfInstance,
			String proc,
			Map<Var, String> var2Path, 
			Var outputVar, 
			String path) {

//		setup
		StringBuffer effectivePath = new StringBuffer();

		System.out.println("generating SQL for proc ="+proc);
		System.out.println("input vars:");
		for (Var v: var2Path.keySet()) {
			System.out.println(v.getVName()+
					" with delta-nl "+
					(v.getActualNestingLevel()-v.getTypeNestingLevel())+
					" and path "+var2Path.get(v));
		}

		int outputVarDnl = outputVar.getTypeNestingLevel();
		System.out.println("dnl of output var "+outputVar.getVName()+": "+outputVarDnl);
		System.out.println("original path: "+path);

		if (path != null) {

			String pathArray[] = path.split(",");

			for (int i=0; i<pathArray.length-outputVarDnl; i++ ) { effectivePath.append(pathArray[i]+","); }
			if (effectivePath.length()>0)
				effectivePath.deleteCharAt(effectivePath.length()-1);

			System.out.println("path used for query: "+effectivePath);

		}

//		generation
		if (!var2Path.isEmpty()) {   // generate query to retrieve inputs
			
			return generateSQL(wfInstance, proc, effectivePath.toString(), true);  // true -> fetch input vars

		} else {  // generate query to retrieve outputs (this is a special case where processor has no inputs) 
 
			System.out.println("lineageQueryGen: proc has no inputs => return output values instead");
			return generateSQL(wfInstance, proc, effectivePath.toString(), false);  // false -> fetch output vars

		}

		// for each collection, traverse the collection (using the Collection table) to the top

		// what happens when there is no iteration?? when is the positionInCollection used??
		// A. possibly, it is never used... CHECK
	}


	/**
	 * 		 if effectivePath is not null:
	 *		 query varBinding using: wfInstanceRef = wfInstance, iteration = effectivePath, PNameRef = proc
	 *		 if input vars is null, then use the output var		
	 *		 this returns the bindings for the set of input vars at the correct iteration
	 *		 if effectivePath is null:
	 *		 fetch VarBindings for all input vars, without constraint on the iteration
	 * @param wfInstance
	 * @param proc
	 * @param effectivePath
	 * @return
	 */
	LineageSQLQuery generateSQL(String wfInstance, String proc, String effectivePath, boolean fetchInputs) {

		LineageSQLQuery lq = new LineageSQLQuery();

		// base query
		String q1 = "SELECT * FROM VarBinding VB JOIN Var V on "+
		"VB.wfInstanceRef = V.wfInstanceRef and VB.PNameRef = V.pnameRef and VB.varNameRef = V.varName ";

		// String q2 = "  JOIN Collection C on C.CollID = VB.collIDRef and C.wfInstanceRef = VB.wfInstanceRef ";

		// constraints:
		Map<String, String>  lineageQueryConstraints = new HashMap<String, String>();

		lineageQueryConstraints.put("VB.wfInstanceRef", wfInstance);
		lineageQueryConstraints.put("VB.PNameRef", proc);
		
		// limit to inputs -- 
		// outputs only need to be returned when proc has no inputs (i.e. assume outputs are constants in this case)
		if (fetchInputs)	lineageQueryConstraints.put("V.inputOrOutput", "1");

		if (effectivePath.length() > 0 ) {
			lineageQueryConstraints.put("VB.iteration", effectivePath.toString()); 
		}

		q1= addWhereClauseToQuery(q1, lineageQueryConstraints, false);

		List<String> orderAttr = new ArrayList<String>();
		orderAttr.add("varNameRef");
		orderAttr.add("iteration");

		q1 = addOrderByToQuery(q1, orderAttr, true);
		System.out.println("generated query: \n"+q1);
		lq.setSQLQuery(q1);

		return lq;
	}



	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#runLineageQuery(net.sf.taverna.t2.provenance.lineageservice.LineageSQLQuery)
	 */
	public LineageQueryResult runLineageQuery(LineageSQLQuery lq) throws SQLException {

		Statement stmt;
		stmt = dbConn.createStatement();

		System.out.println("executing lineage query:\n"+lq.getSQLQuery());

		String q            = lq.getSQLQuery();
//		int    nestingLevel = lq.getNestingLevel();

		boolean success = stmt.execute(q);

		if (success) {
			ResultSet rs = stmt.getResultSet();

			LineageQueryResult lqr = new LineageQueryResult();

			while (rs.next()) {

				String wfInstance = rs.getString("VB.wfInstanceRef");
				String proc = rs.getString("VB.PNameRef");
				String var  = rs.getString("VB.varNameRef");
				String it   = rs.getString("VB.iteration");
				String coll = rs.getString("VB.collIDRef");
				String value = rs.getString("VB.value");

				System.out.println("proc ["+proc+"] var ["+var+"] iteration ["+it+"] collection ["+ coll+"] value ["+value+"]");

				String type = lqr.ATOM_TYPE; // temp -- FIXME

				// analyse results:
				// 1 - no records --> stop
				// at most 1 record for each var --> return them
				// >1 record for some var: --> reconstruct list structure from collID or from iteration TODO

				lqr.addLineageQueryResultRecord(proc, var, wfInstance, it, value, type);				
			}

			return lqr;

		} else return null;
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#runLineageQueries(java.util.List)
	 */
	public List<LineageQueryResult> runLineageQueries(List<LineageSQLQuery> lqList) throws SQLException {

		List<LineageQueryResult> allResults = new ArrayList<LineageQueryResult>();

		for (LineageSQLQuery lq : lqList) {
			if (lq == null) continue;
			allResults.add(runLineageQuery(lq));
		}

		return allResults;
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#recordsToCollection(net.sf.taverna.t2.provenance.lineageservice.LineageQueryResult)
	 */
	public Document recordsToCollection(LineageQueryResult lqr) {

		// process each var name in turn
		// lqr ordered by var name and by iteration number
		Document d = new Document(new Element("list"));


		String currentVar = null;
		for (ListIterator<LineageQueryResultRecord> it = lqr.iterator(); it.hasNext(); ) {

			LineageQueryResultRecord record = it.next();

			if (currentVar != null && record.getVname().equals(currentVar))  {   // multiple occurrences
				addToCollection(record, d);  // adds record to d in the correct position given by the iteration vector
			}
			if (currentVar == null) { currentVar = record.getVname(); }
		}
		return d;		
	}


	void addToCollection(LineageQueryResultRecord record, Document d) {

		Element root = d.getRootElement();

		String[] itVector = record.getIteration().split(",");

		Element currentEl = root;
		// each element gives us a corresponding child in the tree
		for (int i=0; i< itVector.length; i++) {

			int index = Integer.parseInt(itVector[i]);

			List<Element> children = currentEl.getChildren();
			if (index < children.size()) { // we already have the child, just descend
				currentEl = children.get(index);
			} else {  // create child
				if (i == itVector.length-1) { // this is a leaf --> atomic element
					currentEl.addContent(new Element(record.getValue()));					
				} else { // create internal element
					currentEl.addContent(new Element("list"));
				}
			}
		}
	}



	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceQuery#updateVar(net.sf.taverna.t2.provenance.lineageservice.utils.Var)
	 */
	public void updateVar(Var v) throws SQLException {

		Statement stmt;
		stmt = dbConn.createStatement();

		String u = "UPDATE Var "+
		"SET type = \""+v.getType()+"\""+				   
		", inputOrOutput = \""+(v.isInput() ? 1: 0)+"\" "+
		", nestingLevel = \""+v.getTypeNestingLevel()+"\" "+
		", actualNestingLevel = \""+v.getActualNestingLevel()+"\" "+
		", anlSet = \""+(v.isANLset() ? 1: 0)+"\" "+				   
		"WHERE varName = \""+v.getVName()+"\" "+
		"AND pnameRef = \""+v.getPName()+"\" "+
		"AND wfInstanceRef = \""+v.getWfInstanceRef()+"\" ;";

		System.out.println("executing: "+u);

		boolean success = stmt.execute(u);

		System.out.println("update executed");
	}




}
