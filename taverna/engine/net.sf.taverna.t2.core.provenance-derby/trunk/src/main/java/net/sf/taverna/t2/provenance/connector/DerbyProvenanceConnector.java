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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;
import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.lineageservice.derby.DerbyProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.derby.DerbyProvenanceWriter;

import org.apache.log4j.Logger;

public class DerbyProvenanceConnector extends ProvenanceConnector {

	private static Logger logger = Logger
			.getLogger(DerbyProvenanceConnector.class);
	private static final String createTableDatalink = "CREATE TABLE Datalink ("
			+ "sourcePortName varchar(100) NOT NULL ,"
			+ "sourcePortId varchar(36) NOT NULL ,"
			+ "destinationPortId varchar(36) NOT NULL ,"
			+ "destinationPortName varchar(100) NOT NULL,"
			+ "sourceProcessorName varchar(100) NOT NULL,"
			+ "destinationProcessorName varchar(100) NOT NULL,"
			+ "workflowId varchar(36) NOT NULL,"
			+ " PRIMARY KEY  (sourcePortId,destinationPortId,workflowId))";
	private static final String createTableCollection = "CREATE TABLE Collection ("
			+ "collID varchar(100) NOT NULL,"
			+ "parentCollIDRef varchar(100) NOT NULL ,"
			+ "workflowRunId varchar(36) NOT NULL,"
			+ "processorNameRef varchar(100) NOT NULL,"
			+ "portName varchar(100) NOT NULL,"
			+ "iteration varchar(2000) NOT NULL default '',"
			+ " PRIMARY KEY (collID,workflowRunId,processorNameRef,portName,parentCollIDRef,iteration))";	
	private static final String createTableProcessor = "CREATE TABLE Processor ("
			+ "processorId varchar(36) NOT NULL,"
			+ "processorName varchar(100) NOT NULL,"
			+ "workflowId varchar(36) NOT NULL ,"
			+ "firstActivityClass varchar(100) default NULL,"
			+ "isTopLevel smallint, "
			+ "PRIMARY KEY (processorId),"
			+ "CONSTRAINT processor_constraint UNIQUE (processorName,workflowId))";
	private static final String createTablePort = "CREATE TABLE Port ("
			+ "portId varchar(36) NOT NULL,"
			+ "processorId varchar(36),"
			+ "portName varchar(100) NOT NULL,"			
			+ "isInputPort smallint NOT NULL ,"
			+ "processorName varchar(100) NOT NULL,"
			+ "workflowId varchar(36) NOT NULL," 
			+ "depth int,"
			+ "resolvedDepth int," 
			+ "iterationStrategyOrder smallint, "
			+ "PRIMARY KEY (portId),"
			+ "CONSTRAINT port_constraint UNIQUE (portName,isInputPort,processorName,workflowId))";
	private static final String createTablePortBinding = "CREATE TABLE PortBinding ("
			+ "portName varchar(100) NOT NULL,"
			+ "workflowRunId varchar(100) NOT NULL,"
			+ "value varchar(100) default NULL,"
			+ "collIDRef varchar(100),"
			+ "positionInColl int NOT NULL,"
			+ "processorNameRef varchar(100) NOT NULL,"
			+ "valueType varchar(50) default NULL,"
			+ "ref varchar(100) default NULL,"
			+ "iteration varchar(2000) NOT NULL,"
			+ "workflowId varchar(36),"
			+ "PRIMARY KEY (portName,workflowRunId,processorNameRef,iteration, workflowId))";
	private static final String createTableWorkflowRun = "CREATE TABLE WorkflowRun ("
			+ "workflowRunId varchar(36) NOT NULL,"
			+ "workflowId varchar(36) NOT NULL,"
			+ "timestamp timestamp NOT NULL default CURRENT_TIMESTAMP,"
			+ " PRIMARY KEY (workflowRunId, workflowId))";
	private static final String createTableWorkflow = "CREATE TABLE Workflow ("
			+ "workflowId varchar(36) NOT NULL," + "parentWorkflowId varchar(100),"
			+ "externalName varchar(100)," + "dataflow blob, "
			+ "PRIMARY KEY  (workflowId))";
	
	// Also see tables in ProvenanceConnector
	
	private final String TABLE_EXISTS_STATE = "X0Y32";

	public DerbyProvenanceConnector() {
		setWriter(new DerbyProvenanceWriter());
		setQuery(new DerbyProvenanceQuery());
	}

	// FIXME is this needed?
	public List<ProvenanceItem> getProvenanceCollection() {
		return null;
	}

	public void createDatabase() {
		Connection connection = null;
		try {

			Statement stmt = null;

			try {
				connection = getConnection();
				stmt = connection.createStatement();
			} catch (SQLException e1) {
				logger.warn(e1);
			} catch (InstantiationException e) {
				logger.warn("Could not create database: ", e);
			} catch (IllegalAccessException e) {
				logger.warn("Could not create database: ", e);
			} catch (ClassNotFoundException e) {
				logger.warn("Could not create database: ", e);
			}
			try {
				stmt.executeUpdate(createTableDatalink);
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Datalink : ", e);
			}
			try {
				stmt.executeUpdate(createTableCollection);
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Collection : ", e);
			}			
			try {
				stmt.executeUpdate(createTableProcessor);
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Processor : ", e);
			}
			try {
				stmt.executeUpdate(createTablePort);
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Port : ", e);
			}
			try {
				stmt.executeUpdate(createTablePortBinding);
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Port Binding : ", e);
			}
			try {
				stmt.executeUpdate(createTableWorkflowRun);
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table WorkflowRun : ", e);
			}
			try {
				stmt.executeUpdate(createTableWorkflow);
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Workflow : ", e);
			}
			
			try {
				stmt.executeUpdate(ProcessorEnactment.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table ProcessorEnactment : ", e);
			}

			try {
				stmt.executeUpdate(ServiceInvocation.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table "
							+ ServiceInvocation.ServiceInvocation, e);
			}

			try {
				stmt.executeUpdate(DataflowInvocation.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table DataflowInvocation : ", e);
			}

			
			try {
				stmt.executeUpdate(Activity.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table "
							+ Activity.Activity, e);
			}
			try {
				stmt.executeUpdate(DataBinding.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table "
							+ DataBinding.DataBinding, e);
			}
			
			
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException ex) {
					logger
							.warn(
									"There was an error closing the database connection",
									ex);
				}
			}

		}
	}

	public String getName() {
		return ProvenanceConnectorType.DERBY;
	}

	@Override
	public String toString() {
		return getName();
	}

}
