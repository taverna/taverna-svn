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

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.item.IterationProvenanceItem;
import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.lineageservice.EventProcessor;
import net.sf.taverna.t2.provenance.lineageservice.Provenance;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.WorkflowDataProcessor;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceAnalysis;
import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferenceSetService;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;

public class MySQLProvenanceConnector extends ProvenanceConnector {

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


	private ReferenceService referenceService;

	private InvocationContext invocationContext;

	public MySQLProvenanceConnector() {
	}

	public MySQLProvenanceConnector(Provenance provenance,
			ProvenanceAnalysis provenanceAnalysis, String dbURL,
			boolean isClearDB, String saveEvents) {
		super(provenance, provenanceAnalysis, dbURL, isClearDB, saveEvents);

//		// clear the DB prior to collecting new provenance
		if (isClearDB) {
			System.out.println("clearing DB");
			try {
				getProvenance().getPw().clearDBStatic();
				getProvenance().getPw().clearDBDynamic();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			System.out.println("clearDB is FALSE: not clearing");
		}


	}

	@Override
	public String toString() {
		return "MySQL Provenance Connector";
	}

	public List<ProvenanceItem> getProvenanceCollection() {
		System.out
		.println("invoked: LocalConnector::getProvenanceCollection()");
		return null;
	}

	
	
	/**
	 * main entry point into the service
	 */
	public synchronized void addProvenanceItem(final ProvenanceItem provenanceItem) {

		ReferenceService referenceService =
			invocationContext.getReferenceService();

		if (provenanceItem instanceof IterationProvenanceItem) {

			// provenance DB must be up and running when we get here, so it's
			// safe to write to it
			ProvenanceWriter pw = getProvenance().getPw();

			IterationProvenanceItem ipi = (IterationProvenanceItem) provenanceItem;

			Map<String, T2Reference> inputDataMap = ipi.getInputDataItem()
			.getDataMap();
			Map<String, T2Reference> outputDataMap = ipi.getOutputDataItem()
			.getDataMap();

			Set<Map.Entry<String, T2Reference>> allRefs = new HashSet<Map.Entry<String, T2Reference>>();

			for (Map.Entry<String, T2Reference> entry : inputDataMap.entrySet())
				allRefs.add(entry);
			for (Map.Entry<String, T2Reference> entry : outputDataMap
					.entrySet())
				allRefs.add(entry);

			for (Map.Entry<String, T2Reference> entry : allRefs) {

				T2Reference ref = entry.getValue();

				Identified id = referenceService.resolveIdentifier(entry
						.getValue(), null, invocationContext);
				if (id instanceof ReferenceSet) {

					byte[] renderedData = (byte[]) referenceService
					.renderIdentifier(entry.getValue(), byte[].class,
							invocationContext);

					System.out.println("****\ndata in provenance event: "
							+ entry.getValue() + " --> \n" + renderedData
							+ "\n *****");

					try {
						pw.addData(entry.getValue().toString(), getProvenance()
								.getEp().getWfInstanceID(), renderedData);
					} catch (SQLException e) {
						System.out
						.println("Exception while writing data to DB: "
								+ e.getMessage());
						e.printStackTrace();
					}

					// ReferenceSet rs = (ReferenceSet) id;
					// Set<ExternalReferenceSPI> externalRefs =
					// rs.getExternalReferences();
					// externalRefs.
				} else {
					System.out
					.println("input data in provenance event NOT a ReferenceSet: "
							+ entry.getValue());
				}
			}
		}

		// String content = provenanceItem.getAsString();

		if (provenanceItem.getEventType().equals(
				SharedVocabulary.END_WORKFLOW_EVENT_TYPE)) {
			logger.info("EVENT: " + provenanceItem.getEventType());
		}
		if (provenanceItem.getEventType().equals("EOW")) {
			logger.info("EOW EVENT arrived ");
		}

		// if (content == null) {
		//
		// XMLOutputter outputter = new XMLOutputter();
		// content = outputter.outputString(provenanceItem.getAsXML(rs));
		//
		// }

		Runnable runnable = new Runnable() {

			public void run() {
				try {

					getProvenance().acceptRawProvenanceEvent(
							provenanceItem.getEventType(), provenanceItem);

				} catch (SQLException e) {
					logger.warn("Could not add provenance: " + e);
				} catch (IOException e) {
					logger.warn("Could not add provenance: " + e);
				}

			}

		};
		getExecutor().submit(runnable);
	}

	public void createDatabase() {
		Statement stmt;
		try {
			stmt = getConnection().createStatement();
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

	}

	public void deleteDatabase() {

		Statement stmt;
		try {
			stmt = getConnection().createStatement();
			stmt.executeUpdate(deleteDB);
		} catch (SQLException e) {
			logger
			.warn("There was a problem deleting the Provenance database: "
					+ e.toString());
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

	}

	public String getName() {
		return "mysqlprovenance";
	}

	// public void init() {
	// String jdbcString = "jdbc:mysql://" + location + "/T2Provenance?user="
	// + user + "&password=" + password;
	// try {
	// setProvenance(new MySQLProvenance(jdbcString, this.isClearDB));
	// getProvenance().setSaveEvents(this.saveEvents);
	//
	// // clear the events dir -- hacked up in a hurry
	// File dir = new File(EVENTS_LOG_DIR);
	// File[] allFiles = dir.listFiles();
	//
	// if (allFiles != null)
	// for (File f : allFiles) {
	// f.delete();
	// }
	//
	// } catch (InstantiationException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IllegalAccessException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (ClassNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	@Override
	protected void openConnection() throws InstantiationException,
	IllegalAccessException, ClassNotFoundException {
		getClass().getClassLoader().loadClass("com.mysql.jdbc.Driver")
		.newInstance();
		try {
			connection = DriverManager.getConnection(getDbURL());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public ReferenceService getReferenceService() {
		return referenceService;
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public InvocationContext getInvocationContext() {
		return invocationContext;
	}

	public void setInvocationContext(InvocationContext invocationContext) {
		this.invocationContext = invocationContext;
	}

	@Override
	public void init() {
		ProvenanceWriter writer = new MySQLProvenanceWriter();
		writer.setDbURL(getDbURL());
		ProvenanceQuery query = new MySQLProvenanceQuery();
		query.setDbURL(getDbURL());
		WorkflowDataProcessor wfdp = new WorkflowDataProcessor();
		wfdp.setPq(query);
		wfdp.setPw(writer);
		EventProcessor eventProcessor = new EventProcessor();
		eventProcessor.setPw(writer);
		eventProcessor.setPq(query);
		eventProcessor.setWfdp(wfdp);
		ProvenanceAnalysis provenanceAnalysis = null;
		try {
			provenanceAnalysis = new ProvenanceAnalysis(query);
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
		setProvenanceAnalysis(provenanceAnalysis);
		Provenance provenance = new Provenance(eventProcessor, getDbURL());
	}

}
