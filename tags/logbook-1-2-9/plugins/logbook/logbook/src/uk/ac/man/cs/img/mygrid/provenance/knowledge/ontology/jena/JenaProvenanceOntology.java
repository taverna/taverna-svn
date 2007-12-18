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
 * Filename           $RCSfile: JenaProvenanceOntology.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:09 $
 *               by   $Author: stain $
 * Created on 24-Jun-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.InstanceRetrievalException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.OntologyUpdateException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.IDGenerator;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.TimedIDGenerator;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.util.DataName;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.util.DataObject;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Jena implementation of
 * {@link uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology}.
 * 
 * @author dturi
 * @version $Id: JenaProvenanceOntology.java,v 1.13 2006/05/26 10:21:47 turid
 *          Exp $
 */
public class JenaProvenanceOntology extends JenaOntologyAdapter implements
        ProvenanceOntology {

    public static final String DEFAULT_ONTOLOGY_URL = "http://www.cs.man.ac.uk/~dturi/ontologies/provenance.owl";

    /**
     * Logger for this class
     */
    public static final Logger logger = Logger
            .getLogger(JenaProvenanceOntology.class);

    private static final String I3C_CONTENT = "urn:lsid:i3c.org:types:content";

    private static final String I3C_XML_FORMAT = "urn:lsid:i3c.org:formats:xml";

    private final IDGenerator processPropertyIDGenerator = new TimedIDGenerator(
            PROCESS_PROPERTY_NS);

    public String getNamespace() {
        return ProvenanceOntology.PROVENANCE_NS;
    }

    public String getOntologyPrefix() {
        return "provenance";
    }

    public String getOntologyURL() {
        return this.getClass().getClassLoader().getResource("provenance.owl")
                .toString();
    }

    /**
     * Creates an in-memory copy of the provenance ontology at
     * {@link #PROVENANCE_OWL_URL}. It contains no instance data. To populate
     * this ontology use the <code>addXXX()</code> methods in this class.
     */
    public JenaProvenanceOntology() {
        super();
    }

    /**
     * Creates a version of the provenance ontology schema one can reason about
     * and populated with the instance data at <code>instanceDataURI</code>.
     * 
     * @param instanceDataURI
     *            the {@link URI}of the instance data.
     */
    public JenaProvenanceOntology(URI instanceDataURI) {
        super(instanceDataURI);
    }

    /**
     * Creates an in-memory copy of the provenance ontology at
     * {@link #PROVENANCE_OWL_URL}and then adds <code>model</code> to it.
     * 
     * @param rdf
     *            a {@link Model}.
     */
    public JenaProvenanceOntology(Model rdf) {
        super(rdf);
    }

    /**
     * @param instanceData
     */
    public JenaProvenanceOntology(String instanceData) {
        super(instanceData);
    }

    /**
     * Creates an in-memory copy of the provenance ontology at
     * {@link #PROVENANCE_OWL_URL}and then adds each of the <code>models</code>
     * to it.
     * 
     * @param models
     *            a Collection of {@link Model}.
     */
    public JenaProvenanceOntology(Collection models) {
        super(models);
    }

    // public boolean addLabel(String Label){
    //    	
    //    
    // model.createIndividual(Label , ProvenanceVocab.LABEL);
    //    	
    // return true;
    // }
    //    
    //    
    // public void addHasLabel(String experimenter , String Label){
    //		
    // model.add(model.createIndividual(experimenter,
    // ProvenanceVocab.EXPERIMENTER), ProvenanceVocab.HAS_LABEL,
    // model.createIndividual(Label ,
    // ProvenanceVocab.LABEL));
    //    	
    //    	
    // }
    //    
    //    
    // public void addLabels(String Label,String workflowRun){
    //    	
    // model.add(model.createIndividual(Label ,
    // ProvenanceVocab.LABEL), ProvenanceVocab.LABELS,
    // model.createIndividual(workflowRun,ProvenanceVocab.WORKFLOW_RUN));
    //    	
    //    	
    // }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addBelongsTo(java.lang.String,
     *      java.lang.String)
     */

    public void addBelongsTo(String experimenter, String organization) {
        model.add(model.createIndividual(experimenter,
                ProvenanceVocab.EXPERIMENTER), ProvenanceVocab.BELONGS_TO,
                model.createIndividual(organization,
                        ProvenanceVocab.ORGANIZATION));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addLaunchedBy(java.lang.String,
     *      java.lang.String)
     */
    public void addLaunchedBy(String workflowRun, String experimenter) {
        model.add(model.createIndividual(workflowRun,
                ProvenanceVocab.WORKFLOW_RUN), ProvenanceVocab.LAUNCHED_BY,
                model.createIndividual(experimenter,
                        ProvenanceVocab.EXPERIMENTER));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addExecutedProcessRun(java.lang.String,
     *      java.lang.String)
     */
    public void addExecutedProcessRun(String workflowRun, String processRun) {
        model.add(model.createIndividual(workflowRun,
                ProvenanceVocab.WORKFLOW_RUN),
                ProvenanceVocab.EXECUTED_PROCESS_RUN, model.createIndividual(
                        processRun, ProvenanceVocab.PROCESS_RUN));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addRunsWorkflow(java.lang.String,
     *      java.lang.String)
     */
    public void addRunsWorkflow(String workflowRun, String workflow) {
        model.add(model.createIndividual(workflowRun,
                ProvenanceVocab.WORKFLOW_RUN), ProvenanceVocab.RUNS_WORKFLOW,
                model.createIndividual(workflow, ProvenanceVocab.WORKFLOW));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addFailedWorkflowRun(java.lang.String)
     */
    public void addFailedWorkflowRun(String workflowRun) {
        model
                .createIndividual(workflowRun,
                        ProvenanceVocab.FAILED_WORKFLOW_RUN);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addRunsProcess(java.lang.String,
     *      java.lang.String)
     */
    public void addRunsProcess(String processRun, String process) {
        Resource processRunType = ProvenanceVocab.PROCESS_RUN;
        Individual processRunIndividual = model.createIndividual(processRun,
                processRunType);
        model.add(processRunIndividual, ProvenanceVocab.RUNS_PROCESS, model
                .createIndividual(process, ProvenanceVocab.PROCESS));
    }

    /**
     * Relates the two arguments through <code>runsProcess</code>.
     * 
     * @param processRun
     *            an {@link Individual}.
     * @param process
     *            an LSID.
     */
    void addRunsProcess(Individual processRun, String process) {

        model.add(processRun, ProvenanceVocab.RUNS_PROCESS, model
                .createIndividual(process, ProvenanceVocab.PROCESS));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addWorkflowStartTime(java.lang.String)
     */
    public void addWorkflowStartTime(String workflowRun) {
        addStartTime(workflowRun, ProvenanceVocab.WORKFLOW_RUN);
    }

    public void addNestedWorkflowStartTime(String nestedWorkflowRun)
            throws OntologyUpdateException {
        addStartTime(nestedWorkflowRun, ProvenanceVocab.NESTED_WORKFLOW_RUN);
    }

    /**
     * Assigns the current time to <code>runun</code> through the property
     * <code>startTime</code>.
     * 
     * @param run
     *            an LSID.
     * @param runType
     */
    void addStartTime(String run, Resource runType) {
        //int savings = TimeZone.getDefault().getDSTSavings();
        Calendar date = Calendar.getInstance(TimeZone.getDefault(), Locale
                .getDefault());
        //date.add(Calendar.MILLISECOND, savings);
        model.add(model.createIndividual(run, runType),
                ProvenanceVocab.START_TIME, toTimeLiteral(date));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addWorkflowEndTime(java.lang.String)
     */
    public void addWorkflowEndTime(String workflowRun) {
        if (logger.isDebugEnabled()) {
            logger.debug("addWorkflowEndTime(String workflowRun = "
                    + workflowRun + ") - start");
        }

        addEndTime(workflowRun, ProvenanceVocab.WORKFLOW_RUN);

        if (logger.isDebugEnabled()) {
            logger.debug("addWorkflowEndTime(String) - end");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addWorkflowOutputData(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public void addWorkflowOutputData(String outputData, String workflowRunId,
            String name, String syntacticType) {
        addWorkflowData(outputData, workflowRunId, name, syntacticType, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addWorkflowInputData(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public void addWorkflowInputData(String inputData, String workflowRunId,
            String name, String syntacticType) {
        addWorkflowData(inputData, workflowRunId, name, syntacticType, true);
    }

    void addWorkflowData(String data, String workflowRunId, String name,
            String syntacticType, boolean isInput) {
        Resource dataObject;
        if (isCollection(syntacticType))
            dataObject = ProvenanceVocab.DATA_COLLECTION;
        else
            dataObject = ProvenanceVocab.ATOMIC_DATA;

        Individual dataIndividual = model.createIndividual(data, dataObject);
        Individual dataName;
        if (isInput) {
            dataName = model.createIndividual(name,
                    ProvenanceVocab.INPUT_DATA_NAME);
            model.add(dataIndividual, ProvenanceVocab.INPUT_DATA_HAS_NAME,
                    dataName);
            model.add(model.createIndividual(workflowRunId,
                    ProvenanceVocab.WORKFLOW_RUN),
                    ProvenanceVocab.WORKFLOW_INPUT, dataIndividual);
        } else {
            dataName = model.createIndividual(name,
                    ProvenanceVocab.OUTPUT_DATA_NAME);
            model.add(dataIndividual, ProvenanceVocab.OUTPUT_DATA_HAS_NAME,
                    dataName);
            model.add(model.createIndividual(workflowRunId,
                    ProvenanceVocab.WORKFLOW_RUN),
                    ProvenanceVocab.WORKFLOW_OUTPUT, dataIndividual);
        }
        model.add(dataName, ProvenanceVocab.DATA_SYNTACTIC_TYPE, model
                .createTypedLiteral(syntacticType));
    }

    public void addWorkflowInitialLSID(String workflowLSID,
            String workflowInitialLSID) {
        Individual workflowIndividual = model.createIndividual(workflowLSID,
                ProvenanceVocab.WORKFLOW);
        model.add(workflowIndividual, ProvenanceVocab.WORKFLOW_INITIAL_LSID,
                model.createTypedLiteral(workflowInitialLSID));

    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addWorkflowDescription(java.lang.String,
     *      java.lang.String)
     */
    public void addWorkflowDescription(String workflow, String description) {
        Individual workflowIndividual = model.createIndividual(workflow,
                ProvenanceVocab.WORKFLOW);
        model.add(workflowIndividual, ProvenanceVocab.WORKFLOW_DESCRIPTION,
                model.createTypedLiteral(description));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addWorkflowTitle(java.lang.String,
     *      java.lang.String)
     */
    public void addWorkflowTitle(String workflow, String title) {
        Individual workflowIndividual = model.createIndividual(workflow,
                ProvenanceVocab.WORKFLOW);
        model.add(workflowIndividual, ProvenanceVocab.WORKFLOW_TITLE, model
                .createTypedLiteral(title));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addWorkflowAuthor(java.lang.String,
     *      java.lang.String)
     */
    public void addWorkflowAuthor(String workflow, String author) {
        Individual workflowIndividual = model.createIndividual(workflow,
                ProvenanceVocab.WORKFLOW);
        model.add(workflowIndividual, ProvenanceVocab.WORKFLOW_AUTHOR, model
                .createTypedLiteral(author));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addProcessEndTime(java.lang.String)
     */
    public void addProcessEndTime(String processRun) {
        addEndTime(processRun, ProvenanceVocab.PROCESS_RUN);
    }

    /**
     * Assigns the current time to <code>processRun</code> through the
     * property <code>endTime</code>.
     * 
     * @param processRun
     *            an {@link Individual}.
     */
    public void addProcessEndTime(Individual processRun) {
        Property endTime = ProvenanceVocab.END_TIME;
        model.add(processRun, endTime, toTimeLiteral(Calendar.getInstance()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addFailedProcessRun(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void addFailedProcessRun(String processRun, String processor,
            String cause) {
        if (logger.isDebugEnabled()) {
            logger.debug("addFailedProcessRun(String processRun=" + processRun
                    + ", String processor=" + processor + ") - start");
        }

        Individual processRunIndividual = model.createIndividual(processRun,
                ProvenanceVocab.FAILED_PROCESS_RUN);
        addProcessEndTime(processRunIndividual);
        addRunsProcess(processRunIndividual, processor);
        addFailureCause(processRunIndividual, cause);
        if (logger.isDebugEnabled()) {
            logger.debug("addFailedProcessRun(String, String) - end");
        }
    }

    public void addFailureCause(Individual processRunIndividual, String cause) {
        model.add(processRunIndividual, ProvenanceVocab.CAUSE, model
                .createTypedLiteral(cause));

    }

    /**
     * Assigns the current time to <code>run</code> through the property
     * <code>endTime</code>.
     * 
     * @param run
     *            an LSID.
     * @param runType
     */
    void addEndTime(String run, Resource runType) {
        //int savings = TimeZone.getDefault().getDSTSavings();
        Calendar date = Calendar.getInstance(TimeZone.getDefault(), Locale
                .getDefault());
        //date.add(Calendar.MILLISECOND, savings);
        model.add(model.createIndividual(run, runType),
                ProvenanceVocab.END_TIME, toTimeLiteral(date));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addDerivedFrom(java.lang.String,
     *      java.lang.String)
     */
    public void addDerivedFrom(String input, String output) {
        model.add(model.createIndividual(output, ProvenanceVocab.DATA_OBJECT),
                ProvenanceVocab.DATA_DERIVED_FROM, model.createIndividual(
                        input, ProvenanceVocab.DATA_OBJECT));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addInputData(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public void addInputData(String inputData, String processorRunId,
            String name, String syntacticType) {
        addData(inputData, processorRunId, name, syntacticType, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addOutputData(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String)
     */
    public void addOutputData(String outputData, String processorRunId,
            String name, String syntacticType) {
        addData(outputData, processorRunId, name, syntacticType, false);
    }

    void addData(String data, String processorRunId, String name,
            String syntacticType, boolean isInput) {
        Resource dataObject;
        if (isCollection(syntacticType))
            dataObject = ProvenanceVocab.DATA_COLLECTION;
        else
            dataObject = ProvenanceVocab.ATOMIC_DATA;

        Individual dataIndividual = model.createIndividual(data, dataObject);
        Individual dataName;
        if (isInput) {
            dataName = model.createIndividual(name,
                    ProvenanceVocab.INPUT_DATA_NAME);
            model.add(dataIndividual, ProvenanceVocab.INPUT_DATA_HAS_NAME,
                    dataName);

            model.add(model.createIndividual(processorRunId,
                    ProvenanceVocab.PROCESS_RUN),
                    ProvenanceVocab.PROCESS_INPUT, dataIndividual);
        } else {
            dataName = model.createIndividual(name,
                    ProvenanceVocab.OUTPUT_DATA_NAME);
            model.add(dataIndividual, ProvenanceVocab.OUTPUT_DATA_HAS_NAME,
                    dataName);
            model.add(model.createIndividual(processorRunId,
                    ProvenanceVocab.PROCESS_RUN),
                    ProvenanceVocab.PROCESS_OUTPUT, dataIndividual);
        }
        model.add(dataName, ProvenanceVocab.DATA_SYNTACTIC_TYPE, model
                .createTypedLiteral(syntacticType));
        // addI3CInfo(data); //removed because it caused problems in protege
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addContainsData(java.lang.String,
     *      java.lang.String)
     */
    public void addContainsData(String dataLSID, String dataChildLSID) {
        model.add(model.createIndividual(dataLSID,
                ProvenanceVocab.DATA_COLLECTION),
                ProvenanceVocab.CONTAINS_DATA, model.createIndividual(
                        dataChildLSID, ProvenanceVocab.DATA_OBJECT));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addDataWrappedInto(java.lang.String,
     *      java.lang.String)
     */
    public void addDataWrappedInto(String dataLSID, String dataWrapperLSID) {
        model.add(
                model.createIndividual(dataLSID, ProvenanceVocab.ATOMIC_DATA),
                ProvenanceVocab.DATA_WRAPPED_INTO, model.createIndividual(
                        dataWrapperLSID, ProvenanceVocab.DATA_COLLECTION));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addChangedData(java.lang.String,
     *      java.lang.String)
     */
    public void addChangedData(String dataLSID, String oldDataLSID) {
        model.add(model.createIndividual(dataLSID,
                ProvenanceVocab.CHANGED_DATA_OBJECT),
                ProvenanceVocab.OLD_DATA_OBJECT, model.createIndividual(
                        oldDataLSID, ProvenanceVocab.DATA_OBJECT));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addMimeType(java.lang.String,
     *      java.lang.String)
     */
    public void addMimeType(String name, String mimeType) {
        model.add(model.createIndividual(name, ProvenanceVocab.DATA_NAME),
                ProvenanceVocab.MIME_TYPE, model.createTypedLiteral(mimeType));

    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addProcessProperties(java.lang.String,
     *      java.util.Properties)
     */
    public void addProcessProperties(String processName, Properties properties) {
        Individual process = model.createIndividual(processName,
                ProvenanceVocab.PROCESS);
        Set keys = properties.keySet();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            String key = (String) iter.next();
            Individual nextProperty = model.createIndividual(
                    processPropertyIDGenerator.getNextID(),
                    ProvenanceVocab.PROCESS_PROPERTY);
            model.add(nextProperty, ProvenanceVocab.KEY, model
                    .createTypedLiteral(key));
            model.add(nextProperty, ProvenanceVocab.VALUE, model
                    .createTypedLiteral(properties.getProperty(key)));
            model.add(process, ProvenanceVocab.HAS_PROPERTY, nextProperty);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#addProcessClassName(java.lang.String,
     *      java.lang.String)
     */
    public void addProcessClassName(String processName, String className) {
        Individual process = model.createIndividual(processName,
                ProvenanceVocab.PROCESS);
        model.add(process, ProvenanceVocab.CLASS_NAME, model
                .createTypedLiteral(className));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology#addProcessCompletedWithIterations(java.lang.String)
     */
    public void addProcessCompletedWithIterations(String processRunId,
            Set<String> iterations) {
        Individual process = model.createIndividual(processRunId,
                ProvenanceVocab.PROCESS_RUN_WITH_ITERATIONS);
        for (String iterationRun : iterations)
            model.add(process, ProvenanceVocab.ITERATION, model
                    .createIndividual(iterationRun,
                            ProvenanceVocab.PROCESS_ITERATION));
        model.add(process, ProvenanceVocab.NUMBER_OF_ITERATIONS, model
                .createTypedLiteral(iterations.size()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology#addProcessIteration(java.lang.String)
     */
    public void addProcessIteration(String processRunId) {
        model.createIndividual(processRunId, ProvenanceVocab.PROCESS_ITERATION);
    }

    public void addNestedWorkflow(String parentWorkflowId,
            String nestedWorkflowId) {
        model.add(model.createIndividual(parentWorkflowId,
                ProvenanceVocab.WORKFLOW_RUN), ProvenanceVocab.NESTED_RUN,
                model.createIndividual(nestedWorkflowId,
                        ProvenanceVocab.NESTED_WORKFLOW_RUN));
    }

    public void addFailedNestedWorkflow(String parentWorkflowId,
            String nestedWorkflowId) {
        model.add(model.createIndividual(parentWorkflowId,
                ProvenanceVocab.WORKFLOW_RUN), ProvenanceVocab.NESTED_RUN,
                model.createIndividual(nestedWorkflowId,
                        ProvenanceVocab.FAILED_NESTED_WORKFLOW_RUN));
    }

    public void setToNestedWorkflow(String nestedWorkflowId,
            String parentWorkflowId) {
        addNestedWorkflow(parentWorkflowId, nestedWorkflowId);
        Individual nestedRunIndividual = model.createIndividual(
                nestedWorkflowId, ProvenanceVocab.WORKFLOW_RUN);
        nestedRunIndividual.removeRDFType(ProvenanceVocab.WORKFLOW_RUN);
    }

    public void setToFailedNestedWorkflow(String nestedWorkflowRunId,
            String parentWorkflowRunId) throws OntologyUpdateException {
        addFailedNestedWorkflow(parentWorkflowRunId, nestedWorkflowRunId);
        Individual nestedRunIndividual = model.createIndividual(
                nestedWorkflowRunId, ProvenanceVocab.WORKFLOW_RUN);
        nestedRunIndividual.removeRDFType(ProvenanceVocab.WORKFLOW_RUN);
        Individual failedNestedRunIndividual = model.createIndividual(
                nestedWorkflowRunId, ProvenanceVocab.WORKFLOW_RUN);
        failedNestedRunIndividual
                .removeRDFType(ProvenanceVocab.NESTED_WORKFLOW_RUN);
    }

    public void addProcessIsNestedWorkflow(String processRunId,
            String nestedWorkflowId) {
        Individual process = model.createIndividual(processRunId,
                ProvenanceVocab.NESTED_WORKFLOW_PROCESS_RUN);
        model.add(process, ProvenanceVocab.NESTED_WORKFLOW, model
                .createIndividual(nestedWorkflowId,
                        ProvenanceVocab.NESTED_WORKFLOW_RUN));
    }

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
    public void addUserPredicate(String subject, String predicate, String object) {
        ObjectProperty userPredicate = model.createObjectProperty(predicate);
        userPredicate.addSuperProperty(ProvenanceVocab.USER_PREDICATE);
        Individual subjectIndividual = model.createIndividual(subject,
                ProvenanceVocab.DATA_OBJECT);
        Individual objectIndividual = model.createIndividual(object,
                ProvenanceVocab.DATA_OBJECT);
        model.add(subjectIndividual, userPredicate, objectIndividual);
    }

    void addI3CInfo(String data) {
        Individual individual = model.createIndividual(data, model
                .getResource(I3C_CONTENT));
        model.add(individual, DC.format, model.getResource(I3C_XML_FORMAT));
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#getPropertyValue(java.lang.String,
     *      java.lang.String)
     */
    public String getPropertyValue(String sourceIndividual, String propertyName) {
        Individual individual = model.getIndividual(sourceIndividual);
        Property property = model.getProperty(propertyName);
        RDFNode propertyValue = individual.getPropertyValue(property);
        if (propertyValue != null) {
            try {
                if (propertyValue instanceof Literal) {
                    Literal literal = (Literal) propertyValue;
                    return literal.getString();
                }
            } catch (Error e) {
                logger.error("Probably literal " + propertyValue
                        + "is a complex type: " + e);
                return propertyValue.toString();
            }
            return propertyValue.toString();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#getWorkflowName(java.lang.String)
     */
    public String getWorkflowName(String workflowRun) {
        String value = getPropertyValue(workflowRun,
                ProvenanceVocab.RUNS_WORKFLOW.getURI());
        return value;
    }

    public String getWorkflowInitialLSID(String workflowLSID) {
        String value = getPropertyValue(workflowLSID,
                ProvenanceVocab.WORKFLOW_INITIAL_LSID.getURI());
        return value;
    }

    public String getExperimenterOrganization(String workflowRun) {
        String experimenter = getExperimenter(workflowRun);
        Individual experimenterIndividual = model.getIndividual(experimenter);
        NodeIterator types = model.listObjectsOfProperty(
                experimenterIndividual, ProvenanceVocab.BELONGS_TO);
        if (!types.hasNext())
            return null;
        RDFNode node = types.nextNode();
        return node.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#getExperimenter(java.lang.String)
     */
    public String getExperimenter(String workflowRun) {
        Individual workflowRunIndividual = model.getIndividual(workflowRun);
        NodeIterator types = model.listObjectsOfProperty(workflowRunIndividual,
                ProvenanceVocab.LAUNCHED_BY);
        if (!types.hasNext())
            return null;
        RDFNode node = types.nextNode();
        return node.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#getUnparsedWorkflowStartDate(java.lang.String)
     */
    public String getUnparsedWorkflowStartDate(String workflowRun) {
        String value = getPropertyValue(workflowRun, ProvenanceVocab.START_TIME
                .getURI());
        value = value.substring(1, value.lastIndexOf("\""));
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#getUnparsedProcessEndDate(java.lang.String)
     */
    public String getUnparsedProcessEndDate(String processRun) {
        String value = getPropertyValue(processRun,
                ProvenanceOntologyConstants.DatatypeProperties.ENDTIME);
        value = value.substring(1, value.lastIndexOf("\""));
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#getWorkflowStartDate(java.lang.String)
     */
    public Date getWorkflowStartDate(String workflowRun) throws ParseException {
        String value = getUnparsedWorkflowStartDate(workflowRun);
        Date parsedDateTime = parseDateTime(value);
        return parsedDateTime;
    }

    public Date parseDateTime(String dateTime) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss");
        Date date = formatter.parse(dateTime);
        return date;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#getProcessName(java.lang.String)
     */
    public String getProcessName(String processRun) {
        String value = getPropertyValue(processRun,
                ProvenanceVocab.RUNS_PROCESS.getURI());
        if (value.startsWith(ProvenanceGenerator.PROCESS_NS))
            value = value.substring(ProvenanceGenerator.PROCESS_NS.length());
        return value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#getOriginalLSID(java.lang.String)
     */
    public String getOriginalLSID(String changedData) {
        String value = getPropertyValue(changedData,
                ProvenanceVocab.OLD_DATA_OBJECT.getURI());
        return value;
    }

    public static Node toTimeLiteral(Calendar date) {
        XSDDateTime dateTime = new XSDDateTime(date);
        Node timeLiteral = Node.createLiteral(dateTime.toString(), null,
                XSDDatatype.XSDdateTime);
        return timeLiteral;
    }

    /**
     * Returns <code>true</code> if the syntactic type of is a collection.
     * 
     * @param syntacticType
     *            a String
     * @return a boolean.
     */
    static public boolean isCollection(String syntacticType) {
        boolean isCollection = '(' == syntacticType.charAt(1);
        return isCollection;
    }

    // /**
    // * Stores the ontology in <code>graphSet</code> under the name
    // * {@link #PROVENANCE}.
    // *
    // * @param graphSet
    // * a {@link NamedGraphSet}.
    // */
    // public void storeIn(NamedGraphSet graphSet) {
    // NamedGraph namedGraph = new NamedGraphImpl(PROVENANCE, model.getGraph());
    // graphSet.addGraph(namedGraph);
    // }

    // public static void main(String[] args) {
    // try {
    // InputStream inStr = ClassLoader
    // .getSystemResourceAsStream("KAVE.properties");
    // Properties props = new Properties();
    // props.load(inStr);
    // inStr.close();
    // for (Enumeration iter = props.keys(); iter.hasMoreElements();) {
    // String key = (String) iter.nextElement();
    // System.setProperty(key, props.getProperty(key));
    // }
    // NamedGraphSet graphSet = new KAVEImpl().getGraphSet();
    // new ProvenanceOntology().storeIn(graphSet);
    // } catch (Exception e) {
    // logger.error(e);
    // }
    // }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#dataView(java.lang.String)
     */
    public DataObject dataView(String dataLSID) {
        DataObject dataObject = new DataObject(dataLSID);
        Individual dataIndividual = model.getIndividual(dataLSID);

        Property inputName = ProvenanceVocab.INPUT_DATA_HAS_NAME;
        NodeIterator inputNamesNodes = model.listObjectsOfProperty(
                dataIndividual, inputName);
        while (inputNamesNodes.hasNext()) {
            RDFNode node = inputNamesNodes.nextNode();
            String dataName = node.toString();
            String syntacticType = getSyntacticType(model
                    .getIndividual(dataName));
            if (syntacticType == null)
                dataObject.addInputName(new DataName(dataName));
            else
                dataObject.addInputName(new DataName(dataName, syntacticType));
        }

        Property outputName = ProvenanceVocab.OUTPUT_DATA_HAS_NAME;
        NodeIterator outputNamesNodes = model.listObjectsOfProperty(
                dataIndividual, outputName);
        while (outputNamesNodes.hasNext()) {
            RDFNode node = outputNamesNodes.nextNode();
            String dataName = node.toString();
            String syntacticType = getSyntacticType(model
                    .getIndividual(dataName));
            if (syntacticType == null)
                dataObject.addOutputName(new DataName(dataName));
            else
                dataObject.addOutputName(new DataName(dataName, syntacticType));
        }

        Property derivedFrom = ProvenanceVocab.DATA_DERIVED_FROM;
        NodeIterator originsNodes = model.listObjectsOfProperty(dataIndividual,
                derivedFrom);
        while (originsNodes.hasNext()) {
            RDFNode node = originsNodes.nextNode();
            dataObject.addDerivedFrom(node.toString());
        }

        if (isDataCollection(dataIndividual)) {
            dataObject.setDataCollection(true);
            Property property = ProvenanceVocab.CONTAINS_DATA;
            NodeIterator children = model.listObjectsOfProperty(dataIndividual,
                    property);
            while (children.hasNext()) {
                RDFNode node = children.nextNode();
                dataObject.addChild(node.toString());
            }
        } else {
            dataObject.setDataCollection(false);
            Property property = ProvenanceVocab.DATA_WRAPPED_INTO;
            NodeIterator wrappers = model.listObjectsOfProperty(dataIndividual,
                    property);
            while (wrappers.hasNext()) {
                RDFNode node = wrappers.nextNode();
                dataObject.addWrapper(node.toString());
            }
        }

        return dataObject;
    }

    private String getSyntacticType(Individual dataName) {
        Property syntacticType = ProvenanceVocab.DATA_SYNTACTIC_TYPE;
        NodeIterator types = model.listObjectsOfProperty(dataName,
                syntacticType);
        if (!types.hasNext())
            return null;
        RDFNode node = types.nextNode();
        return node.toString();
    }

    public boolean isDataCollection(Individual dataIndividual) {
        boolean isDataCollection = model.contains(dataIndividual, RDF.type,
                ProvenanceVocab.DATA_COLLECTION);
        return isDataCollection;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#isDataCollection(java.lang.String)
     */
    public boolean isDataCollection(String dataLSID) {
        Individual dataIndividual = model.getIndividual(dataLSID);
        return isDataCollection(dataIndividual);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#getProcessList()
     */
    public List<String> getProcessList() {
        return instances(ProvenanceVocab.PROCESS_RUN);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.ac.man.cs.img.mygrid.provenance.knowledge.ProvenanceOntology#getFailedProcessList()
     */
    public List<String> getFailedProcessList() {
        return instances(ProvenanceVocab.FAILED_PROCESS_RUN);
    }

    // public List getLabelsList() {
    // return instances(ProvenanceVocab.LABEL);
    // }

    private List<String> instances(Resource resource) {
        OntClass ontClass = model.getOntClass(resource.getURI());
        Iterator iterator = ontClass.listInstances();
        List instances = new ArrayList();
        while (iterator.hasNext())
            instances.add(((Individual) iterator.next()).getURI());
        return instances;
    }

    public String getWorkflowForProcess(String processURI) {
        ResIterator itr = model.listSubjectsWithProperty(
                ProvenanceVocab.EXECUTED_PROCESS_RUN, processURI);

        String next = (String) itr.next();

        return next;

    }

    public boolean isProcessIteration(String processURI) {

        return model.contains(model.getResource(processURI), RDF.type, model
                .getResource(ProvenanceVocab.PROCESS_ITERATION.getURI()));

    }

    public String getNestedWorkflow(String processURI)
            throws InstanceRetrievalException {
        String value = getPropertyValue(processURI,
                ProvenanceVocab.NESTED_WORKFLOW.getURI());

        return value;
    }

    public List<String> getProcessInputs(String processURI)
            throws InstanceRetrievalException {

        return getProcessIntermediates(processURI,
                ProvenanceVocab.PROCESS_INPUT);

    }

    public List<String> getProcessOutputs(String processURI)
            throws InstanceRetrievalException {

        return getProcessIntermediates(processURI,
                ProvenanceVocab.PROCESS_OUTPUT);
    }

    public List<String> getProcessIntermediates(String processURI,
            ObjectProperty p) {

        List<String> result = new ArrayList();
        NodeIterator ni = model.listObjectsOfProperty(model
                .getResource(processURI), p);
        while (ni.hasNext()) {

            result.add(ni.next().toString());

        }

        return result;

    }

    public List<String> getProcessList(String workflowRunURI)
            throws InstanceRetrievalException {
        // TODO Auto-generated method stub
        List<String> result = new ArrayList();

        NodeIterator ni = model.listObjectsOfProperty(model
                .getResource(workflowRunURI),
                ProvenanceVocab.EXECUTED_PROCESS_RUN);
        while (ni.hasNext()) {

            result.add(ni.next().toString());

        }
        return result;
    }

    public boolean isProcessFailed(String processURI) {

        return model.contains(model.getResource(processURI), RDF.type, model
                .getResource(ProvenanceVocab.FAILED_PROCESS_RUN.getURI()));

    }

    public boolean isProcessNestedWorkflow(String processURI) {

        return model.contains(model.getResource(processURI), RDF.type,
                ProvenanceVocab.NESTED_WORKFLOW_PROCESS_RUN);
    }

    public List<String> getWorkflowOutputs(String workflowRunURI) {

        List<String> result = new ArrayList<String>();

        NodeIterator n = model.listObjectsOfProperty(model
                .getResource(workflowRunURI), ProvenanceVocab.WORKFLOW_OUTPUT);
        while (n.hasNext()) {

            result.add(n.next().toString());

        }
        return result;

    }

    public List<String> getProcessIterations(String processRunURI) {

        List<String> result = new ArrayList<String>();

        NodeIterator n = model.listObjectsOfProperty(model
                .getResource(processRunURI), ProvenanceVocab.ITERATION);
        while (n.hasNext()) {

            result.add(n.next().toString());

        }
        return result;

    }

    public boolean isProcessWithIterations(String processURI) {

        return model.contains(model.getResource(processURI), RDF.type,
                ProvenanceVocab.PROCESS_RUN_WITH_ITERATIONS);

    }

    public boolean isWorkflowNestedWorkflow(String workflowURI) {

        return model.contains(model.getResource(workflowURI), RDF.type,
                ProvenanceVocab.NESTED_WORKFLOW_RUN);

    }

    public boolean isWorkflowRunFailedWorkflowRun(String workflowURI) {

        return model.contains(model.getResource(workflowURI), RDF.type,
                ProvenanceVocab.FAILED_WORKFLOW_RUN);

    }

}