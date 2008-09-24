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
 * Filename           $RCSfile: FailedSubworkflowTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:27 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j;

import java.util.Iterator;
import java.util.Map;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;

public class FailedSubworkflowTest extends ProvenanceGeneratorTests {

    private static final String WORKFLOW_RUN_ID = "urn:lsid:net.sf.taverna:wfInstance:8";

    // private static final String NESTED_WORKFLOW_RUN_ID =
    // "urn:lsid:net.sf.taverna:wfInstance:13";

    // private static final String SUBWORKFLOW_ID =
    // "urn:lsid:net.sf.taverna:wfInstance:14";

    private static final String SUBWORKFLOW = "myGrid/nestedWorkflow-failure.xml";

    String subRunID;

    // "myGrid/subworkflow-example.xml";
    // "AcceptRejectTest.xml";

    public FailedSubworkflowTest() {
        super();
    }

    public void testExecution() throws Exception {
        execute(SUBWORKFLOW);
        subRunID = "urn:lsid:net.sf.taverna:wfInstance:11";

        Model instanceData = metadataService.retrieveGraphModel(RUN);
        assertNotNull("Retrieved model is not null", instanceData);
        TestUtils.writeOut(instanceData);

        assertTrue("Workflow and subworkflow have different ids", !subRunID
                .equals(WORKFLOW_RUN_ID));

        String query = "SELECT ?nestedRun, ?type WHERE <"
                + subRunID
                + "> (<"
                + WORKFLOW_RUN_ID
                + "> "
                + JenaProvenanceOntology.bracketify(ProvenanceVocab.NESTED_RUN
                        .getURI()) + " ?nestedRun . ?nestedRun rdf:type ?type)";
        Iterator iterator = executeQuery(query);
        Map oneResult = (Map) iterator.next();
        String retrievedNestedRunId = ((Node) oneResult.get("nestedRun"))
                .getURI();
        String typeRun = ((Node) oneResult.get("type")).getURI();
        assertEquals(subRunID, retrievedNestedRunId);
        assertEquals(ProvenanceVocab.FAILED_NESTED_WORKFLOW_RUN.getURI(),
                typeRun);
        // assertEquals(1, result.getRowCount());

        /*
         * FIXME: test query = "SELECT processRun FROM CONTEXT <" +
         * WORKFLOW_RUN_ID + "> {processRun} " + JenaProvenanceOntology
         * .bracketify(ProvenanceVocab.RUNS_PROCESS.getURI()) + " {" +
         * JenaProvenanceOntology .bracketify(ProvenanceGenerator.PROCESS_NS +
         * "NestedWorkflow") + "}; rdf:type {" + JenaProvenanceOntology
         * .bracketify(ProvenanceVocab.NESTED_WORKFLOW_PROCESS_RUN .getURI()) +
         * "}"; SesameExecutionTest.serqlQueryToTupleSet(nativeRepository,
         * query);
         * 
         * query = "SELECT NestedRun FROM CONTEXT <" + WORKFLOW_RUN_ID + "> {<" +
         * WORKFLOW_RUN_ID + ">} " +
         * JenaProvenanceOntology.bracketify(ProvenanceVocab.NESTED_RUN
         * .getURI()) + " {NestedRun} rdf:type {" + JenaProvenanceOntology
         * .bracketify(ProvenanceVocab.NESTED_WORKFLOW_RUN .getURI()) + "}";
         * tupleSet = SesameExecutionTest.serqlQueryToTupleSet(
         * nativeRepository, query); retrievedNestedRunId = tupleSet.getValue(0,
         * 0).toString(); assertEquals(subRunID, retrievedNestedRunId);
         * 
         */

    }

    protected void tearDown() throws Exception {
        MetadataServiceFactory.getInstance(configuration).clear();
    }

}
