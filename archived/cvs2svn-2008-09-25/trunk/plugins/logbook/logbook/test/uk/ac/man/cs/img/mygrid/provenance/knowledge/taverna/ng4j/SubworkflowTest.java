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
 * Filename           $RCSfile: SubworkflowTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:26 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j;

import java.util.Iterator;
import java.util.Map;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.JenaMetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.TestConstants;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;

public class SubworkflowTest extends ProvenanceGeneratorTests {

    private static final String WORKFLOW_RUN_ID = "urn:lsid:net.sf.taverna:wfInstance:8";

    // private static final String SUBWORKFLOW_ID =
    // "urn:lsid:net.sf.taverna:wfInstance:14";

    private static final String SUBWORKFLOW = "myGrid/concat.xml";

    // "myGrid/subworkflow-example.xml";
    // "AcceptRejectTest.xml";

    public SubworkflowTest() {
        super();
    }

    public void testExecution() throws Exception {
        execute(SUBWORKFLOW);
        String subRunID = TestConstants.WF_INSTANCE + "14";

        System.out.println("Workflow " + WORKFLOW_RUN_ID + "\n");
        Model instanceData = metadataService.retrieveGraphModel(WORKFLOW_RUN_ID);
        TestUtils.writeOut(instanceData);
        System.out.println("\nSubworkflow " + subRunID + "\n");
        assertTrue("Workflow and subworkflow have same id.", !subRunID
                .equals(WORKFLOW_RUN_ID));
        instanceData = metadataService.retrieveGraphModel(subRunID);
        TestUtils.writeOut(instanceData);

        checkDescription(WORKFLOW_RUN_ID, "Concatenates input and subworkflow");
        checkDescription(subRunID, "Concatenates input and constant");

        String triql = "SELECT ?processRun "
                + "WHERE <"
                + WORKFLOW_RUN_ID
                + "> ( ?processRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.RUNS_PROCESS.getURI())
                + JenaProvenanceOntology
                        .bracketify(ProvenanceGenerator.PROCESS_NS
                                + "NestedWorkflow")
                + " . ?processRun rdf:type "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.NESTED_WORKFLOW_PROCESS_RUN
                                .getURI()) + ") " + USING_RDF;
        executeQuery(triql);

        metadataService.removeGraph(subRunID);
        metadataService.removeGraph(WORKFLOW_RUN_ID);
    }

    private void checkDescription(String run, String expectedDescription) {
        String triql = "SELECT ?description "
                + "WHERE <"
                + run
                + "> ( ?workflow "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.WORKFLOW_DESCRIPTION
                                .getURI()) + " ?description) ";
        Iterator iterator = executeQuery(triql);

        Map oneResult = (Map) iterator.next();
        Node description = (Node) oneResult.get("description");
        assertEquals(expectedDescription, description.getLiteral().getValue()
                .toString());
    }

    protected void tearDown() throws Exception {
        JenaMetadataService repository = (JenaMetadataService) MetadataServiceFactory
                .getInstance(configuration);
        repository.clear();
        //repository.removeGraph(WORKFLOW_RUN_ID);
        super.tearDown();
    }
}
