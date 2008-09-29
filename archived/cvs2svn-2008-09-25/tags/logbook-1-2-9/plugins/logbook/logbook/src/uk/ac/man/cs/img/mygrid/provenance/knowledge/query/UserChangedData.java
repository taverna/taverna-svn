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
 * Filename           $RCSfile: UserChangedData.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:14 $
 *               by   $Author: stain $
 * Created on 16-Aug-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.query;

import java.text.ParseException;
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
 * Queries for all data changed by an experimenter in his/her previously
 * executed workflows.
 * 
 * @author dturi
 * @version $Id: UserChangedData.java,v 1.1 2007-12-14 12:49:14 stain Exp $
 */
public class UserChangedData {
    
    private static final Logger logger = Logger.getLogger(UserChangedData.class);

    public static final String EXPERIMENTER_KEY = "mygrid.usercontext.experimenter";

    private JenaMetadataService rdfRepository;

    private ProvenanceOntology myChangedDataOntology;

    private Map workflowsWithDataChanged;

    /**
     * Retrieves the RDF provenance graphs for workflows run by
     * <code>experimenter</code> which have failed processes.
     * 
     * @param experimenter
     *            a String representation of a URI.
     * @throws MetadataQueryException 
     */
    public UserChangedData(String experimenter) throws LogBookException {
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
            workflowsWithDataChanged = workflowsWithDataChangedByExperimenter(experimenter);
            Set models = new HashSet();
            for (Iterator iter = workflowsWithDataChanged.keySet().iterator(); iter
                    .hasNext();) {
                String run = (String) iter.next();
                Model instanceData = rdfRepository.retrieveGraphModel(run);
                models.add(instanceData);
            }
            myChangedDataOntology = new JenaProvenanceOntology(models);
        } catch (MetadataServiceCreationException e) {
            logger.error("Problems creating query interface", e);
        }
    }

    /**
     * Invokes the unary constructor using the default experimenter, ie the
     * value of the {@link #EXPERIMENTER_KEY} in the System properties.
     * @throws MetadataQueryException 
     */
    public UserChangedData() throws LogBookException {
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
    public ProvenanceOntology getMyChangedDataOntology() {
        return myChangedDataOntology;
    }

    /**
     * Returns a Map with workflow runs as keys and corresponding Sets of failed
     * process runs as values.
     * 
     * @return a Map with String keys and Set of Strings values.
     */
    public Map getWorkflowsWithDataChanged() {
        return workflowsWithDataChanged;
    }

    public String getWorkflowRunOfChangedData(String lsid) {
        final String query = "SELECT ?workflowRun WHERE "
                + "?workflowRun ( "
                + JenaProvenanceOntology.bracketify(lsid)
                + " rdf:type "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.Classes.CHANGEDDATAOBJECT)
                + " ) USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
        logger.debug("TriQL query = " + query);

        Iterator iterator = TriQLQuery.exec(rdfRepository.getGraphSet(), query);
        if (!iterator.hasNext())
            return null;
        Map nextMap = (Map) iterator.next();
        Node workflowRun = (Node) nextMap.get("workflowRun");
        String workflowRunLSID = workflowRun.getURI();
        return workflowRunLSID;
    }

    /**
     * Returns a Map with workflow runs executed by <code>experimenter</code>
     * as keys and corresponding Sets of (changed) data LSIDs as values.
     * 
     * @param experimenter
     *            a String.
     * @return a Map with String keys and Set of Strings values.
     */
    public Map workflowsWithDataChangedByExperimenter(String experimenter) {
        if (logger.isDebugEnabled()) {
            logger
                    .debug("workflowsWithDataChangedByExperimenter(String experimenter="
                            + experimenter + ") - start");
        }

        final String query = "SELECT ?workflowRun, ?changedData WHERE "
                + "?workflowRun ( ?workflowRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.LAUNCHEDBY)
                + JenaProvenanceOntology.bracketify(experimenter)
                + " . ?changedData rdf:type "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.Classes.CHANGEDDATAOBJECT)
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
            Node changedData = (Node) nextMap.get("changedData");
            String changedDataLSID = changedData.getURI();
            logger.debug("changedData = " + changedDataLSID);
            if (result.containsKey(workflowRunLSID))
                dataCollection = (Set) result.get(workflowRunLSID);
            else {
                dataCollection = new HashSet();
                result.put(workflowRunLSID, dataCollection);
            }
            dataCollection.add(changedDataLSID);
        }

        if (logger.isDebugEnabled()) {
            logger
                    .debug("workflowsWithDataChangedByExperimenter(String) - end");
        }
        return result;
    }

    public DataObject dataView(String dataLSID) {
        return myChangedDataOntology.dataView(dataLSID);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = workflowsWithDataChanged.keySet().iterator(); iter
                .hasNext();) {
            String workflowRun = (String) iter.next();
            String startTime;
            try {
                startTime = myChangedDataOntology.getWorkflowStartDate(
                        workflowRun).toString();
            } catch (ParseException e) {
                startTime = myChangedDataOntology
                        .getUnparsedWorkflowStartDate(workflowRun);
            }
            sb.append(myChangedDataOntology.getWorkflowName(workflowRun) + ", "
                    + startTime + " ==> \n");
            Set changedData = (Set) workflowsWithDataChanged.get(workflowRun);
            for (Iterator iterator = changedData.iterator(); iterator.hasNext();) {
                String data = (String) iterator.next();
                String originalData = myChangedDataOntology
                        .getOriginalLSID(data);
                sb.append("\toriginal data::\n");
                sb.append("\t" + dataView(originalData).toString() + "\n");
                sb.append("\tchanged data::\n");
                sb.append("\t" + dataView(data).toString() + "\n");

            }
        }
        return sb.toString();
    }
}
