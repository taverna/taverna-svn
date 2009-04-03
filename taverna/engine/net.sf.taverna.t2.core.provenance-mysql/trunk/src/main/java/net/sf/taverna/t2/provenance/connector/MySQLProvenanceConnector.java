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
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.output.XMLOutputter;

import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResult;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.provenance.lineageservice.LineageSQLQuery;
import net.sf.taverna.t2.provenance.lineageservice.Provenance;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenance;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workbench.provenance.ProvenanceConfiguration;

public class MySQLProvenanceConnector implements ProvenanceConnector {

	private static Logger logger = Logger
			.getLogger(MySQLProvenanceConnector.class);

	private static final String EVENTS_LOG_DIR = "/tmp/TEST-EVENTS";

	private static final String deleteDB = "drop database T2Provenance;";
	
	private static final String createDB = "CREATE DATABASE IF NOT EXISTS T2Provenance";

	private static final String createTableArc = "CREATE TABLE IF NOT EXISTS  `T2Provenance`.`Arc` ("
			+ "`sourceVarNameRef` varchar(100) NOT NULL COMMENT 'ref. to var name for source of arc',"
			+ "`sinkVarNameRef` varchar(100) NOT NULL COMMENT 'ref. to var name for sink of arc',"
			+ "`sourcePNameRef` varchar(100) NOT NULL,"
			+ "`sinkPNameRef` varchar(100) NOT NULL,"
			+ "`wfInstanceRef` varchar(100) NOT NULL,"
			+ "PRIMARY KEY  USING BTREE (`sourceVarNameRef`,`sinkVarNameRef`,`sourcePNameRef`,`sinkPNameRef`,`wfInstanceRef`)"
			+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='static -- arc between two processors';";

	private static final String createTableCollection = "CREATE TABLE  IF NOT EXISTS `T2Provenance`.`Collection` ("
			+ "`collID` varchar(100) NOT NULL COMMENT 'ID of a list (collection). not sure yet what this looks like... ',"
			+ "`parentCollIDRef` varchar(100) NOT NULL default 'TOP' COMMENT 'used for list nesting.\ndefault is dummy list TOP since this attr. is key',"
			+ "`wfInstanceRef` varchar(100) NOT NULL,"
			+ "`PNameRef` varchar(100) NOT NULL,"
			+ "`varNameRef` varchar(100) NOT NULL,"
			+ "`iteration` char(10) NOT NULL default '',"
			+ " PRIMARY KEY  USING BTREE (`collID`,`wfInstanceRef`,`PNameRef`,`varNameRef`,`parentCollIDRef`,`iteration`)"
			+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='dynamic -- contains IDs of lists (T2 collections)';";

	private static final String createTableProcBinding = "CREATE TABLE IF NOT EXISTS  `T2Provenance`.`ProcBinding` ("
			+ "`pnameRef` varchar(100) NOT NULL COMMENT 'ref to static processor name',"
			+ "`execIDRef` varchar(100) NOT NULL COMMENT 'ref. to ID of wf execution',"
			+ "`actName` varchar(100) NOT NULL COMMENT 'name of activity bound to this processor',"
			+ "`iteration` char(10) NOT NULL default '',"
			+ "PRIMARY KEY  USING BTREE (`pnameRef`,`execIDRef`,`iteration`)"
			+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='dynamic -- binding of processor to activity';";

	private static final String createTableProcessor = "CREATE TABLE IF NOT EXISTS `T2Provenance`.`Processor` ("
			+ "`pname` varchar(100) NOT NULL,"
			+ "`wfInstanceRef` varchar(100) NOT NULL COMMENT 'ref to WfInstance.wfInstanceID',"
			+ "`type` varchar(100) default NULL COMMENT 'processor type',"
			+ "PRIMARY KEY  (`pname`,`wfInstanceRef`)"
			+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='static -- all processors for all workflows, by name';";

	private static final String createTableVar = "CREATE TABLE IF NOT EXISTS `T2Provenance`.`Var` ("
			+ "`varName` varchar(100) NOT NULL,"
			+ "`type` varchar(20) default NULL COMMENT 'variable type',"
			+ "`inputOrOutput` tinyint(1) NOT NULL COMMENT '1 = input, 0 = output',"
			+ "`pnameRef` varchar(100) NOT NULL COMMENT 'reference to the processor',"
			+ "`wfInstanceRef` varchar(100) NOT NULL,"
			+ "`nestingLevel` int(10) unsigned default '0',"
			+ "`actualNestingLevel` int(10) unsigned default '0',"
			+ "`anlSet` tinyint(1) default NULL,"
			+ "PRIMARY KEY  USING BTREE (`varName`,`inputOrOutput`,`pnameRef`,`wfInstanceRef`)"
			+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='static -- input and output variables (processor port names i';";

	private static final String createTableVarBinding = "CREATE TABLE IF NOT EXISTS `T2Provenance`.`VarBinding` ("
			+ "`varNameRef` varchar(100) NOT NULL COMMENT 'ref to var name',"
			+ "`wfInstanceRef` varchar(100) NOT NULL COMMENT 'ref to execution ID',"
			+ "`value` varchar(100) default NULL COMMENT 'ref to value. Either a string value or a string ref (URI) to a value',"
			+ "`collIDRef` varchar(100) NOT NULL default 'TOP',"
			+ "`positionInColl` int(10) unsigned NOT NULL default '1' COMMENT 'position within collection. default is 1',"
			+ "`PNameRef` varchar(100) NOT NULL,"
			+ "`valueType` varchar(50) default NULL,"
			+ "`ref` varchar(100) default NULL,"
			+ "`iteration` char(10) NOT NULL default '',"
			+ "PRIMARY KEY  USING BTREE (`varNameRef`,`wfInstanceRef`,`PNameRef`,`positionInColl`,`iteration`,`collIDRef`),"
			+ "KEY `collectionFK` (`wfInstanceRef`,`PNameRef`,`varNameRef`,`collIDRef`)"
			+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='dynamic -- binding of variables to values ';";

	private static final String createTableWFInstance = "CREATE TABLE IF NOT EXISTS `T2Provenance`.`WfInstance` ("
			+ "`instanceID` varchar(100) NOT NULL COMMENT 'T2-generated ID for one execution',"
			+ "`wfnameRef` varchar(100) NOT NULL COMMENT 'ref to name of the workflow being executed',"
			+ "`timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT 'when execution has occurred',"
			+ "PRIMARY KEY  (`instanceID`, `wfnameRef`)"
			+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='dynamic -- execution of a workflow';";

	private static final String createTableWorkflow = "CREATE TABLE IF NOT EXISTS `T2Provenance`.`Workflow` ("
			+ "`wfname` varchar(100) NOT NULL, `parentWFname` varchar(100), "
			+ "PRIMARY KEY  (`wfname`)"
			+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='static -- all known workflows by name';";

	@Override
	public String toString() {
		return "MySQL Provenance Connector";
	}

	private ReferenceService rs = null;
	private Provenance provenance;
	private String password;
	private String user;
	private String location;
	private boolean isClearDB = false;
	private String saveEvents = null;

	private Connection dbConn;

	public MySQLProvenanceConnector() {
	}

	public List<ProvenanceItem> getProvenanceCollection() {
		System.out
				.println("invoked: LocalConnector::getProvenanceCollection()");
		return null;
	}

	public void store(ReferenceService referenceService) {

		System.out.println("invoked: LocalConnector::store()");

	}

	/**
	 * main entry point into the service
	 */
	public void addProvenanceItem(ProvenanceItem provenanceItem) {

		String content = provenanceItem.getAsString();


		if (provenanceItem.getEventType().equals("EOW"))
  		   System.out.println("EVENT: " + provenanceItem.getEventType());

		if (content == null) {

			XMLOutputter outputter = new XMLOutputter();
			content = outputter.outputString(provenanceItem.getAsXML(rs));

		}

		try {
			provenance.acceptRawProvenanceEvent(provenanceItem.getEventType(),
					content);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void createDatabase() {
		try {
			if (dbConn == null) {
				String user = ProvenanceConfiguration.getInstance()
						.getProperty("dbUser");
				if (user != null) {

					this.user = user;
				}
				String password = ProvenanceConfiguration.getInstance()
						.getProperty("dbPassword");
				if (password != null) {

					this.password = password;
				}
				String location = ProvenanceConfiguration.getInstance()
						.getProperty("dbURL");
				if (location != null) {

					this.location = location;
				}

				String jdbcString = "jdbc:mysql://" + this.location
						+ "/T2Provenance?user=" + this.user + "&password="
						+ this.password;

				openConnection(jdbcString);
			}
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
		Statement stmt;
		try {
			stmt = dbConn.createStatement();
			stmt.executeUpdate(createDB);
			stmt.executeUpdate(createTableArc);
			stmt.executeUpdate(createTableCollection);
			stmt.executeUpdate(createTableProcBinding);
			stmt.executeUpdate(createTableProcessor);
			stmt.executeUpdate(createTableVar);
			stmt.executeUpdate(createTableVarBinding);
			stmt.executeUpdate(createTableWFInstance);
			stmt.executeUpdate(createTableWorkflow);

		} catch (SQLException e) {
			logger
					.warn("There was a problem creating the Provenance database database: "
							+ e.toString());
		}

	}

	public void deleteDatabase() {
		try {
			if (dbConn == null) {
				this.user = ProvenanceConfiguration.getInstance().getProperty(
						"dbUser");
				this.password = ProvenanceConfiguration.getInstance()
						.getProperty("dbPassword");
				this.location = ProvenanceConfiguration.getInstance()
						.getProperty("dbURL");
				String jdbcString = "jdbc:mysql://" + location
						+ "/T2Provenance?user=" + user + "&password="
						+ password;
				openConnection(jdbcString);
			}
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
		Statement stmt;
		try {
			stmt = dbConn.createStatement();
			stmt.executeUpdate(deleteDB);
		} catch (SQLException e) {
			logger
					.warn("There was a problem deleting the Provenance database: "
							+ e.toString());
		}

	}

	public String getName() {
		return "mysqlprovenance";
	}

	public ReferenceService getReferenceService() {
		return rs;
	}

	public String getSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDBLocation(String location) {
		this.location = location;
		// this.location = "jdbc:mysql://" + location;
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.rs = referenceService;
	}

	public void setSessionId(String identifier) {
		// TODO Auto-generated method stub

	}

	public void store(ProvenanceItem provenanceItem) {
		// TODO Auto-generated method stub

	}

	public String getPassword() {
		return this.password;
	}

	public String getUser() {
		return this.user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void init() {
		String jdbcString = "jdbc:mysql://" + location + "/T2Provenance?user="
				+ user + "&password=" + password;
		try {
			provenance = new MySQLProvenance(jdbcString, this.isClearDB);
			provenance.setSaveEvents(this.saveEvents);
			
			// clear the events dir -- hacked up in a hurry
			File dir = new File(EVENTS_LOG_DIR);
			File[] allFiles = dir.listFiles();
			
			if (allFiles != null) for (File f:allFiles) { f.delete(); }
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void openConnection(String connection)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {

		getClass().getClassLoader().loadClass("com.mysql.jdbc.Driver")
				.newInstance();
		System.out.println("connection");

		try {
			dbConn = DriverManager.getConnection(connection);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getdbName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setdbName(String dbName) {
		// TODO Auto-generated method stub

	}

	public String getIntermediateValues(String processorName, String dataflowId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @return the isClearDB
	 */
	public boolean isClearDB() {
		return isClearDB;
	}

	public void setClearDB(boolean isClearDB) {
		this.isClearDB = isClearDB;
	}

	public boolean isFinished() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getIntermediateValues(String wfInstance, String pname,
			String vname, String iteration) throws SQLException {
		LineageSQLQuery simpleLineageQuery = ((MySQLProvenance) provenance)
				.getPq()
				.simpleLineageQuery(wfInstance, pname, vname, iteration);
		LineageQueryResult runLineageQuery;
		try {
			runLineageQuery = ((MySQLProvenance) provenance).getPq()
					.runLineageQuery(simpleLineageQuery);
		} catch (SQLException e) {
			throw e;
		}
		// String result =
		// "<table><tr><th>Workflow</th><th>Processor</th><th>Iteration</th><th>Value</th><th>Type</th><th>Variable Name</th></tr>";
		//
		// for (LineageQueryResultRecord record : runLineageQuery.getRecords())
		// {
		// result = result + "<tr><td>" + record.getWfInstance() + "</td><td>"
		// + record.getPname() + "</td><td>" + record.getIteration()
		// + "</td><td>" + record.getValue() + "</td><td>"
		// + record.getType() + "</td><td>" + record.getVname()
		// + "</td></tr>";
		// }
		String result = "<table><tr><th>Iteration</th><th>Value</th><th>Variable Name</th></tr>";

		for (LineageQueryResultRecord record : runLineageQuery.getRecords()) {
			result = result + "<tr><td>" + record.getIteration() + "</td><td>"
					+ record.getValue() + "</td><td>" + record.getVname()
					+ "</td></tr>";
		}
		result = result + "</table>";

		return result;
	}

	/**
	 * @return the saveEvents
	 */
	public String getSaveEvents() {
		return saveEvents;
	}

	/**
	 * @param saveEvents
	 *            the saveEvents to set
	 */
	public void setSaveEvents(String saveEvents) {
		this.saveEvents = saveEvents;
	}

	public String getDataflowInstance(String dataflowId) {
		String instanceID = null;
		try {
			instanceID = ((MySQLProvenance) provenance).getPq()
					.getWFInstanceID(dataflowId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return instanceID;
	}

	public void clearDatabase() {
		try {
			if (dbConn == null) {
				String user = ProvenanceConfiguration.getInstance()
						.getProperty("dbUser");
				if (user != null) {

					this.user = user;
				}
				String password = ProvenanceConfiguration.getInstance()
						.getProperty("dbPassword");
				if (password != null) {

					this.password = password;
				}
				String location = ProvenanceConfiguration.getInstance()
						.getProperty("dbURL");
				if (location != null) {

					this.location = location;
				}

				String jdbcString = "jdbc:mysql://" + this.location
						+ "/T2Provenance?user=" + this.user + "&password="
						+ this.password;

				openConnection(jdbcString);
			}
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
		String q = null;

		Statement stmt = null;
		try {
			stmt = dbConn.createStatement();
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

}
