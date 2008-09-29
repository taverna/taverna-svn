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
 * Filename           $RCSfile: ExecutionTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:24 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j;

import java.util.Iterator;
import java.util.Map;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGeneratorTests;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;

public class ExecutionTest extends ProvenanceGeneratorTests {

    static final String XSLT_INPUT = "urn:lsid:net.sf.taverna:dataItem:6";

    static final String XSLT_OUTPUT = "urn:lsid:net.sf.taverna:dataItem:7";

    String query;

    public ExecutionTest() {
        super();
    }

    public void testExecution() throws Exception {
        boolean execute = true;
        if (execute) {
            metadataService.clear();
            execute(ProvenanceGeneratorTests.BLAST_WORKFLOW);
        }

        Model instanceData = metadataService.retrieveGraphModel(RUN);
        TestUtils.writeOut(instanceData);

        Iterator iterator;

        query = "SELECT ?workflowRun "
                + "WHERE (?workflowRun "
                + JenaProvenanceOntology.bracketify(ProvenanceVocab.LAUNCHED_BY
                        .getURI())
                + " "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceConfigurator.DEFAULT_EXPERIMENTER)
                + " . "
                + "?workflowRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.RUNS_WORKFLOW.getURI())
                + " "
                + JenaProvenanceOntology
                        .bracketify("urn:lsid:net.sf.taverna:wfDefinition:5")
                + " )";
        iterator = executeQuery(query);
        Map oneResult = (Map) iterator.next();
        Node workflow = (Node) oneResult.get("workflowRun");
        String uri = workflow.getURI();
        System.out.println(uri);

        assertEquals(RUN, uri);

        query = "SELECT ?workflowRun, ?organization "
                + "WHERE ?workflowRun ("
                + JenaProvenanceOntology
                        .bracketify(ProvenanceConfigurator.DEFAULT_EXPERIMENTER)
                + JenaProvenanceOntology.bracketify(ProvenanceVocab.BELONGS_TO
                        .getURI()) + " ?organization )";
        iterator = executeQuery(query);

        oneResult = (Map) iterator.next();
        workflow = (Node) oneResult.get("workflowRun");
        assertEquals(RUN, workflow.getURI());
        Node organization = (Node) oneResult.get("organization");
        String organizationUri = organization.getURI();

        assertEquals(ProvenanceConfigurator.DEFAULT_ORGANIZATION,
                organizationUri);

        query = "SELECT ?experimenter "
                + "WHERE "
                + JenaProvenanceOntology.bracketify(RUN)
                + " ( ?experimenter rdf:type "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.EXPERIMENTER.getURI())
                + " ) " + USING_RDF;
        iterator = executeQuery(query);
        oneResult = (Map) iterator.next();
        Node experimenter = (Node) oneResult.get("experimenter");
        String experimenterUri = experimenter.getURI();

        assertEquals(ProvenanceConfigurator.DEFAULT_EXPERIMENTER,
                experimenterUri);

        query = "SELECT ?processRun, ?time "
                + "WHERE ?workflowRun ( ?processRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.RUNSPROCESS)
                + " <"
                + ProvenanceGenerator.PROCESS_NS
                + "Blast2RDF> . ?processRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.DatatypeProperties.ENDTIME)
                + " ?time)";
        iterator = executeQuery(query);
        oneResult = (Map) iterator.next();
        Node processRun = (Node) oneResult.get("processRun");
        assertNotNull("processRun for Blast2RDF is not null", processRun);
        System.out.println("Run id for Blast2RDF: " + processRun);
        Node time = (Node) oneResult.get("time");
        assertNotNull("end time for Blast2RDF is not null", time);
        System.out.println("End time for Blast2RDF: " + time);

        Model retrievedModel = metadataService.retrieveGraphModel(processRun
                .getURI());
        TestUtils.writeOut(retrievedModel);

        query = "SELECT ?inputDataName"
                // + ", ?outputDataName"
                + " WHERE "
                + "<"
                + processRun.getURI()
                + "> "
                + "( <"
                + XSLT_INPUT
                + "> "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.INPUTDATAHASNAME)
                + " ?inputDataName"
                // + " . <"
                // + XSLT_INPUT
                // + "> "
                // + JenaProvenanceOntology
                // .bracketify(ProvenanceOntologyConstants.ObjectProperties.OUTPUTDATAHASNAME)
                // + " ?outputDataName"
                + ")";
        iterator = executeQuery(query);
        oneResult = (Map) iterator.next();
        Node inputName = (Node) oneResult.get("inputDataName");
        assertEquals(ProvenanceGenerator.PROCESS_NS
                + "Blast2RDF_in_BLASTreport", inputName.getURI());
        // Node outputName = (Node) oneResult.get("outputDataName");
        // assertEquals(ProvenanceGenerator.PROCESS_NS + "report_out_value",
        // outputName.getURI());

        query = "SELECT ?outputData "
                + "WHERE ?workflowRun ( ?outputData rdf:type "
                + JenaProvenanceOntology.bracketify(ProvenanceVocab.DATA_OBJECT
                        .getURI())
                + " . ?outputData "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.DATA_DERIVED_FROM.getURI())
                + " <"
                + XSLT_INPUT
                + ">) USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
        iterator = executeQuery(query);
        oneResult = (Map) iterator.next();
        Node outputData = (Node) oneResult.get("outputData");
        assertEquals(XSLT_OUTPUT, outputData.getURI());

        // query = "SELECT ?experimenter "
        // + "WHERE "
        // // + "<"
        // // + processRun.getURI()
        // // + "> "
        // +" ( <"
        // + XSLT_OUTPUT
        // + "> rdf:type "
        // + JenaProvenanceOntology
        // .bracketify(ProvenanceOntologyConstants.Classes.DATAOBJECT)
        // + " )( ?experimenter rdf:type "
        // + JenaProvenanceOntology
        // .bracketify(ProvenanceOntologyConstants.Classes.EXPERIMENTER)
        // + " ) USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
        // iterator = executeQuery(query);
        // oneResult = (Map) iterator.next();
        // experimenter = (Node) oneResult.get("experimenter");
        //
        // System.out.println("Experimenter: " + experimenter);

        // triql = "SELECT ?run "
        // + "WHERE ?workflowRun ( ?run rdf:type "
        // + ProvenanceOntology
        // .bracketify(ProvenanceOntologyConstants.Classes.PROCESSRUN)
        // + " )";
        // iterator = executeQuery(triql);
        // while (iterator.hasNext()) {
        // Map oneResult = (Map) iterator.next();
        // Node run = (Node) oneResult.get("run");
        // System.out.println("Run: " + run);
        // }

        query = "SELECT ?mimeType WHERE <"
                + RUN
                + "> (<"
                + ProvenanceGenerator.PROCESS_NS
                + "result> "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.DatatypeProperties.MIMETYPE)
                + " ?mimeType )";
        iterator = executeQuery(query);
        oneResult = (Map) iterator.next();
        Node mimeType = (Node) oneResult.get("mimeType");
        assertEquals("application/rdf+xml", mimeType.getLiteral().getValue()
                .toString());

        query = "SELECT ?output "
                + "WHERE <"
                + RUN
                + "> ( ?workflow "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.WORKFLOWOUTPUT)
                + " ?output ) ";
        iterator = executeQuery(query);
        oneResult = (Map) iterator.next();
        Node output = (Node) oneResult.get("output");
        assertEquals(XSLT_OUTPUT, output.getURI());

        query = "SELECT ?output, ?name "
                + "WHERE ( ?processRun "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.PROCESSOUTPUT)
                + " ?output . ?output "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.OUTPUTDATAHASNAME)
                + " ?name) ";
        iterator = executeQuery(query);
        boolean found = false;
        while (iterator.hasNext()) {
            oneResult = (Map) iterator.next();
            output = (Node) oneResult.get("output");
            Node name = (Node) oneResult.get("name");
            if (name.getURI().equals(
                    ProvenanceGenerator.PROCESS_NS + "Blast2RDF_out_BLASTRDF")) {
                assertEquals(XSLT_OUTPUT, output.getURI());
                found = true;
            }
            System.out.println("Process output = " + output.getURI());
            System.out.println("Data name = " + name.getURI());
        }
        assertTrue("Output with name BLAST2RDF not found", found);

        query = "SELECT ?key, ?value "
                + "WHERE <"
                + processRun.getURI()
                + "> ( <"
                + ProvenanceGenerator.PROCESS_NS
                + "Blast2RDF> "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.HAS_PROPERTY.getURI())
                + " ?property . ?property "
                + JenaProvenanceOntology.bracketify(ProvenanceVocab.KEY
                        .getURI())
                + " ?key . ?property "
                + JenaProvenanceOntology.bracketify(ProvenanceVocab.VALUE
                        .getURI()) + " ?value ) ";
        iterator = executeQuery(query);

        oneResult = (Map) iterator.next();
        Node key = (Node) oneResult.get("key");
        Node value = (Node) oneResult.get("value");
        assertEquals("WorkerClass", key.getLiteral().getValue().toString());
        assertEquals("uk.ac.man.cs.img.mygrid.scuflworkers.Blast2RDF", value
                .getLiteral().getValue().toString());

        query = "SELECT ?className "
                + "WHERE <"
                + processRun.getURI()
                + "> ( <"
                + ProvenanceGenerator.PROCESS_NS
                + "Blast2RDF> "
                + JenaProvenanceOntology.bracketify(ProvenanceVocab.CLASS_NAME
                        .getURI()) + " ?className ) ";
        iterator = executeQuery(query);

        oneResult = (Map) iterator.next();
        Node className = (Node) oneResult.get("className");
        assertEquals(
                "org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor",
                className.getLiteral().getValue().toString());

        query = "SELECT ?initialLSID, ?title, ?author, ?description "
                + "WHERE <"
                + RUN
                + "> ( ?workflow "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.WORKFLOW_TITLE.getURI())
                + " ?title . ?workflow "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.WORKFLOW_INITIAL_LSID
                                .getURI())
                + " ?initialLSID . ?workflow "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.WORKFLOW_AUTHOR.getURI())
                + " ?author . ?workflow "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.WORKFLOW_DESCRIPTION
                                .getURI()) + " ?description) ";
        iterator = executeQuery(query);

        oneResult = (Map) iterator.next();
        Node title = (Node) oneResult.get("title");
        Node author = (Node) oneResult.get("author");
        Node description = (Node) oneResult.get("description");
        Node initialLSID = (Node) oneResult.get("initialLSID");
        assertEquals("BLAST to RDF example", title.getLiteral().getValue()
                .toString());
        assertEquals("Daniele Turi", author.getLiteral().getValue().toString());
        assertEquals(BLAST_WORKFLOW_ID, initialLSID.getLiteral().getValue()
                .toString());
        assertEquals(
                "A simple workflow to show how to transform BLAST XML reports to RDF",
                description.getLiteral().getValue().toString());

        query = "SELECT ?processRun "
                + "WHERE <"
                + RUN
                + "> ( <"
                + RUN
                + "> "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceVocab.EXECUTED_PROCESS_RUN
                                .getURI()) + " ?processRun) ";
        iterator = executeQuery(query);

        int processRunsCount = 0;
        while (iterator.hasNext()) {
            iterator.next();
            processRunsCount++;
        }
        assertEquals("Wrong number of process runs", 2, processRunsCount);

        query = "SELECT ?userPredicate "
                + "WHERE ?workflowRun ( "
                + "<"
                + XSLT_INPUT
                + "> ?userPredicate <"
                + XSLT_OUTPUT
                + "> "
                + ". ?userPredicate rdfs:subPropertyOf "
                + JenaProvenanceOntology
                        .bracketify(ProvenanceOntologyConstants.ObjectProperties.USERPREDICATE)
                + " ) USING rdfs FOR <http://www.w3.org/2000/01/rdf-schema#>";
        iterator = executeQuery(query);
        oneResult = (Map) iterator.next();
        Node userPredicate = (Node) oneResult.get("userPredicate");
        assertEquals(ProvenanceOntology.PROVENANCE_NS + "#xslt", userPredicate
                .getURI());
    }

    protected void tearDown() throws Exception {
        MetadataServiceFactory.getInstance(configuration).removeGraph(RUN);
        super.tearDown();
    }

}
