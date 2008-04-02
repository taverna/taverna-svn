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
 * Filename           $RCSfile: BocaRemoteMetadataServiceTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:59:52 $
 *               by   $Author: stain $
 * Created on 18-Aug-2005
 *****************************************************************/
package uk.org.mygrid.logbook.metadataservice;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceException;
import uk.org.mygrid.logbook.util.DataProvenance;
import uk.org.mygrid.logbook.util.WorkflowRunBean;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.ibm.adtech.boca.query.QueryResult;

/**
 * @author dturi
 * @version $Id: NamedRDFGraphsRepositoryTest.java,v 1.1 2005/08/22 10:29:54
 *          turid Exp $
 */
public class BocaRemoteMetadataServiceTest {

	public static final String RDFS_PREFIX = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> ";

	static private Properties configuration;

	static private BocaRemoteMetadataService metadataService;

	private String aWorkflowRun;

	private String anotherWorkflowRun;

	private String ontologyName;

	private String anIterationProcessRun;

	@BeforeClass
	public static void setUpClass() throws Exception {
		configuration = ProvenanceConfigurator.getConfiguration();
		TestUtils.setTestProperties();
		metadataService = new BocaRemoteMetadataService(configuration);
	}

	@AfterClass
	public static void tearDownClass() {
		if (metadataService != null)
			metadataService.close();
	}

	@Before
	public void setUp() throws Exception {
		aWorkflowRun = "urn:lsid:net.sf.taverna:wfInstance:16894614-190c-4333-acfc-89552eb7f913";
		URL instanceData = ClassLoader.getSystemResource("aWorkflowRun.owl");
		metadataService.storeModel(instanceData, aWorkflowRun);
		anotherWorkflowRun = "urn:lsid:net.sf.taverna:wfInstance:3f87edcb-a19c-4a04-8cbe-0b59c5e24626";
		URL anotherInstanceData = ClassLoader
				.getSystemResource("anotherWorkflowRun.owl");
		metadataService.storeModel(anotherInstanceData, anotherWorkflowRun);
		anIterationProcessRun = "urn:www.mygrid.org.uk/process_run#1169047932090";
		URL anIterationInstanceData = ClassLoader
				.getSystemResource("anIterationProcessRun.owl");
		metadataService.storeModel(anIterationInstanceData,
				anIterationProcessRun);
		ontologyName = "http://www.mygrid.org.uk/provenance";
		URL ontologyUrl = ClassLoader.getSystemResource("provenance.owl");
		metadataService.storeModel(ontologyUrl, ontologyName);
	}

	@After
	public void tearDown() throws Exception {
		if (metadataService == null)
			return;
		metadataService.removeGraph(aWorkflowRun);
		metadataService.removeGraph(anotherWorkflowRun);
		metadataService.removeGraph(ontologyName);
		metadataService.removeGraph(anIterationProcessRun);
	}

	@Test
	public void testGetSimilarData() throws Exception {
		String dataLSID = "urn:lsid:net.sf.taverna:dataCollection:"
				+ "c0ea0ad9-db75-44cd-9a05-20973cb9e2bf";
		Map<String, DataProvenance> similarData = metadataService.getSimilarData(dataLSID);
		assertEquals(2, similarData.size());
	}

	@Test
	public void testQuery() throws Exception {
		Map<String, WorkflowRunBean> workflowRunBeans = new HashMap<String, WorkflowRunBean>();
		String query = "SELECT * " + "WHERE { " + "?workflowRunId <"
				+ ProvenanceVocab.START_TIME.getURI() + "> ?date "
				+ ". ?workflowRunId <" + ProvenanceVocab.RUNS_WORKFLOW.getURI()
				+ "> ?workflow . ?workflow <"
				+ ProvenanceVocab.WORKFLOW_INITIAL_LSID.getURI()
				+ "> ?workflowInitialId . OPTIONAL { ?workflow   <"
				+ ProvenanceVocab.WORKFLOW_AUTHOR.getURI()
				+ "> ?author } . OPTIONAL { ?workflow  <"
				+ ProvenanceVocab.WORKFLOW_TITLE.getURI()
				+ "> ?title } . OPTIONAL { ?workflow <"
				+ ProvenanceVocab.WORKFLOW_DESCRIPTION.getURI()
				+ "> ?description  } " + " }";
		QueryResult result;
		TupleQueryResult results = null;
		result = metadataService.query(query);
		results = result.getSelectResult();
		while (results.hasNext()) {
			BindingSet sol = results.next();
			WorkflowRunBean workflowRunBean = new WorkflowRunBean();
			String workflowRunId = sol.getValue("workflowRunId").toString();
			workflowRunBean.setWorkflowRunId(workflowRunId);
			String date = sol.getValue("date").toString();
			workflowRunBean.setDate(date);

			workflowRunBeans.put(workflowRunId.toString(), workflowRunBean);
			String workflowInitialId = sol.getValue("workflowInitialId")
					.toString();
			workflowRunBean.setWorkflowInitialId(workflowInitialId);
			// workflowInitialIds.add(workflowInitialId);
			String workflowId = sol.getValue("workflow").toString();
			workflowRunBean.setWorkflowId(workflowId);
			Value optionalValue = sol.getValue("title");
			if (optionalValue != null) {
				String title = optionalValue.toString();
				workflowRunBean.setTitle(title);
			}
			optionalValue = sol.getValue("author");
			if (optionalValue != null) {
				String author = optionalValue.toString();
				workflowRunBean.setAuthor(author);
			}
			optionalValue = sol.getValue("description");
			if (optionalValue != null) {
				String description = optionalValue.toString();
				workflowRunBean.setDescription(description);
			}
		}
		if (results != null)
			results.close();
		Collection<WorkflowRunBean> values = workflowRunBeans.values();
		for (WorkflowRunBean bean : values) {
			System.out.println(bean.getWorkflowId());
			Assert.assertNotNull(bean.getTitle());
		}
		assertEquals(3, workflowRunBeans.size());
		// assertEquals(5, processRunIterationsBeans.size());
		// Set<ProcessRunBean> iterations =
		// processRunIterationsBeans.get("1173976339064");
		// assertEquals(1, iterations.size());
		// ProcessRunBean processRunBean = iterations.iterator().next();
		// assertEquals("urn:www.mygrid.org.uk/process_run#1173976328817",
		// processRunBean.getProcessRunId());
		// assertEquals("getComicStrip", processRunBean.getProcess());
		// assertEquals(1, processRunBean.getProcessIterations().size());

		// assertEquals(workflowRunsToProcessRuns.size(),
		// metadataService.getProcessRunBeans().size());
		//		
		// Map<String, WorkflowRunBean> workflowRunBeans =
		// metadataService.getWorkflowRunBeans();
		//		
		// Set<String> keySet = workflowRunBeans.keySet();
		// Set<String> keySet2 = workflowRunsToProcessRuns.keySet();
		// keySet.removeAll(keySet2);
		// for (String string : keySet) {
		// System.out.println(workflowRunBeans.get(string).getTitle());
		// }
	}

	@Test
	public void testGetUserWorkflows() throws Exception {
		List<String> workflowRuns = metadataService
				.getUserWorkFlows(ProvenanceConfigurator.DEFAULT_EXPERIMENTER);
		assertEquals(2, workflowRuns.size());
		assertTrue(workflowRuns.contains(aWorkflowRun));
		assertTrue(workflowRuns.contains(anotherWorkflowRun));
	}

	@Test
	public void testGetWorkflowOutputs() throws Exception {
		List<String> outputs = metadataService.getWorkflowOutputs(aWorkflowRun);
		for (String output : outputs) {
			System.out.println(output);
		}
		assertEquals(4, outputs.size());
	}

	@Test
	public void testIsProcessIteration() throws Exception {
		boolean processIteration = metadataService
				.isProcessIteration("urn:www.mygrid.org.uk/process_run#1169047932090");
		assertTrue(processIteration);
		processIteration = metadataService
				.isProcessIteration("urn:www.mygrid.org.uk/process_run#1166112167974");
		assertFalse(processIteration);
		// Thread.currentThread().sleep(3600000);
	}

	@Test
	public void testGetObjectPropertyValues() throws Exception {
		List<String> objectPropertyValues = metadataService
				.getObjectPropertyValues(
						"urn:lsid:net.sf.taverna:wfInstance:16894614-190c-4333-acfc-89552eb7f913",
						ProvenanceVocab.RUNS_WORKFLOW.getURI());
		assertEquals(1, objectPropertyValues.size());
		assertEquals(
				"urn:lsid:net.sf.taverna:wfDefinition:a88d52eb-aca0-49e5-ae4c-8deaf394831b",
				objectPropertyValues.get(0));
		assertEquals(
				"urn:lsid:net.sf.taverna:wfDefinition:a88d52eb-aca0-49e5-ae4c-8deaf394831b",
				metadataService
						.getFirstObjectPropertyValue(
								"urn:lsid:net.sf.taverna:wfInstance:16894614-190c-4333-acfc-89552eb7f913",
								ProvenanceVocab.RUNS_WORKFLOW.getURI()));
	}

	// @Test
	// public void testQuerying() throws Exception {
	// Set<String> namedGraphs = new HashSet<String>();
	// namedGraphs.add(ontologyName);
	// namedGraphs.add(aWorkflowRun);
	//
	// String sparqlQuery = "SELECT ?wf WHERE { ?workflowRun <"
	// + ProvenanceVocab.RUNS_WORKFLOW.getURI() + "> ?wf }";
	// System.out.println(sparqlQuery);
	// Graph graph = metadataService.query(sparqlQuery, namedGraphs);
	// RDFInput rdfi = new RDFInput(ModelFactory.createModelForGraph(graph));
	// assertTrue(rdfi.hasNext());
	// QuerySolution qs = rdfi.nextSolution();
	// RDFNode wf = qs.get("wf");
	// assertEquals(
	// "urn:lsid:net.sf.taverna:wfDefinition:a88d52eb-aca0-49e5-ae4c-8deaf394831b",
	// wf.toString());
	// assertTrue(!rdfi.hasNext());
	//
	// graph = metadataService.query(sparqlQuery);
	// Set<String> retrievedWorkflows = new HashSet<String>();
	// graph = metadataService.query(sparqlQuery);
	// rdfi = new RDFInput(ModelFactory.createModelForGraph(graph));
	// assertTrue(rdfi.hasNext());
	// while (rdfi.hasNext()) {
	// qs = rdfi.nextSolution();
	// wf = qs.get("wf");
	// retrievedWorkflows.add(wf.toString());
	// }
	// assertTrue("The provenance of at least 2 distinct workflows is stored",
	// retrievedWorkflows.size() >= 2);
	//
	// sparqlQuery = "SELECT ?wf WHERE { GRAPH <" + aWorkflowRun
	// + "> { ?workflowRun <" + ProvenanceVocab.RUNS_WORKFLOW.getURI()
	// + "> ?wf } }";
	// System.out.println(sparqlQuery);
	// graph = metadataService.query(sparqlQuery, new HashSet<String>(),
	// namedGraphs);
	// rdfi = new RDFInput(ModelFactory.createModelForGraph(graph));
	// assertTrue(rdfi.hasNext());
	// qs = rdfi.nextSolution();
	// wf = qs.get("wf");
	// assertEquals(
	// "urn:lsid:net.sf.taverna:wfDefinition:a88d52eb-aca0-49e5-ae4c-8deaf394831b",
	// wf.toString());
	// assertTrue(!rdfi.hasNext());
	//
	// sparqlQuery = RDFS_PREFIX + "SELECT ?p WHERE { ?p rdfs:subPropertyOf <"
	// + ProvenanceVocab.RUNS.getURI() + "> }";
	// System.out.println(sparqlQuery);
	// retrievedWorkflows = new HashSet<String>();
	// graph = metadataService.query(sparqlQuery, namedGraphs);
	// rdfi = new RDFInput(ModelFactory.createModelForGraph(graph));
	// assertTrue("Subproperty works", rdfi.hasNext());
	// qs = rdfi.nextSolution();
	// RDFNode p = qs.get("p");
	// System.out.println(p);
	//
	// sparqlQuery = RDFS_PREFIX + "SELECT ?p WHERE { ?p rdfs:subPropertyOf <"
	// + ProvenanceVocab.RUNS.getURI() + "> }";
	// System.out.println(sparqlQuery);
	// retrievedWorkflows = new HashSet<String>();
	// Set<String> namedGraphsWithoutOntology = new HashSet<String>();
	// namedGraphsWithoutOntology.add(aWorkflowRun);
	// graph = metadataService.query(sparqlQuery, namedGraphsWithoutOntology);
	// rdfi = new RDFInput(ModelFactory.createModelForGraph(graph));
	// assertTrue("Subproperty needs ontology", !rdfi.hasNext());
	//
	// sparqlQuery = RDFS_PREFIX
	// + "SELECT ?wf WHERE { ?workflowRun ?p ?wf . ?p rdfs:subPropertyOf <"
	// + ProvenanceVocab.RUNS.getURI() + "> }";
	// System.out.println(sparqlQuery);
	// retrievedWorkflows = new HashSet<String>();
	// graph = metadataService.query(sparqlQuery, namedGraphs);
	// rdfi = new RDFInput(ModelFactory.createModelForGraph(graph));
	// assertTrue("Subproperty works", rdfi.hasNext());
	// qs = rdfi.nextSolution();
	// wf = qs.get("wf");
	// assertEquals(
	// "urn:lsid:net.sf.taverna:wfDefinition:a88d52eb-aca0-49e5-ae4c-8deaf394831b",
	// wf.toString());
	// assertTrue(!rdfi.hasNext());
	// }
	//
	// @Test
	// public void testAddQuad() throws Exception {
	// String sparqlQuery = "SELECT ?s ?o WHERE { GRAPH <" + aWorkflowRun
	// + "> { ?s <" + ProvenanceVocab.RUNS_PROCESS.getURI()
	// + "> ?o } }";
	// Set<String> namedGraph = new HashSet<String>();
	// namedGraph.add(aWorkflowRun);
	// Graph results = metadataService.query(sparqlQuery,
	// new HashSet<String>(), namedGraph);
	// RDFInput rdfi = new RDFInput(ModelFactory.createModelForGraph(results));
	// assertTrue(!rdfi.hasNext());
	//
	// String processRun = "urn:aProcessRun";
	// String process = "urn:aProcess";
	// metadataService.addQuad(aWorkflowRun, processRun,
	// ProvenanceVocab.RUNS_PROCESS.getURI(), process);
	//
	// results = metadataService.query(sparqlQuery, new HashSet<String>(),
	// namedGraph);
	// rdfi = new RDFInput(ModelFactory.createModelForGraph(results));
	// assertTrue(rdfi.hasNext());
	// QuerySolution qs = rdfi.nextSolution();
	// RDFNode s = qs.get("s");
	// assertEquals(processRun, s.toString());
	// RDFNode o = qs.get("o");
	// assertEquals(process, o.toString());
	// assertTrue(!rdfi.hasNext());
	//
	// String retrievedGraph = metadataService.retrieveGraph(aWorkflowRun);
	// System.out.println(retrievedGraph);
	// }

	@Test
	public void testRetrieve() throws Exception {
		String nullGraph = metadataService
				.retrieveGraph("urn:lsid:www.mygrid.org.uk:experimentinstance:1");
		assertNull(nullGraph);

	}

	@Test
	public void testStoreRDFGraph() throws Exception {
		String rdfGraph = "<rdf:RDF "
				+ "xmlns:j.0=\"http://www.mygrid.org/provenance#\" "
				+ "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >"
				+ "<rdf:Description rdf:nodeID=\"A0\"><j.0:location>/tmp/eg.tri</j.0:location>"
				+ "<rdf:type rdf:resource=\"http://www.mygrid.org/provenance#TriInput\"/>"
				+ "</rdf:Description></rdf:RDF>";
		String graphName = "urn:test:storeRDFGraph";
		storeDisplayRemove(metadataService, rdfGraph, graphName);
		Model emptyModel = metadataService.retrieveGraphModel(graphName);
		assertEquals(0, emptyModel.size());

		String string = "'text/plain'";
		string = string.replaceAll("\\'", "");
		rdfGraph = "<rdf:RDF "
				+ "xmlns:j.0=\"http://www.mygrid.org/provenance#\" "
				+ "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >"
				+ "<rdf:Description rdf:about=\"http://www.mygrid.org/provenance#:report_out_value\">"
				+ "<j.0:dataSyntacticType rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">"
				+ string + "</j.0:dataSyntacticType>"
				+ "</rdf:Description></rdf:RDF>";

		// "''"^^http://www.w3.org/2001/XMLSchema#string;
		graphName = "urn:test:storeRDFGraph2";
		storeDisplayRemove(metadataService, rdfGraph, graphName);
	}

	private void storeDisplayRemove(BocaRemoteMetadataService metadataService,
			String rdfGraph, String graphName) throws MetadataServiceException {
		metadataService.storeRDFGraph(rdfGraph, graphName);
		String retrievedGraph = metadataService.retrieveGraph(graphName);
		System.out.println(retrievedGraph);
		metadataService.removeGraph(graphName);
	}

}
