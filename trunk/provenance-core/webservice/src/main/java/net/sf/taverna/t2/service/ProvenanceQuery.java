package net.sf.taverna.t2.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.t2.service.ProvenanceAnalysis.LineageAnnotation;
import net.sf.taverna.t2.service.util.Arc;
import net.sf.taverna.t2.service.util.DBconnections;
import net.sf.taverna.t2.service.util.ProcBinding;
import net.sf.taverna.t2.service.util.Processor;
import net.sf.taverna.t2.service.util.Var;
import net.sf.taverna.t2.service.util.VarBinding;


/**
 * @author paolo
 *
 */
public class ProvenanceQuery {

	Connection dbConn = null;
	static boolean useDB = true;
	
	public ProvenanceQuery() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		
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
	private String addWhereClauseToQuery(String q0, Map<String,String> queryConstraints) {
		
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
		q.append(";");
		
		return q.toString();
	}

	
	/**
	 * select Var records that satisfy constraints
	 */
	public List<Var> getVars(Map<String,String> queryConstraints) throws SQLException {
		
		List<Var>  result = new ArrayList<Var>();
		
		String q0 = "SELECT * FROM Var V ";

		String q = addWhereClauseToQuery(q0, queryConstraints);
				
		Statement stmt;
		stmt = dbConn.createStatement();

		// System.out.println("getVars: executing query\n"+q.toString());
		
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
				
				result.add(aVar);
				
			}
		}
		return result;

	}
	
	
	/**
	 * selects all Arcs
	 * @param queryConstraints
	 * @return
	 * @throws SQLException
	 */
	public List<Arc>   getArcs(Map<String,String> queryConstraints) throws SQLException {
		
		List<Arc> result = new ArrayList<Arc>();
		
		String q0 = "SELECT * FROM Arc A ";

		String q = addWhereClauseToQuery(q0, queryConstraints);
				
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
	
	
	/**
	 * all WF instances, in reverse chronological order 
	 * @return
	 * @throws SQLException
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
	
	
	
	/**
	 * all ProCBinding records that satisfy the input constraints
	 * @param constraints
	 * @return
	 * @throws SQLException
	 */
	public List<ProcBinding> getProcBindings(Map<String, String> constraints) throws SQLException {
		
		List<ProcBinding> result = new ArrayList<ProcBinding>();
		
		String q = "SELECT * FROM ProcBinding PB ";

		q = addWhereClauseToQuery(q, constraints);
		
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
	
	/**
	 * 
	 * @param constraints a Map columnName -> value that defines the query constraints. Note: columnName must be fully qualified.
	 * This is not done well at the moment, i.e., PNameRef should be VarBinding.PNameRef to avoid ambiguities
	 * @return
	 * @throws SQLException
	 */
	public List<VarBinding>  getVarBindings(Map<String, String> constraints) throws SQLException {
		
		List<VarBinding> result = new ArrayList<VarBinding>();
		
		String q = "SELECT * FROM VarBinding VB join Var V "+
				   "on (VB.varNameRef = V.varName and  VB.wfInstanceRef = V.wfInstanceRef and VB.PNameRef =  V.PNameRef) ";

		q = addWhereClauseToQuery(q, constraints);
		
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
	
	
	public List<Processor>  getProcessors(Map<String, String> constraints) throws SQLException {
		
		List<Processor> result = new ArrayList<Processor>();
		
		String q = "SELECT * FROM Processor P";

		q = addWhereClauseToQuery(q, constraints);
		
		Statement stmt;
		stmt = dbConn.createStatement();
		
		System.out.println("executing: "+q);
		
		boolean success = stmt.execute(q);
		// System.out.println("result: "+success);

		if (success) {
			ResultSet rs = stmt.getResultSet();
			
			while (rs.next()) {
				Processor proc = new Processor();
				
				proc.setPname(rs.getString("pname"));
				proc.setType(rs.getString("type"));
				proc.setWfInstanceRef(rs.getString("wfInstanceRef"));

				result.add(proc);
				
			}
		}
		return result;
	}
	
	
	
	/**
	 * takes an annotated path and generates a corresponding SQL query based on the LineageAnnotation info
	 * @param path
	 * @return
	 */
	public LineageSQLQuery LineageQueryGen(List<LineageAnnotation> path) {
		
		LineageSQLQuery lq = new LineageSQLQuery();
		
		// this assumes the last annotation has got all we need already...
		
		// base query
		String q1 = "SELECT * FROM VarBinding VB JOIN Var V on "+
		            "VB.wfInstanceRef = V.wfInstanceRef and VB.PNameRef = V.pnameRef and VB.varNameRef = V.varName";
		
		String q2 = "  JOIN Collection C on C.CollID = VB.collIDRef and C.wfInstanceRef = VB.wfInstanceRef ";
		// constraints:
		Map<String, String>  lineageQueryConstraints = new HashMap<String, String>();

		// open last LA
		LineageAnnotation aLA = path.get(path.size()-1);

		// this is where we need to retrieve the containing collection
		if (aLA.getCollectionNesting()>0) {
			q1 = q1 + q2;
			lq.setNestingLevel(aLA.getCollectionNesting());  // indicate that collection should be used in the answer
		}

		if (aLA.getWfInstance() != null)
			lineageQueryConstraints.put("VB.wfInstanceRef", aLA.getWfInstance());
		
		if (aLA.getProc() != null) 
			lineageQueryConstraints.put("VB.PNameRef", aLA.getProc());

//		if (aLA.getVar() != null) 
//			lineageQueryConstraints.put("V.varNameRef", aLA.getVar());
		
		if (aLA.getCollectionRef() != null) 
			lineageQueryConstraints.put("VB.collIDRef", aLA.getCollectionRef());

		lineageQueryConstraints.put("VB.iteration", aLA.getIteration());
		
		lineageQueryConstraints.put("VB.positionInColl", Integer.toString(aLA.getIic() +1));  // +1: in the DB default is 1 not 0
		
		lineageQueryConstraints.put("V.inputOrOutput", "1");

		q1 = addWhereClauseToQuery(q1, lineageQueryConstraints);
		
		lq.setSQLQuery(q1);
		
		return lq;
		
	}


	public void runLineageQueries(List<LineageSQLQuery> lqList) throws SQLException {

		Statement stmt;
		stmt = dbConn.createStatement();

		for (LineageSQLQuery lq : lqList) {

			System.out.println("executing lineage query:\n"+lq.SQLQuery+"\n  with nesting level "+lq.getNestingLevel());

			String q            = lq.getSQLQuery();
			int    nestingLevel = lq.getNestingLevel();
			
			boolean success = stmt.execute(q);

			if (success) {
				ResultSet rs = stmt.getResultSet();

				while (rs.next()) {
					
					String proc = rs.getString("VB.PNameRef");
					String var  = rs.getString("VB.varNameRef");
					
					if (nestingLevel > 0) {  // retrieve collection

						System.out.println("proc ["+proc+"] var ["+var+"] collection: ["+rs.getString("collID")+"]");
						
					} else { // retrieve var values
						
						System.out.println("proc ["+proc+"] var: ["+var+"] val ["+rs.getString("value")+"]");
						
					}

				}
			}
		}
		
		
	}

	
}
