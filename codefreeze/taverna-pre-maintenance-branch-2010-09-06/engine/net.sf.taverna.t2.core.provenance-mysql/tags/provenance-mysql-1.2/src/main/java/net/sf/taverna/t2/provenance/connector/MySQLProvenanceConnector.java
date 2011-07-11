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
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector.DataflowInvocationTable;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector.ProcessorEnactmentTable;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector.ServiceInvocationTable;
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
			String engineAndCharset = " ENGINE=MyISAM DEFAULT CHARSET=utf8";
			stmt.executeUpdate(DataLinkTable.getCreateTable() + engineAndCharset);			
			stmt.executeUpdate(CollectionTable.getCreateTable() + engineAndCharset);			
			stmt.executeUpdate(ProcessorTable.getCreateTable() + engineAndCharset);			
			stmt.executeUpdate(PortTable.getCreateTable() + engineAndCharset);			
			stmt.executeUpdate(PortBindingTable.getCreateTable() + engineAndCharset);			
			stmt.executeUpdate(WorkflowRunTable.getCreateTable() + engineAndCharset);			
			stmt.executeUpdate(WorkflowTable.getCreateTable() + engineAndCharset);
			stmt.executeUpdate(ProcessorEnactmentTable.getCreateTable() + engineAndCharset);
			stmt.executeUpdate(ServiceInvocationTable.getCreateTable() + engineAndCharset);
			stmt.executeUpdate(ActivityTable.getCreateTable() + engineAndCharset);
			stmt.executeUpdate(DataBindingTable.getCreateTable() + engineAndCharset);
			stmt.executeUpdate(DataflowInvocationTable.getCreateTable() + engineAndCharset);
			
			
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
