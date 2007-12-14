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
 * Filename           $RCSfile: MetadataService.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:08 $
 *               by   $Author: stain $
 * Created on 29-Sep-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.store;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.Ontology;
import uk.org.mygrid.logbook.util.DataProvenance;
import uk.org.mygrid.logbook.util.ProcessRunBean;
import uk.org.mygrid.logbook.util.WorkflowRunBean;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.vocabulary.DC;

/**
 * Metadata Service for named RDF graphs.
 * 
 * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory
 * @author dturi
 * @version $Id: MetadataService.java,v 1.1 2007-12-14 12:49:08 stain Exp $
 */
public interface MetadataService {

	public static final String GRAPH_CREATOR = "mygrid.kave.graphcreator";

	public static final String ANONYMOUS_USER = "urn:mygrid.kave.graphsmetadata#anonymuser";

	public static final String GRAPHS_METADATA = "urn:mygrid.kave.graphsmetadata";

	public static final Property CREATOR_PROPERTY = DC.creator;

	public static final Property DATE_PROPERTY = DC.date;

	public void restart() throws MetadataServiceException;

	public void clear() throws MetadataServiceException;

	/**
	 * Initialises the service using the configuration.
	 * 
	 * @throws MetadataServiceException
	 */
	public void initialise() throws MetadataServiceCreationException;

	public Properties getConfiguration() throws MetadataServiceException;

	/**
	 * 
	 * @param configuration
	 *            {@link Properties}.
	 * @throws MetadataServiceException
	 */
	public void setConfiguration(Properties configuration)
			throws MetadataServiceException;

	/**
	 * Stores under <code>graphName</code> the instance data in
	 * <code>ontology</code>. Note that if a graph with the same name already
	 * exists it will be replaced with the new one.
	 * 
	 * @param ontology
	 *            an {@link Ontology}.
	 * @param graphName
	 *            a String.
	 * @throws MetadataServiceException
	 * @see {@link #storeModel(URL, String)}.
	 */
	public abstract void storeInstanceData(Ontology ontology, String graphName)
			throws MetadataServiceException;

	public void updateInstanceData(Ontology ontology, String graphName)
			throws MetadataServiceException;

	/**
	 * Stores under <code>graphName</code> the RDF graph corresponding to
	 * <code>model</code>. Note that if a graph with the same name already
	 * exists it will be replaced with the new one.
	 * 
	 * @param model
	 *            a {@link Model}.
	 * @param graphName
	 *            a String.
	 * @throws MetadataServiceException
	 * @see {@link #storeModel(URL, String)}.
	 */
	public abstract void storeModel(Model model, String graphName)
			throws MetadataServiceException;

	/**
	 * Stores <code>rdfGraph</code> under <code>graphName</code>. Note that
	 * if a graph with the same name already exists it will be replaced with the
	 * new one.
	 * 
	 * @param rdfGraph
	 *            a String consisting of an RDF graph.
	 * @param graphName
	 *            a String.
	 * @throws MetadataServiceException
	 * @see {@link #storeModel(Model, String)}.
	 */
	public abstract void storeRDFGraph(String rdfGraph, String graphName)
			throws MetadataServiceException;

	/**
	 * Stores under <code>graphName</code> the RDF graph corresponding to the
	 * OWL ontology at <code>modelURL</code>. Note that if a graph with the
	 * same name already exists it will be replaced with the new one.
	 * 
	 * @param modelURL
	 *            a URL pointing to an OWL ontology.
	 * @param graphName
	 *            a String.
	 * @throws MetadataServiceException
	 * @see {@link #storeModel(Model, String)}.
	 */
	public abstract void storeModel(URL modelURL, String graphName)
			throws IOException, MetadataServiceException;

	/**
	 * Stores the quad of arguments
	 * 
	 * @param graphName
	 * @param subject
	 * @param predicate
	 * @param object
	 * @throws MetadataServiceException
	 */
	public void addQuad(String graphName, String subject, String predicate,
			String object) throws MetadataServiceException;

	/**
	 * Removes the graph <code>graphName</code> and also all the statements
	 * referring to it in {@link #GRAPHS_METADATA}.
	 * 
	 * @param graphName
	 *            the name of the graph to be removed.
	 * @throws GraphRemovalException
	 */
	public abstract void removeGraph(String graphName)
			throws GraphRemovalException;

	/**
	 * Retrieves the graph corresponding to <code>graphName</code> and returns
	 * it as a String containing its RDF/XML representation.
	 * 
	 * @param graphName
	 *            a String.
	 * @return a String containing an RDF/XML representation of the graph.
	 */
	public abstract String retrieveGraph(String graphName)
			throws MetadataServiceException;

	/**
	 * Retrieves the graph corresponding to <code>graphName</code> and returns
	 * it as a {@link Model}
	 * 
	 * @param graphName
	 *            a String
	 * @return a {@link Model}
	 */
	public abstract Model retrieveGraphModel(String graphName)
			throws MetadataServiceException;

	/**
	 * Retrieves the graph corresponding to <code>graphName</code> and returns
	 * it as a {@link Model} with set <code>namespace</code>,
	 * 
	 * @param graphName
	 *            a String
	 * @param namespacea
	 *            a String
	 * @return a {@link Model}
	 */
	public abstract Model retrieveGraphModel(String graphName, String namespace)
			throws MetadataServiceException;

	/**
	 * Removes the graph corresponding to <code>workflowRun</code> from the
	 * repository and then does the same by recursion for all its possible
	 * nested workflow runs and process runs.
	 * 
	 * @param workflowRun
	 * @throws MetadataServiceException
	 */
	public void removeWorkflowRun(String workflowRun)
			throws MetadataServiceException;

	/**
	 * Retrieves the graph corresponding to <code>workflowRunId</code>
	 * together with all the subgraphs of processes and nested runs and returns
	 * it as a String containing its RDF/XML representation.
	 * 
	 * @param workflowRunId
	 *            a String.
	 * @return a String containing an RDF/XML representation of the graph.
	 */
	public abstract String getWorkflowRun(String workflowRunId)
			throws MetadataServiceException;

	/**
	 * Gets the LSIDs of all the workflow runs in store.
	 * 
	 * @return a List of Strings
	 * @throws MetadataServiceException
	 */
	public List<String> getAllWorkflowRuns() throws MetadataServiceException;

	/**
	 * Gets the LSIDs of the nested workflow runs for <code>workflowRun</code>.
	 * 
	 * @param workflowRun
	 * @return a List of Strings
	 * @throws MetadataServiceException
	 */
	public List<String> getNestedRuns(String workflowRun)
			throws MetadataServiceException;

	/**
	 * Gets the LSIDs of all the nested workflow runs in store.
	 * 
	 * @return a List of Strings
	 * @throws MetadataServiceException
	 */
	public List<String> getAllNestedRuns() throws MetadataServiceException;

	/**
	 * Gets all the (ids of) process runs executed by
	 * <code>workflowRunLSID</code>
	 * 
	 * @param workflowRunLSID
	 * @return List of Strings
	 * @see #getNonNestedProcessRuns(String)
	 */
	public List<String> getProcessesRuns(String workflowRunLSID)
			throws MetadataServiceException;

	/**
	 * Gets all the (ids of) process runs executed by
	 * <code>workflowRunLSID</code> that are not nested workflows.
	 * 
	 * @param workflowRunId
	 * @return List of Strings
	 * @see #getProcessRuns(String)
	 */
	public List<String> getNonNestedProcessRuns(String workflowRunLSID)
			throws MetadataServiceException;

	/**
	 * Gets all the (ids of) workflow inputs for <code>workflowRunLSID</code>
	 * 
	 * @param workflowRunLSID
	 * @return List of Strings
	 */
	public List<String> getWorkflowInputs(String lsid)
			throws MetadataServiceException;

	/**
	 * Gets all the (ids of) workflow outputs for <code>workflowRunLSID</code>
	 * 
	 * @param workflowRunLSID
	 * @return List of Strings
	 */
	public List<String> getWorkflowOutputs(String workflowRunLSID)
			throws MetadataServiceException;

	/**
	 * Gets all the (ids of) outputs for <code>processID</code>
	 * 
	 * @param processID
	 * @return List of Strings
	 */
	public List<String> getProcessOutputs(String processID)
			throws MetadataServiceException;

	/**
	 * Gets all the (ids of) inputs for <code>processID</code>
	 * 
	 * @param processID
	 * @return List of Strings
	 */
	public List<String> getProcessInputs(String processID)
			throws MetadataServiceException;

	/**
	 * Returns the start date of <code>workflowRun</code>.
	 * 
	 * @param workflowRun
	 * @return a String
	 */
	public String getUnparsedWorkflowStartDate(String workflowRun)
			throws MetadataServiceException;

	/**
	 * Returns the end date of <code>processRun</code>.
	 * 
	 * @param processRun
	 * @return a String
	 */
	public String getUnparsedProcessEndDate(String processRun)
			throws MetadataServiceException;

	/**
	 * Returns <code>true</code> if <code>processURI</code> is an iteration
	 * run.
	 * 
	 * @param processURI
	 * @return a boolean
	 */
	public boolean isProcessIteration(String processURI)
			throws MetadataServiceException;

	/**
	 * Returns <code>true</code> if <code>processURI</code> contains
	 * iterations.
	 * 
	 * @param processURI
	 * @return a boolean
	 */
	public boolean isProcessWithIterations(String processURI)
			throws MetadataServiceException;

	/**
	 * Returns all the process runs with iterations,
	 * 
	 * @return a List of Strings.
	 */
	public List<String> getAllProcessRunsWithIterations()
			throws MetadataServiceException;

	/**
	 * Returns the LSIDs in <code>dataCollectionLSID</code>.
	 * 
	 * @param dataCollectionLSID
	 * @return
	 * @throws MetadataServiceException
	 */
	public List<String> getDataCollectionLSIDs(String dataCollectionLSID)
			throws MetadataServiceException;

	/**
	 * Returns the names of the Taverna (input and output) ports where
	 * <code>dataLSID</code> has passed through.
	 * 
	 * @param dataLSID
	 * @return a List of Strings
	 * @throws MetadataServiceException
	 */
	public List<String> getDataPortNames(String dataLSID)
			throws MetadataServiceException;

	/**
	 * Returns the names of the Taverna input ports where <code>dataLSID</code>
	 * has passed through.
	 * 
	 * @param dataLSID
	 * @return a List of Strings
	 * @throws MetadataServiceException
	 */
	public List<String> getInputDataPortNames(String dataLSID)
			throws MetadataServiceException;

	/**
	 * Returns the names of the Taverna output ports where <code>dataLSID</code>
	 * has passed through.
	 * 
	 * @param dataLSID
	 * @return a List of Strings
	 * @throws MetadataServiceException
	 */
	public List<String> getOutputDataPortNames(String dataLSID)
			throws MetadataServiceException;

	/**
	 * Returns the declared syntactic types for the ports <code>dataLSID</code>
	 * has passed through.
	 * 
	 * @param dataLSID
	 * @return a Set of Strings
	 * @throws MetadataServiceException
	 */
	public Set<String> getDataSyntacticTypes(String dataLSID)
			throws MetadataServiceException;

	public Map<String, DataProvenance> getSimilarData(String dataLSID)
			throws MetadataServiceException;

	/**
	 * Query to obtain all the workflow LSIDS for the current user as stated by
	 * mygrid.usercontext.experimenter
	 * 
	 * @param experimenter
	 * @return a Vector of the workflow LSIDs as Strings
	 */
	public Vector<String> getUserWorkFlows(String experimenter)
			throws MetadataServiceException;

	/**
	 * Gets the List of (String representations of) the targets of
	 * <code>objectProperty</code> for <code>sourceIndividual</code>.
	 * 
	 * @param sourceIndividual
	 *            String
	 * @param objectProperty
	 *            String
	 * @return a List of Strings
	 */
	public List<String> getObjectPropertyValues(String sourceIndividual,
			String objectProperty) throws MetadataServiceException;

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
			String objectProperty) throws MetadataServiceException;

	/**
	 * Gets the List of (String representations of) the targets of
	 * <code>datatypeProperty</code> for <code>sourceIndividual</code>.
	 * 
	 * @param sourceIndividual
	 *            String
	 * @param datatypeProperty
	 *            String
	 * @return a List of Strings
	 */
	public List<String> getDatatypePropertyValues(String sourceIndividual,
			String datatypeProperty) throws MetadataServiceException;

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
			String datatypeProperty) throws MetadataServiceException;

	/**
	 * Returns all the individuals of <code>type</code>.
	 * 
	 * @param type
	 * @return a List of Strings.
	 */
	public List<String> getIndividualsOfType(String type)
			throws MetadataServiceException;

	/**
	 * Returns <code>true</code> if <code>individual</code> is of type
	 * <code>type</code>.
	 * 
	 * @param individual
	 * @param type
	 * @return a boolean
	 * @throws MetadataServiceException
	 */
	public boolean isIndividualOfType(String individual, String type)
			throws MetadataServiceException;

	public Map<String, WorkflowRunBean> getWorkflowRunBeans()
			throws MetadataServiceException;

	public Map<String, Set<ProcessRunBean>> getProcessRunBeans()
			throws MetadataServiceException;

	/**
	 * Maps workflow runs ids to their process runs.
	 * 
	 * @return a Map from Strings to Sets of Strings
	 * @throws MetadataServiceException
	 */
	public Map<String, Set<String>> getAllProcessesRuns()
			throws MetadataServiceException;

}