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
 * Filename           $RCSfile: ProvenanceGeneratorEventsTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:52:52 $
 *               by   $Author: stain $
 * Created on 3 Aug 2006
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna;

import junit.framework.TestCase;

public class ProvenanceGeneratorEventsTest extends TestCase {

    private StressTester stressTester;

    public ProvenanceGeneratorEventsTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        stressTester = new StressTester();
        stressTester.createRandomWorkflowRun();
    }

//    public void testEvents() throws Exception {
//        MetadataService metadataService = MetadataServiceFactory
//                .getInstance(ProvenanceConfigurator.getConfiguration());
//        String workflowRunId = TestConstants.WF_INSTANCE + "5";
//        Model retrievedGraphModel = metadataService
//                .retrieveGraphModel(workflowRunId);
//        JenaProvenanceOntology ontology = new JenaProvenanceOntology();
//        //        Map<String, ProvenanceOntology> ontologies = stressTester
////                .getOntologies();
////        String workflowRunId = ontologies.keySet().iterator().next();
////        JenaProvenanceOntology ontology = (JenaProvenanceOntology) ontologies
////                .values().iterator().next();
//        OntModel model = //ontology.getOntModel();
//        ontology.getOntModel();
//        model.add(retrievedGraphModel);
//
//
//
//        String queryString = PROVENANCE_PREFIX + "SELECT ?processRun "
//                + "WHERE { ?processRun  p:runsProcess "
//                + "<urn:www.mygrid.org.uk/process#identifyUniprotProtein> . }";
//        System.out.println(queryString);
//        Query query = QueryFactory.create(queryString);
//        QueryExecution qexec = QueryExecutionFactory.create(query, model);
//        try {
//            ResultSet results = qexec.execSelect();
//            assertTrue("Found process run for identifyUniprotProtein", results
//                    .hasNext());
//            QuerySolution soln = results.nextSolution();
//            Resource iup = soln.getResource("processRun");
//            System.out.println(iup.getURI());
//            List<String> processInputs = ontology
//                    .getProcessInputs(iup.getURI());
//            for (String input : processInputs) {
//                System.out.println("\t input = " + input);
//            }
//            assertEquals(11, processInputs.size());
//        } finally {
//            qexec.close();
//        }
//
//        queryString = PROVENANCE_PREFIX + RDF_PREFIX + "SELECT ?processRun "
//                + "WHERE { ?processRun  p:runsProcess "
//                + "<urn:www.mygrid.org.uk/process#uniprot2GO> . "
//                + "?processRun rdf:type p:ProcessRunWithIterations . }";
//        System.out.println(queryString);
//        query = QueryFactory.create(queryString);
//        qexec = QueryExecutionFactory.create(query, model);
//        String dataCollection = null;
//        try {
//            ResultSet results = qexec.execSelect();
//            assertTrue("Found process run for uniprot2GO", results.hasNext());
//            QuerySolution soln = results.nextSolution();
//            Resource u2go = soln.getResource("processRun");
//            System.out.println(u2go.getURI());
//            List<String> processInputs = ontology.getProcessInputs(u2go
//                    .getURI());
//            // assertEquals(1, processInputs.size()); FIXME
//            dataCollection = processInputs.get(0);
//        } finally {
//            qexec.close();
//        }
//
//        queryString = PROVENANCE_PREFIX + "SELECT ?dataItem " + "WHERE { <"
//                + dataCollection + ">  p:containsData " + "?dataItem . }";
//        System.out.println(queryString);
//        query = QueryFactory.create(queryString);
//        qexec = QueryExecutionFactory.create(query, model);
//        try {
//            ResultSet results = qexec.execSelect();
//            assertTrue("Found dataItems for input of uniprot2GO", results
//                    .hasNext());
//            int rowCount = 0;
//            while (results.hasNext()) {
//                QuerySolution soln = results.nextSolution();
//                Resource dataItem = soln.getResource("dataItem");
//                rowCount++;
//                System.out.println(dataItem.getURI());
//            }
//            assertEquals(10, rowCount);
//        } finally {
//            qexec.close();
//        }
//
//        queryString = PROVENANCE_PREFIX + RDF_PREFIX
//                + "SELECT ?processRun ?iterations "
//                + "WHERE { ?processRun  p:runsProcess "
//                + "<urn:www.mygrid.org.uk/process#uniprot2GO> . "
//                + "?processRun rdf:type p:ProcessRunWithIterations . "
//                + "?processRun p:numberOfIterations ?iterations . }";
//        System.out.println(queryString);
//        query = QueryFactory.create(queryString);
//        qexec = QueryExecutionFactory.create(query, model);
//        try {
//            ResultSet results = qexec.execSelect();
//            assertTrue("Found process run with iterations for uniprot2GO",
//                    results.hasNext());
//            QuerySolution soln = results.nextSolution();
//            Resource u2go = soln.getResource("processRun");
//            Literal iters = soln.getLiteral("iterations");
//            System.out.println(u2go.getURI());
//            System.out.println(iters.getInt());
//        } finally {
//            qexec.close();
//        }
//
//        List<String> processList = ontology.getProcessList(workflowRunId);
//        assertTrue("Process list not empty", !processList.isEmpty());
//        assertEquals(12, processList.size());
//
//    }
}
