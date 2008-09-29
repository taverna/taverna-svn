/*
 * Copyright (C) 2004 The University of Manchester 
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
 * Based on KAVE-Taverna-Plug-in by Chris Wroe.
 */
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.LSIDProvider;
import org.embl.ebi.escience.baclava.store.DuplicateLSIDException;
import org.embl.ebi.escience.scufl.AnnotationTemplate;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.SemanticMarkup;
import org.embl.ebi.escience.scufl.WorkflowDescription;
import org.embl.ebi.escience.scufl.enactor.UserContext;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.event.CollectionConstructionEvent;
import org.embl.ebi.escience.scufl.enactor.event.IterationCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.NestedWorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.UserChangedDataEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowDestroyedEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowInstanceEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowToBeDestroyedEvent;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.OntologyUpdateException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.soton.itinnovation.freefluo.main.Engine;
import uk.org.mygrid.provenance.LogBookException;
import uk.org.mygrid.provenance.dataservice.DataService;
import uk.org.mygrid.provenance.dataservice.DataServiceException;
import uk.org.mygrid.provenance.dataservice.DataServiceFactory;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.ibm.lsid.LSID;
import com.ibm.lsid.MalformedLSIDException;

/**
 * A {@link org.embl.ebi.escience.scufl.enactor.WorkflowEventListener}
 * generating provenance for workflow runs. It used to be only for metadata, but
 * it now also stores data.
 * 
 * @author dturi $Id: ProvenanceGenerator.java,v 1.1 2005/08/16 12:19:30 turid
 *         Exp $
 * 
 */
public class OldProvenanceGenerator implements WorkflowEventListener {

    public static final String PROCESS_RUN_NS = "urn:www.mygrid.org.uk/process_run#";

    // "http://www.mygrid.org/provenance/process_run#";

    public static final String PROCESS_NS = "urn:www.mygrid.org.uk/process#";

    public static final String USER_PREDICATE_QUALIFIER = "provenance";

    public static final String WORKFLOW_PROCESSOR = "org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor";

    public static final String WORKFLOW_NS = "urn:www.mygrid.org.uk/workflow#";

    private static final int SUCCESS = 0;

    private static final int FAILED = 1;

    private static final int ITERATION = 2;

    private static final int COMPLETED_ITERATIONS = 3;

    private static final String LAB_UNKNOWN = "urn:lsid:www.scientific_organisations.org:lab:unknown";

    private static final String PERSON_UNKNOWN = "urn:lsid:www.people.org:person:unknown";

    private static final String NULL_POINTER_IN_RDF_PROVENANCE_GENERATION = "Null pointer in RDF provenance generation";

    private static final String ERROR_UPDATING_ONTOLOGY = "Error updating ontology";

    public static Logger logger = Logger.getLogger(OldProvenanceGenerator.class);

    private UserContext userContext;

    // private String storage_mode;

    private String addVersion;

    // private static final String PER_PROCESS = "per_process";

    // private static final String PER_WORKFLOW = "per_workflow";

    private String rootWorkflowRunId;

    private IDGenerator processRunIdGenerator;

    private MetadataService metadataService;

    public MetadataService getRdfRepository() {
        return metadataService;
    }

    private DataService dataService;

    public DataService getDataService() {
        return dataService;
    }

    // private static final String I3C_CONTENT =
    // "urn:lsid:i3c.org:types:content";

    // private static final String I3C_XML_FORMAT =
    // "urn:lsid:i3c.org:formats:xml";

    private List<String> errorMessages = new ArrayList<String>();

    Map<String, ProvenanceOntology> ontologies;

    private List<String> locks;

    private List<WorkflowInstanceEvent> events;

    private Map<ProcessCompletionEvent, String> iterationRunIds;

    private Map<String, ScuflModel> workflowModels;

    private Map<String, String> workflowNames;

    private ScuflModel getWorkflowModel(String currentWorkflowRunId) {
        return workflowModels.get(currentWorkflowRunId);
    }

    private boolean hasNestedWorkflows = false;

    private Properties configuration;

    private boolean hasNestedWorkflows() {
        return hasNestedWorkflows;
    }

    private boolean hasNestedWorkflows(Processor[] processors) {
        for (int i = 0; i < processors.length; i++) {
            Processor processor = processors[i];
            if (isNestedWorkflow(processor)) {
                logger.info("Workflow contains nested workflow");
                return true;
            }
        }
        return false;
    }

    private boolean isNestedWorkflow(Processor processor) {
        String processorName = processor.getClass().getName();
        return processorName.equals(WORKFLOW_PROCESSOR);
    }

    /**
     * Convoluted method introduced to deal with taverna's erratic behaviour on
     * workflow run ids. Actually it seems to work after changing
     * {@link org.embl.ebi.escience.scufl.enactor.implementation.WorkflowInstanceImpl#getID()}
     * and
     * {@link org.embl.ebi.escience.scufl.enactor.implementation.WorkflowInstanceImpl#WorkflowInstanceImpl(Engine, ScuflModel, String)}
     */
    private String getID(WorkflowInstance workflowInstance) {
        logger.debug("getID(workflowInstance = " + workflowInstance + ")");
        String id = workflowInstance.getID();
        logger.debug("id = " + id);
        // if (id.equals("0")) {
        // if (hasNestedWorkflows()) {
        // // here is were taverna fails
        // logger
        // .debug("workflowInstance.getID() was 0 hence using value for map
        // using description LSID as key");
        // // use the id stored using the workflow description id as key
        // String workflowDescriptionId = workflowInstance
        // .getWorkflowModel().getDescription().getLSID();
        // logger
        // .debug("workflowDescriptionId = "
        // + workflowDescriptionId);
        // id = getWorkflowName(workflowDescriptionId);
        // } else {
        // if (rootWorkflowRunId != null)
        // // since taverna can be erratic, just forget about this
        // // instance and return the first assigned one
        // logger.debug("id = root workflow run id");
        // id = rootWorkflowRunId;
        // }
        // }
        // logger.debug("end getID() with id = " + id);
        return id;
    }

    String getWorkflowName(String workflowDescriptionId) {
        Iterator<String> names = workflowNames.keySet().iterator();
        boolean found = false;
        String name = "0";
        while (names.hasNext() && !found) {
            String nextName = names.next();
            if (workflowNames.get(nextName).equals(workflowDescriptionId)) {
                name = nextName;
                found = true;
            }
        }
        logger.debug("getWorkflowName(workflowDescriptionId = "
                + workflowDescriptionId + ") returns " + name);
        return name;
    }

    /**
     * Gets the root <code>workflowRunId</code>.
     * 
     * @return the current <code>workflowRunId</code>.
     */
    public String getWorkflowRunId() {
        return rootWorkflowRunId;
    }

    synchronized void addEvent(WorkflowInstanceEvent event) {
        events.add(event);
    }

    synchronized void removeEvent(WorkflowInstanceEvent event) {
        events.remove(event);
    }

    synchronized void putIterationRunId(ProcessCompletionEvent event,
            String iterationRunId) {
        iterationRunIds.put(event, iterationRunId);
    }

    synchronized String getIterationRunId(ProcessCompletionEvent event) {
        return iterationRunIds.get(event);
    }

    /**
     * The error messages that could not be thrown. WARN: no message stored at
     * the moment.
     * 
     * @return a List of Strings
     */
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    /**
     * Initialises both a metadata service and a dataservice using the
     * configuration properties given by
     * {@link ProvenanceConfigurator#getConfiguration()}.
     * 
     * @throws ProvenanceGeneratorException
     * @throws LogBookException 
     */
    public OldProvenanceGenerator() throws LogBookException {
        this(ProvenanceConfigurator.getConfiguration());
    }

    /**
     * Initialises both a metadata service and a dataservice using the
     * <code>configuration</code> properties.
     * 
     * @param configuration
     *            the required {@link Properties}.
     * @throws ProvenanceGeneratorException
     */
    public OldProvenanceGenerator(Properties configuration)
            throws ProvenanceGeneratorException {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("ProvenanceGenerator(Properties rdfRepositoryProperties="
                            + configuration + ") - start");
        }

        init();
        this.configuration = configuration;
        addVersion = System.getProperty("taverna.KAVE.versionLSID", "false");
        try {
            metadataService = MetadataServiceFactory.getInstance(configuration);
        } catch (MetadataServiceCreationException e) {
            logger.error("No provenance will be recorded: " + e.getMessage());
            throw new ProvenanceGeneratorException(
                    "ProvenanceGenerator failed to create metadata service.", e);
        }
        processRunIdGenerator = new TimedIDGenerator(PROCESS_RUN_NS);
        // logger.debug("storage mode = " + storage_mode);

        // Properties dataStoreConfiguration = ProvenanceConfigurator
        // .getDataStoreConfiguration();
        try {
            dataService = DataServiceFactory.getInstance(configuration);
        } catch (DataServiceException e) {
            logger.warn("Data service could not be started. Data will not "
                    + "be persisted (but provenance metadata will).");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("ProvenanceGenerator(Properties) - end");
        }
    }

    /**
     * Initialises all the needed synchronised collections and sets root
     * workflow run back to <code>null</code>.
     * 
     */
    synchronized public void init() {
        rootWorkflowRunId = null;

        ontologies = Collections
                .synchronizedMap(new HashMap<String, ProvenanceOntology>());

        locks = Collections.synchronizedList(new ArrayList<String>());

        events = Collections
                .synchronizedList(new ArrayList<WorkflowInstanceEvent>());

        iterationRunIds = Collections
                .synchronizedMap(new HashMap<ProcessCompletionEvent, String>());

        workflowModels = Collections
                .synchronizedMap(new HashMap<String, ScuflModel>());

        workflowNames = Collections
                .synchronizedMap(new HashMap<String, String>());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scufl.enactor.WorkflowEventListener#workflowCreated(org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent)
     */
    public void workflowCreated(WorkflowCreationEvent creationEvent) {
        logger.debug("workflowCreated(WorkflowCreationEvent creationEvent = "
                + creationEvent + ") - start");
        addEvent(creationEvent);
        try {
            WorkflowInstance workflowRun = creationEvent.getWorkflowInstance();
            String workflowRunId = getID(workflowRun); // workflowRun.getID();

            locks.add(workflowRunId); // TODO: sanity check
            synchronized (locks.get(locks.indexOf(workflowRunId))) {
                logger.info("Started workflow " + workflowRunId);
                if (rootWorkflowRunId == null)
                    rootWorkflowRunId = workflowRunId;
                ProvenanceOntology newOntology = ProvenanceOntologyFactory
                        .getInstance(configuration);
                ontologies.put(workflowRunId, newOntology);
                ProvenanceOntology provenanceOntology = newOntology;
                // } else {
                // locks.get(locks.indexOf(workflowRunId));
                // provenanceOntology = ontologies.get(workflowRunId);
                // }

                if (!hasNestedWorkflows()) {
                    Processor[] processors = creationEvent.getModel()
                            .getProcessors();
                    hasNestedWorkflows = hasNestedWorkflows(processors);
                }

                logger.debug("workflowRunId = " + workflowRunId);

                userContext = workflowRun.getUserContext();

                String workflowName = creationEvent.getDefinitionLSID();
                workflowNames.put(workflowRunId, workflowName);

                ScuflModel workflowModel = creationEvent.getModel();
                String accurateWorkflowName = getAccurateWorkflowName(workflowModel);
                storeWorkflow(accurateWorkflowName, workflowModel);
                // workflowModel.addListener(new ScuflModelEventListener() {
                // public void receiveModelEvent(ScuflModelEvent event) {
                // System.err.println("Workflow model for " + workflowName
                // + " has event: " + event);
                // }
                // });

                workflowModels.put(workflowRunId, workflowModel);
                logger
                        .debug("workflowCreated(WorkflowCreationEvent) -  : ScuflModel workflowModel="
                                + workflowModel);

                String personLSID = userContext.getPersonLSID();
                if (personLSID == null) {
                    personLSID = System
                            .getProperty("mygrid.usercontext.experimenter");
                    if (personLSID == null)
                        personLSID = PERSON_UNKNOWN;
                }
                System.setProperty(MetadataService.GRAPH_CREATOR, personLSID);
                String organizationLSID = userContext.getOrganizationLSID();
                if (organizationLSID == null) {
                    organizationLSID = System
                            .getProperty("mygrid.usercontext.organisation");
                    if (organizationLSID == null)
                        organizationLSID = LAB_UNKNOWN;
                }

                WorkflowDescription description = workflowModel
                        .getDescription();
                String title = description.getTitle();
                String author = description.getAuthor();
                String freeTextDescription = description.getText();
                if (title != null)
                    if (!title.equals(""))
                        provenanceOntology.addWorkflowTitle(
                                accurateWorkflowName, title);
                if (author != null)
                    if (!author.equals(""))
                        provenanceOntology.addWorkflowAuthor(
                                accurateWorkflowName, author);
                if (freeTextDescription != null)
                    if (!freeTextDescription.equals(""))
                        provenanceOntology.addWorkflowDescription(
                                accurateWorkflowName, freeTextDescription);
                provenanceOntology.addLaunchedBy(workflowRunId, personLSID);
                provenanceOntology.addBelongsTo(personLSID, organizationLSID);
                provenanceOntology.addWorkflowInitialLSID(accurateWorkflowName,
                        workflowName);
                provenanceOntology.addWorkflowStartTime(workflowRunId);
                provenanceOntology.addRunsWorkflow(workflowRunId,
                        accurateWorkflowName);

                Map inputs = creationEvent.getInputs();
                addMimeTypes(inputs, workflowModel.getWorkflowSourcePorts(),
                        provenanceOntology);
                logger.debug(inputs);
                String workflowNameWithoutLSID = getWorkflowNameWithoutLSID(accurateWorkflowName);
                addWorkflowInputData(inputs, workflowRunId,
                        workflowNameWithoutLSID, provenanceOntology);
                store(inputs);
            }
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        } catch (ProvenanceOntologyCreationException e) {
            logger.error("Error creating ontology", e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        } finally {
            removeEvent(creationEvent);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("workflowCreated(WorkflowCreationEvent) - end");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scufl.enactor.WorkflowEventListener#workflowFailed(org.embl.ebi.escience.scufl.enactor.event.WorkflowFailureEvent)
     */
    public void workflowFailed(WorkflowFailureEvent failureEvent) {
        logger.debug("workflowFailed(WorkflowFailureEvent failureEvent = "
                + failureEvent + ") start -");
        addEvent(failureEvent);
        try {
            WorkflowInstance workflowInstance = failureEvent
                    .getWorkflowInstance();
            String currentWorkflowRunId = getID(workflowInstance);
            synchronized (locks.get(locks.indexOf(currentWorkflowRunId))) {
                ProvenanceOntology provenanceOntology = ontologies
                        .get(currentWorkflowRunId);

                logger.info("Workflow id at end = " + currentWorkflowRunId);
                endFailedWorkflowProvenance(currentWorkflowRunId,
                        provenanceOntology);
                logger
                        .debug("rdf plugin workflow completed but workflow failed,");
            }
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        } finally {
            removeEvent(failureEvent);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scufl.enactor.WorkflowEventListener#workflowCompleted(org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent)
     */
    public void workflowCompleted(WorkflowCompletionEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("workflowCompleted(WorkflowCompletionEvent event = "
                    + event + ") - start");
        }

        try {
            WorkflowInstance workflowInstance = event.getWorkflowInstance();
            String currentWorkflowRunId = getID(workflowInstance);
            // TODO: sanity check
            synchronized (locks.get(locks.indexOf(currentWorkflowRunId))) {
                //                
                // String errorMessage = "Wrong workflow run id "
                // + currentWorkflowRunId
                // + "\n\tIt does not correspond to a created workflow.";
                // errorMessages.add(errorMessage);
                // logger.error(errorMessage);

                ProvenanceOntology provenanceOntology = ontologies
                        .get(currentWorkflowRunId);

                logger.info("Workflow id at end = " + currentWorkflowRunId);
                logger
                        .debug("workflowCompleted(WorkflowCompletionEvent) -  : WorkflowInstance workflowInstance="
                                + workflowInstance);
                Map outputs = workflowInstance.getOutput();
                addMimeTypes(outputs, getWorkflowModel(currentWorkflowRunId)
                        .getWorkflowSinkPorts(), provenanceOntology);

                String workflowName = workflowNames.get(currentWorkflowRunId);
                String workflowNameWithoutLSID = getWorkflowNameWithoutLSID(workflowName);
                addWorkflowOutputData(outputs, currentWorkflowRunId,
                        workflowNameWithoutLSID, provenanceOntology);
                endSuccessfulWorkflowProvenance(currentWorkflowRunId,
                        provenanceOntology);
                store(outputs);
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("workflowCompleted(WorkflowCompletionEvent) - end");
                }
                // printRun();
            }
        } catch (NullPointerException e) {
            logger.error(e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        } finally {
            removeEvent(event);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scufl.enactor.WorkflowEventListener#processCompleted(org.embl.ebi.escience.scufl.enactor.event.ProcessCompletionEvent)
     */
    public void processCompleted(ProcessCompletionEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("processCompleted(ProcessCompletionEvent event = "
                    + event + ") - start");
        }
        if (rootWorkflowRunId == null) {
            logger.warn("Provenance will be incomplete: event " + event
                    + " received after workflow completion event.");
            return;
        }
        addEvent(event);
        try {
            WorkflowInstance workflowInstance = event.getWorkflowInstance();
            Processor processor = event.getProcessor();
            Map inputs = event.getInputMap();
            Map outputs = event.getOutputMap();
            if (event.isIterating()) {
                String processRunId = successfulIterationCompletion(
                        workflowInstance, inputs, outputs, processor);
                putIterationRunId(event, processRunId);
            } else
                successfulProcessCompletion(workflowInstance, inputs, outputs,
                        processor);
            store(inputs);
            store(outputs);
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        } finally {
            removeEvent(event);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("processCompleted(ProcessCompletionEvent) - end");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scufl.enactor.WorkflowEventListener#processCompletedWithIteration(org.embl.ebi.escience.scufl.enactor.event.IterationCompletionEvent)
     * 
     */
    public void processCompletedWithIteration(IterationCompletionEvent event) {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("processCompletedWithIteration(IterationCompletionEvent event = "
                            + event + ") - start");
        }

        if (rootWorkflowRunId == null) {
            logger.warn("Provenance will be incomplete: event " + event
                    + " received after workflow completion event.");
            return;
        }
        addEvent(event);
        try {
            WorkflowInstance workflowInstance = event.getWorkflowInstance();
            Processor processor = event.getProcessor();
            Map inputs = event.getOverallInputs();
            Map outputs = event.getOverallOutputs();
            // Map inputShredding = event.getInputShredding();
            // Map structureMapping = event.getStructureMapping();
            List<ProcessCompletionEvent> associatedCompletionEvents = event
                    .getAssociatedCompletionEvents();
            successfulProcessCompletionWithIterations(workflowInstance, inputs,
                    outputs, processor, associatedCompletionEvents);
            store(inputs);
            store(outputs);
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        } finally {
            removeEvent(event);
        }

        if (logger.isDebugEnabled()) {
            logger
                    .debug("processCompletedWithIteration(IterationCompletionEvent) - end");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scufl.enactor.WorkflowEventListener#processFailed(org.embl.ebi.escience.scufl.enactor.event.ProcessFailureEvent)
     */
    public void processFailed(ProcessFailureEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("processFailed(ProcessFailureEvent event=" + event
                    + ") - start");
        }

        if (rootWorkflowRunId == null) {
            logger.warn("Provenance will be incomplete: event " + event
                    + " received after workflow completion event.");
            return;
        }
        addEvent(event);
        try {
            WorkflowInstance workflowInstance = event.getWorkflowInstance();
            Processor processor = event.getProcessor();
            Map inputs = event.getInputMap();
            Exception cause = event.getCause();
            failedProcessCompletion(workflowInstance, processor, inputs, cause);
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        } finally {
            removeEvent(event);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("processFailed(ProcessFailureEvent) - end");
        }
    }

    public void nestedWorkflowCreated(NestedWorkflowCreationEvent e) {
        // FIXME: implement me        
    }

    public void nestedWorkflowCompleted(
            NestedWorkflowCompletionEvent nestedEvent) {
        WorkflowInstance nestedWorkflowInstance = nestedEvent
                .getNestedWorkflowInstance();
        WorkflowInstance parentWorkflowInstance = nestedEvent
                .getWorkflowInstance();
        logger.info("NESTED ID = " + nestedWorkflowInstance.getID());
        Processor processor = nestedEvent.getProcessor();
        Map inputs = nestedEvent.getInputMap();
        Map outputs = nestedEvent.getOutputMap();
        try {
            endNestedWorkflow(nestedWorkflowInstance, parentWorkflowInstance,
                    processor, inputs, outputs, SUCCESS);
        } catch (NullPointerException e) {
            logger.error(e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        }
    }

    public void nestedWorkflowFailed(NestedWorkflowFailureEvent nestedEvent) {
        WorkflowInstance nestedWorkflowInstance = nestedEvent
                .getNestedWorkflow();
        WorkflowInstance parentWorkflowInstance = nestedEvent
                .getWorkflowInstance();
        logger.info("NESTED ID = " + nestedWorkflowInstance.getID());
        Processor processor = nestedEvent.getProcessor();
        Map inputs = nestedEvent.getInputMap();
        try {
            endNestedWorkflow(nestedWorkflowInstance, parentWorkflowInstance,
                    processor, inputs, null, FAILED);
        } catch (NullPointerException e) {
            logger.error(e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scufl.enactor.WorkflowEventListener#collectionConstructed(org.embl.ebi.escience.scufl.enactor.event.CollectionConstructionEvent)
     */
    public void collectionConstructed(CollectionConstructionEvent event) {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("collectionConstructed(CollectionConstructionEvent event = "
                            + event + ") - start");
        }

        if (rootWorkflowRunId == null) {
            logger.warn("Provenance will be incomplete: event " + event
                    + " received after workflow completion event.");
            return;
        }

        addEvent(event);
        try {
            WorkflowInstance workflowInstance = event.getWorkflowInstance();
            String currentWorkflowRunId = getID(workflowInstance);
            // TODO: sanity check
            synchronized (locks.get(locks.indexOf(currentWorkflowRunId))) {
                ProvenanceOntology provenanceOntology = ontologies
                        .get(currentWorkflowRunId);
                try {
                    String originalLSID = event.getOriginalLSID();
                    String[] collectionLSIDs = event.getCollectionLSIDs();
                    for (int i = 0; i < collectionLSIDs.length; i++) {
                        provenanceOntology.addDataWrappedInto(originalLSID,
                                collectionLSIDs[i]);
                    }
                } catch (NullPointerException e) {
                    logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
                } catch (OntologyUpdateException e) {
                    logger.error(ERROR_UPDATING_ONTOLOGY, e);
                }
            }
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        } finally {
            removeEvent(event);
        }

        if (logger.isDebugEnabled()) {
            logger
                    .debug("collectionConstructed(CollectionConstructionEvent) - end");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scufl.enactor.WorkflowEventListener#dataChanged(org.embl.ebi.escience.scufl.enactor.event.UserChangedDataEvent)
     */
    public void dataChanged(UserChangedDataEvent event) {
        if (logger.isDebugEnabled()) {
            logger.debug("dataChanged(UserChangedDataEvent event = " + event
                    + ") - start");
        }

        if (rootWorkflowRunId == null) {
            logger.warn("Provenance will be incomplete: event " + event
                    + " received after workflow completion event.");
            return;
        }
        addEvent(event);
        try {
            WorkflowInstance workflowInstance = event.getWorkflowInstance();
            String currentWorkflowRunId = getID(workflowInstance);
            // TODO: sanity check
            synchronized (locks.get(locks.indexOf(currentWorkflowRunId))) {
                ProvenanceOntology provenanceOntology = ontologies
                        .get(currentWorkflowRunId);
                try {
                    DataThing dataThing = event.getDataThing();
                    String lsid = dataThing.getLSID(dataThing.getDataObject());
                    if (!lsid.equals("")) {
                        String versionLSID = versionLSID(lsid);
                        addCollection(dataThing, versionLSID,
                                provenanceOntology);
                        String oldDataThingID = event.getOldDataThingID();
                        provenanceOntology.addChangedData(versionLSID,
                                versionLSID(oldDataThingID));
                    }
                } catch (NullPointerException e) {
                    logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
                } catch (OntologyUpdateException e) {
                    logger.error(ERROR_UPDATING_ONTOLOGY, e);
                }
            }
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        } finally {
            removeEvent(event);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("dataChanged(UserChangedDataEvent) - end");
        }
    }

    String getAccurateWorkflowName(ScuflModel workflowModel) {
        LSIDProvider authority = DataThing.SYSTEM_DEFAULT_LSID_PROVIDER;
        String lsid = authority.getID(LSIDProvider.WFDEFINITION);
        return lsid;
    }

    void endSuccessfulWorkflowProvenance(String workflowRunId,
            ProvenanceOntology provenanceOntology) throws NullPointerException,
            OntologyUpdateException {
        endWorkflowProvenance(workflowRunId, true, provenanceOntology);
    }

    void endFailedWorkflowProvenance(String workflowRunId,
            ProvenanceOntology provenanceOntology) throws NullPointerException,
            OntologyUpdateException {
        endWorkflowProvenance(workflowRunId, false, provenanceOntology);
    }

    void endWorkflowProvenance(String workflowRunId, boolean isSuccess,
            ProvenanceOntology provenanceOntology) throws NullPointerException,
            OntologyUpdateException {
        if (logger.isDebugEnabled()) {
            logger.debug("endWorkflowProvenance(String workflowRunId = "
                    + workflowRunId + ", boolean isSuccess = " + isSuccess
                    + ", ProvenanceOntology provenanceOntology = "
                    + provenanceOntology + ") - start");
        }

        try {

            if (hasNestedWorkflows())
                if (ontologies.containsKey(workflowRunId))
                    provenanceOntology = (ProvenanceOntology) ontologies
                            .get(workflowRunId);
            logger.debug("provenanceOntology = " + provenanceOntology);

            if (!isSuccess)
                provenanceOntology.addFailedWorkflowRun(workflowRunId);
            provenanceOntology.addWorkflowEndTime(workflowRunId);

        } finally {
            try {
                if (isRootWorkflowRun(workflowRunId))
                    persistOntologiesAndRestart();
            } catch (MetadataServiceException e) {
                logger.error("Error while persisting instance data", e);
            }

            // if (!hasNestedWorkflows)
            // writeToFile();

            if (logger.isDebugEnabled()) {
                logger.debug("endWorkflowProvenance(String) - end");
            }
        }
    }

    void endNestedWorkflow(WorkflowInstance nestedWorkflowInstance,
            WorkflowInstance parentWorkflowInstance, Processor processor,
            Map inputs, Map outputs, int type) throws OntologyUpdateException {
        String processorName = PROCESS_NS + processor.getName();
        String processRunId = processRunIdGenerator.getNextID();
        String processorClassName = processor.getClass().getName();
        String parentWorkflowRunId = parentWorkflowInstance.getID();
        String nestedWorkflowRunId = nestedWorkflowInstance.getID();

        // TODO: sanity check
        synchronized (locks.get(locks.indexOf(parentWorkflowRunId))) {
            ProvenanceOntology provenanceOntology = (ProvenanceOntology) ontologies
                    .get(parentWorkflowRunId);
            provenanceOntology.addProcessIsNestedWorkflow(processRunId,
                    nestedWorkflowRunId);
            provenanceOntology.addNestedWorkflow(parentWorkflowRunId,
                    nestedWorkflowRunId);
            provenanceOntology.addProcessClassName(processorName,
                    processorClassName);

            try {
                Properties processorProperties = processor.getProperties();
                if (processorProperties != null)
                    provenanceOntology.addProcessProperties(processorName,
                            processorProperties);
            } catch (NullPointerException e) {
                logger.warn(
                        "Null pointer when getting properties of processor", e);
            }

            provenanceOntology.addProcessEndTime(processRunId);
            provenanceOntology.addRunsProcess(processRunId, processorName);
            provenanceOntology.addExecutedProcessRun(parentWorkflowRunId,
                    processRunId);

            addInputData(inputs, processRunId, processorName,
                    provenanceOntology);

            InputPort[] inputPorts = processor.getInputPorts();
            boolean hasInputs = inputs != null;
            if (hasInputs && inputPorts != null)
                addMimeTypes(inputs, inputPorts, provenanceOntology);

            addOutputData(outputs, processRunId, processorName,
                    provenanceOntology);
            OutputPort[] outputPorts = processor.getOutputPorts();
            boolean hasOutputs = outputs != null;
            if (hasOutputs && outputPorts != null)
                addMimeTypes(outputs, outputPorts, provenanceOntology);

            if (!(!hasInputs || !hasOutputs)) {
                for (Iterator iter = inputs.keySet().iterator(); iter.hasNext();) {
                    String name = (String) iter.next();
                    DataThing inputDataThing = (DataThing) inputs.get(name);
                    String inputLSID = inputDataThing.getLSID(inputDataThing
                            .getDataObject());
                    for (Iterator iterator = outputs.keySet().iterator(); iterator
                            .hasNext();) {
                        String outputName = (String) iterator.next();
                        DataThing outputDataThing = (DataThing) outputs
                                .get(outputName);
                        try {
                            String outputLSID = outputDataThing
                                    .getLSID(outputDataThing.getDataObject());
                            addDerivedFrom(inputLSID, outputLSID,
                                    provenanceOntology);
                        } catch (NullPointerException npe) {
                            logger.error(
                                    NULL_POINTER_IN_RDF_PROVENANCE_GENERATION,
                                    npe);
                        }
                    }
                }
                storeAnnotationTriples(processor.getAnnotationTemplates(),
                        inputs, outputs, provenanceOntology);
            }
        }

        try {
            // TODO: sanity check
            synchronized (locks.get(locks.indexOf(nestedWorkflowRunId))) {
                ProvenanceOntology nestedRunOntology = ontologies
                        .get(nestedWorkflowRunId);
                switch (type) {
                case FAILED:
                    nestedRunOntology.setToFailedNestedWorkflow(
                            nestedWorkflowRunId, parentWorkflowRunId);
                    break;
                default:
                    nestedRunOntology.setToNestedWorkflow(nestedWorkflowRunId,
                            parentWorkflowRunId);
                    break;
                }
            }
        } catch (OntologyUpdateException e) {
            logger.error(e.getMessage(), e);
        }
    }

    String successfulProcessCompletion(WorkflowInstance workflowInstance,
            Map inputs, Map outputs, Processor processor)
            throws OntologyUpdateException {
        return processCompletion(workflowInstance, processor, inputs, outputs,
                SUCCESS, null, null);
    }

    String successfulProcessCompletionWithIterations(
            WorkflowInstance workflowInstance, Map inputs, Map outputs,
            Processor processor,
            List<ProcessCompletionEvent> associatedCompletionEvents)
            throws OntologyUpdateException {
        // logger.debug("ITERATION EVENTS BEGIN");
        // logger.debug(processor);

        String processRunId = processCompletion(workflowInstance, processor,
                inputs, outputs, COMPLETED_ITERATIONS, null,
                associatedCompletionEvents);
        // logger.debug("ITERATION EVENTS END");
        return processRunId;
    }

    String successfulIterationCompletion(WorkflowInstance workflowInstance,
            Map inputs, Map outputs, Processor processor)
            throws OntologyUpdateException {
        return processCompletion(workflowInstance, processor, inputs, outputs,
                ITERATION, null, null);
    }

    String failedProcessCompletion(WorkflowInstance workflowInstance,
            Processor processor, Map inputs, Exception cause)
            throws OntologyUpdateException {
        return processCompletion(workflowInstance, processor, inputs, null,
                FAILED, cause, null);
    }

    String processCompletion(WorkflowInstance workflowInstance,
            Processor processor, Map inputs, Map outputs, int type,
            Exception failureCause,
            List<ProcessCompletionEvent> associatedCompletionEvents)
            throws OntologyUpdateException {
        if (logger.isDebugEnabled()) {
            logger.debug("processCompletionProvenance(Processor processor="
                    + processor + ", Map inputs=" + inputs + ", Map outputs="
                    + outputs + ", int type=" + type
                    + ", Exception failureCause=" + failureCause + ") - start");
        }

        String processorName = PROCESS_NS + processor.getName();
        String processRunId = processRunIdGenerator.getNextID();
        String processorClassName = processor.getClass().getName();
        String currentWorkflowRunId = getID(workflowInstance);
        // TODO: sanity check
        logger.debug("Reached " + processorName + " with run ID = "
                + currentWorkflowRunId);
        synchronized (locks.get(locks.indexOf(currentWorkflowRunId))) {
            ProvenanceOntology provenanceOntology = ontologies
                    .get(currentWorkflowRunId);
            logger.info("Got ontology " + provenanceOntology);
            // if (hasNestedWorkflows())
            // if (ontologies.containsKey(currentWorkflowRunId)) {
            // provenanceOntology = (ProvenanceOntology) ontologies
            // .get(currentWorkflowRunId);
            // } else {
            // String errorMessage = "Wrong workflow run id "
            // + currentWorkflowRunId
            // + " provided by process event for processor "
            // + processorName
            // + "\n\tIt does not correspond to a created workflow.";
            // errorMessages.add(errorMessage);
            // logger.error(errorMessage);
            // }
            // if (isNestedWorkflow(processor))
            // provenanceOntology.addNestedWorkflow(processRunId);
            provenanceOntology.addProcessClassName(processorName,
                    processorClassName);

            try {
                Properties processorProperties = processor.getProperties();
                if (processorProperties != null)
                    provenanceOntology.addProcessProperties(processorName,
                            processorProperties);
            } catch (NullPointerException e) {
                logger.warn(
                        "Null pointer when getting properties of processor", e);
            }

            provenanceOntology.addProcessEndTime(processRunId);
            provenanceOntology.addRunsProcess(processRunId, processorName);
            provenanceOntology.addExecutedProcessRun(currentWorkflowRunId,
                    processRunId);

            addInputData(inputs, processRunId, processorName,
                    provenanceOntology);

            InputPort[] inputPorts = processor.getInputPorts();
            boolean hasInputs = inputs != null;
            if (hasInputs && inputPorts != null)
                addMimeTypes(inputs, inputPorts, provenanceOntology);

            switch (type) {
            case FAILED:
                String failureMessage = "";
                if (failureCause != null)
                    failureMessage = failureCause.getMessage();
                provenanceOntology.addFailedProcessRun(processRunId,
                        processorName, failureMessage);

                if (logger.isDebugEnabled()) {
                    logger
                            .debug("processCompletionProvenance(Processor, Map, Map, boolean) - end");
                }
                return processRunId;
            case ITERATION:
                provenanceOntology.addProcessIteration(processRunId);
                break;
            case COMPLETED_ITERATIONS:
                HashSet<String> iterations = new HashSet<String>();
                for (ProcessCompletionEvent event : associatedCompletionEvents) {
                    String iterationRunId = getIterationRunId(event);
                    iterations.add(iterationRunId);
                }
                provenanceOntology.addProcessCompletedWithIterations(
                        processRunId, iterations);
                break;
            default:
                break;
            }

            addOutputData(outputs, processRunId, processorName,
                    provenanceOntology);
            OutputPort[] outputPorts = processor.getOutputPorts();
            boolean hasOutputs = outputs != null;
            if (hasOutputs && outputPorts != null)
                addMimeTypes(outputs, outputPorts, provenanceOntology);

            if (!hasInputs || !hasOutputs) {
                if (logger.isDebugEnabled()) {
                    logger
                            .debug("processCompletionProvenance(Processor, Map, Map, boolean) - end");
                }
                return processRunId;
            }
            for (Iterator iter = inputs.keySet().iterator(); iter.hasNext();) {
                String name = (String) iter.next();
                DataThing inputDataThing = (DataThing) inputs.get(name);
                String inputLSID = inputDataThing.getLSID(inputDataThing
                        .getDataObject());
                for (Iterator iterator = outputs.keySet().iterator(); iterator
                        .hasNext();) {
                    String outputName = (String) iterator.next();
                    DataThing outputDataThing = (DataThing) outputs
                            .get(outputName);
                    try {
                        String outputLSID = outputDataThing
                                .getLSID(outputDataThing.getDataObject());
                        addDerivedFrom(inputLSID, outputLSID,
                                provenanceOntology);
                    } catch (NullPointerException npe) {
                        logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION,
                                npe);
                    }
                }
            }

            storeAnnotationTriples(processor.getAnnotationTemplates(), inputs,
                    outputs, provenanceOntology);

            if (logger.isDebugEnabled()) {
                logger
                        .debug("processCompletionProvenance(Processor, Map, Map, boolean) - end");
            }

            return processRunId;
        }
    }

    void addWorkflowInputData(Map dataMap, String workflowRunId,
            String workflowNameWithoutLSID,
            ProvenanceOntology provenanceOntology) throws NullPointerException,
            OntologyUpdateException {
        addWorkflowData(dataMap, workflowRunId, workflowNameWithoutLSID, true,
                provenanceOntology);
    }

    void addWorkflowOutputData(Map dataMap, String workflowRunId,
            String workflowNameWithoutLSID,
            ProvenanceOntology provenanceOntology) throws NullPointerException,
            OntologyUpdateException {
        addWorkflowData(dataMap, workflowRunId, workflowNameWithoutLSID, false,
                provenanceOntology);
    }

    void addWorkflowData(Map dataMap, String workflowRunId,
            String workflowNameWithoutLSID, boolean isInput,
            ProvenanceOntology provenanceOntology) throws NullPointerException,
            OntologyUpdateException {

        for (Iterator iter = dataMap.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            DataThing dataThing = (DataThing) dataMap.get(name);
            String syntacticType = dataThing.getSyntacticType();
            syntacticType = syntacticType.replaceAll("\\'", "");
            String lsid = dataThing.getLSID(dataThing.getDataObject());
            if (!lsid.equals("")) {
                String versionLsid = versionLSID(lsid);
                logger.debug("provenanceOntology = " + provenanceOntology);
                if (isInput)
                    provenanceOntology.addWorkflowInputData(versionLsid,
                            workflowRunId, workflowNameWithoutLSID + "_in_"
                                    + name, syntacticType);
                else
                    provenanceOntology.addWorkflowOutputData(versionLsid,
                            workflowRunId, workflowNameWithoutLSID + "_out_"
                                    + name, syntacticType);
                addCollection(dataThing, versionLsid, provenanceOntology);
            }
        }

    }

    void addInputData(Map dataMap, String processorRunId, String processorName,
            ProvenanceOntology provenaceOntology) throws NullPointerException,
            OntologyUpdateException {
        addData(dataMap, processorRunId, processorName, true, provenaceOntology);
    }

    void addOutputData(Map dataMap, String processorRunId,
            String processorName, ProvenanceOntology provenaceOntology)
            throws NullPointerException, OntologyUpdateException {
        addData(dataMap, processorRunId, processorName, false,
                provenaceOntology);
    }

    void addData(Map dataMap, String processorRunId, String processorName,
            boolean isInput, ProvenanceOntology provenanceOntology)
            throws NullPointerException, OntologyUpdateException {
        if (dataMap == null)
            return;
        for (Iterator iter = dataMap.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            DataThing dataThing = (DataThing) dataMap.get(name);
            String syntacticType = dataThing.getSyntacticType();
            syntacticType = syntacticType.replaceAll("\\'", "");
            // logger.debug(dataName + " -> " + syntacticType);
            String lsid = dataThing.getLSID(dataThing.getDataObject());
            if (!lsid.equals("")) {
                String versionLSID = versionLSID(lsid);
                if (isInput)
                    provenanceOntology.addInputData(versionLSID,
                            processorRunId, processorName + "_in_" + name,
                            syntacticType);
                else
                    provenanceOntology.addOutputData(versionLSID,
                            processorRunId, processorName + "_out_" + name,
                            syntacticType);
                addCollection(dataThing, versionLSID, provenanceOntology);
            }
        }
    }

    void addCollection(DataThing dataThing, String parentLSID,
            ProvenanceOntology provenanceOntology) throws NullPointerException,
            OntologyUpdateException {
        if (logger.isDebugEnabled()) {
            logger.debug("addCollection(DataThing dataThing = " + dataThing
                    + ", String parentLSID = " + parentLSID + ") - start");
        }

        for (Iterator iterator = dataThing.childIterator(); iterator.hasNext();) {
            DataThing childDataThing = (DataThing) iterator.next();
            String childLSID = childDataThing.getLSID(childDataThing
                    .getDataObject());
            if (!childLSID.equals("")) {
                String versionChildLSID = versionLSID(childLSID);
                provenanceOntology
                        .addContainsData(parentLSID, versionChildLSID);
                addCollection(childDataThing, versionChildLSID,
                        provenanceOntology);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("addCollection(DataThing, String) - end");
        }
    }

    void persistOntologiesAndRestart() throws MetadataServiceException {
        logger.info("persisting ontologies");
        checkNoMoreEvents();
        try {
            for (String key : ontologies.keySet())
                metadataService.storeInstanceData(ontologies.get(key), key);
            logger.info("ontologies persisted");
        } catch (MetadataServiceException e) {
            logger
                    .warn("Metadata service error: provenance might be incomplete");
        } finally {
            ontologies = null;
            init();
            logger.info("generator restarted");
        }
    }

    private void checkNoMoreEvents() {
        if (events.size() > 1)
            logger
                    .error("Processing of root event completion happening before "
                            + "all other events have been processed");
    }

    void addMimeTypes(Map data, Port[] ports,
            ProvenanceOntology provenanceOntology)
            throws OntologyUpdateException {
        if (logger.isDebugEnabled()) {
            logger.debug("addMimeTypes(Map data=" + data + ", Port[] ports="
                    + ports + ") - start");
        }

        try {
            for (Iterator iter = data.keySet().iterator(); iter.hasNext();) {
                String outputName = (String) iter.next();
                Port port = getPortNamed(outputName, ports);
                if (port != null) {
                    SemanticMarkup metadata = port.getMetadata();
                    String[] types = metadata.getMIMETypes();
                    for (int i = 0; i < types.length; i++)
                        if (provenanceOntology != null)
                            provenanceOntology.addMimeType(PROCESS_NS
                                    + outputName, types[i]);
                    // String type = metadata.getSemanticType();
                    // logger.debug("semantic type = " + type);
                } else {
                    logger.warn("Port " + outputName + " not found");
                }
            }
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("addMimeTypes(Map, Port[]) - end");
        }
    }

    /**
     * @param string
     * @return
     */
    Port getPortNamed(String name, Port[] ports) throws NullPointerException {
        for (int i = 0; i < ports.length; i++) {
            Port port = ports[i];
            logger.debug("sink port name " + port.getName()
                    + " : testing against " + name);
            if (port.getName().equals(name)) {
                return port;
            }

        }
        return null;
    }

    void addDerivedFrom(String input, String output,
            ProvenanceOntology provenanceOntology) throws NullPointerException,
            OntologyUpdateException {
        String inputLSID, outputLSID;
        if (input == null || output == null)
            return;
        if (input.equals("") || output.equals(""))
            return;
        inputLSID = versionLSID(input);
        outputLSID = versionLSID(output);

        logger.debug("input lsid: " + inputLSID + " output lsid " + outputLSID);
        provenanceOntology.addDerivedFrom(inputLSID, outputLSID);
    }

    // void addProperties(String propertyPath) throws Exception {
    // try {
    // InputStream inStr = this.getClass().getResourceAsStream(
    // propertyPath);
    // if (inStr != null) {
    // Properties props = new Properties();
    // // load configuration properties
    // props.load(inStr);
    // inStr.close();
    // for (Enumeration iter = props.keys(); iter.hasMoreElements();) {
    // String key = (String) iter.nextElement();
    // System.setProperty(key, props.getProperty(key));
    // }
    // } else {
    // logger.error("failed to find " + propertyPath + " file...");
    // }
    //
    // } catch (IOException e1) {
    // throw new Exception("Problem loading configuration", e1);
    // } catch (NullPointerException npe) {
    // throw new Exception("Couldn't find configuration", npe);
    // }
    // }

    /**
     * Stores the triples in <code>templates</code>.
     * 
     * @param templates
     *            an array of {@link AnnotationTemplate}s.
     * @param inputMap
     * @param outputMap
     * @param provenanceOntology
     * @throws OntologyUpdateException
     */
    void storeAnnotationTriples(AnnotationTemplate[] templates, Map inputMap,
            Map outputMap, ProvenanceOntology provenanceOntology)
            throws NullPointerException, OntologyUpdateException {

        Map templateInputs = new HashMap();
        Map templateOutputs = new HashMap();
        nameToDataMapper(inputMap, outputMap, templateInputs, templateOutputs);

        for (int j = 0; j < templates.length; j++) {
            AnnotationTemplate annotation = templates[j];
            String triple = annotation.getTextAnnotation(templateInputs,
                    templateOutputs);
            // text = AnnotationTemplate.convert(text);
            logger.debug("Annotation template: " + triple);

            // add namespace stuff in
            String[] pair = triple
                    .split("(?<=xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\")");
            StringBuffer nsTriple = new StringBuffer(pair[0] + "\n xmlns:"
                    + USER_PREDICATE_QUALIFIER + "=\""
                    + ProvenanceOntology.PROVENANCE_NS + "#\"");
            for (int k = 1; k < pair.length; k++)
                nsTriple.append(pair[k]);

            String[] strings = nsTriple.toString().split("<(?!rdf|/rdf)");
            StringBuffer processedTriple = new StringBuffer();
            for (int k = 0; k < strings.length; k++) {
                if ((k % 2) == 0)
                    processedTriple.append(strings[k]);
                else {
                    processedTriple
                            .append("<" + USER_PREDICATE_QUALIFIER + ":");
                    processedTriple.append(strings[k]);
                }
            }
            String processedTripleAsString = processedTriple.toString();
            logger.debug("Processed annotation template: "
                    + processedTripleAsString);
            Model aBox = ModelFactory.createDefaultModel();
            aBox.read(new StringReader(processedTripleAsString),
                    ProvenanceOntology.PROVENANCE_NS);
            StmtIterator iter = aBox.listStatements();
            while (iter.hasNext()) {
                Statement nextStatement = iter.nextStatement();
                Resource subject = nextStatement.getSubject();
                String subjectURI = subject.getURI();
                Property predicate = nextStatement.getPredicate();
                String predicateURI = predicate.getURI();
                RDFNode object = nextStatement.getObject();
                String objectURI = object.asNode().getURI();
                provenanceOntology.addUserPredicate(subjectURI, predicateURI,
                        objectURI);
            }
        }
    }

    private void store(Map map) {
        if (dataService == null)
            return;
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            DataThing dataThing = (DataThing) map.get(name);
            try {
                dataService.storeDataThing(dataThing, true);
                logger.debug("Stored " + name);
            } catch (DuplicateLSIDException e) {
                logger.warn(e.getMessage());
                logger.info(name + " already stored");
            } catch (DataServiceException e) {
                logger.warn(e.getMessage());
            }
        }
    }

    private void storeWorkflow(String lsid, ScuflModel model) {
        if (dataService == null)
            return;
        try {
            dataService.storeWorkflow(lsid, model);
            logger.info("Stored workflow " + lsid);
        } catch (DataServiceException ex) {
            logger.error("Could not store workflow " + lsid, ex);
        }
    }

    /**
     * @deprecated
     * 
     * implementation removed as it can cause problems with Jena on some
     * machines.
     */
    public void writeToFile() {
        // String instanceData = provenanceOntology.getInstanceDataAsString();
        //
        // try {
        // ProvenanceOntology writable = ProvenanceOntologyFactory
        // .getInstance();
        // writable.loadInstanceData(instanceData);
        // String outFile = System
        // .getProperty("mygrid.kave.generate.provenance.out");
        // logger.debug("outFile = " + outFile);
        // if (outFile != null)
        // if (!outFile.equals("")) {
        // File out = new File(outFile);
        // writable.writeForProtege(out);
        // }
        // } catch (ProvenanceOntologyCreationException e) {
        // logger.error("Error writing instances to file", e);
        // } catch (OntologyLoadInstanceDataException e) {
        // logger.error("Error writing instances to file", e);
        // } catch (FileNotFoundException e) {
        // logger.error("Error writing instances to file", e);
        // } catch (IOException e) {
        // logger.error("Error writing instances to file", e);
        // }
    }

    /**
     * @param input
     * @return
     */
    private String versionLSID(String lsid) throws NullPointerException {
        String versionLSID = lsid;
        if (addVersion.equals("true")) {
            versionLSID = versionLSID + ":1";
        }
        return versionLSID;
    }

    private void nameToDataMapper(Map inputDataMap, Map outputDataMap,
            Map inputResultMap, Map outputResultOutputs) {
        for (Iterator i = inputDataMap.keySet().iterator(); i.hasNext();) {
            String inputName = (String) i.next();
            DataThing inputValue = (DataThing) inputDataMap.get(inputName);
            String objectLSID = inputValue.getLSID(inputValue.getDataObject());

            objectLSID = versionLSID(objectLSID);

            if (objectLSID != null && !objectLSID.equals("")) {
                inputResultMap.put(inputName, objectLSID);
            }
        }
        for (Iterator i = outputDataMap.keySet().iterator(); i.hasNext();) {
            String outputName = (String) i.next();
            DataThing outputValue = (DataThing) outputDataMap.get(outputName);
            String objectLSID = outputValue
                    .getLSID(outputValue.getDataObject());

            // add version workaround
            objectLSID = versionLSID(objectLSID);

            if (objectLSID != null && !objectLSID.equals("")) {
                outputResultOutputs.put(outputName, objectLSID);
            }
        }
    }

    private String getWorkflowNameWithoutLSID(String workflowName) {
        try {
            String workflowNameObject = new LSID(workflowName).getObject();
            String workflowNameWithoutLSID = WORKFLOW_NS + workflowNameObject;
            return workflowNameWithoutLSID;
        } catch (MalformedLSIDException e) {
            logger.error("Error while extracting object from the LSID "
                    + workflowName, e);
            return workflowName;
        }
    }

    private boolean isRootWorkflowRun(String workflowRunId) {
        logger.debug("isRootWorkflowRun(String workflowRunId = "
                + workflowRunId + ") where rootWorkflowRunId = "
                + rootWorkflowRunId);
        return workflowRunId.equals(rootWorkflowRunId);
    }

    public Map<String, ProvenanceOntology> getOntologies() {
        return ontologies;
    }
    
    public void workflowDestroyed(WorkflowDestroyedEvent event) {
        event.getWorkflowInstanceID();
        
    }

    public void workflowToBeDestroyed(WorkflowToBeDestroyedEvent event) {
        event.getWorkflowInstance();
        
    }

}