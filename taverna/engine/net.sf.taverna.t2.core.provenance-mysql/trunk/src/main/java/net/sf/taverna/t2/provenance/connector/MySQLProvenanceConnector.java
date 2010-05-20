/*******************************************************************************
 * Copyright (C) 2007-2010 The University of Manchester   
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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector.ProcessorEnactment;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector.ServiceInvocation;
import net.sf.taverna.t2.provenance.item.IterationProvenanceItem;
import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenanceWriter;
import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;

public class MySQLProvenanceConnector extends ProvenanceConnector {

	private static Logger logger = Logger
	.getLogger(MySQLProvenanceConnector.class);

	private static final String deleteDB = "drop database T2Provenance;";

	private static final String createDB = "CREATE DATABASE IF NOT EXISTS T2Provenance";

	private static final String createTableDatalink = "CREATE TABLE IF NOT EXISTS  `T2Provenance`.`Datalink` ("
		+ "`sourcePortName` varchar(100) NOT NULL COMMENT 'ref. to var name for source of datalink',"
		+ "`destinationPortName` varchar(100) NOT NULL COMMENT 'ref. to var name for sink of datalink',"
		+ "`sourceProcessorName` varchar(100) NOT NULL,"
		+ "`destinationProcessorName` varchar(100) NOT NULL,"
		+ "`workflowId` varchar(100) NOT NULL,"
		+ "PRIMARY KEY  USING BTREE (`sourcePortName`,`destinationPortName`,`sourceProcessorName`,`destinationProcessorName`,`workflowId`)"
		+ ") ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='static -- datalink between two processors';";

	private static final String createTableCollection = "CREATE TABLE  IF NOT EXISTS `T2Provenance`.`Collection` ("
		+ "`collID` varchar(100) NOT NULL COMMENT 'ID of a list (collection). not sure yet what this looks like... ',"
		+ "`parentCollIDRef` varchar(100) NOT NULL default 'TOP' COMMENT 'used for list nesting.\ndefault is dummy list TOP since this attr. is key',"
		+ "`wfInstanceRef` varchar(100) NOT NULL,"
		+ "`PNameRef` varchar(100) NOT NULL,"
		+ "`varNameRef` varchar(100) NOT NULL,"
		+ "`iteration` char(10) NOT NULL default '',"
		+ " PRIMARY KEY  USING BTREE (`collID`,`wfInstanceRef`,`PNameRef`,`varNameRef`,`parentCollIDRef`,`iteration`)"
		+ ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='dynamic -- contains IDs of lists (T2 collections)';";

	private static final String createTableProcessor = "CREATE TABLE IF NOT EXISTS `T2Provenance`.`Processor` ("
		+ "`pname` varchar(100) NOT NULL,"
		+ "`wfInstanceRef` varchar(100) NOT NULL COMMENT 'ref to WfInstance.wfInstanceID',"
		+ "`type` varchar(100) default NULL COMMENT 'processor type',"
		+ "`isTopLevel` tinyint(1) default '0',"
		+ "PRIMARY KEY  (`pname`,`wfInstanceRef`)"
		+ ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='static -- all processors for all workflows, by name';";

	private static final String createTablePort = "CREATE TABLE IF NOT EXISTS `T2Provenance`.`Port` ("
		+ "`portId` varchar(36) NOT NULL,"
		+ "`processorId` varchar(36),"
		+ "`portName` varchar(100) NOT NULL,"
		+ "`isInputPort` tinyint(1) NOT NULL COMMENT '1 = input, 0 = output',"
		+ "`processorName` varchar(100) NOT NULL COMMENT 'reference to the processor',"
		+ "`workflowId` varchar(100) NOT NULL,"
		+ "`depth` int(10) unsigned default '0',"
		+ "`resolvedDepth` int(10) unsigned default NULL,"
		+ "`iterationStrategyOrder` tinyint(4) default NULL,"
		+ "PRIMARY KEY  USING BTREE (`portId`)"
		+ ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='static -- input and output variables (processor port names i';";

	private static final String createTablePortBinding = "CREATE TABLE IF NOT EXISTS `T2Provenance`.`PortBinding` ("
		+ "`varNameRef` varchar(100) NOT NULL COMMENT 'ref to var name',"
		+ "`wfInstanceRef` varchar(100) NOT NULL COMMENT 'ref to execution ID',"
		+ "`value` varchar(100) default NULL COMMENT 'ref to value. Either a string value or a string ref (URI) to a value',"
		+ "`collIDRef` varchar(100) default 'TOP',"
		+ "`positionInColl` int(10) unsigned NOT NULL default '1' COMMENT 'position within collection. default is 1',"
		+ "`PNameRef` varchar(100) NOT NULL,"
		+ "`valueType` varchar(50) default NULL,"
		+ "`ref` varchar(100) default NULL,"
		+ "`iteration` char(10) NOT NULL default '',"
		+ "  `wfNameRef` varchar(100) NOT NULL, "
		+ "PRIMARY KEY  USING BTREE (`varNameRef`,`wfInstanceRef`,`PNameRef`,`iteration`, `wfNameRef`),"
		+ "KEY `collectionFK` (`wfInstanceRef`,`PNameRef`,`varNameRef`,`collIDRef`)"
		+ ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='dynamic -- binding of variables to values ';";

	private static final String createTableWFInstance = "CREATE TABLE IF NOT EXISTS `T2Provenance`.`WfInstance` ("
		+ "`instanceID` varchar(100) NOT NULL COMMENT 'T2-generated ID for one execution',"
		+ "`wfnameRef` varchar(100) NOT NULL COMMENT 'ref to name of the workflow being executed',"
		+ "`timestamp` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP COMMENT 'when execution has occurred',"
		+ "PRIMARY KEY  (`instanceID`, `wfnameRef`)"
		+ ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='dynamic -- execution of a workflow';";

	private static final String createTableWorkflow = "CREATE TABLE IF NOT EXISTS `T2Provenance`.`Workflow` ("
		+ "`wfname` varchar(100) NOT NULL, `parentWFname` varchar(100) default NULL, `externalName` varchar(100) default NULL, `dataflow` longblob,"
		+ "PRIMARY KEY  (`wfname`)"
		+ ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COMMENT='static -- all known workflows by name';";


	// Also see tables in ProvenanceConnector

    public MySQLProvenanceConnector() {
    	super();
		setWriter(new MySQLProvenanceWriter());
		setQuery(new MySQLProvenanceQuery());
    }
	

	@Override
	public String toString() {
		return "MySQL Provenance Connector";
	}

	public List<ProvenanceItem> getProvenanceCollection() {
		logger.info("invoked: LocalConnector::getProvenanceCollection()");
		return null;
	}

	
	
	/**
	 * main entry point into the service
	 */
	public synchronized void addProvenanceItem(final ProvenanceItem provenanceItem) {

		ReferenceService referenceService =
			getInvocationContext().getReferenceService();

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
		}

		// String content = provenanceItem.getAsString();

		if (provenanceItem.getEventType().equals(
				SharedVocabulary.END_WORKFLOW_EVENT_TYPE)) {
			logger.debug("EVENT: " + provenanceItem.getEventType());
		}
		if (provenanceItem.getEventType().equals("EOW")) {
			logger.debug("EOW EVENT arrived ");
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
//					logger.debug("Running xx for " + provenanceItem);
					getProvenance().acceptRawProvenanceEvent(
							provenanceItem.getEventType(), provenanceItem);

				} catch (SQLException e) {
					logger.warn("Could not add provenance", e);
				} catch (IOException e) {
					logger.warn("Could not add provenance", e);
				}

			}
		};
		runnable.run();
// 	getExecutor().submit(runnable);
	}

	public void createDatabase() {
		Statement stmt;
                Connection connection = null;
		try {
            connection=getConnection();
			stmt = connection.createStatement();			
			stmt.executeUpdate(createDB);			
			stmt.executeUpdate(createTableDatalink);			
			stmt.executeUpdate(createTableCollection);			
			stmt.executeUpdate(createTableProcessor);			
			stmt.executeUpdate(createTablePort);			
			stmt.executeUpdate(createTablePortBinding);			
			stmt.executeUpdate(createTableWFInstance);			
			stmt.executeUpdate(createTableWorkflow);
			String engineAndCharset = " ENGINE=MyISAM DEFAULT CHARSET=utf8";
			stmt.executeUpdate(ProcessorEnactment.getCreateTable() + engineAndCharset);
			stmt.executeUpdate(ServiceInvocation.getCreateTable() + engineAndCharset);
			stmt.executeUpdate(Activity.getCreateTable() + engineAndCharset);
			stmt.executeUpdate(DataBinding.getCreateTable() + engineAndCharset);
			
			
		} catch (SQLException e) {
			logger.error("There was a problem creating the Provenance database database: ",e);
		} catch (InstantiationException e) {
			logger.error("Error creating MySQL database tables",e);
		} catch (IllegalAccessException e) {
			logger.error("Error creating MySQL database tables",e);
		} catch (ClassNotFoundException e) {
			logger.error("Error creating MySQL database tables",e);
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    logger.error("Error closing database connection",ex);
                }
            }
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
			logger.error("Could not delete database", e);
		} catch (IllegalAccessException e) {
			logger.error("Could not delete database", e);
		} catch (ClassNotFoundException e) {
			logger.error("Could not delete database", e);
		}

	}

	public String getName() {
		return ProvenanceConnectorType.MYSQL;
	}


}
