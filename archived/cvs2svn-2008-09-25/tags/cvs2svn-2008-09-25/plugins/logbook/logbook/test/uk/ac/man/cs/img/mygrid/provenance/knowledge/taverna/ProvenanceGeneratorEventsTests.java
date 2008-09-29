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
 * Filename           $RCSfile: ProvenanceGeneratorEventsTests.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:52:51 $
 *               by   $Author: stain $
 * Created on 3 Aug 2006
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.JenaMetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;

import de.fuberlin.wiwiss.ng4j.NamedGraphSet;
import de.fuberlin.wiwiss.ng4j.triql.TriQLQuery;

public class ProvenanceGeneratorEventsTests extends TestCase {

    private static final String RDF_POSTFIX = "USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";

    private StressTester stressTester;

    private NamedGraphSet graphSet;

    private MetadataService metadataService;

    private Properties configuration;

    public ProvenanceGeneratorEventsTests(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        configuration = ProvenanceConfigurator
                                        .getConfiguration("stresstest");
        metadataService = MetadataServiceFactory
                .getInstance(configuration);
        metadataService.clear();
        stressTester = new StressTester();
        for (int i = 0; i < 2; i++) {
            stressTester.createRandomWorkflowRun();            
        }
    }

    public void testEvents() throws Exception {
        graphSet = ((JenaMetadataService) metadataService).getGraphSet();
        String workflowRunId = TestConstants.WF_INSTANCE + "5";
        Model retrievedGraphModel = metadataService
                .retrieveGraphModel(workflowRunId);
        assertTrue("retrieved model is not empty", !retrievedGraphModel
                .isEmpty());
        JenaProvenanceOntology ontology = new JenaProvenanceOntology();

        OntModel model = ontology.getOntModel();
        model.add(retrievedGraphModel);

        // model.write(System.out, "N3");

        // WorkflowRun workflowRun = provenanceFactory.createWorkflowRun(
        // workflowRunId, model);
        // Iterator executedProcessRuns = workflowRun.getExecutedProcessRun();
        // while (executedProcessRuns.hasNext()) {
        // ProcessRun processRun = (ProcessRun) executedProcessRuns.next();
        // System.out.println(processRun.uri());
        // Process process = processRun.getRunsProcess();
        // System.out.println(process.uri());
        // }

        String queryString = "SELECT ?processRun "
                + "WHERE <"
                + workflowRunId
                + ">( "
                + JenaProvenanceOntology.bracketify(workflowRunId)
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.EXECUTED_PROCESS_RUN
                                .getURI())
                + "?processRun ) "
                + "( ?processRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.RUNS_PROCESS.getURI())
                + "<urn:www.mygrid.org.uk/process#identifyUniprotProtein> )";
        Iterator iterator = executeQuery(queryString);
        assertTrue("Found process run for identifyUniprotProtein", iterator
                .hasNext());
        Map soln = (Map) iterator.next();
        Node iup = (Node) soln.get("processRun");
        String processRunId = iup.getURI();
        System.out.println(processRunId);

        retrievedGraphModel = metadataService.retrieveGraphModel(processRunId);
        model.add(retrievedGraphModel);
        List<String> processInputs = ontology.getProcessInputs(iup.getURI());
        for (String input : processInputs) {
            System.out.println("\t input = " + input);
        }
        assertEquals(11, processInputs.size());

        queryString = "SELECT ?processRun "
                + "WHERE <"
                + workflowRunId
                + ">( "
                + JenaProvenanceOntology.bracketify(workflowRunId)
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.EXECUTED_PROCESS_RUN
                                .getURI())
                + "?processRun ) "
                + "( ?processRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.RUNS_PROCESS.getURI())
                + "<urn:www.mygrid.org.uk/process#uniprot2GO> . "
                + "?processRun rdf:type "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.PROCESS_RUN_WITH_ITERATIONS
                                .getURI()) + " ) " + RDF_POSTFIX;

        iterator = executeQuery(queryString);
        assertTrue("Found process run for uniprot2GO", iterator.hasNext());
        soln = (Map) iterator.next();
        Node u2go = (Node) soln.get("processRun");
        processRunId = u2go.getURI();
        System.out.println(processRunId);

        queryString = "SELECT ?processInput, ?inputName "
                + "WHERE <"
                + processRunId
                + ">( "
                + JenaProvenanceOntology.bracketify(processRunId)
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.PROCESS_INPUT.getURI())
                + "?processInput .  ?processInput "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.INPUT_DATA_HAS_NAME
                                .getURI()) + " ?inputName)";
        iterator = executeQuery(queryString);
        while (iterator.hasNext()) {
            soln = (Map) iterator.next();
            Node processInput = (Node) soln.get("processInput");
            Node inputName = (Node) soln.get("inputName");
            System.out.println("Processor input = " + processInput.getURI()
                    + " with name " + inputName);
        }

        retrievedGraphModel = metadataService.retrieveGraphModel(processRunId);
        model.add(retrievedGraphModel);
        processInputs = ontology.getProcessInputs(u2go.getURI());
        assertEquals(1, processInputs.size());
        String dataCollection = processInputs.get(0);
        System.out.println(dataCollection);

        queryString = "SELECT ?dataItem "
                + "WHERE <"
                + processRunId
                + ">( <"
                + dataCollection
                + "> "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.CONTAINS_DATA.getURI())
                + "?dataItem )";
        iterator = executeQuery(queryString);
        assertTrue("Found dataItems for input of uniprot2GO", iterator
                .hasNext());
        int rowCount = 0;
        while (iterator.hasNext()) {
            soln = (Map) iterator.next();
            Node dataItem = (Node) soln.get("dataItem");
            rowCount++;
            System.out.println(dataItem.getURI());
        }
        assertEquals(10, rowCount);

        queryString = "SELECT ?processRun, ?iterations "
                + "WHERE <"
                + processRunId
                + ">( ?processRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.RUNS_PROCESS.getURI())
                + "<urn:www.mygrid.org.uk/process#uniprot2GO> . "
                + "?processRun rdf:type "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.PROCESS_RUN_WITH_ITERATIONS
                                .getURI())
                + " . "
                + "?processRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.NUMBER_OF_ITERATIONS
                                .getURI()) + " ?iterations )" + RDF_POSTFIX;
        iterator = executeQuery(queryString);
        assertTrue("Found process run with iterations for uniprot2GO", iterator
                .hasNext());
        soln = (Map) iterator.next();
        u2go = (Node) soln.get("processRun");
        processRunId = u2go.getURI();
        System.out.println(processRunId);
        Node iters = (Node) soln.get("iterations");
        assertEquals(10, iters.getLiteral().getValue());

        //
        // List<String> processList = ontology.getProcessList(workflowRunId);
        // assertTrue("Process list not empty", !processList.isEmpty());
        // assertEquals(12, processList.size());
        //
        // // for (String process : processList) {
        // // System.out.println(process);
        // // List<String> processInputs = ontology.getProcessInputs(process);
        // // for (String input : processInputs) {
        // // System.out.println("\t input = " + input);
        // // }
        // // List<String> processOutputs = ontology.getProcessOutputs(process);
        // // for (String output : processOutputs) {
        // // System.out.println("\t output = " + output);
        // // }
        // // }
        //
        // // ontology.writeOut();
    }

    protected Iterator executeQuery(String triql) {
        System.out.println("Testing the following TriQL query:");
        System.out.println(triql);
        Iterator iterator = TriQLQuery.exec(graphSet, triql);
        assertTrue("query result is empty", iterator.hasNext());
        return iterator;
    }

    public void query(String query) throws Exception {
        System.out.println(query);
        JenaMetadataService rdfRepository = new JenaMetadataService(configuration);
        Iterator iterator = rdfRepository.query(query);
        while (iterator.hasNext()) {
            Map nextMap = (Map) iterator.next();
            System.out.println(nextMap);
        }
    }
}
