package net.sf.taverna.t2.activities.localworker.xmlsplitter;

import static org.junit.Assert.*;

import net.sf.taverna.t2.activities.testutils.DummyProcessor;
import net.sf.taverna.t2.activities.testutils.LocationConstants;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLOutputSplitterActivity;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLSplitterConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.java.EchoList;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.embl.ebi.escience.scuflworkers.java.XMLInputSplitter;
import org.embl.ebi.escience.scuflworkers.java.XMLOutputSplitter;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.junit.Test;

public class XMLOutputSplitterLocalWorkerTranslatorTest implements LocationConstants {

	@Test
	public void testCanHandle() throws Exception {
		XMLOutputSplitterLocalWorkerTranslator translator = new XMLOutputSplitterLocalWorkerTranslator();
		LocalServiceProcessor processor = new LocalServiceProcessor(
				null, "XMLInputSplitter",
				new XMLOutputSplitter());
		assertTrue("should be able to handle XMLOutputSplitter",translator.canHandle(processor));
	}
	
	@Test
	public void testCantHandleInputSplitter() throws Exception {
		XMLOutputSplitterLocalWorkerTranslator translator = new XMLOutputSplitterLocalWorkerTranslator();
		LocalServiceProcessor processor = new LocalServiceProcessor(
				null, "XMLInputSplitter",
				new XMLInputSplitter());
		assertFalse("should not be able to handle XMLInputSplitter",translator.canHandle(processor));
	}
	
	@Test
	public void testLocalWorkerTranslatorCantHandle() throws Exception {
		XMLOutputSplitterLocalWorkerTranslator translator = new XMLOutputSplitterLocalWorkerTranslator();
		Processor processor = new DummyProcessor();
		assertFalse("should be not be able ot handle the dummy processor",translator.canHandle(processor));
		assertFalse("should be not be able ot handle null processor",translator.canHandle(null));
	}
	
	@Test
	public void testCantHandleOtherLocalWorkers() throws Exception {
		XMLOutputSplitterLocalWorkerTranslator translator = new XMLOutputSplitterLocalWorkerTranslator();
		LocalServiceProcessor processor = new LocalServiceProcessor(null, "EchoList", new EchoList());
		assertFalse("should not be able to handle the EchoList localworker",translator.canHandle(processor));
	}
	
	@Test
	public void testTranslation() throws Exception {
		WSDLBasedProcessor wsdlProcessor = new WSDLBasedProcessor(null,"test_wsdl",WSDL_TEST_BASE+"TestServices-wrapped.wsdl","getPerson");
		
		XMLOutputSplitter splitter = new XMLOutputSplitter();
		splitter.setUpOutputs(wsdlProcessor.getOutputPorts()[1]); //first output is the attachment list
		
		XMLOutputSplitterLocalWorkerTranslator translator = new XMLOutputSplitterLocalWorkerTranslator();
		LocalServiceProcessor processor = new LocalServiceProcessor(
				null, "XMLOutputSplitter",splitter);
		Activity<XMLSplitterConfigurationBean> activity = translator.doTranslation(processor);
		
		assertNotNull("The activity is null",activity);
		assertTrue("It should be an XMLOutputSplitterActivity",activity instanceof XMLOutputSplitterActivity);
		
		assertNotNull("configuration should not be null",activity.getConfiguration());
		assertTrue("configuration should be an XMLSplitterConfigurationBean",activity.getConfiguration() instanceof XMLSplitterConfigurationBean);
	}
}
