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
package net.sf.taverna.t2.provenance.connector.derby;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import net.sf.taverna.t2.provenance.api.ProvenanceConnectorType;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.lineageservice.derby.DerbyProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.derby.DerbyProvenanceWriter;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;

import org.apache.log4j.Logger;

import uk.org.taverna.configuration.database.DatabaseManager;

public class DerbyProvenanceConnector extends ProvenanceConnector {

	private static Logger logger = Logger
			.getLogger(DerbyProvenanceConnector.class);

	private final String TABLE_EXISTS_STATE = "X0Y32";

	public DerbyProvenanceConnector(DatabaseManager databaseManager, XMLSerializer xmlSerializer) {
		super(databaseManager, xmlSerializer);
		setWriter(new DerbyProvenanceWriter(databaseManager));
		setQuery(new DerbyProvenanceQuery(databaseManager));
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
			}
			try {
				stmt.executeUpdate(DataLinkTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Datalink : ", e);
			}
			try {
				stmt.executeUpdate(CollectionTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Collection : ", e);
			}
			try {
				stmt.executeUpdate(ProcessorTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Processor : ", e);
			}
			try {
				stmt.executeUpdate(PortTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Port : ", e);
			}
			try {
				stmt.executeUpdate(PortBindingTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Port Binding : ", e);
			}
			try {
				stmt.executeUpdate(WorkflowRunTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table WorkflowRun : ", e);
			}
			try {
				stmt.executeUpdate(WorkflowTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table Workflow : ", e);
			}

			try {
				stmt.executeUpdate(ProcessorEnactmentTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table ProcessorEnactment : ", e);
			}

			try {
				stmt.executeUpdate(ServiceInvocationTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table "
							+ ServiceInvocationTable.ServiceInvocation, e);
			}

			try {
				stmt.executeUpdate(DataflowInvocationTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table DataflowInvocation : ", e);
			}


			try {
				stmt.executeUpdate(ActivityTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table "
							+ ActivityTable.Activity, e);
			}
			try {
				stmt.executeUpdate(DataBindingTable.getCreateTable());
			} catch (SQLException e) {
				if (!e.getSQLState().equals(TABLE_EXISTS_STATE))
					logger.warn("Could not create table "
							+ DataBindingTable.DataBinding, e);
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
