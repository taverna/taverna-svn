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
package net.sf.taverna.t2.provenance.connector.mysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.mysql.MySQLProvenanceWriter;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;

import org.apache.log4j.Logger;

import uk.org.taverna.configuration.database.DatabaseManager;

public class MySQLProvenanceConnector extends ProvenanceConnector {

	private static Logger logger = Logger
	.getLogger(MySQLProvenanceConnector.class);

	private static final String deleteDB = "drop database T2Provenance;";

	private static final String createDB = "CREATE DATABASE IF NOT EXISTS T2Provenance";

    public MySQLProvenanceConnector(DatabaseManager databaseManager, XMLSerializer xmlSerializer) {
    	super(databaseManager, xmlSerializer);
		setWriter(new MySQLProvenanceWriter(databaseManager));
		setQuery(new MySQLProvenanceQuery(databaseManager));
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

	@Override
	public synchronized void addProvenanceItem(final ProvenanceItem provenanceItem) {

		Runnable runnable = new Runnable() {

			public void run() {
				try {
//					logger.debug("Running xx for " + provenanceItem);
					if (getProvenance() == null) {
						System.err.println("getProvenance returned null");
					}
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

	/**
	 * MySQL overriden tables, to avoid
	 * "Specified key was too long; max key length is 1000 bytes" on massive
	 * primary keys (T2-1605)
	 */
	// TODO: Fix the primary keys to not be composite, and use foreign keys for
	// processors/ports/iterations
	public static enum ActivityTable {
		Activity, activityId, activityDefinition, workflowId;

		public static String getCreateTable() {
			return "CREATE TABLE " + Activity + "(\n"
			+ activityId + " varchar(36) NOT NULL,\n"
			+ activityDefinition + " blob NOT NULL,\n"
			+ workflowId + " varchar(100) NOT NULL, \n"
			+ "PRIMARY KEY (" + activityId + ")\n" + ")";
		}
	}

	public static enum CollectionTable {
		Collection, collID, parentCollIDRef, workflowRunId, processorNameRef, portName, iteration;
		public static String getCreateTable() {
			return "CREATE TABLE " + Collection + " (\n"
			+ collID + " varchar(100) NOT NULL,\n"
			+ parentCollIDRef + " varchar(100) NOT NULL ,\n"
			+ workflowRunId + " varchar(36) NOT NULL,\n"
			+ processorNameRef + " varchar(70) NOT NULL,\n"
			+ portName + " varchar(70) NOT NULL,\n"
			+ iteration + " varchar(40) NOT NULL default '',\n"
			+ " PRIMARY KEY (\n" + collID + "," + workflowRunId + ","
					+ processorNameRef + "," + portName
					+ "," + iteration + "))";
		}
	}

	public static enum DataBindingTable {
		DataBinding, dataBindingId, portId, t2Reference, workflowRunId;

		public static String getCreateTable() {
			return "CREATE TABLE " + DataBinding + "(\n"
			+ dataBindingId + " varchar(36) NOT NULL,\n"
			+ portId + " varchar(36) NOT NULL,\n"
			+ t2Reference + " varchar(100) NOT NULL,\n"
			+ workflowRunId + " varchar(100) NOT NULL, \n"
			+ "PRIMARY KEY (" + dataBindingId + "," + portId + ")\n" + ")";
		}
	}

	public static enum DataflowInvocationTable {
		DataflowInvocation, dataflowInvocationId,
		workflowId,
		invocationStarted, invocationEnded,
		inputsDataBinding, outputsDataBinding,
		parentProcessorEnactmentId, workflowRunId, completed;

		public static String getCreateTable() {
			return "CREATE TABLE " + DataflowInvocation + "(\n"
			+ dataflowInvocationId + " varchar(36) NOT NULL,\n"
			+ workflowId + " varchar(100) NOT NULL, \n"
			+ invocationStarted + " timestamp, \n"
			+ invocationEnded + " timestamp, \n"
			+ inputsDataBinding + " varchar(36),\n"
			+ outputsDataBinding + " varchar(36),\n"
			+ parentProcessorEnactmentId + " varchar(36), \n"
			+ workflowRunId + " varchar(100) NOT NULL, \n"
			+ completed + " smallint NOT NULL,\n"
			+ "PRIMARY KEY (" + dataflowInvocationId+ ")\n" + ")";
		}
	}

	public static enum DataLinkTable {
		Datalink, sourcePortName, sourcePortId, destinationPortId,
		destinationPortName, sourceProcessorName, destinationProcessorName, workflowId;
		public static String getCreateTable() {
			return "CREATE TABLE " + Datalink + " (\n"
					+ sourcePortName + " varchar(70) NOT NULL ,\n"
					+ sourcePortId + " varchar(36) NOT NULL ,\n"
					+ destinationPortId + " varchar(36) NOT NULL ,\n"
					+ destinationPortName + " varchar(70) NOT NULL,\n"
					+ sourceProcessorName + " varchar(70) NOT NULL,\n"
					+ destinationProcessorName + " varchar(70) NOT NULL,\n"
					+ workflowId + " varchar(36) NOT NULL,"
					+ " PRIMARY KEY  ("
					+ sourcePortId + "," + destinationPortId + "," + workflowId
					+ "))";
		}
	}

	public static enum PortBindingTable {
		PortBinding, portName, workflowRunId, value, collIDRef, positionInColl, processorNameRef, valueType, ref, iteration, workflowId;
		public static String getCreateTable() {
			return "CREATE TABLE " + PortBinding + " (\n"
				+ portName
					+ " varchar(70) NOT NULL,\n"
					+ workflowRunId + " varchar(100) NOT NULL,\n"
					+ value + " varchar(100) default NULL,\n"
					+ collIDRef + " varchar(100),\n"
					+ positionInColl + " int NOT NULL,\n"
					+ processorNameRef + " varchar(70) NOT NULL,\n"
					+ valueType + " varchar(50) default NULL,\n"
					+ ref + " varchar(100) default NULL,\n"
					+ iteration + " varchar(40) NOT NULL,\n"
					+ workflowId + " varchar(36),\n"
					+ "PRIMARY KEY (\n" + portName + "," + workflowRunId + "," + processorNameRef + "," + iteration
					+ ", " + workflowId + "))";
		}
	}


	public static enum PortTable {
		Port, portId, processorId, portName, isInputPort, processorName,
		workflowId, depth, resolvedDepth, iterationStrategyOrder;
		public static String getCreateTable() {
			return  "CREATE TABLE " + Port + " (\n"
			+ portId + " varchar(36) NOT NULL,\n"
			+ processorId + " varchar(36),\n"
			+ portName + " varchar(70) NOT NULL,\n"
			+ isInputPort + " smallint NOT NULL ,\n"
			+ processorName + " varchar(70) NOT NULL,\n"
			+ workflowId + " varchar(36) NOT NULL,\n"
			+ depth + " int,\n"
			+ resolvedDepth + " int,\n"
			+ iterationStrategyOrder + " smallint, \n"
			+ "PRIMARY KEY (" + "portId" + "),\n"
			+ "CONSTRAINT port_constraint UNIQUE (\n"
			+ portName + "," + isInputPort + "," + processorName + "," + workflowId + "\n"
			+ "))";
		}
	}

	public static enum ProcessorEnactmentTable {
		ProcessorEnactment, processEnactmentId, workflowRunId, processorId,
		processIdentifier, iteration, parentProcessorEnactmentId,
		enactmentStarted, enactmentEnded, initialInputsDataBindingId,
		finalOutputsDataBindingId;

		public static String getCreateTable() {
			return "CREATE TABLE " + ProcessorEnactment + " (\n"
			+ processEnactmentId + " varchar(36) NOT NULL, \n"
			+ workflowRunId + " varchar(100) NOT NULL, \n"
			+ processorId + " varchar(36) NOT NULL, \n"
			+ processIdentifier + " varchar(2047) NOT NULL, \n"
			+ iteration + " varchar(100) NOT NULL, \n"
			+ parentProcessorEnactmentId + " varchar(36), \n"
			+ enactmentStarted + " timestamp, \n"
			+ enactmentEnded + " timestamp, \n"
			+ initialInputsDataBindingId + " varchar(36), \n"
			+ finalOutputsDataBindingId + " varchar(36), \n"
			+ " PRIMARY KEY (" + processEnactmentId + ")" + ")";
		}
	}

	public static enum ProcessorTable {
		Processor,processorId, processorName,workflowId,firstActivityClass,isTopLevel ;
		public static String getCreateTable() {
			return  "CREATE TABLE "+ Processor +" (\n"
			+ processorId + " varchar(36) NOT NULL,\n"
			+ processorName + " varchar(70) NOT NULL,\n"
			+ workflowId + " varchar(36) NOT NULL ,\n\n"
			+ firstActivityClass + " varchar(100) default NULL,\n"
			+ isTopLevel + " smallint, \n"
			+ "PRIMARY KEY (" + processorId+ "),\n"
			+ "CONSTRAINT processor_constraint UNIQUE (\n"
			+	processorName + "," + workflowId + "))";
		}
	}

	public static enum ServiceInvocationTable {
		ServiceInvocation, processorEnactmentId, workflowRunId,
		invocationNumber, invocationStarted, invocationEnded,
		inputsDataBinding, outputsDataBinding, failureT2Reference,
		activityId, initiatingDispatchLayer, finalDispatchLayer;

		public static String getCreateTable() {
			return "CREATE TABLE " + ServiceInvocation + "(\n"
			+ processorEnactmentId + " varchar(36) NOT NULL,\n"
			+ workflowRunId + " varchar(100) NOT NULL, \n"
			+ invocationNumber + " bigint NOT NULL,\n"
			+ invocationStarted + " timestamp, \n"
			+ invocationEnded + " timestamp, \n"
			+ inputsDataBinding + " varchar(36),\n"
			+ outputsDataBinding + " varchar(36),\n"
			+ failureT2Reference + " varchar(100) default NULL,\n"
			+ activityId + " varchar(36),\n"
			+ initiatingDispatchLayer + " varchar(250) NOT NULL,\n"
			+ finalDispatchLayer + " varchar(250) NOT NULL,\n"
			+ "PRIMARY KEY (" + processorEnactmentId + ", "
			+ invocationNumber + "))";
		}
	}

	public static enum WorkflowRunTable {
		WorkflowRun, workflowRunId, workflowId, timestamp;
		public static String getCreateTable() {
			return  "CREATE TABLE " + WorkflowRun + " (\n"
			+ workflowRunId + " varchar(36) NOT NULL,\n"
			+ workflowId + " varchar(36) NOT NULL,\n"
			+ timestamp + " timestamp NOT NULL default CURRENT_TIMESTAMP,\n"
			+ " PRIMARY KEY (" + workflowRunId + ", " + workflowId + "))";
		}
	}

	public static enum WorkflowTable {
		WorkflowTable, workflowId, parentWorkflowId, externalName, dataflow;
		public static String getCreateTable() {
			return "CREATE TABLE " + "Workflow (\n" +
					workflowId	+ " varchar(36) NOT NULL,\n"
					+ parentWorkflowId + " varchar(100),\n"
					+ externalName + " varchar(100),\n"
					+ dataflow + " longblob, \n"
					+ "PRIMARY KEY  (" + workflowId	+ "))";
		}
	}


	@Override
	public void createDatabase() {
		Statement stmt;
                Connection connection = null;
		try {
            connection=getConnection();
			stmt = connection.createStatement();
			stmt.executeUpdate(createDB);
			String engineAndCharset = " ENGINE=MyISAM DEFAULT CHARSET=utf8";

			stmt.executeUpdate(DataLinkTable.getCreateTable()
					+ engineAndCharset);
			stmt.executeUpdate(CollectionTable.getCreateTable()
					+ engineAndCharset);
			stmt.executeUpdate(ProcessorTable.getCreateTable()
					+ engineAndCharset);
			stmt.executeUpdate(PortTable.getCreateTable() + engineAndCharset);
			stmt.executeUpdate(PortBindingTable.getCreateTable()
					+ engineAndCharset);
			stmt.executeUpdate(WorkflowRunTable.getCreateTable()
					+ engineAndCharset);
			stmt.executeUpdate(WorkflowTable.getCreateTable()
					+ engineAndCharset);
			stmt.executeUpdate(ProcessorEnactmentTable.getCreateTable()
					+ engineAndCharset);
			stmt.executeUpdate(ServiceInvocationTable.getCreateTable()
					+ engineAndCharset);
			stmt.executeUpdate(ActivityTable.getCreateTable()
					+ engineAndCharset);
			stmt.executeUpdate(DataBindingTable.getCreateTable()
					+ engineAndCharset);
			stmt.executeUpdate(DataflowInvocationTable.getCreateTable()
					+ engineAndCharset);


		} catch (SQLException e) {
			logger.error("There was a problem creating the Provenance database database: ",e);
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
		}

	}

	@Override
	public String getName() {
		return ProvenanceConnectorType.MYSQL;
	}

}
