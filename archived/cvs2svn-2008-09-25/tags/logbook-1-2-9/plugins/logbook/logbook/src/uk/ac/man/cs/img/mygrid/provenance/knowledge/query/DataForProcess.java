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
 * Filename           $RCSfile: DataForProcess.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:14 $
 *               by   $Author: stain $
 * Created on 18-Aug-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.JenaMetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.org.mygrid.provenance.LogBookException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

import com.hp.hpl.jena.graph.Node;

import de.fuberlin.wiwiss.ng4j.triql.TriQLQuery;

/**
 * Queries for input and output data of previously executed processes.
 * 
 * @author dturi
 * @version $Id: DataForProcess.java,v 1.1 2007-12-14 12:49:14 stain Exp $
 */
public class DataForProcess {
    /**
     * Logger for this class
     */
    private static final Logger logger = Logger.getLogger(DataForProcess.class);

    public JenaMetadataService rdfRepository;

    /**
     * @throws MetadataQueryException
     * 
     */
    public DataForProcess() throws LogBookException {
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
        } catch (MetadataServiceCreationException e) {
            logger.error("Problems creating query interface", e);
            throw new MetadataQueryException(e);
        }
    }

    /**
     * 
     * @param process
     *            the name of a process.
     * @param dataName
     *            the name of some data.
     * @return a Map where keys are LSIDs of workflowRuns and values Sets of
     *         pairs String[2]; the elements of the pairs are input and output
     *         LSIDs for data with name <code>dataName</code> processed by
     *         <code>process</code>;
     */
    public Map dataForProcess(String process, String dataName) {
        Map result = new HashMap();
        final String query = "SELECT ?workflowRun, ?inputData, ?outputData "
                + "WHERE ?workflowRun (?processRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.RUNSPROCESS)
                + " <"
                + process
                + "> . ?processRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.PROCESSINPUT)
                + " ?inputData . ?inputData "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.INPUTDATAHASNAME)
                + " <"
                + dataName
                + "> . ?processRun"
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.PROCESSOUTPUT)
                + " ?outputData )";
        logger.debug("TriQL query = " + query);
        Iterator iterator = TriQLQuery.exec(rdfRepository.getGraphSet(), query);
        Collection dataCollection;
        while (iterator.hasNext()) {
            Map nextMap = (Map) iterator.next();
            Node workflowRun = (Node) nextMap.get("workflowRun");
            String key = workflowRun.getURI();
            logger.debug("workflowRun = " + key);
            Node inputData = (Node) nextMap.get("inputData");
            String inputDataLSID = inputData.getURI();
            logger.debug("inputData = " + inputDataLSID);
            Node outputData = (Node) nextMap.get("outputData");
            String outputDataLSID = outputData.getURI();
            logger.debug("outputData = " + outputDataLSID);
            if (result.containsKey(key))
                dataCollection = (Collection) result.get(key);
            else {
                dataCollection = new HashSet();
                result.put(key, dataCollection);
            }
            dataCollection.add(new String[] { inputDataLSID, outputDataLSID });
        }
        return result;
    }

}
