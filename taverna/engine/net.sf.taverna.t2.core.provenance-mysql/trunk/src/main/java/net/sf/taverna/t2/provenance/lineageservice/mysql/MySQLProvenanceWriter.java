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
package net.sf.taverna.t2.provenance.lineageservice.mysql;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcBinding;
import net.sf.taverna.t2.provenance.lineageservice.utils.Var;
import net.sf.taverna.t2.provenance.lineageservice.utils.VarBinding;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
import net.sf.taverna.t2.reference.impl.T2ReferenceImpl;

/**
 * @author paolo
 *
 */
public class MySQLProvenanceWriter  implements ProvenanceWriter {

	Connection dbConn = null;
	private int cnt;  // counts number of calls to VarBinding 
	static boolean useDB = true;

	public MySQLProvenanceWriter(String connection) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		// open singleton connection to DB

		if (useDB) {
			openConnection(connection);
			System.out.println("successfully opened DB connection");
		}
	}


	public void openConnection(String connection) throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		getClass().getClassLoader().loadClass("com.mysql.jdbc.Driver")
		.newInstance();

		try {
			dbConn = DriverManager.getConnection(connection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

			q = "INSERT INTO Var "+
			"SET varname = \""+v.getVName()+"\", "+
			"pNameRef = \""+v.getPName()+"\", "+
			"inputOrOutput = \""+isInput+"\", "+
			"nestingLevel = \""+( v.getTypeNestingLevel() >= 0 ? v.getTypeNestingLevel() : 0 )+"\", "+
			"wfInstanceRef = \""+wfId+"\";";

//			System.out.println("executing: "+q);

			try {

				int result = stmt.executeUpdate(q);
//				System.out.println(result+ " rows added to DB");

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

		//	System.out.println("executing: "+q);
		int result = stmt.executeUpdate(q);
		//	System.out.println("workflow id: "+result+" rows added to DB");
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

		//	System.out.println("executing: "+q);
		int result = stmt.executeUpdate(q);
		//	System.out.println("workflow id: "+result+" rows added to DB");

	}




	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceWriter#addWFId(java.lang.String)
	 */
	public void addWFId(String wfId) throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO Workflow SET wfname = \""+wfId+"\";";

//		System.out.println("executing: "+q);
		int result = stmt.executeUpdate(q);
//		System.out.println("workflow id: "+result+" rows added to DB");
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceWriter#addWFId(java.lang.String)
	 */
	public void addWFId(String wfId, String parentWFname) throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO Workflow SET wfname = \""+wfId+"\", parentWFname = \""+parentWFname+"\";";

//		System.out.println("executing: "+q);
		int result = stmt.executeUpdate(q);
//		System.out.println("workflow id: "+result+" rows added to DB");
	}



	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceWriter#addWFInstanceId(java.lang.String, java.lang.String)
	 */
	public void addWFInstanceId(String wfId, String wfInstanceId) throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO WfInstance SET instanceID = \""+wfInstanceId+"\"" +
		", wfnameRef = \""+wfId+"\";";

//		System.out.println("executing: "+q);
		int result = stmt.executeUpdate(q);
//		System.out.println("workflow id: "+result+" rows added to DB");
	}


	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceWriter#addProcessor(java.lang.String, java.lang.String)
	 * add a processor to the DB with NULL type
	 */
	public void addProcessor(String name, String wfID) throws SQLException {
		addProcessor(name, null, wfID);
	}


	/**
	 * add a processor to the static portion of the DB with given name, type and wfnameRef scope 
	 * @param name
	 * @param type
	 * @param wfNameRef
	 * @throws SQLException
	 */
	public void addProcessor(String name, String type, String wfNameRef) throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO Processor SET pname = \""+name+"\", type = \""+type+"\", wfInstanceRef = \""+wfNameRef+"\";";

//		System.out.println("executing: "+q);

		int result = stmt.executeUpdate(q);

//		System.out.println("*** addProcessor: "+result+ " rows added to DB");

	}

		
	public void addProcessorBinding(ProcBinding pb) throws SQLException {
	}
	
		
	public void addProcessorBindingOFF(ProcBinding pb) throws SQLException {

		Statement stmt = dbConn.createStatement();

		String q = "INSERT INTO ProcBinding SET "+
		"pnameRef = \""+pb.getPNameRef()+"\", "+
		"execIDRef = \""+pb.getExecIDRef()+"\", "+
		"iteration = \""+pb.getIterationVector()+"\", "+
		"actName = \""+pb.getActName()+"\";";

//		System.out.println("executing: "+q);

		stmt.executeUpdate(q);

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

		//	System.out.println("collection: processor ["+processorId+"] collId ["+collId+"]");

		int result = stmt.executeUpdate(q);

		//	System.out.println("*** collection: "+result+ " rows added to DB");

		return newParentCollectionId;

	}

	
	/**
	 * adds (dataRef, data) pairs to the Data table (only for string data)
	 */
	public void addData(String dataRef, String wfInstanceId, String data) throws SQLException {
		
		Statement stmt;
		try {
			stmt = dbConn.createStatement();

			String q = "INSERT INTO Data SET "+
			"dataReference = '"+dataRef+"', "+
			"wfInstanceID = '"+wfInstanceId+"', "+			
			"data = '"+data+"'; ";

			int result = stmt.executeUpdate(q);

			cnt++;

		} catch (SQLException e) {
			// the same ID will come in several times -- duplications are expected, don't panic
//			System.out.println("****  insert failed due to ["+e.getMessage()+"]");
		}  catch (ReferenceServiceException e1) {
			System.out.println(e1.getMessage());
		}

	}



	/**
	 * catching SQL errors  -> these may happen when the same event is generated twice due to a fail-retry condition
	 */
	public void addVarBinding(VarBinding vb, Object context) {

		Statement stmt;
		try {
			stmt = dbConn.createStatement();

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

//			if (cnt % 100 == 0) {
//			System.out.println("Var binding: instance ["+vb.getWfInstanceRef()+"] processor ["+vb.getPNameRef()+"] varName ["+vb.getVarNameRef()+
//					"] collIdRef ["+vb.getCollIDRef()+"] iteration ["+vb.getIteration()+
//					"] positionInCollection ["+vb.getPositionInColl()+"] value ["+vb.getValue()+"]");
//			}
			
			
			int result = stmt.executeUpdate(q);

			cnt++;

		} catch (SQLException e) {
			System.out.println("****  insert failed due to ["+e.getMessage()+"]");
		}  catch (ReferenceServiceException e1) {
			System.out.println(e1.getMessage());
		}
	}

	
	
	public void updateVarBinding(VarBinding vb) {

		Statement stmt;
		try {
			stmt = dbConn.createStatement();

			String q = "UPDATE VarBinding SET "+
			"valueType = \""+vb.getValueType()+"\", "+
			"value  = \""+vb.getValue()+"\", "+
			"ref    = \""+vb.getRef()+"\", "+			       
			"collIdRef    = \""+vb.getCollIDRef()+"\" "+
			"WHERE varNameRef = \""+vb.getVarNameRef()+"\" and "+
			"wfInstanceRef = \""+vb.getWfInstanceRef()+"\" and "+
			"pnameRef = \""+vb.getPNameRef()+"\" and "+
			"positionInColl = \""+vb.getPositionInColl()+"\" and "+
			"iteration    = \""+vb.getIteration()+"\" ";
			
//			if (cnt % 100 == 0) {
//			System.out.println("Var binding: instance ["+vb.getWfInstanceRef()+"] processor ["+vb.getPNameRef()+"] varName ["+vb.getVarNameRef()+
//					"] collIdRef ["+vb.getCollIDRef()+"] iteration ["+vb.getIteration()+
//					"] positionInCollection ["+vb.getPositionInColl()+"] value ["+vb.getValue()+"]");
//			}
			
			
			int result = stmt.executeUpdate(q);

			cnt++;

		} catch (SQLException e) {
			System.out.println("****  insert failed due to ["+e.getMessage()+"]");
		}  catch (ReferenceServiceException e1) {
			System.out.println(e1.getMessage());
		}
	}

	public void clearDBStatic() throws SQLException {

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

		System.out.println(" **** DB cleared STATIC ****");
	}


	/**
	 * deletes the static structure of a WF identified by its ID<br/>
	 * used to overwrite a WF structure over repeated runs
	 */
	public void clearDBStatic(String wfID) throws SQLException {

		String q = null;
		int result = 0;

		Statement stmt = dbConn.createStatement();

		q = "DELETE FROM Workflow WHERE wfname = \""+wfID+"\";";		
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Processor WHERE wfInstanceRef = \""+wfID+"\";";
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Arc WHERE wfInstanceRef = \""+wfID+"\";";	
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		q = "DELETE FROM Var WHERE wfInstanceRef = \""+wfID+"\";";		
		//System.out.println("executing: "+q);
		result = stmt.executeUpdate(q);
		//System.out.println(result+ " rows removed from DB");

		System.out.println(" **** DB cleared STATICfor wfID "+wfID+" ****");

	}



	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.provenance.lineageservice.mysql.ProvenanceWriter#clearDB()
	 */
	public void clearDBDynamic() throws SQLException {

		String q = null;
		int result = 0;

		Statement stmt = dbConn.createStatement();

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

		q = "DELETE FROM Data;";		
		result = stmt.executeUpdate(q);

		System.out.println(" **** DB cleared DYNAMIC ****");

	}


	public Connection openConnection() throws InstantiationException,
	IllegalAccessException, ClassNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}


	public void clearDD() {

		Statement stmt;
		try {
			stmt = dbConn.createStatement();

			String q = "DELETE FROM DD; ";		
			stmt.executeUpdate(q);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * used to support the implementation of 
	 * @param pname
	 * @param vFrom
	 * @param valFrom
	 * @param vTo
	 * @param valTo
	 * @param iteration 
	 * @param wfInstanceID 
	 */
	public void writeDDRecord(String pFrom, String vFrom, String valFrom, String pTo, String vTo, String valTo, String iteration, String wfInstanceID) {

		Statement stmt;
		try {
			stmt = dbConn.createStatement();

			String q = "INSERT INTO DD SET "+
			"PFrom = \""+pFrom+"\", "+
			"VFrom = \""+vFrom+"\",  "+
			"valFrom = \""+valFrom+"\", "+
			"PTo = \""+pTo+"\", "+
			"VTo = \""+vTo+"\", "+
			"valTo  = \""+valTo+"\", "+
			"iteration = \""+iteration+"\", "+
			"wfInstance = \""+wfInstanceID+"\"; ";

			stmt.executeUpdate(q);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}





}
