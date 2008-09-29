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
 * Filename           $RCSfile: IterationTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:27 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.OldProvenanceGenerator;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Resource;

public class IterationTest extends ProvenanceGeneratorTests {

    private static final String WORKFLOW_OUTPUT_NAME = "urn:www.mygrid.org.uk/workflow#VI9FMF5HBQ10_out_Output";
    static protected final String ITERATION_EXAMPLE = "TomOinn/IterationStrategyExample.xml";

    public IterationTest() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void testIteration() throws Exception {
        execute(IterationTest.ITERATION_EXAMPLE);
        Set<String> processRuns = getProcessRuns(RUN);
        populateModel(RUN, processRuns);
        // ontModel.write(System.out, "N3");

        int iterations = 0;

        String queryString = ProvenanceGeneratorTests.PROVENANCE_PREFIX
                + ProvenanceGeneratorTests.RDF_PREFIX
                + "SELECT ?iteration "
                + "WHERE { ?iteration rdf:type  "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.PROCESS_ITERATION.getURI())
                + " . ?iteration p:runsProcess "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceGenerator.PROCESS_NS
                                + "ShapeAnimals") + " . }";
        System.out.println(queryString);
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, ontModel);
        try {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext());
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource iteration = soln.getResource("iteration");
                System.out.println(iteration.getURI());
                iterations++;
            }
        } finally {
            qexec.close();
        }
        assertEquals(6, iterations);

        int processesWithIterations = 0;
        queryString = ProvenanceGeneratorTests.PROVENANCE_PREFIX
                + ProvenanceGeneratorTests.RDF_PREFIX
                + "SELECT ?processWithIterations ?iteration "
                + "WHERE { ?processWithIterations rdf:type  "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.PROCESS_RUN_WITH_ITERATIONS
                                .getURI())
                + " . ?processWithIterations "
                + JenaProvenanceOntology.bracketify(ProvenanceVocab.ITERATION
                        .getURI()) + " ?iteration  . }";
        System.out.println(queryString);
        query = QueryFactory.create(queryString);
        qexec = QueryExecutionFactory.create(query, ontModel);
        try {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext());
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource processWithIterations = soln
                        .getResource("processWithIterations");
                System.out.println(processWithIterations.getURI());
                Resource iteration = soln.getResource("iteration");
                System.out.println(iteration.getURI());
                processesWithIterations++;
            }
        } finally {
            qexec.close();
        }
        assertEquals(8, processesWithIterations);

        iterations = 0;
        queryString = ProvenanceGeneratorTests.PROVENANCE_PREFIX
                + ProvenanceGeneratorTests.RDF_PREFIX
                + "SELECT ?processWithIterations ?iteration "
                + "WHERE { ?processWithIterations rdf:type "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.PROCESS_RUN_WITH_ITERATIONS
                                .getURI())
                + " . ?processWithIterations "
                + JenaProvenanceOntology.bracketify(ProvenanceVocab.ITERATION
                        .getURI())
                + " ?iteration . ?processWithIterations "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.RUNS_PROCESS.getURI())
                + " "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceGenerator.PROCESS_NS
                                + "ShapeAnimals") + " . }";
        System.out.println(queryString);
        query = QueryFactory.create(queryString);
        qexec = QueryExecutionFactory.create(query, ontModel);
        try {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext());
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource iteration = soln.getResource("iteration");
                System.out.println(iteration.getURI());
                iterations++;
            }
        } finally {
            qexec.close();
        }
        assertEquals(6, iterations);

        queryString = ProvenanceGeneratorTests.PROVENANCE_PREFIX
                + ProvenanceGeneratorTests.RDF_PREFIX
                + "SELECT ?processWithIterations ?numberOfIterations "
                + "WHERE { ?processWithIterations rdf:type "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.PROCESS_RUN_WITH_ITERATIONS
                                .getURI())
                + " . ?processWithIterations "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.NUMBER_OF_ITERATIONS
                                .getURI())
                + " ?numberOfIterations . ?processWithIterations "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.RUNS_PROCESS.getURI())
                + " "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceGenerator.PROCESS_NS
                                + "ShapeAnimals") + " . }";
        System.out.println(queryString);
        query = QueryFactory.create(queryString);
        qexec = QueryExecutionFactory.create(query, ontModel);
        try {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext());
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Literal numberOfIterations = soln
                        .getLiteral("numberOfIterations");
                iterations = Integer.valueOf(numberOfIterations.getValue()
                        .toString());
                assertEquals(6, iterations);
            }
        } finally {
            qexec.close();
        }

        queryString = ProvenanceGeneratorTests.PROVENANCE_PREFIX
                + ProvenanceGeneratorTests.RDF_PREFIX
                + "SELECT ?outputName "
                + "WHERE { <urn:lsid:net.sf.taverna:dataCollection:22> "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.OUTPUT_DATA_HAS_NAME
                                .getURI()) + " ?outputName . }";
        System.out.println(queryString);
        query = QueryFactory.create(queryString);
        qexec = QueryExecutionFactory.create(query, ontModel);
        try {
            ResultSet results = qexec.execSelect();
            assertTrue(results.hasNext());
            Set<String> outputNames = new HashSet<String>();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                Resource outputName = soln.getResource("outputName");
                System.out.println(outputName.getURI());
                outputNames.add(outputName.getURI());
            }
            assertEquals(2, outputNames.size());
            assertTrue(
                    "Contains ShapeAnimals output",
                    outputNames
                            .contains("urn:www.mygrid.org.uk/process#ShapeAnimals_out_output"));
            assertTrue(
                    "Contains Workflow Output",
                    outputNames
                            .contains(WORKFLOW_OUTPUT_NAME));

        } finally {
            qexec.close();
        }

        String triqlQuery = "SELECT ?workflowOutput WHERE <" + RUN + "> ( <" + RUN
                + "> <" + ProvenanceVocab.WORKFLOW_OUTPUT.getURI()
                + ">  ?workflowOutput )";

        Iterator iterator = executeQuery(triqlQuery);
        Map oneResult = (Map) iterator.next();
        Node output = (Node) oneResult.get("workflowOutput");
        String outputLSID = output.getURI();
        System.out.println(outputLSID);

        triqlQuery = "SELECT ?value WHERE ( <" + outputLSID + "> <"
                + ProvenanceVocab.OUTPUT_DATA_HAS_NAME.getURI() + "> ?value )";
        iterator = executeQuery(triqlQuery);
        String name = null;
        while (iterator.hasNext()) {
            oneResult = (Map) iterator.next();
            Node value = (Node) oneResult.get("value");
            String outputName = value.getURI();
            if (outputName.startsWith(OldProvenanceGenerator.WORKFLOW_NS)) {
                System.out.println(outputName);
                name = outputName;
                break;
            }
        }
        assertNotNull(name);
        assertEquals(WORKFLOW_OUTPUT_NAME, name);
    }

    protected void tearDown() throws Exception {
        MetadataServiceFactory.getInstance(configuration).removeGraph(RUN);
        super.tearDown();
    }
}
