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
 * Filename           $RCSfile: StressTester.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:52:51 $
 *               by   $Author: stain $
 * Created on 1 Aug 2006
 *****************************************************************/
package uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.LSIDProvider;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.enactor.EnactorProxy;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.event.IterationCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.ProcessCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntology;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.ontology.ProvenanceOntologyCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataService;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.store.MetadataServiceCreationException;
import uk.ac.man.cs.img.mygrid.provenance.knowledge.taverna.ng4j.TestLSIDProvider;
import uk.ac.soton.itinnovation.freefluo.conf.ConfigurationDescription;
import uk.ac.soton.itinnovation.freefluo.conf.EngineConfiguration;
import uk.ac.soton.itinnovation.freefluo.conf.EngineConfigurationImpl;
import uk.ac.soton.itinnovation.freefluo.main.Engine;
import uk.ac.soton.itinnovation.freefluo.main.EngineImpl;
import uk.org.mygrid.provenance.LogBookException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

public class StressTester {

    public static final String IDENTIFY_UNIPROT_PROTEIN = "identifyUniprotProtein";

    private static final String SCUFL = "myGrid/stressTester.xml";

    ProvenanceOntology ontology;

    MetadataService metadataService;

    String workflowRunId;

    private Processor[] processors;

    private WorkflowInstance workflowInstance;

    private ProvenanceGenerator provenanceGenerator;

    private TestLSIDProvider provider;

    private EngineImpl engine;

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            StressTester stressTester = new StressTester();
            for (int i = 0; i < 2; i++) {
                stressTester.createRandomWorkflowRun();
                System.out.println("Created " + i + "-th run.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @throws ProvenanceOntologyCreationException
     * @throws MetadataServiceCreationException
     * @throws LogBookException 
     * 
     */
    public StressTester() throws ProvenanceOntologyCreationException,
            MetadataServiceCreationException, LogBookException {
        super();
        provenanceGenerator = ProvenanceGenerator.getInstance();
        provenanceGenerator.setConfiguration(ProvenanceConfigurator
                .getConfiguration("stresstest"));
        provenanceGenerator.initialise();
        provider = new TestLSIDProvider();
        DataThing.SYSTEM_DEFAULT_LSID_PROVIDER = provider;
    }

    Engine getEngine() {
        if (engine != null)
            return engine;
        EngineConfiguration engineConfig = getEngineConfiguration();
        engine = new EngineImpl(engineConfig);
        return engine;
    }

    private EngineConfiguration getEngineConfiguration() {
        ConfigurationDescription configDescription = new ConfigurationDescription(
                "taverna",
                "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaScuflModelParser",
                "uk.ac.soton.itinnovation.freefluo.exts.taverna.TavernaDataHandler");
        try {
            return new EngineConfigurationImpl(configDescription, getClass()
                    .getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage(), e);
        }

    }

    public void createRandomWorkflowRun() throws Exception {
        workflowRunId = provider.getID(LSIDProvider.WFINSTANCE);
        SAXBuilder builder = new SAXBuilder();
        String inputFile = "myGrid/longIterationInputFile.xml";
        String inputFilePath = ClassLoader.getSystemResource(inputFile)
                .getFile();
        Document inputDoc = builder.build(new FileReader(inputFilePath));
        Map<String, DataThing> inputs = DataThingXMLFactory
                .parseDataDocument(inputDoc);

        final ScuflModel model = new ScuflModel();
        InputStream scufl = ClassLoader.getSystemResourceAsStream(SCUFL);
        XScuflParser.populate(scufl, model, null);
        scufl.close();

        EnactorProxy enactor = FreefluoEnactorProxy.getInstance();
        workflowInstance = enactor.compileWorkflow(model, new HashMap(), null);
        // workflowInstance = new WorkflowInstanceImpl(getEngine(), model,
        // workflowRunId);
        WorkflowCreationEvent workflowCreated = new WorkflowCreationEvent(
                workflowInstance, inputs,
                "urn:lsid:www.mygrid.org.uk:operation:660KZBTN7D2");
        System.out.println("Workflow Run Id = " + workflowInstance.getID());
        processors = model.getProcessors();
        Map<String, DataThing> inputsFirstProcessor = new HashMap<String, DataThing>();
        int i = 0;
        Set<String> inputsKeys = inputs.keySet();
        for (String key : inputsKeys) {
            DataThing dataThing = inputs.get(key);
            dataThing.fillLSIDValues(provider);
            inputsFirstProcessor.put("in" + (i++), dataThing);
        }

        Map<String, DataThing> outputsFirstProcessorMap = new HashMap<String, DataThing>();

        DataThing emptyCollection = DataThingFactory.bake(new String[] {});
        emptyCollection.fillLSIDValues(provider);
        outputsFirstProcessorMap.put("attachmentList", emptyCollection);

        String out0 = "Q989P";
        String out1 = "Q984X";
        String out2 = "Q986Y";
        String out3 = "Q986X";
        String out4 = "Q766X";
        String out5 = "Q886X";
        String out6 = "Q586X";
        String out7 = "Q986P";
        String out8 = "Q987P";
        String out9 = "Q988P";

        DataThing outputFirstProcessor = DataThingFactory.bake(new String[] {
                out0, out1, out2, out3, out4, out5, out6, out7, out8, out9 });
        outputFirstProcessor.fillLSIDValues(provider);
        outputsFirstProcessorMap.put("identifyUniprotProteinReturn",
                outputFirstProcessor);

        Processor firstProcessor = getProcessor(IDENTIFY_UNIPROT_PROTEIN);
        ProcessCompletionEvent processCompletionEvent = new ProcessCompletionEvent(
                false, inputsFirstProcessor, outputsFirstProcessorMap,
                firstProcessor, workflowInstance);

        Map<String, DataThing> inputsU2GO = new HashMap<String, DataThing>();
        inputsU2GO.put("in0", outputFirstProcessor);
        //
        // String[][] data = new String[][] {
        // { "001234", "001327", "078167", "00012" },
        // { "12886", "76586", "55986" },
        // { "12886", "76586", "55986", "12886", "76586", "55986" } };
        // DataThing outputSecondProcessor = DataThingFactory.bake(data);
        // outputSecondProcessor.fillLSIDValues(provider);
        Map<String, DataThing> outputSecondProcessorMap = new HashMap<String, DataThing>();
        // outputSecondProcessorMap.put("uniprot2GOReturn",
        // outputSecondProcessor);

        File dataFile = new File("test-resource/stresstest.data");
        byte[] largeData = new byte[(int) dataFile.length()];
        DataInputStream in = new DataInputStream(new FileInputStream(dataFile));
        in.readFully(largeData);
        in.close();

        byte[] smallData = "abc".getBytes();

        byte[][] bytes0 = new byte[][] { largeData, smallData };
        byte[][] bytes1 = new byte[][] { smallData, smallData, smallData };
        byte[][] bytes2 = new byte[][] { smallData, largeData };
        byte[][] bytes3 = new byte[][] { smallData, smallData, smallData,
                smallData, smallData };
        byte[][] bytes4 = new byte[][] { largeData, largeData };
        byte[][] bytes5 = new byte[][] { smallData };
        byte[][] bytes6 = new byte[][] { largeData, smallData };
        byte[][] bytes7 = new byte[][] { smallData, largeData, smallData };
        byte[][] bytes8 = new byte[][] { largeData, smallData };
        byte[][] bytes9 = new byte[][] { smallData };

        DataThing largeDataThing = DataThingFactory.bake(new byte[][][] {
                bytes0, bytes1, bytes2, bytes3, bytes4, bytes5, bytes6, bytes7,
                bytes8, bytes9 });
        largeDataThing.fillLSIDValues(provider);

        outputSecondProcessorMap.put("uniprot2GOReturn", largeDataThing);

        Processor secondProcessor = getProcessor("uniprot2GO");

        List<ProcessCompletionEvent> assocCompletionEvents = new ArrayList<ProcessCompletionEvent>();

        Map inputLsidMap = outputFirstProcessor.getLSIDMap();
        byte[][] output = bytes1;

        DataThing outputDataThing = new DataThing(output);
        outputDataThing.setLSID(output, (String) largeDataThing.getLSIDMap()
                .get(output));
        Map<String, DataThing> outputIterationMap = new HashMap<String, DataThing>();
        outputIterationMap.put("uniprot2GOReturn", outputDataThing);

        Map<String, DataThing> workflowOutputMap = workflowInstance.getOutput();
        workflowOutputMap.put("GOId", largeDataThing);
        WorkflowCompletionEvent workflowCompletionEvent = new WorkflowCompletionEvent(
                workflowInstance);

        triggerIterationEvent(out0, inputLsidMap, secondProcessor,
                assocCompletionEvents, outputsFirstProcessorMap,
                outputIterationMap);
        triggerIterationEvent(out1, inputLsidMap, secondProcessor,
                assocCompletionEvents, outputsFirstProcessorMap,
                outputSecondProcessorMap);
        triggerIterationEvent(out2, inputLsidMap, secondProcessor,
                assocCompletionEvents, outputsFirstProcessorMap,
                outputSecondProcessorMap);
        triggerIterationEvent(out3, inputLsidMap, secondProcessor,
                assocCompletionEvents, outputsFirstProcessorMap,
                outputSecondProcessorMap);
        triggerIterationEvent(out4, inputLsidMap, secondProcessor,
                assocCompletionEvents, outputsFirstProcessorMap,
                outputSecondProcessorMap);
        triggerIterationEvent(out5, inputLsidMap, secondProcessor,
                assocCompletionEvents, outputsFirstProcessorMap,
                outputSecondProcessorMap);
        triggerIterationEvent(out6, inputLsidMap, secondProcessor,
                assocCompletionEvents, outputsFirstProcessorMap,
                outputSecondProcessorMap);
        triggerIterationEvent(out7, inputLsidMap, secondProcessor,
                assocCompletionEvents, outputsFirstProcessorMap,
                outputSecondProcessorMap);
        triggerIterationEvent(out8, inputLsidMap, secondProcessor,
                assocCompletionEvents, outputsFirstProcessorMap,
                outputSecondProcessorMap);
        triggerIterationEvent(out9, inputLsidMap, secondProcessor,
                assocCompletionEvents, outputsFirstProcessorMap,
                outputSecondProcessorMap);

        IterationCompletionEvent iterationCompletionEvent = new IterationCompletionEvent(
                new HashMap(), new HashMap(), workflowInstance,
                secondProcessor, assocCompletionEvents, inputsU2GO,
                outputSecondProcessorMap);
        provenanceGenerator
                .processCompletedWithIteration(iterationCompletionEvent);

        provenanceGenerator.workflowCreated(workflowCreated);

        provenanceGenerator.workflowCompleted(workflowCompletionEvent);

        provenanceGenerator.processCompleted(processCompletionEvent);

        workflowInstance.destroy();
    }

    private void triggerIterationEvent(String input, Map lsidMap,
            Processor processor,
            List<ProcessCompletionEvent> assocCompletionEvents,
            Map<String, DataThing> inputMap, Map<String, DataThing> outputMap) {
        DataThing inputDataThing = new DataThing(input);
        inputDataThing.setLSID(input, (String) lsidMap.get(input));
        triggerIterationEvent(inputDataThing, processor, assocCompletionEvents,
                outputMap);
    }

    private void triggerIterationEvent(DataThing input, Processor processor,
            List<ProcessCompletionEvent> assocCompletionEvents,
            Map<String, DataThing> outputMap) {
        Map<String, DataThing> inputIterationMap = new HashMap<String, DataThing>();
        inputIterationMap.put("in0", input);
        ProcessCompletionEvent processIterationCompletionEvent = new ProcessCompletionEvent(
                true, inputIterationMap, outputMap, processor, workflowInstance);
        assocCompletionEvents.add(processIterationCompletionEvent);
        provenanceGenerator.processCompleted(processIterationCompletionEvent);
    }

    private Processor getProcessor(String name) throws Exception {
        Processor processor;
        for (int j = 0; j < processors.length; j++) {
            if (processors[j].getName().equals(name)) {
                processor = processors[j];
                return processor;
            }
        }
        throw new Exception("Processor not found");
    }

    static public Properties getConfiguration() {
        Properties provenanceProperties = System.getProperties();
        ResourceBundle rb = ResourceBundle.getBundle("stresstest");
        Enumeration keys = rb.getKeys();
        while (keys.hasMoreElements()) {
            String key = (String) keys.nextElement();
            String value = (String) rb.getString(key);
            provenanceProperties.put(key, value);
        }
        return provenanceProperties;
    }

}
