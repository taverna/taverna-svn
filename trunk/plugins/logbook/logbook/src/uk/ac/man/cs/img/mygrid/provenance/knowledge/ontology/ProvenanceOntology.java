/*
 * Copyright (C) 2003 The University of Manchester 
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
 * Filename           $RCSfile: ProvenanceOntology.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:11 $
 *               by   $Author: stain $
 * Created on 09-Nov-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.util.DataObject;

/**
 * Provenance ontology interface.
 * 
 * @author dturi
 * @version $Id: ProvenanceOntology.java,v 1.1 2007-12-14 12:49:11 stain Exp $
 */
public interface ProvenanceOntology extends Ontology {

    public static final String PROCESS_PROPERTY_NS = "urn:www.mygrid.org.uk/process_property#";

    public static final String PROVENANCE_NS = "http://www.mygrid.org.uk/provenance";

    /**
     * Relates the two arguments through <code>belongsTo</code>.
     * 
     * @param experimenter
     *            an LSID.
     * @param organization
     *            an LSID.
     * @throws OntologyUpdateException
     */
    public abstract void addBelongsTo(String experimenter, String organization)
            throws OntologyUpdateException;

    /**
     * Relates the two arguments through <code>launchedBy</code>.
     * 
     * @param workflowRun
     *            an LSID.
     * @param experimenter
     *            an LSID.
     * @throws OntologyUpdateException
     */
    public abstract void addLaunchedBy(String workflowRun, String experimenter)
            throws OntologyUpdateException;

    /**
     * Relates the two arguments through <code>executedProcessRun</code>.
     * 
     * @param workflowRun
     *            an LSID.
     * @param processRun
     *            an LSID.
     * @throws OntologyUpdateException
     */
    public abstract void addExecutedProcessRun(String workflowRun,
            String processRun) throws OntologyUpdateException;

    /**
     * Relates the two arguments through <code>runsWorkflow</code>.
     * 
     * @param workflowRun
     *            an LSID.
     * @param workflow
     *            an LSID.
     * @throws OntologyUpdateException
     */
    public abstract void addRunsWorkflow(String workflowRun, String workflow)
            throws OntologyUpdateException;

    /**
     * Asserts that <code>workflowRun</code> is failed.
     * 
     * @param workflowRun
     *            an LSID.
     */
    public abstract void addFailedWorkflowRun(String workflowRun)
            throws OntologyUpdateException;

    /**
     * Relates the two arguments through <code>runsProcess</code>.
     * 
     * @param processRun
     *            an LSID.
     * @param process
     *            an LSID.
     * @throws OntologyUpdateException
     */
    public abstract void addRunsProcess(String processRun, String process)
            throws OntologyUpdateException;

    /**
     * Assignes the current time to <code>workflowRun</code> through the
     * property <code>startTime</code>.
     * 
     * @param workflowRun
     *            an LSID.
     * @throws OntologyUpdateException
     */
    public abstract void addWorkflowStartTime(String workflowRun)
            throws OntologyUpdateException;
    
    /**
     * Assignes the current time to <code>nestedWorkflowRun</code> through the
     * property <code>startTime</code>.
     * 
     * @param nestedWorkflowRun
     *            an LSID.
     * @throws OntologyUpdateException
     */
    public abstract void addNestedWorkflowStartTime(String nestedWorkflowRun)
            throws OntologyUpdateException;

    /**
     * Assigns the current time to <code>workflowRun</code> through the
     * property <code>endTime</code>.
     * 
     * @param workflowRun
     *            an LSID.
     * @throws OntologyUpdateException
     */
    public abstract void addWorkflowEndTime(String workflowRun)
            throws OntologyUpdateException;

    /**
     * Sets the name and syntactic type for <code>inputData</code> of
     * <code>workflowRunId/code>. It also
     * uses <code>syntacticType</code> to decide whether
     * <code>inputData</code> is atomic or a collection.
     * 
     * @param inputData
     *            an individual of type data object.
     * @param workflowRunId
     * @param name
     *            an individual of type data name.
     * @param syntacticType
     *            the syntactic type of <code>inputData</code>.
     */
    public abstract void addWorkflowInputData(String inputData,
            String workflowRunId, String name, String syntacticType)
            throws OntologyUpdateException;

    /**
     * Sets the name and syntactic type for <code>outputData</code> of
     * <code>workflowRunId/code>. It also
     * uses <code>syntacticType</code> to decide whether
     * <code>outputData</code> is atomic or a collection.
     * 
     * @param outputData
     *            an individual of type data object.
     * @param workflowRunId
     * @param name
     *            an individual of type data name.
     * @param syntacticType
     *            the syntactic type of <code>outputData</code>.
     * @throws OntologyUpdateException
     */
    public abstract void addWorkflowOutputData(String outputData,
            String workflowRunId, String name, String syntacticType)
            throws OntologyUpdateException;

    public abstract void addWorkflowInitialLSID(String accurateWorkflowName,
            String workflowName) throws OntologyUpdateException;

    /**
     * Adds <code>description</code> to <code>workflow</code>.
     * 
     * @param workflow
     *            a String
     * @param description
     *            a String
     * @throws OntologyUpdateException
     */
    public abstract void addWorkflowDescription(String workflow,
            String description) throws OntologyUpdateException;

    /**
     * Adds <code>description</code> to <code>workflow</code>.
     * 
     * @param workflow
     *            a String
     * @param title
     *            a String
     * @throws OntologyUpdateException
     */
    public abstract void addWorkflowTitle(String workflow, String title)
            throws OntologyUpdateException;

    /**
     * Adds <code>author</code> to <code>workflow</code>.
     * 
     * @param workflow
     *            a String
     * @param author
     *            a String
     * @throws OntologyUpdateException
     */
    public abstract void addWorkflowAuthor(String workflow, String author)
            throws OntologyUpdateException;

    /**
     * Assigns the current time to <code>processRun</code> through the
     * property <code>endTime</code>.
     * 
     * @param processRun
     *            an LSID.
     * @throws OntologyUpdateException
     */
    public abstract void addProcessEndTime(String processRun)
            throws OntologyUpdateException;

    /**
     * Generates all the metadata corresponding to the failure of
     * <code>processRun</code> on <code>processor</code>.
     * 
     * @param processRun
     * @param processor
     * @param cause
     * @throws OntologyUpdateException
     */
    public abstract void addFailedProcessRun(String processRun,
            String processor, String cause) throws OntologyUpdateException;

    /**
     * States that <code>output</code> is derived from <code>input</code>.
     * 
     * @param input
     *            an individual of type data object.
     * @param output
     *            an individual of type data object.
     * @throws OntologyUpdateException
     */
    public abstract void addDerivedFrom(String input, String output)
            throws OntologyUpdateException;

    /**
     * Sets the name and syntactic type for <code>inputData</code> of
     * <code>processorRunId</code>. It also uses <code>syntacticType</code>
     * to decide whether <code>inputData</code> is atomic or a collection.
     * 
     * @param inputData
     *            an individual of type data object.
     * @param processorRunId
     * @param name
     *            an individual of type data name.
     * @param syntacticType
     *            the syntactic type of <code>inputData</code>.
     * @throws OntologyUpdateException
     */
    public abstract void addInputData(String inputData, String processorRunId,
            String name, String syntacticType) throws OntologyUpdateException;

    /**
     * Sets the name and syntactic type for <code>outputData</code> of
     * <code>processorRunId</code>. It also uses <code>syntacticType</code>
     * to decide whether <code>outputData</code> is atomic or a collection.
     * 
     * @param outputData
     *            an individual of type data object.
     * @param processorRunId
     * @param name
     *            an individual of type data name.
     * @param syntacticType
     *            the syntactic type of <code>outputData</code>.
     */
    public abstract void addOutputData(String outputData,
            String processorRunId, String name, String syntacticType)
            throws OntologyUpdateException;

    /**
     * Relates <code>dataLSID</code> and <code>dataChildLSID</code> via
     * <code>containsData</code>.
     * 
     * @param dataLSID
     *            a String
     * @param dataChildLSID
     *            a String
     */
    public abstract void addContainsData(String dataLSID, String dataChildLSID)
            throws OntologyUpdateException;

    /**
     * Relates <code>dataLSID</code> and <code>dataWrapperLSID</code> via
     * <code>dataWrappedInto</code>.
     * 
     * @param dataLSID
     *            a String
     * @param dataWrapperLSID
     *            a String
     */
    public abstract void addDataWrappedInto(String dataLSID,
            String dataWrapperLSID) throws OntologyUpdateException;

    /**
     * Relates <code>dataLSID</code> and <code>oldDataLSID</code> via
     * <code>dataWrappedInto</code>.
     * 
     * @param dataLSID
     *            a String
     * @param dataWrapperLSID
     *            a String
     */
    public abstract void addChangedData(String dataLSID, String oldDataLSID)
            throws OntologyUpdateException;

    /**
     * Assigns <code>mimeType</code> to <code>name</code>.
     * 
     * @param mimeType
     *            a String
     */
    public abstract void addMimeType(String name, String mimeType)
            throws OntologyUpdateException;

    /**
     * @param processName
     * @param properties
     */
    public abstract void addProcessProperties(String processName,
            Properties properties) throws OntologyUpdateException;

    /**
     * @param processName
     * @param className
     */
    public abstract void addProcessClassName(String processName,
            String className) throws OntologyUpdateException;

    /**
     * States the <code>processRunId</code> is a process run involving
     * iterations.
     * 
     * @param processRunId
     *            a String
     * @param iterations
     */
    public abstract void addProcessCompletedWithIterations(String processRunId,
            Set<String> iterations) throws OntologyUpdateException;

    /**
     * States the <code>processRunId</code> is a process iteration.
     * 
     * @param processRunId
     *            a String
     */
    public abstract void addProcessIteration(String processRunId)
            throws OntologyUpdateException;

    /**
     * States that the process <code>processRunId</code> is the nested
     * workflow <code>nestedWorkflowId</code>.
     * 
     * @param processRunId
     * @param nestedWorkflowId
     */
    public abstract void addProcessIsNestedWorkflow(String processRunId,
            String nestedWorkflowId) throws OntologyUpdateException;

    /**
     * States that <code>nestedWorkflowRun</code> is a subworkflow of
     * <code>parentWorkflowRun</code>. This is called in the named graph of
     * the <code>parentWorkflowRun</code>.
     * 
     * @param parentWorkflowRun
     * @param nestedWorkflowRun
     * 
     * @see #setToNestedWorkflow(String, String)
     */
    public abstract void addNestedWorkflow(String parentWorkflowRun,
            String nestedWorkflowRun) throws OntologyUpdateException;

    public void addFailedNestedWorkflow(String parentWorkflowId,
            String nestedWorkflowId) throws OntologyUpdateException;

    /**
     * States that <code>nestedWorkflowRun</code> is a subworkflow of
     * <code>parentWorkflowRun</code> and removes the statement that
     * <code>nestedWorkflowRun</code> is an ordinary workflow run. This is
     * called in the named graph of the <code>nestedWorkflowRun</code>.
     * 
     * @param nestedWorkflowId
     * @param parentWorkflowId
     * @throws OntologyUpdateException
     * 
     * @see #addNestedWorkflow(String, String)
     */
    public abstract void setToNestedWorkflow(String nestedWorkflowId,
            String parentWorkflowId) throws OntologyUpdateException;

    public abstract void setToFailedNestedWorkflow(String nestedWorkflowRunId,
            String parentWorkflowRunId) throws OntologyUpdateException;

    /**
     * Adds the triple and states that <code>predicate</code> is a subproperty
     * of <code>userPredicate</code>.
     * 
     * @param subject
     *            a String
     * @param predicate
     *            a String
     * @param object
     *            a String
     */
    public abstract void addUserPredicate(String subject, String predicate,
            String object) throws OntologyUpdateException;

    /**
     * @param sourceIndividual
     * @param propertyName
     * @return
     */
    public abstract String getPropertyValue(String sourceIndividual,
            String propertyName);

    /**
     * @param workflowRun
     * @return
     */
    public abstract String getWorkflowName(String workflowRun);

    public abstract String getWorkflowInitialLSID(String workflowLSID);
    
    /**
     * @param workflowRun
     * @return
     */
    public abstract String getExperimenter(String workflowRun);

    /**
     * @param workflowRun
     * @return the organization URI (or LSID) of the person who launched
     *         <code>workflowRun</code>
     */
    public abstract String getExperimenterOrganization(String workflowRun);

    /**
     * @param workflowRun
     * @return
     */
    public abstract String getUnparsedWorkflowStartDate(String workflowRun);

    /**
     * @param processRun
     * @return
     */
    public abstract String getUnparsedProcessEndDate(String processRun);

    /**
     * @param workflowRun
     * @return
     * @throws ParseException
     */
    public abstract Date getWorkflowStartDate(String workflowRun)
            throws ParseException;

    /**
     * @param processRun
     * @return
     */

    public abstract String getProcessName(String processRun);

    /**
     * Finds the oldDataObject value for <code>changedData</code>.
     * 
     * @param changedData
     *            a String representing a resource of which has an oldDataObject
     *            value.
     * @return a String
     */
    public abstract String getOriginalLSID(String changedData);

    /**
     * @param dataLSID
     *            a String representing a resource of RDF type DataObject.
     * @return a {@link DataObject}.
     */
    public abstract DataObject dataView(String dataLSID);

    /**
     * Return true if <code>dataLSID</code> is of RDF type DataCollection.
     * 
     * @param dataLSID
     *            a String
     * @return a boolean
     */
    public abstract boolean isDataCollection(String dataLSID);

    /**
     * Returns a <code>List</code> containing each processRun in the ontology
     * as a <code>String</code>.
     * 
     * @return a List of Strings.
     * @throws InstanceRetrievalException
     */
    public abstract List<String> getProcessList() throws InstanceRetrievalException;

    /**
     * Returns a <code>List</code> containing each processRun in the ontology
     * for the given <code>workflowRunURI</code>.
     * 
     * @return a List of Strings.
     * @throws InstanceRetrievalException
     */

    public abstract List<String> getProcessList(String workflowRunURI)
            throws InstanceRetrievalException;

    /**
     * Returns a <code>List</code> containing each failed processRun in the
     * ontology as a <code>String</code>.
     * 
     * @return a List of Strings.
     * @throws InstanceRetrievalException
     */
    public abstract List<String> getFailedProcessList()
            throws InstanceRetrievalException;

    public abstract String getNestedWorkflow(String processURI)
            throws InstanceRetrievalException;

    public abstract boolean isProcessNestedWorkflow(String processURI);

    public abstract boolean isProcessFailed(String processURI);

    public abstract List<String> getProcessInputs(String processURI)
            throws InstanceRetrievalException;

    public abstract List<String> getProcessOutputs(String processURI)
            throws InstanceRetrievalException;

    public abstract boolean isProcessIteration(String processURI);

    public abstract List<String> getWorkflowOutputs(String workflowRunURI)
            throws InstanceRetrievalException;

    public abstract List<String> getProcessIterations(String processURI)
            throws InstanceRetrievalException;

    public abstract boolean isProcessWithIterations(String processURI);

    public boolean isWorkflowNestedWorkflow(String workflowURI);

    public boolean isWorkflowRunFailedWorkflowRun(String workflowURI);

}