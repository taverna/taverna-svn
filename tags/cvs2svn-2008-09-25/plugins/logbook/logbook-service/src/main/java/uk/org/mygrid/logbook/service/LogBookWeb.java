/*
 * Copyright (C) 2007 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: LogBookWeb.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008-04-02 16:24:14 $
 *               by   $Author: stain $
 * Created on 2 Feb 2007
 *****************************************************************/
package uk.org.mygrid.logbook.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.store.NoSuchLSIDException;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.JenaMetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.org.mygrid.provenance.dataservice.DataService;
import uk.org.mygrid.provenance.dataservice.DataServiceException;
import uk.org.mygrid.provenance.dataservice.DataServiceFactory;

/**
 * A web service wrapper for the MetadataService and DataService of LogBook. It
 * allows to retrieve metadata and data collected by the LogBook Taverna plugin
 * during the execution of workflows. It also allows to store metadata (but not data).
 * 
 * To configure the necessary databases edit the file <code>logbook-service.properties</code> 
 * in the <code>/WEB-INF/classes</code> folder.
 * 
 * @author dturi
 * @version $Id: LogBookWeb.java,v 1.1 2008-04-02 16:24:14 stain Exp $
 */
public class LogBookWeb {

    /**
     * Value = {@value}
     */
    public static final String LOGBOOK_PROPERTIES = "/logbook-service.properties";

    private static Logger logger = Logger.getLogger(LogBookWeb.class);

    private MetadataService metadataService;

    private DataService dataService;

    /**
     * Initialises metadata and data services using {@link #LOGBOOK_PROPERTIES}.
     */
    public LogBookWeb() {
        try {
            Properties configuration = getConfiguration();
            metadataService = new JenaMetadataService(configuration);
            dataService = DataServiceFactory.getInstance(configuration);
        } catch (MetadataServiceCreationException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        } catch (DataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }
    
    private Properties getConfiguration() {
        Properties provenanceProperties = new Properties();
        try {
            InputStream inStream = LogBookWeb.class.getResourceAsStream(LOGBOOK_PROPERTIES);
            provenanceProperties.load(inStream);
            inStream.close();
        } catch (FileNotFoundException e) {
            logger.error("Configuration file " + LOGBOOK_PROPERTIES
                    + " not found", e);
            throw new LogBookWebError(e);
        } catch (IOException e) {
            logger.error("Error reading from file " + LOGBOOK_PROPERTIES, e);
            throw new LogBookWebError(e);
        }
        return provenanceProperties;
    }

    /**
     * Returns the Scufl workflow (as an XML string) that was executed in
     * <code>workflowRun</code>
     * 
     * @param workflowRun
     *            the LSID of a workflow run.
     * @return a String
     * @see #getWorkflow(String)
     */
    public String getWorkflowExecutedByRun(String workflowRun) {
        try {
            String workflowLSID = metadataService.getFirstObjectPropertyValue(
                    workflowRun,
                    ProvenanceOntologyConstants.ObjectProperties.RUNSWORKFLOW);
            return dataService.fetchUnparsedWorkflow(workflowLSID);
        } catch (DataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns the Scufl workflow (as an XML string) with (dynamically assigned)
     * LSID <code>workflowLSID</code>. Note that <code>workflowLSID</code>
     * is not the declared LSID for the workflow, but rather the one dynamically
     * assigned by the LogBook during its execution in Taverna.
     * 
     * @param workflowLSID
     * @return a String
     * @see #getWorkflowExecutedByRun(String)
     */
    public String getWorkflow(String workflowLSID) {
        try {
            return dataService.fetchUnparsedWorkflow(workflowLSID);
        } catch (DataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns the Taverna data object corresponding to
     * <code>dataItemLSID</code> as an array of bytes, even if it is a string.
     * 
     * @param dataItemLSID
     * @return byte[]
     */
    public byte[] getDataItem(String dataItemLSID) {
        try {
            DataThing dataThing = getDataThing(dataItemLSID);
            Object o = dataThing.getDataObject();
            if (o instanceof byte[])
                return (byte[]) o;
            else if (o instanceof String) {
                String dataString = (String) o;
                return dataString.getBytes();
            } else
                return null;
        } catch (NoSuchLSIDException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        } catch (DataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns the Taverna data object corresponding to
     * <code>dataItemLSID</code> as an array of bytes if that is the type of
     * the data object, <code>null</code> otherwise.
     * 
     * @param dataItemLSID
     * @return byte[]
     */
    public byte[] getBytesDataItem(String dataItemLSID) {
        try {
            DataThing dataThing = getDataThing(dataItemLSID);
            Object o = dataThing.getDataObject();
            if (o instanceof byte[])
                return (byte[]) o;
            else
                return null;
        } catch (NoSuchLSIDException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        } catch (DataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns the Taverna data object corresponding to
     * <code>dataItemLSID</code> as a String if that is the type of the data
     * object, <code>null</code> otherwise.
     * 
     * @param dataItemLSID
     * @return String
     */
    public String getStringDataItem(String dataItemLSID) {
        try {
            DataThing dataThing = getDataThing(dataItemLSID);
            Object o = dataThing.getDataObject();
            if (o instanceof String)
                return (String) o;
            else
                return null;
        } catch (NoSuchLSIDException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        } catch (DataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    private DataThing getDataThing(String lsid) throws NoSuchLSIDException,
            DataServiceException {
        DataThing dataThing = dataService.fetchDataThing(lsid);
        return dataThing;
    }

    /**
     * Returns the names of the Taverna (input and output) ports where
     * <code>dataURN</code> has passed through.
     * 
     * @param dataURN
     * @return an array of Strings
     */
    public String[] getDataPortNames(String dataURN) {
        try {
            List<String> names = metadataService.getDataPortNames(dataURN);
            return names.toArray(new String[names.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns the names of the Taverna input ports where <code>dataURN</code>
     * has passed through.
     * 
     * @param dataURN
     * @return an array of Strings
     */
    public String[] getInputDataPortNames(String dataURN) {
        try {
            List<String> names = metadataService.getInputDataPortNames(dataURN);
            return names.toArray(new String[names.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns the names of the Taverna output ports where <code>dataURN</code>
     * has passed through.
     * 
     * @param dataURN
     * @return an array of Strings
     */
    public String[] getOutputDataPortNames(String dataURN) {
        try {
            List<String> names = metadataService
                    .getOutputDataPortNames(dataURN);
            return names.toArray(new String[names.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns the LSIDs in <code>dataCollectionLSID</code>.
     * 
     * @param dataCollectionLSID
     * @return an array of Strings
     */
    public String[] getDataCollectionLSIDs(String dataCollectionLSID) {
        try {
            List<String> dataCollectionLSIDs = metadataService
                    .getDataCollectionLSIDs(dataCollectionLSID);
            return dataCollectionLSIDs.toArray(new String[dataCollectionLSIDs
                    .size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns the declared syntactic types for the ports <code>dataLSID</code>
     * has passed through.
     * 
     * @param dataLSID
     * @return an array of Strings
     */
    public String[] getDataSyntacticTypes(String dataLSID) {
        try {
            Set<String> syntacticTypes = metadataService
                    .getDataSyntacticTypes(dataLSID);
            return syntacticTypes.toArray(new String[syntacticTypes.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Stores <code>rdfGraph</code> under <code>graphName</code>. Note that
     * if a graph with the same name already exists it will be replaced with the
     * new one.
     * 
     * @param rdfGraph
     *            a String consisting of (an XML serialization of) an RDF graph.
     * @param graphName
     *            a String.
     */
    public void storeRDFGraph(String rdfGraph, String graphName) {
        try {
            metadataService.storeRDFGraph(rdfGraph, graphName);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Stores under <code>graphName</code> the RDF graph corresponding to the
     * OWL ontology at <code>url</code>. Note that if a graph with the same
     * name already exists it will be replaced with the new one.
     * 
     * @param url
     *            a URL pointing to an OWL ontology.
     * @param graphName
     *            a String.
     */
    public void storeModelFromURL(String url, String graphName) {
        try {
            metadataService.storeModel(new URL(url), graphName);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        } catch (MalformedURLException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        } catch (IOException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Stores the quad of arguments
     * 
     * @param graphName
     * @param subject
     * @param predicate
     * @param object
     */
    public void addQuad(String graphName, String subject, String predicate,
            String object) {
        try {
            metadataService.addQuad(graphName, subject, predicate, object);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Retrieves the graph corresponding to <code>graphName</code> and returns
     * it as a String containing its RDF/XML representation.
     * 
     * @param workflowRunId
     *            a String.
     * @return a String containing (an XML serialization of) an RDF
     *         representation of the graph.
     */
    public String retrieveGraph(String workflowRunId) {
        try {
            return metadataService.retrieveGraph(workflowRunId);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Removes the graph <code>graphName</code>.
     * 
     * @param graphName
     *            the name of the graph to be removed.
     */
    public void removeGraph(String graphName) {
        try {
            metadataService.removeGraph(graphName);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Removes the graph corresponding to <code>workflowRun</code> from the
     * repository and then does the same by recursion for all its possible
     * nested workflow runs and process runs.
     * 
     * @param workflowRun
     */
    public void removeWorkflowRun(String workflowRun) {
        try {
            metadataService.removeWorkflowRun(workflowRun);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Retrieves the graph corresponding to <code>workflowRunId</code>
     * together with all the subgraphs of processes and nested runs and returns
     * it as a String containing its RDF/XML representation.
     * 
     * @param workflowRunId
     *            a String.
     * @return a String containing an RDF/XML representation of the graph.
     */
    public String getWorkflowRun(String workflowRunId) {
        try {
            return metadataService.getWorkflowRun(workflowRunId);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets the LSIDs of all the workflow runs in store.
     * 
     * @return an array of Strings
     */
    public String[] getAllWorkflowRuns() {
        try {
            List<String> runs = metadataService.getAllWorkflowRuns();
            return runs.toArray(new String[runs.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets all the (ids of) process runs executed by
     * <code>workflowRunLSID</code>
     * 
     * @param workflowRunId
     * @return an array of Strings
     * @see #getNonNestedProcessRuns(String)
     */
    public String[] getProcessRuns(String workflowRunId) {
        try {
            List<String> processesRuns = metadataService
                    .getProcessesRuns(workflowRunId);
            return processesRuns.toArray(new String[processesRuns.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets the names of the nested workflow runs for <code>workflowRun</code>.
     * 
     * @param workflowRunId
     * @return an array of Strings
     */
    public String[] getNestedRuns(String workflowRunId) {
        try {
            List<String> nestedRuns = metadataService
                    .getNestedRuns(workflowRunId);
            return nestedRuns.toArray(new String[nestedRuns.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets the LSIDs of all the nested workflow runs in store.
     * 
     * @return a List of Strings
     */
    public String[] getAllNestedRuns() {
        try {
            List<String> runs = metadataService.getAllNestedRuns();
            return runs.toArray(new String[runs.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets all the (ids of) process runs executed by
     * <code>workflowRunLSID</code> that are not nested workflows.
     * 
     * @param workflowRunId
     * @return an array of Strings
     * @see #getProcessRuns(String)
     */
    public String[] getNonNestedProcessRuns(String workflowRunId) {
        try {
            List<String> processesRuns = metadataService
                    .getNonNestedProcessRuns(workflowRunId);
            return processesRuns.toArray(new String[processesRuns.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets all the (ids of) workflow inputs for <code>workflowRunLSID</code>
     * 
     * @param lsid
     * @return an array of Strings
     */
    public String[] getWorkflowInputs(String lsid) {
        try {
            List<String> workflowInputs = metadataService
                    .getWorkflowInputs(lsid);
            return workflowInputs.toArray(new String[workflowInputs.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets all the (ids of) workflow outputs for <code>workflowRunLSID</code>
     * 
     * @param workflowRunLSID
     * @return an array of Strings
     */
    public String[] getWorkflowOutputs(String workflowRunLSID) {
        try {
            List<String> workflowOutputs = metadataService
                    .getWorkflowOutputs(workflowRunLSID);
            return workflowOutputs.toArray(new String[workflowOutputs.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets all the (ids of) outputs for <code>processID</code>
     * 
     * @param processID
     * @return an array of Strings
     */
    public String[] getProcessOutputs(String processID) {
        try {
            List<String> processOutputs = metadataService
                    .getProcessOutputs(processID);
            return processOutputs.toArray(new String[processOutputs.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets all the (ids of) inputs for <code>processID</code>
     * 
     * @param processID
     * @return an array of Strings
     */
    public String[] getProcessInputs(String processID) {
        try {
            List<String> processInputs = metadataService
                    .getProcessInputs(processID);
            return processInputs.toArray(new String[processInputs.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns the LSIDs of all the runs executed by <code>experimenter</code>.
     * Note that the experimenter for a run is set by default to
     * <code>http://www.someplace/someuser</code>.
     * 
     * @param experimenter
     * @return an array of Strings
     */
    public String[] getUserWorkFlows(String experimenter) {
        try {
            Vector<String> instances = metadataService
                    .getUserWorkFlows(experimenter);
            return instances.toArray(new String[instances.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns <code>true</code> if <code>processURI</code> is an iteration
     * run.
     * 
     * @param processURI
     * @return a boolean
     */
    public boolean isProcessIteration(String processURI) {
        try {
            return metadataService.isProcessIteration(processURI);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns <code>true</code> if <code>processURI</code> contains
     * iterations.
     * 
     * @param processURI
     * @return a boolean
     */
    public boolean isProcessWithIterations(String processURI) {
        try {
            return metadataService.isProcessWithIterations(processURI);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns the start date of <code>workflowRun</code>.
     * 
     * @param workflowRun
     * @return a String
     */
    public String getUnparsedWorkflowStartDate(String workflowRun) {
        try {
            return metadataService.getUnparsedWorkflowStartDate(workflowRun);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns the end date of <code>processRun</code>.
     * 
     * @param processRun
     * @return a String
     */
    public String getUnparsedProcessEndDate(String processRun) {
        try {
            return metadataService.getUnparsedProcessEndDate(processRun);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets the array of (String representations of) the targets of
     * <code>objectProperty</code> for <code>sourceIndividual</code>.
     * 
     * @param sourceIndividual
     *            String
     * @param objectProperty
     *            String
     * @return an array of Strings
     */
    public String[] getObjectPropertyValues(String sourceIndividual,
            String objectProperty) {
        try {
            List<String> values = metadataService.getObjectPropertyValues(
                    sourceIndividual, objectProperty);
            return values.toArray(new String[values.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets the String representation of the first target of
     * <code>objectProperty</code> for <code>sourceIndividual</code>.
     * 
     * @param sourceIndividual
     *            String
     * @param objectProperty
     *            String
     * @return String
     */
    public String getFirstObjectPropertyValue(String sourceIndividual,
            String objectProperty) {
        try {
            return metadataService.getFirstObjectPropertyValue(
                    sourceIndividual, objectProperty);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }

    }

    /**
     * Gets the array of (String representations of) the targets of
     * <code>datatypeProperty</code> for <code>sourceIndividual</code>.
     * 
     * @param sourceIndividual
     *            String
     * @param datatypeProperty
     *            String
     * @return an array of Strings
     */
    public String[] getDatatypePropertyValues(String sourceIndividual,
            String datatypeProperty) {
        try {
            List<String> values = metadataService.getDatatypePropertyValues(
                    sourceIndividual, datatypeProperty);
            return values.toArray(new String[values.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Gets the first String value of <code>datatypeProperty</code> for
     * <code>sourceIndividual</code>.
     * 
     * @param sourceIndividual
     *            String
     * @param datatypeProperty
     *            String
     * @return String
     */
    public String getFirstDatatypePropertyValue(String sourceIndividual,
            String datatypeProperty) {
        try {
            return metadataService.getFirstDatatypePropertyValue(
                    sourceIndividual, datatypeProperty);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns all the individuals of <code>type</code>.
     * 
     * @param type
     * @return a List of Strings.
     */
    public String[] getIndividualsOfType(String type) {
        try {
            List<String> instances = metadataService.getIndividualsOfType(type);
            return instances.toArray(new String[instances.size()]);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

    /**
     * Returns <code>true</code> if <code>individual</code> is of type
     * <code>type</code>.
     * 
     * @param individual
     * @param type
     * @return a boolean
     */
    public boolean isIndividualOfType(String individual, String type) {
        try {
            return metadataService.isIndividualOfType(individual, type);
        } catch (MetadataServiceException e) {
            logger.error(e);
            throw new LogBookWebError(e);
        }
    }

}
