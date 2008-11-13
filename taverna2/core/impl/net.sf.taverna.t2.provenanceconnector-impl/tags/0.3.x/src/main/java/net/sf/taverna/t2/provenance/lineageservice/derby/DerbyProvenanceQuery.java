/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.provenance.lineageservice.derby;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom.Document;

import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResult;
import net.sf.taverna.t2.provenance.lineageservice.LineageSQLQuery;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.derby.DerbyProvenanceAnalysis.LineageAnnotation;
import net.sf.taverna.t2.provenance.lineageservice.utils.Arc;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceProcessor;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;
import net.sf.taverna.t2.provenance.lineageservice.utils.VarBinding;


/**
 * Add your own database and carry out provenance queries
 * 
 * @author Paolo Missier
 * @author Ian Dunlop
 *
 */
public class DerbyProvenanceQuery implements ProvenanceQuery {

	private Connection openConnection;
	private String location;
	static boolean useDB = true;

	public DerbyProvenanceQuery() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public void openConnection() {
//		try {
//			dbConn = getDBConnection();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	/**
	 * Implement type of database connection here
	 * @return
	 * @throws SQLException 
	 */
	public Connection getDBConnection() throws SQLException {
		try {
			getClass().getClassLoader().loadClass("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
//			Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String dbURL = "jdbc:derby:provenance;create=true;upgrade=true";
		return DriverManager.getConnection(dbURL);
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
				if (entry.getKey().contains("inputOr")) {
					q.append(" "+entry.getKey()+" = "+ entry.getValue());
				} else {
					q.append(" "+entry.getKey()+" = \'"+ entry.getValue()+ "\' ");
				}
				first = false;
			}
		}

		return q.toString();
	}


	/**
	 * select Var records that satisfy constraints
	 */
	public List<Var> getVars(Map<String,String> queryConstraints) throws SQLException {

		List<Var>  result = new ArrayList<Var>();

		String q0 = "SELECT * FROM Var V ";

		String q = addWhereClauseToQuery(q0, queryConstraints);
		
		System.out.println(q);

		Statement stmt;
		stmt = openConnection.createStatement();

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
				aVar.setActualNestingLevel(rs.getInt("actualNestingLevel"));
				aVar.setANLset((rs.getInt("anlSet") == 1 ? true : false));

				result.add(aVar);

			}
		}
		return result;
	}


	/**
	 * return the input variables for a given processor and a wfInstanceId
	 * @param pname
	 * @param wfInstanceId
	 * @return list of input variables
	 * @throws SQLException 
	 */
	public List<Var> getInputVars(String pname, String wfInstanceId) throws SQLException {

		// get (var, proc) from Var  to see if it's input/output
		Map<String, String>  varQueryConstraints = new HashMap<String, String>();

		varQueryConstraints.put("V.wfInstanceRef", wfInstanceId);
		varQueryConstraints.put("V.pnameRef", pname);  
		varQueryConstraints.put("V.inputOrOutput", "1");

		return getVars(varQueryConstraints);		
	}


	/**
	 * return the output variables for a given processor and a wfInstanceId
	 * @param pname
	 * @param wfInstanceId
	 * @return list of output variables
	 * @throws SQLException 
	 */
	public List<Var> getOutputVars(String pname, String wfInstanceId) throws SQLException {

		// get (var, proc) from Var  to see if it's input/output
		Map<String, String>  varQueryConstraints = new HashMap<String, String>();

		varQueryConstraints.put("V.wfInstanceRef", wfInstanceId);
		varQueryConstraints.put("V.pnameRef", pname);  
		varQueryConstraints.put("V.inputOrOutput", "0");

		return getVars(varQueryConstraints);		
	}



	/**
	 * selects all Arcs
	 * @param queryConstraints
	 * @return
	 * @throws SQLException
	 */
	public List<Arc>   getArcs(Map<String,String> queryConstraints) throws SQLException {

		List<Arc> result = new ArrayList<Arc>();

		String q0 = "SELECT * FROM Arc A";

		String q = addWhereClauseToQuery(q0, queryConstraints);

		Statement stmt;
		stmt = openConnection.createStatement();

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
		stmt = openConnection.createStatement();

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
		stmt = openConnection.createStatement();

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
		stmt = openConnection.createStatement();

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



	/**
	 * used in the toposort phase -- propagation of anl() values through the graph
	 * @return
	 * @throws SQLException
	 */
	public Map<String, Integer> getProcessorsIncomingLinks(String wfInstanceRef) throws SQLException {

		Map<String, Integer> result = new HashMap<String, Integer>();

		// get all processors and init their incoming links to 0
		String q = "SELECT pname "+
		"FROM Processor "+
		"WHERE wfInstanceRef = \'"+wfInstanceRef+"\'";

		Statement stmt;
		stmt = openConnection.createStatement();

		System.out.println("executing: "+q);

		boolean success = stmt.execute(q);

		System.out.println("query executed");

		if (success) {
			System.out.println("SUCCESS");
			ResultSet rs = stmt.getResultSet();

			while (rs.next()) {
				String string = rs.getString("pname");
				System.out.println("PROCESSOR: " + string);
				result.put(string, new Integer(0));
			}
		}

		// exclude processors connected to inputs -- those have 0 predecessors for our purposes
		// and we add them later
		q = "SELECT sinkPNameRef, count(*) as cnt "+
		"FROM Arc "+
		"WHERE wfInstanceRef = \'"+wfInstanceRef+"\' "+
		"AND sourcePNameRef <> \'_INPUT_\' "+
		"GROUP BY sinkPNameRef";

		stmt = openConnection.createStatement();

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


	public List<Var> getSuccVars(String pName, String vName, String wfInstanceRef) throws SQLException {

		List<Var> result = new ArrayList<Var>();

		String q = "SELECT v.*   "+
		"FROM Arc a JOIN Var v ON a.sinkPNameRef = v.pnameRef "+
		"AND  a.sinkVarNameRef = v.varName "+
		"AND a.wfInstanceRef = v.wfInstanceRef "+
		"WHERE a.wfInstanceRef = \'"+wfInstanceRef+"\' "+
		"AND sourceVarNameRef = \'"+vName+"\' "+
		"AND sourcePNameRef = \'"+pName+"\'";
		
		Statement stmt;
		stmt = openConnection.createStatement();

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


	public List<String> getSuccProcessors(String pName, String wfInstanceRef) throws SQLException {

		List<String> result = new ArrayList<String>();

		String q = "SELECT distinct sinkPNameRef "+
		"FROM Arc "+
		"WHERE wfInstanceRef = \'"+wfInstanceRef+"\' "+
		"AND sourcePNameRef = \'"+pName+"\'";

		Statement stmt;
		stmt = openConnection.createStatement();

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




	public List<ProvenanceProcessor>  getProcessors(Map<String, String> constraints) throws SQLException {

		List<ProvenanceProcessor> result = new ArrayList<ProvenanceProcessor>();

		String q = "SELECT * FROM Processor P";

		q = addWhereClauseToQuery(q, constraints);

		Statement stmt;
		stmt = openConnection.createStatement();

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


		if (aLA.getWfInstance() != null)
			lineageQueryConstraints.put("VB.wfInstanceRef", aLA.getWfInstance());

		if (aLA.getProc() != null) 
			lineageQueryConstraints.put("VB.PNameRef", aLA.getProc());

		if (aLA.getVar() != null) 
			lineageQueryConstraints.put("VB.varNameRef", aLA.getVar());

		if (aLA.getCollectionRef() != null) 
			lineageQueryConstraints.put("VB.collIDRef", aLA.getCollectionRef());

		lineageQueryConstraints.put("VB.iteration", aLA.getIterationVector());  // changed to iteration vector 

		lineageQueryConstraints.put("VB.positionInColl", Integer.toString(aLA.getIic() +1));  // +1: in the DB default is 1 not 0

		lineageQueryConstraints.put("V.inputOrOutput", "1");

		// this is where we need to retrieve the containing collection
		if (aLA.getCollectionNesting()>0) {
			q1 = q1 + q2;
			lq.setNestingLevel(aLA.getCollectionNesting());  // indicate that collection should be used in the answer

			lineageQueryConstraints.put("C.iteration", aLA.getIterationVector());

		}

		q1 = addWhereClauseToQuery(q1, lineageQueryConstraints);

		lq.setSQLQuery(q1);

		return lq;

	}


	public List<LineageQueryResult> runLineageQueries(List<LineageSQLQuery> lqList) throws SQLException {

		Statement stmt;
		stmt = openConnection.createStatement();

		for (LineageSQLQuery lq : lqList) {

			System.out.println("executing lineage query:\n"+lq.getSQLQuery()+"\n  with nesting level "+lq.getNestingLevel());

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
		return null;  // TODO -- see MySQLProvenanceQuery

	}


	/**
	 * persists var v back to DB 
	 * @param v
	 * @throws SQLException 
	 */
	public void updateVar(Var v) throws SQLException {
		
		Statement stmt;
		stmt = openConnection.createStatement();
		
		String u = "UPDATE Var "+
				   "SET type = \'"+v.getType()+"\'"+				   
				   ", inputOrOutput = \'"+(v.isInput() ? 1: 0)+"\' "+
				   ", nestingLevel = \'"+v.getTypeNestingLevel()+"\' "+
				   ", actualNestingLevel = \'"+v.getActualNestingLevel()+"\' "+
				   ", anlSet = \'"+(v.isANLset() ? 1: 0)+"\' "+				   
				   "WHERE varName = \'"+v.getVName()+"\' "+
				   "AND pnameRef = \'"+v.getPName()+"\' "+
				   "AND wfInstanceRef = \'"+v.getWfInstanceRef()+"\'";

		System.out.println("executing: "+u);

		boolean success = stmt.execute(u);

		System.out.println("update executed");
	}

	public String getLocation() {
		return location;
	}

	public void setConnection(Connection openConnection) {
		this.openConnection = openConnection;
		
	}

	public LineageSQLQuery lineageQueryGen(String wfInstance, String proc,
			Map<Var, String> var2Path, Var outputVar, String path) {
		// TODO Auto-generated method stub
		return null;
	}

	public Document recordsToCollection(LineageQueryResult lqr) {
		// TODO Auto-generated method stub
		return null;
	}

	public LineageQueryResult runLineageQuery(LineageSQLQuery lq)
			throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public LineageSQLQuery simpleLineageQuery(String wfInstance, String pname,
			String vname, String iteration) {
		// TODO Auto-generated method stub
		return null;
	}


}
