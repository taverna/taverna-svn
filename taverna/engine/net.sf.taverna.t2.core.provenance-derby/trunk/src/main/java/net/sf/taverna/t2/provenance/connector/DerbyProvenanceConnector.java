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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import java.util.logging.Level;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.provenance.connector.configview.DerbyConfigView;
import net.sf.taverna.t2.provenance.item.ProvenanceItem;
import net.sf.taverna.t2.provenance.lineageservice.EventProcessor;
import net.sf.taverna.t2.provenance.lineageservice.Provenance;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.WorkflowDataProcessor;
import net.sf.taverna.t2.provenance.lineageservice.derby.DerbyProvenanceQuery;
import net.sf.taverna.t2.provenance.lineageservice.derby.DerbyProvenanceWriter;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceAnalysis;
import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;
import net.sf.taverna.t2.reference.ReferenceService;

import org.apache.log4j.Logger;

public class DerbyProvenanceConnector extends ProvenanceConnector {

    private static Logger logger = Logger.getLogger(DerbyProvenanceConnector.class);
    private static final String createTableData = "CREATE TABLE Data (dataReference VARCHAR(100), wfInstanceID VARCHAR(100), data BLOB)";
    private static final String createTableArc = "CREATE TABLE Arc (" + "sourceVarNameRef varchar(100) NOT NULL ," + "sinkVarNameRef varchar(100) NOT NULL," + "sourcePNameRef varchar(100) NOT NULL," + "sinkPNameRef varchar(100) NOT NULL," + "wfInstanceRef varchar(100) NOT NULL," + " PRIMARY KEY  (sourceVarNameRef,sinkVarNameRef,sourcePNameRef,sinkPNameRef,wfInstanceRef))";
    private static final String createTableCollection = "CREATE TABLE Collection (" + "collID varchar(100) NOT NULL," + "parentCollIDRef varchar(100) NOT NULL ," + "wfInstanceRef varchar(100) NOT NULL," + "PNameRef varchar(100) NOT NULL," + "varNameRef varchar(100) NOT NULL," + "iteration varchar(2000) NOT NULL default ''," + " PRIMARY KEY (collID,wfInstanceRef,PNameRef,varNameRef,parentCollIDRef,iteration))";
    private static final String createTableProcBinding = "CREATE TABLE ProcBinding (" + "pnameRef varchar(100) NOT NULL ," + "execIDRef varchar(100) NOT NULL ," + "actName varchar(100) NOT NULL ," + "iteration char(10) NOT NULL default ''," + "PRIMARY KEY (pnameRef,execIDRef,iteration))";
    private static final String createTableProcessor = "CREATE TABLE Processor (" + "pname varchar(100) NOT NULL," + "wfInstanceRef varchar(100) NOT NULL ," + "type varchar(100) default NULL," + "isTopLevel smallint, " + "PRIMARY KEY  (pname,wfInstanceRef))";
    private static final String createTableVar = "CREATE TABLE Var (" + "varName varchar(100) NOT NULL," + "type varchar(20) default NULL," + "inputOrOutput smallint NOT NULL ," + "pnameRef varchar(100) NOT NULL," + "wfInstanceRef varchar(100) NOT NULL," + "nestingLevel int," + "actualNestingLevel int," + "anlSet smallint default NULL," + "reorder smallint, " + "PRIMARY KEY (varName,inputOrOutput,pnameRef,wfInstanceRef))";
    private static final String createTableVarBinding = "CREATE TABLE VarBinding (" + "varNameRef varchar(100) NOT NULL," + "wfInstanceRef varchar(100) NOT NULL," + "value varchar(100) default NULL," + "collIDRef varchar(100)," + "positionInColl int NOT NULL," + "PNameRef varchar(100) NOT NULL," + "valueType varchar(50) default NULL," + "ref varchar(100) default NULL," + "iteration varchar(2000) NOT NULL," + "PRIMARY KEY (varNameRef,wfInstanceRef,PNameRef,positionInColl,iteration))";
    // + " KEY collectionFK (wfInstanceRef,PNameRef,varNameRef,collIDRef))";
    private static final String createTableWFInstance = "CREATE TABLE WfInstance (" + "instanceID varchar(100) NOT NULL," + "wfnameRef varchar(100) NOT NULL," + "timestamp timestamp NOT NULL default CURRENT_TIMESTAMP," + " PRIMARY KEY (instanceID, wfnameRef))";
    private static final String createTableWorkflow = "CREATE TABLE Workflow (" + "wfname varchar(100) NOT NULL," + "parentWFname varchar(100)," + "externalName varchar(100)," + "PRIMARY KEY  (wfname))";
    private ReferenceService referenceService;
    private InvocationContext invocationContext;

    public DerbyProvenanceConnector() {
    }

    public DerbyProvenanceConnector(Provenance provenance,
            ProvenanceAnalysis provenanceAnalysis, String dbURL,
            boolean isClearDB, String saveEvents) {
        super(provenance, provenanceAnalysis, dbURL, isClearDB, saveEvents);
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
                logger.warn("Could not create database: " + e);
            } catch (IllegalAccessException e) {
                logger.warn("Could not create database: " + e);
            } catch (ClassNotFoundException e) {
                logger.warn("Could not create database: " + e);
            }
            try {
                stmt.executeUpdate(createTableArc);
            } catch (Exception e) {
                // probably means that the database already existed so just log
                // the exception and return
                logger.warn("Could not create table Arc : " + e);
                return;
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
            try {
                stmt.executeUpdate(createTableData);
            } catch (Exception e) {
                logger.warn("Could not create table Data : " + e);
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    logger.warn("There was an error closing the database connection", ex);
                }
            }

        }
    }

    public String getName() {
        return "Derby DB Connector";
    }

    /**
     * Uses a {@link ScheduledThreadPoolExecutor} to process events in a Thread
     * safe manner
     */
    public synchronized void addProvenanceItem(
            final ProvenanceItem provenanceItem) {

        if (provenanceItem.getEventType().equals(
                SharedVocabulary.END_WORKFLOW_EVENT_TYPE)) {
            logger.info("EVENT: " + provenanceItem.getEventType());
        }

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
        getExecutor().execute(runnable);

    }

    public void setReferenceService(ReferenceService referenceService) {
        this.referenceService = referenceService;
    }

    public ReferenceService getReferenceService() {
        return referenceService;
    }

    @Override
    public String toString() {
        return "Derby DB Connector";
    }

    public InvocationContext getInvocationContext() {
        return invocationContext;
    }

    public void setInvocationContext(InvocationContext invocationContext) {
        this.invocationContext = invocationContext;

    }

    @Override
    public void init() {
        String dbURL = getDbURL();
        if (dbURL == null) {
            setDbURL(DerbyConfigView.getDBURL());
        }
        createDatabase();
        ProvenanceWriter writer = new DerbyProvenanceWriter();
        writer.setDbURL(getDbURL());
        ProvenanceQuery query = new DerbyProvenanceQuery();
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
            logger.error("Error creating new provenance analysis for query", e);
        } catch (IllegalAccessException e) {
            logger.error("Error creating new provenance analysis for query", e);
        } catch (ClassNotFoundException e) {
            logger.error("Error creating new provenance analysis for query", e);
        } catch (SQLException e) {
            logger.error("Error creating new provenance analysis for query", e);
        }
        setProvenanceAnalysis(provenanceAnalysis);
        Provenance provenance = new Provenance(eventProcessor, getDbURL());
        setProvenance(provenance);
    }
}
