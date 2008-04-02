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
 * Filename           $RCSfile: FailedProcesses.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:14 $
 *               by   $Author: stain $
 * Created on 16-Aug-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.query;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.JenaMetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.util.DataObject;
import uk.org.mygrid.provenance.LogBookException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;

import de.fuberlin.wiwiss.ng4j.triql.TriQLQuery;

/**
 * Queries for all failed processes in the workflows executed by an
 * experimenter.
 * 
 * @author dturi
 * @version $Id: FailedProcesses.java,v 1.1 2007-12-14 12:49:14 stain Exp $
 */
public class FailedProcesses {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(FailedProcesses.class);

    public static final String EXPERIMENTER_KEY = "mygrid.usercontext.experimenter";

    private JenaMetadataService rdfRepository;

    private ProvenanceOntology myFailedProcessesOntology;

    private Map workflowsWithFailedProcesses;

    /**
     * Retrieves the RDF provenance graphs for workflows run by
     * <code>experimenter</code> which have failed processes.
     * 
     * @param experimenter
     *            a String representation of a URI.
     * @throws MetadataQueryException 
     */
    public FailedProcesses(String experimenter) throws LogBookException {
        try {
            Properties configuration = ProvenanceConfigurator
                    .getMetadataStoreConfiguration();
            MetadataService metadataService = MetadataServiceFactory
                    .getInstance(configuration);
            if (metadataService instanceof JenaMetadataService) {
                rdfRepository = (JenaMetadataService) metadataService;
            } else
                throw new MetadataQueryException(
                        MetadataQueryException.SERVICE_CREATED_NOT_OF_TYPE_JENA);
            if (experimenter == null)
                experimenter = System.getProperty(EXPERIMENTER_KEY);
            logger.info("Experimenter = " + experimenter);
            workflowsWithFailedProcesses = workflowsWithFailedProcessesForExperimenter(experimenter);
            Set models = new HashSet();
            for (Iterator iter = workflowsWithFailedProcesses.keySet()
                    .iterator(); iter.hasNext();) {
                String run = (String) iter.next();
                Model instanceData = rdfRepository.retrieveGraphModel(run);
                models.add(instanceData);
            }
            myFailedProcessesOntology = new JenaProvenanceOntology(models);
        } catch (MetadataServiceCreationException e) {
            logger.error("Problems creating query interface", e);
            throw new MetadataQueryException(e);
        }
    }

    /**
     * Invokes the unary constructor using the default experimenter, ie the
     * value of the {@link #EXPERIMENTER_KEY} in the System properties.
     * @throws MetadataQueryException 
     */
    public FailedProcesses() throws LogBookException {
        this(null);
    }

    public JenaMetadataService getRdfRepository() {
        return rdfRepository;
    }

    /**
     * The {@link JenaProvenanceOntology}populated with all the workflows run
     * by <code>experimenter</code> which have failed process runs.
     * 
     * @return
     */
    public ProvenanceOntology getMyFailedProcessesOntology() {
        return myFailedProcessesOntology;
    }

    /**
     * Returns a Map with workflow runs as keys and corresponding Sets of failed
     * process runs as values.
     * 
     * @return a Map with String keys and Set of Strings values.
     */
    public Map getWorkflowsWithFailedProcesses() {
        return workflowsWithFailedProcesses;
    }

    /**
     * Returns the Set of input data LSIDs (as Strings) for
     * <code>processRun</code>.
     * 
     * @param processRun
     *            the LSID of a process run.
     * @return a Set of Strings.
     */
    public Set inputDataOfFailedProcess(String processRun) {
        final String query = "SELECT ?inputData WHERE ?workflowRun ( "
                + JenaProvenanceOntology.bracketify(processRun)
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.PROCESSINPUT)
                + " ?inputData )";
        logger.debug("TriQL query = " + query);
        Set result = new HashSet();
        Iterator iterator = TriQLQuery.exec(rdfRepository.getGraphSet(), query);
        while (iterator.hasNext()) {
            Map nextMap = (Map) iterator.next();
            Node inputData = (Node) nextMap.get("inputData");
            String inputDataLSID = inputData.getURI();
            logger.debug("inputData = " + inputDataLSID);
            result.add(inputDataLSID);
        }
        return result;
    }

    /**
     * 
     * @param workflowRun
     * @return a Map of type String -> Set <String>with key being the name of a
     *         failed process and value the set of input data for it in the run.
     */
    public Map inputDataOfFailedProcessesInWorkflow(String workflowRun) {
        if (logger.isDebugEnabled()) {
            logger.debug("inputDataOfFailedProcesses(String workflowRun = "
                    + workflowRun + ") - start");
        }

        final String query = "SELECT ?process, ?inputData WHERE <"
                + workflowRun
                + "> ( ?run rdf:type "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.Classes.FAILEDPROCESSRUN)
                + " . ?run "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.RUNSPROCESS)
                + " ?process . ?run "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.PROCESSINPUT)
                + " ?inputData ) USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
        logger.debug("TriQL query = " + query);
        Map result = new HashMap();
        Iterator iterator = TriQLQuery.exec(rdfRepository.getGraphSet(), query);
        Collection dataCollection;
        while (iterator.hasNext()) {
            Map nextMap = (Map) iterator.next();
            Node process = (Node) nextMap.get("process");
            String processLSID = process.getURI();
            logger.debug("process = " + processLSID);
            Node inputData = (Node) nextMap.get("inputData");
            String inputDataLSID = inputData.getURI();
            logger.debug("inputData = " + inputDataLSID);
            if (result.containsKey(processLSID))
                dataCollection = (Collection) result.get(processLSID);
            else {
                dataCollection = new HashSet();
                result.put(processLSID, dataCollection);
            }
            dataCollection.add(inputDataLSID);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("inputDataOfFailedProcesses(String) - end");
        }
        return result;
    }

    /**
     * Returns a Map with workflow runs executed by <code>experimenter</code>
     * as keys and corresponding Sets of failed process runs as values.
     * 
     * @param experimenter
     *            a String.
     * @return a Map with String keys and Set of Strings values.
     */
    public Map workflowsWithFailedProcessesForExperimenter(String experimenter) {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("workflowsWithFailedProcessesForExperimenter(String experimenter = "
                            + experimenter + ") - start");
        }

        final String query = "SELECT ?workflowRun, ?failedRun WHERE "
                + "?workflowRun ( ?workflowRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.LAUNCHEDBY)
                + JenaProvenanceOntology.bracketify(experimenter)
                + " . ?failedRun rdf:type "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.Classes.FAILEDPROCESSRUN)
                + " ) USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
        logger.debug("TriQL query = " + query);
        Map result = new HashMap();
        Set dataCollection;
        Iterator iterator = TriQLQuery.exec(rdfRepository.getGraphSet(), query);
        while (iterator.hasNext()) {
            Map nextMap = (Map) iterator.next();
            Node workflowRun = (Node) nextMap.get("workflowRun");
            String workflowRunLSID = workflowRun.getURI();
            logger.debug("workflowRun = " + workflowRunLSID);
            Node failedRun = (Node) nextMap.get("failedRun");
            String failedRunLSID = failedRun.getURI();
            logger.debug("failedRun = " + failedRunLSID);
            if (result.containsKey(workflowRunLSID))
                dataCollection = (Set) result.get(workflowRunLSID);
            else {
                dataCollection = new HashSet();
                result.put(workflowRunLSID, dataCollection);
            }
            dataCollection.add(failedRunLSID);
        }

        if (logger.isDebugEnabled()) {
            logger
                    .debug("workflowsWithFailedProcessesForExperimenter(String) - end");
        }
        return result;
    }

    public DataObject dataView(String dataLSID) {
        return myFailedProcessesOntology.dataView(dataLSID);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = workflowsWithFailedProcesses.keySet().iterator(); iter
                .hasNext();) {
            String workflowRun = (String) iter.next();
            String startTime;
            try {
                startTime = myFailedProcessesOntology.getWorkflowStartDate(
                        workflowRun).toString();
            } catch (ParseException e) {
                startTime = myFailedProcessesOntology
                        .getUnparsedWorkflowStartDate(workflowRun);
            }
            sb.append(myFailedProcessesOntology.getWorkflowName(workflowRun)
                    + ", " + startTime + " ==> \n");
            Set failedRuns = (Set) workflowsWithFailedProcesses
                    .get(workflowRun);
            for (Iterator iterator = failedRuns.iterator(); iterator.hasNext();) {
                String failedRun = (String) iterator.next();
                sb.append("\t"
                        + myFailedProcessesOntology.getProcessName(failedRun)
                        + "\n");
                Set inputDataOfFailedProcess = inputDataOfFailedProcess(failedRun);
                for (Iterator it = inputDataOfFailedProcess.iterator(); it
                        .hasNext();) {
                    String dataLSID = (String) it.next();
                    sb.append("\t" + dataView(dataLSID).toString() + "\n");

                }
            }
        }
        return sb.toString();
    }
}
