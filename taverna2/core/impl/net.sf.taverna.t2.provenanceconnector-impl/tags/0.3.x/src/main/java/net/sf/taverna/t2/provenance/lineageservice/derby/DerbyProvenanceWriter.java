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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;
import net.sf.taverna.t2.provenance.lineageservice.utils.VarBinding;

/**
 * Uses your database of choice to write workflow provenance
 * 
 * @author Paolo Missier
 * @author Ian Dunlop
 * 
 */
public class DerbyProvenanceWriter implements ProvenanceWriter {

	private static final String createDB = "CREATE DATABASE IF NOT EXISTS T2Provenance";

	private static final String createTableArc = "CREATE TABLE Arc ("
			+ "sourceVarNameRef varchar(100) NOT NULL ,"
			+ "sinkVarNameRef varchar(100) NOT NULL,"
			+ "sourcePNameRef varchar(100) NOT NULL,"
			+ "sinkPNameRef varchar(100) NOT NULL,"
			+ "wfInstanceRef varchar(100) NOT NULL,"
			+ " PRIMARY KEY  (sourceVarNameRef,sinkVarNameRef,sourcePNameRef,sinkPNameRef,wfInstanceRef))";

	private static final String createTableCollection = "CREATE TABLE Collection ("
			+ "collID varchar(100) NOT NULL,"
			+ "parentCollIDRef varchar(100) NOT NULL ,"
			+ "wfInstanceRef varchar(100) NOT NULL,"
			+ "PNameRef varchar(100) NOT NULL,"
			+ "varNameRef varchar(100) NOT NULL,"
			+ "iteration char(10) NOT NULL default '',"
			+ " PRIMARY KEY (collID,wfInstanceRef,PNameRef,varNameRef,parentCollIDRef,iteration))";

	private static final String createTableProcBinding = "CREATE TABLE ProcBinding ("
			+ "pnameRef varchar(100) NOT NULL ,"
			+ "execIDRef varchar(100) NOT NULL ,"
			+ "actName varchar(100) NOT NULL ,"
			+ "iteration char(10) NOT NULL default '',"
			+ "PRIMARY KEY (pnameRef,execIDRef,iteration))";

	private static final String createTableProcessor = "CREATE TABLE Processor ("
			+ "pname varchar(100) NOT NULL,"
			+ "wfInstanceRef varchar(100) NOT NULL ,"
			+ "type varchar(100) default NULL,"
			+ "PRIMARY KEY  (pname,wfInstanceRef))";

	private static final String createTableVar = "CREATE TABLE Var ("
			+ "varName varchar(100) NOT NULL,"
			+ "type varchar(20) default NULL,"
			+ "inputOrOutput smallint NOT NULL ,"
			+ "pnameRef varchar(100) NOT NULL,"
			+ "wfInstanceRef varchar(100) NOT NULL,"
			+ "nestingLevel int,"
			+ "actualNestingLevel int,"
			+ "anlSet smallint default NULL,"
			+ "PRIMARY KEY (varName,inputOrOutput,pnameRef,wfInstanceRef))";

	private static final String createTableVarBinding = "CREATE TABLE VarBinding ("
			+ "varNameRef varchar(100) NOT NULL,"
			+ "wfInstanceRef varchar(100) NOT NULL,"
			+ "value varchar(100) default NULL,"
			+ "collIDRef varchar(100) NOT NULL,"
			+ "positionInColl int NOT NULL,"
			+ "PNameRef varchar(100) NOT NULL,"
			+ "valueType varchar(50) default NULL,"
			+ "ref varchar(100) default NULL,"
			+ "iteration char(10) NOT NULL,"
			+ "PRIMARY KEY (varNameRef,wfInstanceRef,PNameRef,positionInColl,iteration,collIDRef))";
	// + " KEY collectionFK (wfInstanceRef,PNameRef,varNameRef,collIDRef))";

	private static final String createTableWFInstance = "CREATE TABLE WfInstance ("
			+ "instanceID varchar(100) NOT NULL,"
			+ "wfnameRef varchar(100) NOT NULL,"
			+ "timestamp timestamp NOT NULL default CURRENT_TIMESTAMP,"
			+ " PRIMARY KEY (instanceID))";

	private static final String createTableWorkflow = "CREATE TABLE Workflow ("
			+ "wfname varchar(100) NOT NULL," + "PRIMARY KEY  (wfname))";

	Connection dbConn = null;
	private String location;
	static boolean useDB = true;

	public DerbyProvenanceWriter() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {

	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Connection openConnection() {
		try {
			dbConn = getDBConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dbConn;
	}

	/**
	 * Implement type of databse connection here
	 * 
	 * @return
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
	 * add each Var as a row into the VAR DB table<br/>
	 * <strong>note: no static var type available as part of the
	 * dataflow...</strong>
	 * 
	 * @param vars
	 * @param wfId
	 * @throws SQLException
	 */
	public void addVariables(List<Var> vars, String wfId) throws SQLException {

		Statement stmt;
		stmt = dbConn.createStatement();

		String q;
		for (Var v : vars) {

			int isInput = v.isInput() ? 1 : 0;

			// q = "INSERT INTO Var "
			// + "SET varname = \""
			// + v.getVName()
			// + "\", "
			// + "pNameRef = \""
			// + v.getPName()
			// + "\", "
			// + "inputOrOutput = \""
			// + isInput
			// + "\", "
			// + "nestingLevel = \""
			// + (v.getTypeNestingLevel() >= 0 ? v.getTypeNestingLevel()
			// : 0) + "\", " + "wfInstanceRef = \"" + wfId + "\";";

			q = "INSERT INTO Var (varname, pNameRef, inputOrOutput, nestingLevel, wfInstanceRef) VALUES(\'"
					+ v.getVName()
					+ "\',\'"
					+ v.getPName()
					+ "\',"
					+ isInput
					+ ","
					+ (v.getTypeNestingLevel() >= 0 ? v.getTypeNestingLevel()
							: 0) + ",\'" + wfId + "\')";

			// q = "UPDATE Var "
			// + "SET varname = \""
			// + v.getVName()
			// + "\", "
			// + "pNameRef = \""
			// + v.getPName()
			// + "\", "
			// + "inputOrOutput = \""
			// + isInput
			// + "\", "
			// + "nestingLevel = \""
			// + (v.getTypeNestingLevel() >= 0 ? v.getTypeNestingLevel()
			// : 0) + "\", " + "wfInstanceRef = \"" + wfId + "\"";

			System.out.println("executing: " + q);

			try {

				int result = stmt.executeUpdate(q);
				System.out.println(result + " rows added to DB");

			} catch (Exception e) {
				System.out.println(e.toString());
				// ignore this insert and continue
				continue;
			}

		}
	}

	/**
	 * inserts one row into the ARC DB table -- OBSOLETE, see instead
	 * 
	 * @param sourceVar
	 * @param sinkVar
	 * @param wfId
	 */
	public void addArc(Var sourceVar, Var sinkVar, String wfId)
			throws SQLException {

		Statement stmt = dbConn.createStatement();

		// String q = "INSERT INTO Arc SET wfInstanceRef = \"" + wfId + "\", "
		// + "sourcePNameRef = \"" + sourceVar.getPName() + "\", "
		// + "sourceVarNameRef = \"" + sourceVar.getVName() + "\", "
		// + "sinkPNameRef = \"" + sinkVar.getPName() + "\", "
		// + "sinkVarNameRef = \"" + sinkVar.getVName() + "\";";

		String q = "INSERT INTO wfInstanceRef (wfInstanceRef, sourcePNameRef, SourceVarNameRef, sinkPNameRef,sinkVarNameRef) VALUES(\'"
				+ wfId
				+ "\',\'"
				+ sourceVar.getPName()
				+ "\',\'"
				+ sourceVar.getVName()
				+ "\',\'"
				+ sinkVar.getPName()
				+ "\',\'"
				+ sinkVar.getVName() + "\')";

		// String q = "UPDATE Arc SET wfInstanceRef = \"" + wfId + "\", "
		// + "sourcePNameRef = \"" + sourceVar.getPName() + "\", "
		// + "sourceVarNameRef = \"" + sourceVar.getVName() + "\", "
		// + "sinkPNameRef = \"" + sinkVar.getPName() + "\", "
		// + "sinkVarNameRef = \"" + sinkVar.getVName() + "\"";

		System.out.println("executing: " + q);
		int result = stmt.executeUpdate(q);
		System.out.println("workflow id: " + result + " rows added to DB");
	}

	public void addArc(String sourceVarName, String sourceProcName,
			String sinkVarName, String sinkProcName, String wfId)
			throws SQLException {

		Statement stmt = dbConn.createStatement();

		// String q = "INSERT INTO Arc SET wfInstanceRef = \"" + wfId + "\", "
		// + "sourcePNameRef = \"" + sourceProcName + "\", "
		// + "sourceVarNameRef = \"" + sourceVarName + "\", "
		// + "sinkPNameRef = \"" + sinkProcName + "\", "
		// + "sinkVarNameRef = \"" + sinkVarName + "\";";

		String q = "INSERT INTO Arc (wfInstanceRef, sourcePNameRef, sourceVarNameRef, sinkPNameRef, sinkVarNameRef) VALUES(\'"
				+ wfId
				+ "\',\'"
				+ sourceProcName
				+ "\',\'"
				+ sourceVarName
				+ "\',\'" + sinkProcName + "\',\'" + sinkVarName + "\')";
		//
		// String q = "UPDATE Arc SET wfInstanceRef = \"" + wfId + "\", "
		// + "sourcePNameRef = \"" + sourceProcName + "\", "
		// + "sourceVarNameRef = \"" + sourceVarName + "\", "
		// + "sinkPNameRef = \"" + sinkProcName + "\", "
		// + "sinkVarNameRef = \"" + sinkVarName + "\"";

		System.out.println("executing: " + q);
		int result = stmt.executeUpdate(q);
		System.out.println("workflow id: " + result + " rows added to DB");

	}

	public void addWFId(String wfId) throws SQLException {

		Statement stmt = dbConn.createStatement();

		// String q = "INSERT INTO Workflow SET wfname = \"" + wfId + "\";";

		String q = "INSERT INTO Workflow (wfName) VALUES (\'"
				+ wfId + "\')";

		// String q = "UPDATE Workflow SET wfname = \"" + wfId + "\"";

		System.out.println("executing: " + q);
		int result = stmt.executeUpdate(q);
		System.out.println("workflow id: " + result + " rows added to DB");
	}

	public void addWFInstanceId(String wfId) throws SQLException {

		Statement stmt = dbConn.createStatement();

		// String q = "INSERT INTO WfInstance SET instanceID = \"" + wfId + "\""
		// + ", wfnameRef = \"" + wfId + "\";";

		String q = "INSERT INTO WfInstance (instanceID, wfnameRef) VALUES(\'"
				+ wfId + "\',\'" + wfId + "\')";

		// String q = "UPDATE WfInstance SET instanceID = \"" + wfId + "\""
		// + ", wfnameRef = \"" + wfId + "\"";

		System.out.println("executing: " + q);
		int result = stmt.executeUpdate(q);
		System.out.println("workflow id: " + result + " rows added to DB");
	}

	/**
	 * insert new processor into the provenance DB
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public void addProcessor(String name, String wfID) throws SQLException {

		Statement stmt = dbConn.createStatement();

		// String q = "INSERT INTO Processor SET pname = \"" + name
		// + "\", wfInstanceRef = \"" + wfID + "\";";

		String q = "INSERT INTO Processor (pname, wfInstanceRef) VALUES(\'"
				+ name + "\',\'" + wfID + "\')";

		// String q = "UPDATE Processor SET pname = \"" + name
		// + "\", wfInstanceRef = \"" + wfID + "\"";

		System.out.println("executing: " + q);

		int result = stmt.executeUpdate(q);

		System.out.println("*** addProcessor: " + result + " rows added to DB");
	}

	public void addProcessorBinding(ProcBinding pb) throws SQLException {

		Statement stmt = dbConn.createStatement();

		// String q = "INSERT INTO ProcBinding SET " + "pnameRef = \""
		// + pb.getPNameRef() + "\", " + "execIDRef = \""
		// + pb.getExecIDRef() + "\", " + "iteration = \""
		// + pb.getIterationVector() + "\", " + "actName = \""
		// + pb.getActName() + "\";";

		String q = "INSERT INTO ProcBinding (pnameRef, execIDRef, iteration, actName) VALUES(\'"
				+ pb.getPNameRef()
				+ "\',\'"
				+ pb.getExecIDRef()
				+ "\',\'"
				+ pb.getIterationVector() + "\',\'" + pb.getActName() + "\')";

		// String q = "UPDATE ProcBinding SET " + "pnameRef = \""
		// + pb.getPNameRef() + "\", " + "execIDRef = \""
		// + pb.getExecIDRef() + "\", " + "iteration = \""
		// + pb.getIterationVector() + "\", " + "actName = \""
		// + pb.getActName() + "\"";

		// System.out.println("executing: "+q);

		int result = stmt.executeUpdate(q);

		// System.out.println("Processor binding: processor
		// ["+pb.getPNameRef()+"] activity ["+pb.getActName()+"]");
		// System.out.println("*** addProcessorBinding: "+result+ " rows added
		// to DB");

	}

	public String addCollection(String processorId, String collId,
			String parentCollectionId, String iteration, String portName,
			String dataflowId) throws SQLException {

		String newParentCollectionId = null;

		Statement stmt = dbConn.createStatement();

		if (parentCollectionId == null) {
			// this is a top-level list
			parentCollectionId = "TOP";
		}

		newParentCollectionId = collId;

		// String q = "INSERT INTO Collection SET " + "PNameRef = \""
		// + processorId + "\", " + "wfInstanceRef = \"" + dataflowId
		// + "\",  " + "varNameRef = \"" + portName + "\", "
		// + "iteration = \"" + iteration + "\", "
		// + "parentCollIdRef = \"" + parentCollectionId + "\", "
		// + "collId = \"" + collId + "\";";

		String q = "INSERT INTO Collection (PNameRef, wfInstanceRef, varNameRef, iteration, parentCollIdRef, collId) VALUES(\'"
				+ processorId
				+ "\',\'"
				+ dataflowId
				+ "\',\'"
				+ portName
				+ "\',\'"
				+ iteration
				+ "\',\'"
				+ parentCollectionId
				+ "\',\'"
				+ collId + "\')";

		// String q = "UPDATE Collection SET " + "PNameRef = \""
		// + processorId + "\", " + "wfInstanceRef = \"" + dataflowId
		// + "\",  " + "varNameRef = \"" + portName + "\", "
		// + "iteration = \"" + iteration + "\", "
		// + "parentCollIdRef = \"" + parentCollectionId + "\", "
		// + "collId = \"" + collId + "\"";

		System.out.println("collection: processor [" + processorId
				+ "] collId [" + collId + "]");

		int result = stmt.executeUpdate(q);

		System.out.println("*** collection: " + result + " rows added to DB");

		return newParentCollectionId;

	}

	public void addVarBinding(VarBinding vb) throws SQLException {

		Statement stmt = dbConn.createStatement();

		// String q = "INSERT INTO VarBinding SET " + "pnameRef = \""
		// + vb.getPNameRef() + "\", " + "wfInstanceRef = \""
		// + vb.getWfInstanceRef() + "\",  " + "varNameRef = \""
		// + vb.getVarNameRef() + "\", " + "valueType = \""
		// + vb.getValueType() + "\", " + "value  = \"" + vb.getValue()
		// + "\", " + "ref    = \"" + vb.getRef() + "\", "
		// + "collIdRef    = \"" + vb.getCollIDRef() + "\", "
		// + "iteration    = \"" + vb.getIteration() + "\", "
		// + "positionInColl = \"" + vb.getPositionInColl() + "\";";

		String q = "INSERT INTO VarBinding (pnameRef, wfInstanceRef, varNameRef, valueType, value, ref, collIdRef, iteration,positionInCall) VALUES(\'"
				+ vb.getPNameRef()
				+ "\',\'"
				+ vb.getWfInstanceRef()
				+ "\',\'"
				+ vb.getVarNameRef()
				+ "\',\'"
				+ vb.getValueType()
				+ "\',\'"
				+ vb.getValue()
				+ "\',\'"
				+ vb.getRef()
				+ "\',\'"
				+ vb.getCollIDRef()
				+ "\',\'"
				+ vb.getIteration()
				+ "\',\'"
				+ vb.getPositionInColl() + "\')";

		// String q = "UPDATE VarBinding SET " + "pnameRef = \""
		// + vb.getPNameRef() + "\", " + "wfInstanceRef = \""
		// + vb.getWfInstanceRef() + "\",  " + "varNameRef = \""
		// + vb.getVarNameRef() + "\", " + "valueType = \""
		// + vb.getValueType() + "\", " + "value  = \"" + vb.getValue()
		// + "\", " + "ref    = \"" + vb.getRef() + "\", "
		// + "collIdRef    = \"" + vb.getCollIDRef() + "\", "
		// + "iteration    = \"" + vb.getIteration() + "\", "
		// + "positionInColl = \"" + vb.getPositionInColl() + "\"";

		System.out.println("Var binding: processor [" + vb.getPNameRef()
				+ "] varName [" + vb.getVarNameRef() + "] collIdRef ["
				+ vb.getCollIDRef() + "] iteration [" + vb.getIteration()
				+ "] positionInCollection [" + vb.getPositionInColl()
				+ "] value [" + vb.getValue() + "]");

		int result = stmt.executeUpdate(q);

		System.out
				.println("*** addVarBinding: " + result + " rows added to DB");

	}

	/**
	 * deletes entire DB contents -- for testing purposes
	 * 
	 * @throws SQLException
	 */
	public void clearDB() throws SQLException {

		String q = null;
		int result = 0;

		if (dbConn == null) {
			openConnection();
		}

		Statement stmt = dbConn.createStatement();

		q = "DELETE FROM Workflow";
		// System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		// System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Processor";
		// System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		// System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Arc";
		// System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		// System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Var";
		// System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		// System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM WfInstance";
		// System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		// System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM ProcBinding";
		// System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		// System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM VarBinding";
		// System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		// System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Collection";
		// System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		// System.out.println(result+ " rows removed from DB");

		System.out.println(" **** DB cleared ****");

	}

	/**
	 * Create a new database and insert all the necessary tables
	 * 
	 * @throws SQLException
	 */
	public void createDB() throws SQLException {
		System.out.println("!!!!!!!!!!!creating database!!!!!!!!!!");

		Statement stmt = dbConn.createStatement();
//		int result = stmt.executeUpdate(createDB);
		int result;
		try {
			result = stmt.executeUpdate(createTableArc);
		} catch (Exception e) {
			System.out.println("create table arc failed: " + e.toString());
		}
		try {
			stmt.executeUpdate(createTableCollection);
		} catch (Exception e) {
			System.out.println("create table collection failed: "
					+ e.toString());
		}
		try {
			stmt.executeUpdate(createTableProcBinding);
		} catch (Exception e) {
			System.out.println("create table proc binding failed: "
					+ e.toString());
		}

		try {
			stmt.executeUpdate(createTableProcessor);
		} catch (Exception e) {
			System.out
					.println("create table processor failed: " + e.toString());
		}
		try {
			stmt.executeUpdate(createTableVar);
		} catch (Exception e) {
			System.out.println(createTableVar);
			System.out.println("create table var failed: " + e.toString());
		}
		try {
			stmt.executeUpdate(createTableVarBinding);
		} catch (Exception e) {
			System.out.println(createTableVarBinding);
			System.out.println("create table var binding failed: "
					+ e.toString());
		}
		try {
			stmt.executeUpdate(createTableWFInstance);
		} catch (Exception e) {
			System.out.println("create table wfinstance failed: "
					+ e.toString());
		}
		try {
			stmt.executeUpdate(createTableWorkflow);
		} catch (Exception e) {
			System.out.println("create table workflow failed: " + e.toString());
		}

	}

	/**
	 *Where should the database be placed, not always necessary depending on
	 * the db solution you are using
	 * 
	 * @return
	 */
	public String getLocation() {
		return location;
	}

	public void addWFInstanceId(String wfId, String wfInstanceId)
			throws SQLException {
		// TODO Auto-generated method stub
		
	}

}
