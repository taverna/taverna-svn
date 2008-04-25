/**
 * 
 */
package net.sf.taverna.t2.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.sf.taverna.t2.service.util.Var;

/**
 * @author paolo
 *
 */
public class ProvenanceWriter {

	Connection dbConn = null;
	static boolean useDB = true;
	
	public ProvenanceWriter() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		
		// open singleton connection to DB
		if (useDB) {
			dbConn = openConnection();
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


	int extractIterationCount(String iteration) {
		
	// iteration is of the form "[n]" so we extract n
	String iterationN = iteration.substring(1, iteration.length()-1);

	if (iterationN.length() == 0) return 0;
	
	return Integer.parseInt(iterationN);

	}
	
	public void addProcessorBinding(String processorId, String activityId,
			String iteration, String dataflowId) throws SQLException {

		int iterationN = extractIterationCount(iteration);
		
		System.out.println("iteration N = ["+iterationN+"]");
		
		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO ProcBinding SET "+
		           "pnameRef = \""+processorId+"\", "+
			       "execIDRef = \""+dataflowId+"\", "+
			       "iteration = \""+iterationN +"\", "+
			       "actName = \""+activityId+"\";";

//		System.out.println("executing: "+q);

		int result = stmt.executeUpdate(q);

		System.out.println("Processor binding: processor ["+processorId+"] activity ["+activityId+"]");
		System.out.println("*** addProcessorBinding: "+result+ " rows added to DB");
		
	}

	
	public String addCollection(String processorId, String collId, String parentCollectionId, String portName, String dataflowId) throws SQLException {
		
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
			       "parentCollIdRef = \""+parentCollectionId+"\", "+
			       "collId = \""+collId+"\";";
		
		System.out.println("collection: processor ["+processorId+"] collId ["+collId+"]");

		int result = stmt.executeUpdate(q);

		System.out.println("*** collection: "+result+ " rows added to DB");
		
		return newParentCollectionId;

	}

	
	/**
	 * 
	 * @param processorId
	 * @param value  literal value, if type is literal
	 * @param ref
	 * @param one of valueType  literal, dataDocument, list
	 * @param varName
	 * @param collIdRef not null if value is part of a collection (list)
	 * @param iterationCount  as specified in the iteration id
	 * @param dataflowId
	 * @throws SQLException
	 */
	public void addVarBinding(String processorId, 
							  String value, 
							  String ref,
							  String valueType, 
							  String varName,
							  String collIdRef,
							  int    positionInCollection,
							  String iterationCount, 
							  String dataflowId) throws SQLException {
		
		Statement stmt = dbConn.createStatement();

		int iterationN = extractIterationCount(iterationCount);
		
		String q = "INSERT INTO VarBinding SET "+
				   "pnameRef = \""+processorId+"\", "+
			       "wfInstanceRef = \""+dataflowId+"\",  "+
			       "varNameRef = \""+varName+"\", "+
			       "valueType = \""+valueType+"\", "+
			       "value  = \""+value+"\", "+
			       "ref    = \""+ref+"\", "+			       
			       "collIdRef    = \""+collIdRef+"\", "+
			       "iteration    = \""+iterationN+"\", "+
			       "positionInColl = \""+positionInCollection+"\";";

		System.out.println("Var binding: processor ["+processorId+"] varName ["+varName+
						   "] collIdRef ["+collIdRef+"] iteration ["+iterationCount+
						   "] positionInCollection ["+positionInCollection+"] value ["+value+"]");

		int result = stmt.executeUpdate(q);

		System.out.println("*** addVarBinding: "+result+ " rows added to DB");
		
	}


	/**
	 * connection parameters hardcoded for testing
	 * @return a connection to the provenance DB
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SQLException 
	 */
	private java.sql.Connection openConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		//		mysql on rpc264
		String DB_URL = "jdbc:mysql://rpc264.cs.man.ac.uk/T2Provenance?autoReconnect=true";  // URL of database server
		String DB_USER = "paolo";                        // database user id
		String DB_PASSWD = "riccardino";                          // database password

		Class.forName("com.mysql.jdbc.Driver").newInstance();

		System.out.println("opening DB connection to "+DB_URL);

		return  DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWD);
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
		System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Processor;";		
		System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Arc;";		
		System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Var;";		
		System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM ProcBinding;";		
		System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM VarBinding;";		
		System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Collection;";		
		System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		System.out.println(result+ " rows removed from DB");

		System.out.println(" **** DB cleared ****");

	}


	
}
