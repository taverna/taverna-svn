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
 * Last modified on   $Date: 2007-12-14 12:59:52 $
 *               by   $Author: stain $
 * Created on 04-Oct-2005
 *****************************************************************/
package uk.org.mygrid.logbook.reporter.boca;

import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyConstants;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.JenaProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceFactory;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ProvenanceGenerator;
import uk.org.mygrid.logbook.metadataservice.BocaRemoteMetadataService;
import uk.org.mygrid.logbook.metadataservice.BocaRemoteMetadataServiceTest;
import uk.org.mygrid.logbook.reporter.LogBookReporterTests;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.ibm.adtech.boca.model.Constants;

public class ExecutionTest extends LogBookReporterTests {

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
			execute(BLAST_WORKFLOW);
		}

		Model instanceData = metadataService.retrieveGraphModel(RUN);
		TestUtils.writeOut(instanceData);

		TupleQueryResult queryResult;

		query = "SELECT ?workflowRun "
				+ "WHERE { ?workflowRun "
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
				+ " }";
		queryResult = executeQuery(query);
		BindingSet bindingSet = queryResult.next();
		Value workflow = bindingSet.getValue("workflowRun");
		String uri = workflow.toString();
		System.out.println(uri);

		assertEquals(RUN, uri);

		// query = "SELECT ?workflowRun ?organization "
		// + "WHERE { GRAPH ?workflowRun {"
		// + JenaProvenanceOntology
		// .bracketify(ProvenanceConfigurator.DEFAULT_EXPERIMENTER)
		// + JenaProvenanceOntology.bracketify(ProvenanceVocab.BELONGS_TO
		// .getURI()) + " ?organization } }";
		// rdfInput = executeQuery(query, Constants.allNamedGraphsUri);
		//
		// qs = rdfInput.nextSolution();
		// workflow = qs.get("workflowRun").asNode();
		// assertEquals(RUN, workflow.getURI());
		// Node organization = qs.get("organization").asNode();
		// String organizationUri = organization.getURI();

		// assertEquals(ProvenanceConfigurator.DEFAULT_ORGANIZATION,
		// organizationUri);

		query = BocaRemoteMetadataService.RDF_PREFIX
				+ "SELECT ?experimenter WHERE { GRAPH "
				+ JenaProvenanceOntology.bracketify(RUN)
				+ " { ?experimenter rdf:type "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.EXPERIMENTER.getURI())
				+ " } }";
		queryResult = executeQuery(query, RUN);
		bindingSet = queryResult.next();
		Value experimenter = bindingSet.getValue("experimenter");
		String experimenterUri = experimenter.toString();

		assertEquals(ProvenanceConfigurator.DEFAULT_EXPERIMENTER,
				experimenterUri);

		query = "SELECT ?processRun ?time "
				+ "WHERE { GRAPH ?workflowRun { ?processRun "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceOntologyConstants.ObjectProperties.RUNSPROCESS)
				+ " <"
				+ ProvenanceGenerator.PROCESS_NS
				+ "Blast2RDF> . ?processRun "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceOntologyConstants.DatatypeProperties.ENDTIME)
				+ " ?time } }";
		queryResult = executeQuery(query, Constants.allNamedGraphsUri);
		bindingSet = queryResult.next();
		Value processRun = bindingSet.getValue("processRun");
		assertNotNull("processRun for Blast2RDF is not null", processRun);
		System.out.println("Run id for Blast2RDF: " + processRun);
		Value time = bindingSet.getValue("time");
		assertNotNull("end time for Blast2RDF is not null", time);
		System.out.println("End time for Blast2RDF: " + time);

		Model retrievedModel = metadataService.retrieveGraphModel(processRun
				.toString());
		TestUtils.writeOut(retrievedModel);

		query = "SELECT ?inputDataName"
				// + ", ?outputDataName"
				+ " WHERE { GRAPH "
				+ "<"
				+ processRun.toString()
				+ "> "
				+ "{ <"
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
				+ "} }";
		queryResult = executeQuery(query, processRun.toString());
		bindingSet = queryResult.next();
		Value inputName = bindingSet.getValue("inputDataName");
		assertEquals(ProvenanceGenerator.PROCESS_NS
				+ "Blast2RDF_in_BLASTreport", inputName.toString());
		// Node outputName = (Node) oneResult.get("outputDataName");
		// assertEquals(ProvenanceGenerator.PROCESS_NS + "report_out_value",
		// outputName.getURI());

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

		query = "SELECT ?mimeType WHERE { GRAPH <"
				+ RUN
				+ "> { <"
				+ ProvenanceGenerator.PROCESS_NS
				+ "result> "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceOntologyConstants.DatatypeProperties.MIMETYPE)
				+ " ?mimeType } }";
		queryResult = executeQuery(query, RUN);
		bindingSet = queryResult.next();
		Value mimeType = bindingSet.getValue("mimeType");
		assertEquals("application/rdf+xml", ((Literal) mimeType).getLabel());

		query = "SELECT ?output "
				+ "WHERE { GRAPH <"
				+ RUN
				+ "> { ?workflow "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceOntologyConstants.ObjectProperties.WORKFLOWOUTPUT)
				+ " ?output } }";
		queryResult = executeQuery(query, RUN);
		bindingSet = queryResult.next();
		Value output = bindingSet.getValue("output");
		assertEquals(XSLT_OUTPUT, output.toString());

		query = "SELECT ?output ?name "
				+ "WHERE { ?processRun "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceOntologyConstants.ObjectProperties.PROCESSOUTPUT)
				+ " ?output . ?output "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceOntologyConstants.ObjectProperties.OUTPUTDATAHASNAME)
				+ " ?name }";
		queryResult = executeQuery(query);
		boolean found = false;
		while (queryResult.hasNext()) {
			bindingSet = queryResult.next();
			output = bindingSet.getValue("output");
			Value name = bindingSet.getValue("name");
			if (name.toString().equals(
					ProvenanceGenerator.PROCESS_NS + "Blast2RDF_out_BLASTRDF")) {
				assertEquals(XSLT_OUTPUT, output.toString());
				found = true;
			}
			System.out.println("Process output = " + output.toString());
			System.out.println("Data name = " + name.toString());
		}
		assertTrue("Output with name BLAST2RDF not found", found);

		query = "SELECT ?key ?value "
				+ "WHERE { GRAPH <"
				+ processRun.toString()
				+ "> { <"
				+ ProvenanceGenerator.PROCESS_NS
				+ "Blast2RDF> "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.HAS_PROPERTY.getURI())
				+ " ?property . ?property "
				+ JenaProvenanceOntology.bracketify(ProvenanceVocab.KEY
						.getURI())
				+ " ?key . ?property "
				+ JenaProvenanceOntology.bracketify(ProvenanceVocab.VALUE
						.getURI()) + " ?value } } ";
		queryResult = executeQuery(query, processRun.toString());
		bindingSet = queryResult.next();
		Value key = bindingSet.getValue("key");
		Value value = bindingSet.getValue("value");
		assertEquals("WorkerClass", key.toString());
		assertEquals("uk.ac.man.cs.img.mygrid.scuflworkers.Blast2RDF", value
				.toString());

		query = "SELECT ?className "
				+ "WHERE { GRAPH <"
				+ processRun.toString()
				+ "> { <"
				+ ProvenanceGenerator.PROCESS_NS
				+ "Blast2RDF> "
				+ JenaProvenanceOntology.bracketify(ProvenanceVocab.CLASS_NAME
						.getURI()) + " ?className } } ";
		queryResult = executeQuery(query, processRun.toString());

		bindingSet = queryResult.next();
		Value className = bindingSet.getValue("className");
		assertEquals(
				"org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor",
				className.toString());

		query = "SELECT ?processRun "
				+ "WHERE { GRAPH <"
				+ RUN
				+ "> { <"
				+ RUN
				+ "> "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.EXECUTED_PROCESS_RUN
								.getURI()) + " ?processRun }} ";
		queryResult = executeQuery(query, RUN);

		int processRunsCount = 0;
		while (queryResult.hasNext()) {
			queryResult.next();
			processRunsCount++;
		}
		assertEquals("Wrong number of process runs", 2, processRunsCount);

		query = BocaRemoteMetadataServiceTest.RDFS_PREFIX
				+ "SELECT ?userPredicate WHERE { "
				+ "<"
				+ XSLT_INPUT
				+ "> ?userPredicate <"
				+ XSLT_OUTPUT
				+ "> "
				+ ". ?userPredicate rdfs:subPropertyOf "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceOntologyConstants.ObjectProperties.USERPREDICATE)
				+ " }";
		queryResult = executeQuery(query);
		bindingSet = queryResult.next();
		Value userPredicate = bindingSet.getValue("userPredicate");
		assertEquals(ProvenanceOntology.PROVENANCE_NS + "#xslt", userPredicate
				.toString());

		query = BocaRemoteMetadataService.RDF_PREFIX
				+ "SELECT ?outputData "
				+ "WHERE { ?outputData rdf:type "
				+ JenaProvenanceOntology.bracketify(ProvenanceVocab.DATA_OBJECT
						.getURI())
				+ " . ?outputData "
				+ JenaProvenanceOntology
						.bracketify(ProvenanceVocab.DATA_DERIVED_FROM.getURI())
				+ " <" + XSLT_INPUT + "> }";
		queryResult = executeQuery(query);
		bindingSet = queryResult.next();
		Value outputData = bindingSet.getValue("outputData");
		assertEquals(XSLT_OUTPUT, outputData.toString());

		query = "SELECT ?initialLSID ?title ?author ?description "
				+ "WHERE { GRAPH <"
				+ RUN
				+ "> { ?workflow "
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
								.getURI()) + " ?description }}";
		queryResult = executeQuery(query, RUN);

		bindingSet = queryResult.next();
		Value title = bindingSet.getValue("title");
		Value author = bindingSet.getValue("author");
		Value description = bindingSet.getValue("description");
		Value initialLSID = bindingSet.getValue("initialLSID");
		assertEquals("BLAST to RDF example", title.toString());
		assertEquals("Daniele Turi", author.toString());
		assertEquals(BLAST_WORKFLOW_ID, initialLSID.toString());
		assertEquals(
				"A simple workflow to show how to transform BLAST XML reports to RDF",
				description.toString());
	}

	protected void tearDown() throws Exception {
		MetadataServiceFactory.getInstance(configuration).removeGraph(RUN);
		super.tearDown();
	}

}
