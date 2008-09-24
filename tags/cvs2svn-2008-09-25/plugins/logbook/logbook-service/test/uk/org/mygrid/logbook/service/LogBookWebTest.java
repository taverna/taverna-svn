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
 * Filename           $RCSfile: LogBookWebTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 13:00:26 $
 *               by   $Author: stain $
 * Created on 6 Feb 2007
 *****************************************************************/
package uk.org.mygrid.logbook.service;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

public class LogBookWebTest {

    private String aWorkflowRun;

    private String anotherWorkflowRun;

    private String ontologyName;

    private String anIterationProcessRun;

    static private LogBookWeb logBookWebService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        logBookWebService = new LogBookWeb();
    }

    @Before
    public void setUp() throws Exception {
        aWorkflowRun = "urn:lsid:net.sf.taverna:wfInstance:16894614-190c-4333-acfc-89552eb7f913";
        URL instanceData = ClassLoader.getSystemResource("aWorkflowRun.owl");
        logBookWebService.storeModelFromURL(instanceData.toString(),
                aWorkflowRun);
        anotherWorkflowRun = "urn:lsid:net.sf.taverna:wfInstance:3f87edcb-a19c-4a04-8cbe-0b59c5e24626";
        URL anotherInstanceData = ClassLoader
                .getSystemResource("anotherWorkflowRun.owl");
        logBookWebService.storeModelFromURL(anotherInstanceData.toString(),
                anotherWorkflowRun);
        anIterationProcessRun = "urn:www.mygrid.org.uk/process_run#1169047932090";
        URL anIterationInstanceData = ClassLoader
                .getSystemResource("anIterationProcessRun.owl");
        logBookWebService.storeModelFromURL(anIterationInstanceData.toString(),
                anIterationProcessRun);
        ontologyName = "http://www.mygrid.org.uk/provenance";
        URL ontologyUrl = ClassLoader.getSystemResource("provenance.owl");
        logBookWebService.storeModelFromURL(ontologyUrl.toString(),
                ontologyName);
    }

    @After
    public void tearDown() throws Exception {
        if (logBookWebService == null)
            return;
        logBookWebService.removeGraph(aWorkflowRun);
        logBookWebService.removeGraph(anotherWorkflowRun);
        logBookWebService.removeGraph(ontologyName);
        logBookWebService.removeGraph(anIterationProcessRun);
    }
    
    @Test
    public void testDataSyntacticTypes() throws Exception {
        String input = "urn:lsid:net.sf.taverna:dataItem:986509ec-80a7-4fb1-833d-21dc028e3100";
        String[] types = logBookWebService.getDataSyntacticTypes(input);
        assertEquals(1, types.length);
        assertEquals("text/plain,text/x-taverna-web-url", types[0]);
    }

    @Test
    public void testDataPortNames() throws Exception {
        String input = "urn:lsid:net.sf.taverna:dataItem:986509ec-80a7-4fb1-833d-21dc028e3100";
        String[] inputNames = logBookWebService.getInputDataPortNames(input);
        assertEquals(1, inputNames.length);
        assertEquals("base", inputNames[0]);
        String[] names = logBookWebService.getDataPortNames(input);
        assertEquals("base", names[0]);
        String output = "urn:lsid:net.sf.taverna:dataCollection:37d44296-4932-48f2-b16f-a627e9fe133c";
        String[] outputNames = logBookWebService.getOutputDataPortNames(output);
        assertEquals(1, outputNames.length);
        assertEquals("MMusIDs", outputNames[0]);
    }

    @Test
    public void testGetWorkflow() throws Exception {
        String workflowId = "urn:lsid:net.sf.taverna:wfDefinition:21dcd543-66ca-467e-8525-8ed876917e38";
        String workflow = logBookWebService.getWorkflow(workflowId);
        assertNotNull("Found workflow", workflow);
        System.out.println(workflow);
        String workflowExecutedByRun = logBookWebService
                .getWorkflowExecutedByRun("urn:lsid:net.sf.taverna:wfInstance:889eb356-6128-4306-9a95-46f25ce76ac3");
        assertEquals(workflow, workflowExecutedByRun);
    }

    @Test
    public void testData() throws Exception {
        String dataCollection = "urn:lsid:net.sf.taverna:dataCollection:f9e32ab8-40e7-40c6-afab-983c52968d4e";
        String byteDataItem = "urn:lsid:net.sf.taverna:dataItem:10053166-a5e3-4b83-a556-fbfcf3ad2311";
        String stringDataItem = "urn:lsid:net.sf.taverna:dataItem:6b1de7a4-762a-4d86-9188-6a3a440d50a0";
        String value = "http://www.dilbert.com/";
        assertEquals(value, logBookWebService.getStringDataItem(stringDataItem));
        assertEquals(26872,
                logBookWebService.getBytesDataItem(byteDataItem).length);
        String[] dataCollectionLSIDs = logBookWebService
                .getDataCollectionLSIDs(dataCollection);
        assertEquals(byteDataItem, dataCollectionLSIDs[0]);
        dataCollectionLSIDs = logBookWebService
                .getDataCollectionLSIDs("urn:lsid:net.sf.taverna:dataCollection:b5a24e0e-5d57-4b5c-90d1-238fef8aafb6");
        assertEquals(31, dataCollectionLSIDs.length);
    }

    @Test
    public void testGetNonNestedProcessRuns() throws Exception {
        String[] runs = logBookWebService
                .getNonNestedProcessRuns("urn:lsid:net.sf.taverna:wfInstance:cd8ec7d9-ee6e-4de6-8724-b8899cf11f12");
        for (String run : runs) {
            System.out.println(run);
        }
        assertEquals(5, runs.length);
    }

    @Test
    public void testGetWorkflowRun() throws Exception {
        String workflowRun = logBookWebService
                .getWorkflowRun("urn:lsid:net.sf.taverna:wfInstance:" +
                // "889eb356-6128-4306-9a95-46f25ce76ac3"
                        "cd8ec7d9-ee6e-4de6-8724-b8899cf11f12");
        System.out.println(workflowRun);
    }

    @Test
    public void testRetrieve() throws Exception {
        String nullGraph = logBookWebService
                .retrieveGraph("urn:lsid:www.mygrid.org.uk:experimentinstance:1");
        assertNull(nullGraph);
    }

    @Test
    public void testGetUserWorkflows() throws Exception {
        List<String> workflowRuns = Arrays.asList(logBookWebService
                .getUserWorkFlows(ProvenanceConfigurator.DEFAULT_EXPERIMENTER));
        assertTrue(workflowRuns.contains(aWorkflowRun));
        assertTrue(workflowRuns.contains(anotherWorkflowRun));
    }

    @Test
    public void testGetWorkflowOutputs() throws Exception {
        String[] outputs = logBookWebService.getWorkflowOutputs(aWorkflowRun);
        for (String output : outputs) {
            System.out.println(output);
        }
        assertEquals(4, outputs.length);
    }

    @Test
    public void testIsProcessIteration() throws Exception {
        boolean processIteration = logBookWebService
                .isProcessIteration("urn:www.mygrid.org.uk/process_run#1169047932090");
        assertTrue(processIteration);
        processIteration = logBookWebService
                .isProcessIteration("urn:www.mygrid.org.uk/process_run#1166112167974");
        assertFalse(processIteration);
        // Thread.currentThread().sleep(3600000);
    }

    @Test
    public void testGetObjectPropertyValues() throws Exception {
        String[] objectPropertyValues = logBookWebService
                .getObjectPropertyValues(
                        "urn:lsid:net.sf.taverna:wfInstance:16894614-190c-4333-acfc-89552eb7f913",
                        ProvenanceVocab.RUNS_WORKFLOW.getURI());
        assertEquals(1, objectPropertyValues.length);
        assertEquals(
                "urn:lsid:net.sf.taverna:wfDefinition:a88d52eb-aca0-49e5-ae4c-8deaf394831b",
                objectPropertyValues[0]);
        assertEquals(
                "urn:lsid:net.sf.taverna:wfDefinition:a88d52eb-aca0-49e5-ae4c-8deaf394831b",
                logBookWebService
                        .getFirstObjectPropertyValue(
                                "urn:lsid:net.sf.taverna:wfInstance:16894614-190c-4333-acfc-89552eb7f913",
                                ProvenanceVocab.RUNS_WORKFLOW.getURI()));
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
        storeDisplayRemove(rdfGraph, graphName);

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
        storeDisplayRemove(rdfGraph, graphName);
    }

    private void storeDisplayRemove(String rdfGraph, String graphName) {
        logBookWebService.storeRDFGraph(rdfGraph, graphName);
        String retrievedGraph = logBookWebService.retrieveGraph(graphName);
        System.out.println(retrievedGraph);
        logBookWebService.removeGraph(graphName);
    }

}
