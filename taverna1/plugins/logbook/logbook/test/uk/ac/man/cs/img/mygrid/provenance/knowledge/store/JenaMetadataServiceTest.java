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
 * Filename           $RCSfile: JenaMetadataServiceTest.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:53:39 $
 *               by   $Author: stain $
 * Created on 18-Aug-2005
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.store;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.jena.ProvenanceVocab;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;
import uk.org.mygrid.provenance.util.TestUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author dturi
 * @version $Id: NamedRDFGraphsRepositoryTest.java,v 1.1 2005/08/22 10:29:54
 *          turid Exp $
 */
public class JenaMetadataServiceTest {

    static private Properties configuration;

    private String aWorkflowRun;

    private String anotherWorkflowRun;

    private String ontologyName;

    private String anIterationProcessRun;

    static private MetadataService metadataService;

    @BeforeClass
    public static void setUpClass() throws Exception {
        configuration = ProvenanceConfigurator.getMetadataStoreConfiguration();
        TestUtils.setTestProperties();
        metadataService = new JenaMetadataService(configuration);
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
    public void testDataSyntacticTypes() throws Exception {
        String input = "urn:lsid:net.sf.taverna:dataItem:986509ec-80a7-4fb1-833d-21dc028e3100";
        Set<String> types = metadataService.getDataSyntacticTypes(input);
        assertEquals(1, types.size());
        assertTrue(types.contains("text/plain,text/x-taverna-web-url"));
    }

    @Test
    public void testDataPortNames() throws Exception {
        String input = "urn:lsid:net.sf.taverna:dataItem:986509ec-80a7-4fb1-833d-21dc028e3100";
        List<String> inputNames = metadataService.getInputDataPortNames(input);
        assertEquals(1, inputNames.size());
        assertEquals("base", inputNames.get(0));
        List<String> names = metadataService.getDataPortNames(input);
        assertEquals("base", names.get(0));
        String output = "urn:lsid:net.sf.taverna:dataCollection:37d44296-4932-48f2-b16f-a627e9fe133c";
        List<String> outputNames = metadataService
                .getOutputDataPortNames(output);
        assertEquals(1, outputNames.size());
        assertEquals("MMusIDs", outputNames.get(0));
    }

    @Test
    public void testGetDataCollectionLSIDs() throws Exception {
        List<String> lsids = metadataService
                .getDataCollectionLSIDs("urn:lsid:net.sf.taverna:dataCollection:b5a24e0e-5d57-4b5c-90d1-238fef8aafb6");
        for (String lsid : lsids) {
            System.out.println(lsid);
        }
        assertEquals(31, lsids.size());
    }

    @Test
    public void testGetNonNestedProcessRuns() throws Exception {
        List<String> runs = metadataService
                .getNonNestedProcessRuns("urn:lsid:net.sf.taverna:wfInstance:" +
                // "889eb356-6128-4306-9a95-46f25ce76ac3"
                        "cd8ec7d9-ee6e-4de6-8724-b8899cf11f12");
        for (String run : runs) {
            System.out.println(run);
        }
        assertEquals(5, runs.size());
    }

    @Test
    public void testGetWorkflowRun() throws Exception {
        String workflowRun = metadataService
                .getWorkflowRun("urn:lsid:net.sf.taverna:wfInstance:" +
                // "889eb356-6128-4306-9a95-46f25ce76ac3"
                        "cd8ec7d9-ee6e-4de6-8724-b8899cf11f12");
        System.out.println(workflowRun);
    }

    @Test
    public void testRetrieve() throws Exception {
        String nullGraph = metadataService
                .retrieveGraph("urn:lsid:www.mygrid.org.uk:experimentinstance:1");
        assertNull(nullGraph);
    }

    @Test
    public void testGetUserWorkflows() throws Exception {
        List<String> workflowRuns = metadataService
                .getUserWorkFlows(ProvenanceConfigurator.DEFAULT_EXPERIMENTER);
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

    @Test
    public void testStoreQuad() throws Exception {
        String rdfGraph = "<rdf:RDF "
                + "xmlns:j.0=\"http://www.mygrid.org/provenance#\" "
                + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" >"
                + "<rdf:Description rdf:about=\"http://www.mygrid.org/provenance#:report_out_value\">"
                + "<j.0:dataSyntacticType rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">"
                + "text/plain" + "</j.0:dataSyntacticType>"
                + "</rdf:Description></rdf:RDF>";
        metadataService.storeRDFGraph(rdfGraph, "urn:workflowrun");
        metadataService
                .addQuad("urn:workflowrun", "urn:workflowrun",
                        ProvenanceVocab.EXECUTED_PROCESS_RUN.getURI(),
                        "urn:processrun");
        Model model = ModelFactory.createDefaultModel();
        Resource workflowRun = model.createResource("urn:workflowrun");
        String workFlowRunUri = workflowRun.getURI();
        Resource processRun = model.createResource("urn:processrun");
        Model retrievedGraphModel = metadataService
                .retrieveGraphModel(workFlowRunUri);
        retrievedGraphModel.write(System.out, "N3");
        retrievedGraphModel.contains(workflowRun,
                ProvenanceVocab.EXECUTED_PROCESS_RUN, processRun);
        StmtIterator listStatements = retrievedGraphModel.listStatements();
        int statementsCount = 0;
        while (listStatements.hasNext()) {
            listStatements.next();
            statementsCount++;
        }
        listStatements.close();
        assertEquals(2, statementsCount);
        metadataService.removeGraph(workFlowRunUri);
    }

    private void storeDisplayRemove(String rdfGraph, String graphName)
            throws MetadataServiceException {
        metadataService.storeRDFGraph(rdfGraph, graphName);
        String retrievedGraph = metadataService.retrieveGraph(graphName);
        System.out.println(retrievedGraph);
        metadataService.removeGraph(graphName);
    }

    // public void testStoreDicomData() throws Exception {
    // JenaMetadataService rdfRepository = new JenaMetadataService(
    // ProvenanceConfigurator.getMetadataStoreConfiguration());
    // rdfRepository.storeModel(new URL(
    // "http://www.cs.man.ac.uk/~alanrw/tags.owl"),
    // "urn:lsid:dicom:test:tags1");
    // rdfRepository.storeModel(new File(
    // "etc/ontology/instances/annotated-webbrain.owl").toURL(),
    // "urn:lsid:dicom:test:tags2");
    // String triql = "SELECT ?iod WHERE (?iod rdf:type "
    // + "dicom:MrImageInformationObjectDefinition ) "
    // + "USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
    // + "dicom FOR <http://www.cs.man.ac.uk/~alanrw/dicom.owl#>";
    // System.out.println(triql);
    // Iterator iterator = rdfRepository.query(triql);
    // while (iterator.hasNext()) {
    // Map oneResult = (Map) iterator.next();
    // Node iod = (Node) oneResult.get("iod");
    // System.out.println(iod.getURI());
    // // assertEquals("#MrImageInformationObjectDefinition",
    // // iod.getURI());
    // assertEquals(
    // "http://www.mygrid.org.uk/osi/#_1.3.46.670589.11.30.9.1060494102827752900201.11.1.1.1.0.0.1",
    // iod.getURI());
    // }
    // }
    //
    // public void testRemoveDicomData() throws Exception {
    // JenaMetadataService rdfRepository = new JenaMetadataService(
    // ProvenanceConfigurator.getMetadataStoreConfiguration());
    // String[] lsids = new String[] {
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:0QJWG459G9",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:2UA77JVL4V",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:307CRA6VK7",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:3S3JTZD0Z6",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:46IH3B13BH",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:4KLSVK0XB9",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:525LLY1ZFS",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:54OMC9TTHG",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:5TFLLYLRUC",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:7E9TXBTTDU",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:7GXRVAG4L0",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:872SUUY0AC",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:8P9AS286BP",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:9N2PMKVO5S",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:ANP7UNISQF",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:AZSX84S5RR",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:BGTD1FK0HH",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:BVUJKNQUH9",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:BYG63T7WQV",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:C5CODEF8SR",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:DOVGYF2E03",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:E2QVPKOVO9",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:E8E7OL7R1O",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:EQ5VKXN93Y",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:F50AMLY3TP",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:FFAI338JKM",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:FMJJKH4OXI",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:FV70Q2F3YB",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:GCQWSK3FQT",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:I18NSCPT1X",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:I74LIP9KA3",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:JTDFNFXH5V",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:KYDTMG83U3",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:L2BLJYIXS9",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:LEHV8WN2CJ",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:LLMWFA0MPE",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:LW03UI44LQ",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:M3IOUIW3Y9",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:MDMSUVZ767",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:O06EM2HMJL",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:P7OX7ECHZQ",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:PO1IYUAHGW",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:QKY75P7NV9",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:R65F6LSGKP",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:RG1A29VSAJ",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:S4VE6HBQIY",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:SE7BPE6VWN",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:VG8ACUDEY6",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:WFFXUSEE08",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:WYPXVX56IW",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:YFT5FSJQRB",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:YOJGWXC4EX",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:_0.0.0.0.1.8811.2.1.20010413115754.12432",
    // "urn:lsid:www.mygrid.org.uk:mias_dicom:_0.0.0.0.1.8811.2.2.20010413115754.12432",
    // "urn:test:kave2" };
    // for (int i = 0; i < lsids.length; i++) {
    // rdfRepository.removeGraph(lsids[i]);
    // }
    // }

    // public void testSyntacticTypes() throws Exception {
    // NamedRDFGraphsRepository rdfRepository = new NamedRDFGraphsRepository();
    // String triql = "SELECT ?workflow, ?data, ?in, ?out, ?synType, ?date,
    // ?workflowName WHERE "
    // + "?workflow (?data "
    // + ProvenanceOntology
    // .bracketify(ProvenanceOntologyConstants.DatatypeProperties.DATASYNTACTICTYPE)
    // + " ?synType . ?data "
    // + ProvenanceOntology
    // .bracketify(ProvenanceOntologyConstants.ObjectProperties.INPUTDATAHASNAME)
    // + " ?in . ?data "
    // + ProvenanceOntology
    // .bracketify(ProvenanceOntologyConstants.ObjectProperties.OUTPUTDATAHASNAME)
    // + " ?out . ?workflow "
    // + ProvenanceOntology
    // .bracketify(ProvenanceOntologyConstants.DatatypeProperties.ENDTIME)
    // + " ?date . ?workflow "
    // + ProvenanceOntology
    // .bracketify(ProvenanceOntologyConstants.ObjectProperties.RUNSWORKFLOW)
    // + " ?workflowName )";
    // System.out.println(triql);
    // Iterator iterator = rdfRepository.query(triql);
    // Map synTypes = new HashMap();
    // while (iterator.hasNext()) {
    // Map oneResult = (Map) iterator.next();
    // Node workflow = (Node) oneResult.get("workflow");
    // String workflowURI = workflow.getURI();
    // Node workflowName = (Node) oneResult.get("workflowName");
    // String workflowNameURI = workflowName.getURI();
    // Node data = (Node) oneResult.get("data");
    // String dataURI = data.getURI();
    // Node in = (Node) oneResult.get("in");
    // String inURI = in.getURI();
    // Node out = (Node) oneResult.get("out");
    // String outURI = out.getURI();
    // Node synType = (Node) oneResult.get("synType");
    // String synTypeLiteral = synType.getLiteral().toString();
    // Node date = (Node) oneResult.get("date");
    // String dateLiteral = date.getLiteral().toString();
    // String key = workflowURI + dataURI;
    // // System.out.println(workflowURI + " - " + dateLiteral);
    // // System.out.println(dataURI + ": " + synTypeLiteral);
    // if (synTypes.containsKey(key)) {
    // System.out.println(key);
    // System.out.println(inURI + " - " + outURI);
    // System.out.println(synTypes.get(key));
    // System.out.println(synTypeLiteral);
    // System.out.println(workflowNameURI);
    // System.out.println(dateLiteral);
    // } else
    // synTypes.put(key, synTypeLiteral);
    // }
    // }

    // public void testFailedProcesses() throws Exception {
    // NamedRDFGraphsRepository rdfRepository = new NamedRDFGraphsRepository();
    // final String query = "SELECT ?workflowRun WHERE ?workflowRun ( ?run
    // rdf:type "
    // + ProvenanceOntology
    // .bracketify(ProvenanceOntologyConstants.Classes.FAILEDPROCESSRUN)
    // + " ) USING rdf FOR <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";
    // Iterator workflowRuns = rdfRepository.query(query);
    // Set models = new HashSet();
    // while (workflowRuns.hasNext()) {
    // Map next = (Map) workflowRuns.next();
    // Node workflow = (Node) next.get("workflowRun");
    // Model model = rdfRepository.retrieveGraphModel(workflow.getURI());
    // models.add(model);
    // }
    // new ProvenanceOntology(models).writeForProtege(new File(INSTANCES_DIR,
    // "failed.owl"));
    // }

}
