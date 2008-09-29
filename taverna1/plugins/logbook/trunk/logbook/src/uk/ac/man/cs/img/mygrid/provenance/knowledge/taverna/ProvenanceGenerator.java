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
import org.embl.ebi.escience.scufl.enactor.event.WorkflowToBeDestroyedEvent;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.OntologyUpdateException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
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
public class ProvenanceGenerator implements WorkflowEventListener {

    public static final String INPUT_MARKER = "_in_";

    public static final String OUTPUT_MARKER = "_out_";

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

    private static final String NULL_POINTER_IN_RDF_PROVENANCE_GENERATION = "Null pointer in RDF provenance generation";

    private static final String ERROR_UPDATING_ONTOLOGY = "Error updating ontology";

    public static Logger logger = Logger.getLogger(ProvenanceGenerator.class);

    private static ProvenanceGenerator instance = null;

    private UserContext userContext;

    private String addVersion;

    private IDGenerator processRunIdGenerator;

    private MetadataService metadataService;

    private DataService dataService;

    private List<String> errorMessages = new ArrayList<String>();

    // private List<String> locks;

    private Map<Integer, String> iterationRunIds;

    private Map<String, String> workflowNames;

    private Properties configuration;

    private int logLevel;

    /**
     * Singleton constructor.
     * 
     * @return {@link ProvenanceGenerator} instance.
     * @throws LogBookException
     */
    public static ProvenanceGenerator getInstance() throws LogBookException {
        if (instance == null) {
            new ProvenanceGenerator();
        }
        return instance;
    }

    /**
     * Initialises both a metadata service and a dataservice using the
     * configuration properties given by
     * {@link ProvenanceConfigurator#getConfiguration()}.
     * 
     * @throws ProvenanceGeneratorException
     *             if one instance already exists.
     */
    public ProvenanceGenerator() throws LogBookException {
        this(ProvenanceConfigurator.getConfiguration());
    }

    /**
     * Initialises both a metadata service and a dataservice using the
     * <code>configuration</code> properties.
     * 
     * @param configuration
     *            the required {@link Properties}.
     * @throws ProvenanceGeneratorException
     *             if one instance already exists.
     */
    public ProvenanceGenerator(Properties configuration)
            throws ProvenanceGeneratorException {
        if (logger.isDebugEnabled()) {
            logger.debug("ProvenanceGenerator(Properties configuration="
                    + configuration + ") - start");
        }

        setConfiguration(configuration);

        initialise();

        if (instance != null)
            throw new ProvenanceGeneratorException(
                    "One instance already created - use ProvenanceGenerator.getInstance() instead.");
        instance = this;

        if (logger.isDebugEnabled()) {
            logger.debug("ProvenanceGenerator(Properties) - end");
        }
    }

    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }

    public void initialise() throws ProvenanceGeneratorException {
        init();

        addVersion = System.getProperty("taverna.KAVE.versionLSID", "false");
        try {
            metadataService = MetadataServiceFactory
                    .getInstance(this.configuration);
        } catch (MetadataServiceCreationException e) {
            throw new ProvenanceGeneratorException(
                    "ProvenanceGenerator failed to create metadata service.", e);
        }
        processRunIdGenerator = new TimedIDGenerator(PROCESS_RUN_NS);
        // logger.debug("storage mode = " + storage_mode);

        // Properties dataStoreConfiguration = ProvenanceConfigurator
        // .getDataStoreConfiguration();
        try {
            dataService = DataServiceFactory.getInstance(this.configuration);
        } catch (DataServiceException e) {
            logger.warn("Data service could not be started. Data will not "
                    + "be persisted (but provenance metadata will).");
        }

        String logLevelProperty = this.configuration
                .getProperty(ProvenanceConfigurator.LOGBOOK_LEVEL);
        logLevel = LogLevel.toLogLevel(logLevelProperty);
    }

    /**
     * Initialises all the needed synchronised collections.
     * 
     */
    synchronized public void init() {

        // locks = Collections.synchronizedList(new ArrayList<String>());

        iterationRunIds = Collections
                .synchronizedMap(new HashMap<Integer, String>());

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
        if (logLevel == LogLevel.NOTHING) {
            logger.debug("logLevel = " + logLevel
                    + " too low - method will return.");
            return;
        }
        try {
            WorkflowInstance workflowRun = creationEvent.getWorkflowInstance();
            String workflowRunId = getID(workflowRun);
            logger.info("Started workflow " + workflowRunId);
            ProvenanceOntology provenanceOntology = ProvenanceOntologyFactory
                    .getInstance(configuration);

            logger.debug("workflowRunId = " + workflowRunId);

            userContext = workflowRun.getUserContext();

            String workflowName = creationEvent.getDefinitionLSID();
            workflowNames.put(workflowRunId, workflowName);

            ScuflModel workflowModel = creationEvent.getModel();
            String accurateWorkflowName = getAccurateWorkflowName(workflowModel);
            storeWorkflow(accurateWorkflowName, workflowModel);

            logger
                    .debug("workflowCreated(WorkflowCreationEvent) -  : ScuflModel workflowModel="
                            + workflowModel);

            String personLSID = userContext.getPersonLSID();
            if (personLSID == null) {
                personLSID = configuration.getProperty(
                        ProvenanceConfigurator.EXPERIMENTER_KEY,
                        ProvenanceConfigurator.DEFAULT_EXPERIMENTER);
            }
            configuration
                    .setProperty(MetadataService.GRAPH_CREATOR, personLSID);
            String organizationLSID = userContext.getOrganizationLSID();
            if (organizationLSID == null) {
                organizationLSID = configuration.getProperty(
                        ProvenanceConfigurator.ORGANISATION_KEY,
                        ProvenanceConfigurator.DEFAULT_ORGANIZATION);
            }

            WorkflowDescription description = workflowModel.getDescription();
            String title = description.getTitle();
            String author = description.getAuthor();
            String freeTextDescription = description.getText();
            if (title != null)
                if (!title.equals(""))
                    provenanceOntology.addWorkflowTitle(accurateWorkflowName,
                            title);
            if (author != null)
                if (!author.equals(""))
                    provenanceOntology.addWorkflowAuthor(accurateWorkflowName,
                            author);
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
            logger
                    .debug("workflowCreated(WorkflowCreationEvent) - updating instance data");
            metadataService.updateInstanceData(provenanceOntology,
                    workflowRunId);
            logger
                    .debug("workflowCreated(WorkflowCreationEvent) - updated instance data");
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        } catch (ProvenanceOntologyCreationException e) {
            logger.error("Error creating ontology", e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        } catch (MetadataServiceException e) {
            logger.error(e);
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

        try {
            WorkflowInstance workflowInstance = failureEvent
                    .getWorkflowInstance();
            String currentWorkflowRunId = getID(workflowInstance);
            ProvenanceOntology provenanceOntology = ProvenanceOntologyFactory
                    .getInstance(configuration);

            logger.info("Workflow id at end = " + currentWorkflowRunId);
            endFailedWorkflowProvenance(currentWorkflowRunId,
                    provenanceOntology);
            logger.debug("rdf plugin workflow completed but workflow failed,");
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        } catch (ProvenanceOntologyCreationException e) {
            logger.error(e);
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
        if (logLevel == LogLevel.NOTHING) {
            logger.debug("logLevel = " + logLevel
                    + " too low - method will return.");
            return;
        }
        try {
            WorkflowInstance workflowInstance = event.getWorkflowInstance();
            String currentWorkflowRunId = getID(workflowInstance);
            ProvenanceOntology provenanceOntology = ProvenanceOntologyFactory
                    .getInstance(configuration);
            logger.info("Workflow id at end = " + currentWorkflowRunId);
            logger
                    .debug("workflowCompleted(WorkflowCompletionEvent) -  : WorkflowInstance workflowInstance="
                            + workflowInstance);
            Map outputs = workflowInstance.getOutput();
            addMimeTypes(outputs, workflowInstance.getWorkflowModel()
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
        } catch (NullPointerException e) {
            logger.error(e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        } catch (ProvenanceOntologyCreationException e) {
            logger.error(e);
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

        if (logLevel < LogLevel.PROCESS) {
            logger.debug("logLevel = " + logLevel
                    + " too low - method will return.");
            return;
        }

        try {
            WorkflowInstance workflowInstance = event.getWorkflowInstance();
            Processor processor = event.getProcessor();
            Map inputs = event.getInputMap();
            Map outputs = event.getOutputMap();
            if (event.isIterating()) {
                if (logLevel < LogLevel.ITERATION) {
                    logger.debug("logLevel = " + logLevel
                            + " too low - method will return.");
                    return;
                }
                String processRunId = successfulIterationCompletion(
                        workflowInstance, inputs, outputs, processor);
                putIterationRunId(event.hashCode(), processRunId);
            } else
                successfulProcessCompletion(workflowInstance, inputs, outputs,
                        processor);
            store(inputs);
            store(outputs);
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        } catch (MetadataServiceException e) {
            logger.error(e);
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
        if (logLevel < LogLevel.PROCESS) {
            logger.debug("logLevel = " + logLevel
                    + " too low - method will return.");
            return;
        }

        try {
            WorkflowInstance workflowInstance = event.getWorkflowInstance();
            Processor processor = event.getProcessor();
            Map inputs = event.getOverallInputs();
            Map outputs = event.getOverallOutputs();
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
        } catch (MetadataServiceException e) {
            logger.error(e);
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
        } catch (MetadataServiceException e) {
            logger.error(e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("processFailed(ProcessFailureEvent) - end");
        }
    }

    public void nestedWorkflowCreated(NestedWorkflowCreationEvent nestedEvent) {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("nestedWorkflowCreated(NestedWorkflowCreationEvent nestedEvent="
                            + nestedEvent + ") - start");
        }

        if (logLevel < LogLevel.NESTED) {
            logger.debug("logLevel = " + logLevel
                    + " too low - method will return.");
            return;
        }

        try {
            WorkflowInstance nestedWorkflowInstance = nestedEvent
                    .getNestedWorkflowInstance();
            WorkflowInstance parentWorkflowInstance = nestedEvent
                    .getParentWorkflowInstance();
            Map inputs = nestedEvent.getInputs();
            String nestedWorkflowRunId = getID(nestedWorkflowInstance);
            String parentWorkflowRunId = getID(parentWorkflowInstance);
            logger.info("Started provenance for nested workflow "
                    + nestedWorkflowRunId);
            ProvenanceOntology provenanceOntology = ProvenanceOntologyFactory
                    .getInstance(configuration);
            provenanceOntology.addNestedWorkflow(parentWorkflowRunId,
                    nestedWorkflowRunId);

            provenanceOntology.addNestedWorkflowStartTime(nestedWorkflowRunId);
            ScuflModel workflowModel = nestedWorkflowInstance
                    .getWorkflowModel();
            String workflowName = workflowModel.getDescription().getLSID();
            workflowNames.put(nestedWorkflowRunId, workflowName);

            String accurateWorkflowName = getAccurateWorkflowName(workflowModel);
            storeWorkflow(accurateWorkflowName, workflowModel);

            WorkflowDescription description = workflowModel.getDescription();
            String title = description.getTitle();
            String author = description.getAuthor();
            String freeTextDescription = description.getText();
            if (title != null)
                if (!title.equals(""))
                    provenanceOntology.addWorkflowTitle(accurateWorkflowName,
                            title);
            if (author != null)
                if (!author.equals(""))
                    provenanceOntology.addWorkflowAuthor(accurateWorkflowName,
                            author);
            if (freeTextDescription != null)
                if (!freeTextDescription.equals(""))
                    provenanceOntology.addWorkflowDescription(
                            accurateWorkflowName, freeTextDescription);
            provenanceOntology.addWorkflowInitialLSID(accurateWorkflowName,
                    workflowName);
            provenanceOntology.addRunsWorkflow(nestedWorkflowRunId,
                    accurateWorkflowName);

            addMimeTypes(inputs, workflowModel.getWorkflowSourcePorts(),
                    provenanceOntology);
            logger.debug(inputs);
            String workflowNameWithoutLSID = getWorkflowNameWithoutLSID(accurateWorkflowName);
            addWorkflowInputData(inputs, nestedWorkflowRunId,
                    workflowNameWithoutLSID, provenanceOntology);
            store(inputs);
            metadataService.updateInstanceData(provenanceOntology,
                    nestedWorkflowRunId);
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        } catch (ProvenanceOntologyCreationException e) {
            logger.error("Error creating ontology", e);
        } catch (OntologyUpdateException e) {
            logger.error(ERROR_UPDATING_ONTOLOGY, e);
        } catch (MetadataServiceException e) {
            logger.error(e);
        }

        logger
                .debug("nestedWorkflowCreated(NestedWorkflowCreationEvent) - end");
    }

    public void nestedWorkflowCompleted(
            NestedWorkflowCompletionEvent nestedEvent) {
        if (logLevel < LogLevel.NESTED) {
            logger.debug("logLevel = " + logLevel
                    + " too low - method will return.");
            return;
        }
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
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
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
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
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

        try {
            WorkflowInstance workflowInstance = event.getWorkflowInstance();
            String currentWorkflowRunId = getID(workflowInstance);
            try {
                ProvenanceOntology provenanceOntology = ProvenanceOntologyFactory
                        .getInstance(configuration);
                String originalLSID = event.getOriginalLSID();
                String[] collectionLSIDs = event.getCollectionLSIDs();
                for (int i = 0; i < collectionLSIDs.length; i++) {
                    provenanceOntology.addDataWrappedInto(originalLSID,
                            collectionLSIDs[i]);
                }
                // FIXME: store quads instead of ontology
                metadataService.updateInstanceData(provenanceOntology,
                        currentWorkflowRunId);
            } catch (NullPointerException e) {
                logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
            } catch (OntologyUpdateException e) {
                logger.error(ERROR_UPDATING_ONTOLOGY, e);
            } catch (ProvenanceOntologyCreationException e) {
                logger.error(e);
            } catch (MetadataServiceException e) {
                logger.error(e);
            }
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
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

        try {
            WorkflowInstance workflowInstance = event.getWorkflowInstance();
            String currentWorkflowRunId = getID(workflowInstance);
            try {
                ProvenanceOntology provenanceOntology = ProvenanceOntologyFactory
                        .getInstance(configuration);
                DataThing dataThing = event.getDataThing();
                String lsid = dataThing.getLSID(dataThing.getDataObject());
                if (!lsid.equals("")) {
                    String versionLSID = versionLSID(lsid);
                    addCollection(dataThing, versionLSID, provenanceOntology);
                    String oldDataThingID = event.getOldDataThingID();
                    provenanceOntology.addChangedData(versionLSID,
                            versionLSID(oldDataThingID));
                }
                // FIXME: store quads instead of ontology
                metadataService.updateInstanceData(provenanceOntology,
                        currentWorkflowRunId);
            } catch (NullPointerException e) {
                logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
            } catch (OntologyUpdateException e) {
                logger.error(ERROR_UPDATING_ONTOLOGY, e);
            } catch (ProvenanceOntologyCreationException e) {
                logger.error(e);
            } catch (MetadataServiceException e) {
                logger.error(e);
            }
        } catch (NullPointerException e) {
            logger.error(NULL_POINTER_IN_RDF_PROVENANCE_GENERATION, e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("dataChanged(UserChangedDataEvent) - end");
        }
    }

    public void workflowDestroyed(WorkflowDestroyedEvent event) {
        String workflowInstanceID = event.getWorkflowInstanceID();
        logger.debug("Workflow " + workflowInstanceID + " destroyed");
    }

    public void workflowToBeDestroyed(WorkflowToBeDestroyedEvent event) {
        WorkflowInstance workflowInstance = event.getWorkflowInstance();
        String workflowInstanceID = workflowInstance.getID();
        logger.debug("Workflow " + workflowInstanceID + " to be destroyed");
    }

    public MetadataService getRdfRepository() {
        return metadataService;
    }

    public DataService getDataService() {
        return dataService;
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
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

    synchronized String getIterationRunId(int eventHashCode) {
        return iterationRunIds.get(eventHashCode);
    }

    synchronized void putIterationRunId(int eventHashCode, String iterationRunId) {
        iterationRunIds.put(eventHashCode, iterationRunId);
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

    String getAccurateWorkflowName(ScuflModel workflowModel) {
        LSIDProvider authority = DataThing.SYSTEM_DEFAULT_LSID_PROVIDER;
        String lsid = authority.getID(LSIDProvider.WFDEFINITION);
        return lsid;
    }

    String getID(WorkflowInstance workflowInstance) {
        if (logger.isDebugEnabled()) {
            logger.debug("getID(workflowInstance = " + workflowInstance + ")");
        }
        String id = workflowInstance.getID();
        logger.debug("id = " + id);
        return id;
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
            logger.debug("provenanceOntology = " + provenanceOntology);
            if (!isSuccess)
                provenanceOntology.addFailedWorkflowRun(workflowRunId);
            provenanceOntology.addWorkflowEndTime(workflowRunId);
            metadataService.updateInstanceData(provenanceOntology,
                    workflowRunId);

        } catch (MetadataServiceException e) {
            logger.error("Error while persisting instance data", e);
        }
        logger.debug("endWorkflowProvenance(String) - end");
    }

    void endNestedWorkflow(WorkflowInstance nestedWorkflowInstance,
            WorkflowInstance parentWorkflowInstance, Processor processor,
            Map inputs, Map outputs, int type) throws OntologyUpdateException {
        String processorName = PROCESS_NS + processor.getName();
        String processRunId = processRunIdGenerator.getNextID();
        String processorClassName = processor.getClass().getName();
        String parentWorkflowRunId = parentWorkflowInstance.getID();
        String nestedWorkflowRunId = nestedWorkflowInstance.getID();
        try {
            ProvenanceOntology provenanceOntology = ProvenanceOntologyFactory
                    .getInstance(configuration);
            provenanceOntology.addProcessIsNestedWorkflow(processRunId,
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
            metadataService.updateInstanceData(provenanceOntology,
                    parentWorkflowRunId);
        } catch (ProvenanceOntologyCreationException e1) {
            logger.error(e1);
        } catch (MetadataServiceException e) {
            logger.error(e);
        }

        try {
            ProvenanceOntology nestedRunOntology = ProvenanceOntologyFactory
                    .getInstance(configuration);
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
            metadataService.updateInstanceData(nestedRunOntology,
                    nestedWorkflowRunId);
        } catch (OntologyUpdateException e) {
            logger.error(e.getMessage(), e);
        } catch (ProvenanceOntologyCreationException e) {
            logger.error(e);
        } catch (MetadataServiceException e) {
            logger.error(e);
        }
    }

    String successfulProcessCompletion(WorkflowInstance workflowInstance,
            Map inputs, Map outputs, Processor processor)
            throws OntologyUpdateException, MetadataServiceException {
        return processCompletion(workflowInstance, processor, inputs, outputs,
                SUCCESS, null, null);
    }

    String successfulProcessCompletionWithIterations(
            WorkflowInstance workflowInstance, Map inputs, Map outputs,
            Processor processor,
            List<ProcessCompletionEvent> associatedCompletionEvents)
            throws OntologyUpdateException, MetadataServiceException {
        String processRunId = processCompletion(workflowInstance, processor,
                inputs, outputs, COMPLETED_ITERATIONS, null,
                associatedCompletionEvents);
        return processRunId;
    }

    String successfulIterationCompletion(WorkflowInstance workflowInstance,
            Map inputs, Map outputs, Processor processor)
            throws OntologyUpdateException, MetadataServiceException {
        return processCompletion(workflowInstance, processor, inputs, outputs,
                ITERATION, null, null);
    }

    String failedProcessCompletion(WorkflowInstance workflowInstance,
            Processor processor, Map inputs, Exception cause)
            throws OntologyUpdateException, MetadataServiceException {
        return processCompletion(workflowInstance, processor, inputs, null,
                FAILED, cause, null);
    }

    String processCompletion(WorkflowInstance workflowInstance,
            Processor processor, Map inputs, Map outputs, int type,
            Exception failureCause,
            List<ProcessCompletionEvent> associatedCompletionEvents)
            throws OntologyUpdateException, MetadataServiceException {
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
        logger.debug("Reached " + processorName + " with run ID = "
                + currentWorkflowRunId);
        metadataService.addQuad(currentWorkflowRunId, currentWorkflowRunId,
                ProvenanceVocab.EXECUTED_PROCESS_RUN.getURI(), processRunId);
        ProvenanceOntology provenanceOntology;
        try {
            provenanceOntology = ProvenanceOntologyFactory
                    .getInstance(configuration);
        } catch (ProvenanceOntologyCreationException e1) {
            logger.error(e1);
            return processRunId;
        }
        provenanceOntology.addProcessClassName(processorName,
                processorClassName);

        try {
            Properties processorProperties = processor.getProperties();
            if (processorProperties != null)
                provenanceOntology.addProcessProperties(processorName,
                        processorProperties);
        } catch (NullPointerException e) {
            logger.warn("Null pointer when getting properties of processor", e);
        }

        provenanceOntology.addProcessEndTime(processRunId);
        provenanceOntology.addRunsProcess(processRunId, processorName);

        addInputData(inputs, processRunId, processorName, provenanceOntology);

        InputPort[] inputPorts = processor.getInputPorts();
        boolean hasInputs = inputs != null;
        if (hasInputs && inputPorts != null)
            addMimeTypes(inputs, inputPorts, provenanceOntology);

        switch (type) {
        case FAILED:
            String failureMessage = "";
            if (failureCause != null)
                failureMessage = failureCause.getMessage();
            provenanceOntology.addFailedProcessRun(processRunId, processorName,
                    failureMessage);
            metadataService
                    .updateInstanceData(provenanceOntology, processRunId);
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
                String iterationRunId = getIterationRunId(event.hashCode());
                iterations.add(iterationRunId);
            }
            provenanceOntology.addProcessCompletedWithIterations(processRunId,
                    iterations);
            break;
        default:
            break;
        }

        addOutputData(outputs, processRunId, processorName, provenanceOntology);
        OutputPort[] outputPorts = processor.getOutputPorts();
        boolean hasOutputs = outputs != null;
        if (hasOutputs && outputPorts != null)
            addMimeTypes(outputs, outputPorts, provenanceOntology);

        if (hasInputs && hasOutputs) {
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
        }

        if (logger.isDebugEnabled()) {
            logger
                    .debug("processCompletionProvenance(Processor, Map, Map, boolean) - end");
        }

        metadataService.updateInstanceData(provenanceOntology, processRunId);
        return processRunId;
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
                            workflowRunId, createInputDataURN(
                                    workflowNameWithoutLSID, name),
                            syntacticType);
                else
                    provenanceOntology.addWorkflowOutputData(versionLsid,
                            workflowRunId, createOutputDataURN(
                                    workflowNameWithoutLSID, name),
                            syntacticType);
                addCollection(dataThing, versionLsid, provenanceOntology);
            }
        }

    }

    static public String createInputDataURN(String prefix, String name) {
        return prefix + INPUT_MARKER + name;
    }

    static public String getInputDataFromURN(String dataURN) {
        String[] split = dataURN.split(INPUT_MARKER);
        return split[1];
    }

    static public String createOutputDataURN(String prefix, String name) {
        return prefix + OUTPUT_MARKER + name;
    }
    
    static public String getOutputDataFromURN(String dataURN) {
        String[] split = dataURN.split(OUTPUT_MARKER );
        return split[1];
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
                            processorRunId,
                            processorName + INPUT_MARKER + name, syntacticType);
                else
                    provenanceOntology.addOutputData(versionLSID,
                            processorRunId, processorName + OUTPUT_MARKER
                                    + name, syntacticType);
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
            String message = ex.getMessage();
            if (message
                    .startsWith("java.sql.SQLException: Invalid argument value,  message from server: \"Duplicate entry"))
                logger.info("Workflow " + lsid + " already in store");
            else
                logger.error("Could not store workflow " + lsid, ex);
        }
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

}