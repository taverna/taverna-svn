/**
 * 
 */
package net.sf.taverna.t2.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.sf.taverna.t2.service.util.DBconnections;
import net.sf.taverna.t2.service.util.ProcBinding;
import net.sf.taverna.t2.service.util.Var;
import net.sf.taverna.t2.service.util.VarBinding;

/**
 * @author paolo
 *
 */
public class ProvenanceWriter {

	Connection dbConn = null;
	static boolean useDB = true;
	
	public ProvenanceWriter() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		// open singleton connection to DB
		
		DBconnections DBconn = new DBconnections();
		
		if (useDB) {
			dbConn = DBconn.openConnection();
			System.out.println("successfully opened DB connection");
		}
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

			q = "INSERT INTO Var SET varname = \""+v.getVName()+"\", "+
			"pNameRef = \""+v.getPName()+"\", "+
			"inputOrOutput = \""+isInput+"\", "+			
			"wfInstanceRef = \""+wfId+"\";";
			
			System.out.println("executing: "+q);
			
			try {
			
				int result = stmt.executeUpdate(q);
				System.out.println(result+ " rows added to DB");

			} catch (com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException e) {
				// ignore this insert and continue
				continue;
			}


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
	
	
	public void addWFId(String wfId) throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO Workflow SET wfname = \""+wfId+"\";";

		System.out.println("executing: "+q);
		int result = stmt.executeUpdate(q);
		System.out.println("workflow id: "+result+" rows added to DB");
	}

	
	
	public void addWFInstanceId(String wfId) throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO WfInstance SET instanceID = \""+wfId+"\"" +
		           ", wfnameRef = \""+wfId+"\";";

		System.out.println("executing: "+q);
		int result = stmt.executeUpdate(q);
		System.out.println("workflow id: "+result+" rows added to DB");
	}
	

	/**
	 * insert new processor into the provenance DB
	 * @param name
	 * @throws SQLException 
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
	



	/**
	 * deletes entire DB contents -- for testing purposes 
	 * @throws SQLException 
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
