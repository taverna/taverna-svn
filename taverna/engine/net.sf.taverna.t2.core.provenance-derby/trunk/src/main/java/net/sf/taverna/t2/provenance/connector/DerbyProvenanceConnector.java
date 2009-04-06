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
package net.sf.taverna.t2.provenance.connector;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResult;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.provenance.lineageservice.LineageSQLQuery;
import net.sf.taverna.t2.provenance.lineageservice.Provenance;
import net.sf.taverna.t2.provenance.lineageservice.derby.DerbyProvenance;
import net.sf.taverna.t2.reference.ReferenceService;

import org.apache.commons.io.FileUtils;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Logger;
import org.jdom.output.XMLOutputter;

public class DerbyProvenanceConnector implements ProvenanceConnector {

	private static Logger logger = Logger
			.getLogger(DerbyProvenanceConnector.class);

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
			+ "wfInstanceRef varchar(100) NOT NULL," + "nestingLevel int,"
			+ "actualNestingLevel int," + "anlSet smallint default NULL,"
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
			+ " PRIMARY KEY (instanceID, wfnameRef))";

	private static final String createTableWorkflow = "CREATE TABLE Workflow ("
			+ "wfname varchar(100) NOT NULL," + "parentWFname varchar(100),"
			+ "PRIMARY KEY  (wfname))";

	private ArrayList<ProvenanceItem> provenanceCollection;

	private boolean driverLoaded = false;

	private String provenance;

	private String name;

	private Provenance localProvenance;

	private ReferenceService referenceService;

	private String portlocation = "27467";

	private Connection dbConn;

	private boolean isClearDB = false;

	private NetworkServerControl server;

	private String location;

	private String dbURL = "jdbc:derby:provenance;create=true;upgrade=true";

	public DerbyProvenanceConnector() {

	}

	public void openConnection() {
		try {
			dbConn = DriverManager.getConnection(dbURL);
			dbConn.setAutoCommit(true);
		} catch (SQLException e) {
			logger.warn(e);
		}
	}

	public void init() {

		File applicationHomeDir = ApplicationRuntime.getInstance()
				.getApplicationHomeDir();
		File dbFile = new File(applicationHomeDir, "provenance");
		dbFile.toString();

		dbURL = "jdbc:derby:" + dbFile + "/db;create=true;upgrade=true";

		provenanceCollection = new ArrayList<ProvenanceItem>();
		name = "Local Derby DB";

		try {

			localProvenance = new DerbyProvenance(location, this);
		} catch (InstantiationException e) {
			logger.warn(e);
		} catch (IllegalAccessException e) {
			logger.warn(e);
		} catch (ClassNotFoundException e) {
			logger.warn(e);
		} catch (SQLException e) {
			logger.warn(e);
		}

		if (isClearDB) {
			try {
				((DerbyProvenance) localProvenance).getPw().clearDBStatic();
				((DerbyProvenance) localProvenance).getPw().clearDBDynamic();
			} catch (SQLException e) {
				logger.warn(e);
			}
		}

	}

	public String getProvenance() {
		return provenance;
	}

	public void saveProvenance(String annotation) {
		provenance = annotation;
	}

	public List<ProvenanceItem> getProvenanceCollection() {
		return provenanceCollection;
	}

	@SuppressWarnings("unchecked")
	public synchronized void store(ProvenanceItem provenanceItem) {

	}

	public void createDatabase() {
		File applicationHomeDir = ApplicationRuntime.getInstance()
				.getApplicationHomeDir();
		File dbFile = new File(applicationHomeDir, "provenance");
		try {
			FileUtils.forceMkdir(dbFile);
		} catch (IOException e2) {
			logger.warn("Could not create root provenance directory: "
					+ dbFile.toString() + " " + e2);
		}
		dbURL = "jdbc:derby:" + dbFile.toString() + "/db;create=true;upgrade=true";

		Statement stmt = null;
		try {
			stmt = getConnection().createStatement();
		} catch (SQLException e1) {
			logger.warn(e1);
		}

		try {
			stmt.executeUpdate(createTableArc);
		} catch (Exception e) {
			logger.warn("Could not create table Arc : " + e);
		}
		try {
			stmt.executeUpdate(createTableCollection);
		} catch (Exception e) {
			logger.warn("Could not create table Collection : " + e);
		}
		try {
			stmt.executeUpdate(createTableProcBinding);
		} catch (Exception e) {
			logger.warn("Could not create table ProcBinding : " + e);
			;
		}

		try {
			stmt.executeUpdate(createTableProcessor);
		} catch (Exception e) {
			logger.warn("Could not create table Processor : " + e);
		}
		try {
			stmt.executeUpdate(createTableVar);
		} catch (Exception e) {
			logger.warn("Could not create table Var : " + e);
		}
		try {
			stmt.executeUpdate(createTableVarBinding);
		} catch (Exception e) {
			logger.warn("Could not create table Var Binding : " + e);
		}
		try {
			stmt.executeUpdate(createTableWFInstance);
		} catch (Exception e) {
			logger.warn("Could not create table WfInstance : " + e);
		}
		try {
			stmt.executeUpdate(createTableWorkflow);
		} catch (Exception e) {
			logger.warn("Could not create table Workflow : " + e);
		}
	}

	public void deleteDatabase() {

		try {
			dbConn.close();
		} catch (SQLException e) {
			logger.warn("Could not close the database");
		}
		dbConn = null;
		driverLoaded = false;
		File applicationHomeDir = ApplicationRuntime.getInstance()
				.getApplicationHomeDir();
		File dbFile = new File(applicationHomeDir, "provenance");
		File provFile = new File(dbFile, "db");
		try {
			FileUtils.deleteDirectory(provFile);
		} catch (IOException e) {
			logger.warn("Could not delete provenance directory: "
					+ provFile.toString() + " " + e);
		}
		// FIXME probably just remove the derby database file!!
		// if (dbConn == null) {
		// openConnection();
		// }
		//		
		// Statement stmt = null;
		// try {
		// stmt = dbConn.createStatement();
		// } catch (SQLException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		// // int result = stmt.executeUpdate(createDB);
		// int result;
		// try {
		// result = stmt.executeUpdate(deleteDB);
		// } catch (SQLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public String getName() {
		return "Derby DB Connector";
	}

	public void setDBLocation(String location) {
		this.location = location;
		// localProvenance.setLocation(location);
	}

	@Override
	public String toString() {
		return "Derby DB Connector";
	}

	public void addProvenanceItem(ProvenanceItem provenanceItem) {
		String content = provenanceItem.getAsString();

		if (content == null) {

			XMLOutputter outputter = new XMLOutputter();
			content = outputter.outputString(provenanceItem
					.getAsXML(referenceService));

		}

		try {
			localProvenance.acceptRawProvenanceEvent(provenanceItem
					.getEventType(), content);
		} catch (SQLException e) {
			logger.warn("Problem adding provenance item : " + e);
		} catch (IOException e) {
			logger.warn("Problem adding provenance item : " + e);
		}
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public ReferenceService getReferenceService() {
		return referenceService;
	}

	public String getSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSessionId(String identifier) {
		// TODO Auto-generated method stub

	}

	public String getPassword() {
		return null;
	}

	public String getUser() {
		return null;
	}

	public void setPassword(String password) {

	}

	public void setUser(String user) {

	}

	public String getdbName() {
		return null;
	}

	public void setdbName(String dbName) {

	}

	public String getIntermediateValues(String wfInstance, String pname,
			String vname, String iteration) throws SQLException {

		LineageSQLQuery simpleLineageQuery = ((DerbyProvenance) localProvenance)
				.getPq()
				.simpleLineageQuery(wfInstance, pname, vname, iteration);
		LineageQueryResult runLineageQuery;
		try {
			runLineageQuery = ((DerbyProvenance) localProvenance).getPq()
					.runLineageQuery(simpleLineageQuery);
		} catch (SQLException e) {
			throw e;
		}
		String result = "<table><tr><th>Iteration</th><th>Value</th><th>Variable Name</th></tr>";

		for (LineageQueryResultRecord record : runLineageQuery.getRecords()) {
			result = result + "<tr><td>" + record.getIteration() + "</td><td>"
					+ record.getValue() + "</td><td>" + record.getVname()
					+ "</td></tr>";
		}
		result = result + "</table>";

		return result;
	}

	public boolean isClearDB() {
		return isClearDB;
	}

	public boolean isFinished() {
		return false;
	}

	public void setClearDB(boolean isClearDB) {
		this.isClearDB = isClearDB;

	}

	public String getDataflowInstance(String dataflowId) {
		String instanceID = null;
		try {
			instanceID = ((DerbyProvenance) localProvenance).getPq()
					.getWFInstanceID(dataflowId);
		} catch (SQLException e) {
			logger.warn("Problem getting dataflow instance : " + e);
		}
		return instanceID;
	}

	public synchronized Connection getConnection() {
		if (!driverLoaded) {
			loadDriver();
		}

		if (dbConn == null) {
			openConnection();
		}
		return dbConn;
	}

	private void loadDriver() {
		try {
			getClass().getClassLoader().loadClass(
					"org.apache.derby.jdbc.EmbeddedDriver").newInstance();
		} catch (InstantiationException e) {
			logger.warn(e);
		} catch (IllegalAccessException e) {
			logger.warn(e);
		} catch (ClassNotFoundException e) {
			logger.warn(e);
		}
		driverLoaded = true;
	}

	public void clearDatabase() {
		String q = null;

		Statement stmt = null;
		try {
			stmt = getConnection().createStatement();
		} catch (SQLException e) {
			logger.warn("Could not create database statement :" + e);
		}

		q = "DELETE FROM Workflow";
		try {
			stmt.executeUpdate(q);
		} catch (SQLException e) {
			logger.warn("Could not execute statement " + q + " :" + e);
		}

		q = "DELETE FROM Processor";
		try {
			stmt.executeUpdate(q);
		} catch (SQLException e) {
			logger.warn("Could not execute statement " + q + " :" + e);
		}

		q = "DELETE FROM Arc";
		try {
			stmt.executeUpdate(q);
		} catch (SQLException e) {
			logger.warn("Could not execute statement " + q + " :" + e);
		}

		q = "DELETE FROM Var";
		try {
			stmt.executeUpdate(q);
		} catch (SQLException e) {
			logger.warn("Could not execute statement " + q + " :" + e);
		}
		
		q = "DELETE FROM WfInstance";
		try {
			stmt.executeUpdate(q);
		} catch (SQLException e) {
			logger.warn("Could not execute statement " + q + " :" + e);
		}

		q = "DELETE FROM ProcBinding";
		try {
			stmt.executeUpdate(q);
		} catch (SQLException e) {
			logger.warn("Could not execute statement " + q + " :" + e);
		}

		q = "DELETE FROM VarBinding";
		try {
			stmt.executeUpdate(q);
		} catch (SQLException e) {
			logger.warn("Could not execute statement " + q + " :" + e);
		}

		q = "DELETE FROM Collection";
		try {
			stmt.executeUpdate(q);
		} catch (SQLException e) {
			logger.warn("Could not execute statement " + q + " :" + e);
		}

		logger.info("Cleared provenance database");
		
	}

	public void addProvenanceItem(ProvenanceItem provenanceItem,
			Object invocationContext) {
		// TODO Auto-generated method stub
		
	}

	public String getSaveEvents() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setSaveEvents(String saveEvents) {
		// TODO Auto-generated method stub
		
	}
}
