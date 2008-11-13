/**
 * 
 */
package net.sf.taverna.t2.provenance.lineageservice.mysql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.utils.DBconnections;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;
import net.sf.taverna.t2.provenance.lineageservice.utils.VarBinding;

/**
 * @author paolo
 *
 */
public class MySQLProvenanceWriter  implements ProvenanceWriter {

	Connection dbConn = null;
	static boolean useDB = true;
	
	public MySQLProvenanceWriter() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		// open singleton connection to DB
				
		if (useDB) {
			dbConn = openConnection();
			System.out.println("successfully opened DB connection");
		}
	}
	
	
	public Connection openConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		DBconnections DBconn = new DBconnections();
		try {
			dbConn = DBconn.openConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dbConn;
	}
	
	
	/**
	 * add each Var as a row into the VAR DB table<br/>
	 * <strong>note: no static var type available as part of the dataflow...</strong>
	 * @param vars
	 * @param wfId
	 * @throws SQLException 
	 */
	public  void addVariables(List<Var> vars, String wfId) throws SQLException {

		Statement stmt;
			stmt = dbConn.createStatement();

		String q;
		for (Var v:vars) {
			
			int isInput = v.isInput() ? 1 : 0;

			q = "INSERT INTO Var "+
			"SET varname = \""+v.getVName()+"\", "+
			"pNameRef = \""+v.getPName()+"\", "+
			"inputOrOutput = \""+isInput+"\", "+
			"nestingLevel = \""+( v.getTypeNestingLevel() >= 0 ? v.getTypeNestingLevel() : 0 )+"\", "+
			"wfInstanceRef = \""+wfId+"\";";
			
			System.out.println("executing: "+q);
			
//			try {
			
				int result = stmt.executeUpdate(q);
				System.out.println(result+ " rows added to DB");

				// Avoid mySQL dependency 
				
//			} catch (com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException e) {
//				// ignore this insert and continue
//				continue;
//			}


		}
	}
	
	

	/**
	 * inserts one row into the ARC DB table
	 * @param sourceVar
	 * @param sinkVar
	 * @param wfId
	 */
	public void addArc(Var sourceVar, Var sinkVar, String wfId) throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO Arc SET wfInstanceRef = \""+wfId+"\", " +
		           "sourcePNameRef = \""+sourceVar.getPName()+"\", " +
		           "sourceVarNameRef = \""+sourceVar.getVName()+"\", " +
		           "sinkPNameRef = \""+sinkVar.getPName()+"\", " +
		           "sinkVarNameRef = \""+sinkVar.getVName()+"\";"; 		           

		System.out.println("executing: "+q);
		int result = stmt.executeUpdate(q);
		System.out.println("workflow id: "+result+" rows added to DB");
	}
	
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceWriter#addArc(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void addArc(String sourceVarName, String sourceProcName, String sinkVarName, String sinkProcName, String wfId) 
	throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO Arc SET wfInstanceRef = \""+wfId+"\", " +
		           "sourcePNameRef = \""+sourceProcName+"\", " +
		           "sourceVarNameRef = \""+sourceVarName+"\", " +
		           "sinkPNameRef = \""+sinkProcName+"\", " +
		           "sinkVarNameRef = \""+sinkVarName+"\";"; 		           

		System.out.println("executing: "+q);
		int result = stmt.executeUpdate(q);
		System.out.println("workflow id: "+result+" rows added to DB");
		
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceWriter#addWFId(java.lang.String)
	 */
	public void addWFId(String wfId) throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO Workflow SET wfname = \""+wfId+"\";";

		System.out.println("executing: "+q);
		int result = stmt.executeUpdate(q);
		System.out.println("workflow id: "+result+" rows added to DB");
	}

	
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceWriter#addWFInstanceId(java.lang.String, java.lang.String)
	 */
	public void addWFInstanceId(String wfId, String wfInstanceId) throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO WfInstance SET instanceID = \""+wfId+"\"" +
		           ", wfnameRef = \""+wfInstanceId+"\";";

		System.out.println("executing: "+q);
		int result = stmt.executeUpdate(q);
		System.out.println("workflow id: "+result+" rows added to DB");
	}
	

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceWriter#addProcessor(java.lang.String, java.lang.String)
	 */
	public void addProcessor(String name, String wfID) throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO Processor SET pname = \""+name+"\", wfInstanceRef = \""+wfID+"\";";

		System.out.println("executing: "+q);

		int result = stmt.executeUpdate(q);

		System.out.println("*** addProcessor: "+result+ " rows added to DB");
	}


	
	public void addProcessorBinding(ProcBinding pb) throws SQLException {
		
		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO ProcBinding SET "+
		           "pnameRef = \""+pb.getPNameRef()+"\", "+
			       "execIDRef = \""+pb.getExecIDRef()+"\", "+
			       "iteration = \""+pb.getIterationVector()+"\", "+
			       "actName = \""+pb.getActName()+"\";";

		//System.out.println("executing: "+q);

		int result = stmt.executeUpdate(q);

		//System.out.println("Processor binding: processor ["+pb.getPNameRef()+"] activity ["+pb.getActName()+"]");
		//System.out.println("*** addProcessorBinding: "+result+ " rows added to DB");
		
		
	}

			
	
	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceWriter#addCollection(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String addCollection(String processorId, 
						        String collId, 
						        String parentCollectionId, 
						        String iteration,
						        String portName, 
						        String dataflowId) throws SQLException {
		
		String newParentCollectionId = null;
		
		Statement stmt = dbConn.createStatement();

		if (parentCollectionId == null) {
			// this is a top-level list
			parentCollectionId = "TOP";
		}
		
		newParentCollectionId = collId;

		String q = "INSERT INTO Collection SET "+
				   "PNameRef = \""+processorId+"\", "+
			       "wfInstanceRef = \""+dataflowId+"\",  "+
			       "varNameRef = \""+portName+"\", "+
			       "iteration = \""+iteration+"\", "+			       
			       "parentCollIdRef = \""+parentCollectionId+"\", "+
			       "collId = \""+collId+"\";";
		
		System.out.println("collection: processor ["+processorId+"] collId ["+collId+"]");

		int result = stmt.executeUpdate(q);

		System.out.println("*** collection: "+result+ " rows added to DB");
		
		return newParentCollectionId;

	}

	
	public void addVarBinding(VarBinding vb) throws SQLException {
		
		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO VarBinding SET "+
		   "pnameRef = \""+vb.getPNameRef()+"\", "+
	       "wfInstanceRef = \""+vb.getWfInstanceRef()+"\",  "+
	       "varNameRef = \""+vb.getVarNameRef()+"\", "+
	       "valueType = \""+vb.getValueType()+"\", "+
	       "value  = \""+vb.getValue()+"\", "+
	       "ref    = \""+vb.getRef()+"\", "+			       
	       "collIdRef    = \""+vb.getCollIDRef()+"\", "+
	       "iteration    = \""+vb.getIteration()+"\", "+
	       "positionInColl = \""+vb.getPositionInColl()+"\";";
		
		System.out.println("Var binding: processor ["+vb.getPNameRef()+"] varName ["+vb.getVarNameRef()+
				   "] collIdRef ["+vb.getCollIDRef()+"] iteration ["+vb.getIteration()+
				   "] positionInCollection ["+vb.getPositionInColl()+"] value ["+vb.getValue()+"]");

		int result = stmt.executeUpdate(q);

		System.out.println("*** addVarBinding: "+result+ " rows added to DB");

	}
	



	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceWriter#clearDB()
	 */
	public void clearDB() throws SQLException {

		String q = null;
		int result = 0;

		Statement stmt = dbConn.createStatement();

		q = "DELETE FROM Workflow;";		
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Processor;";		
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Arc;";		
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Var;";		
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM WfInstance;";		
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		
		q = "DELETE FROM ProcBinding;";		
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM VarBinding;";		
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Collection;";		
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		System.out.println(" **** DB cleared ****");

	}



	
	
}
