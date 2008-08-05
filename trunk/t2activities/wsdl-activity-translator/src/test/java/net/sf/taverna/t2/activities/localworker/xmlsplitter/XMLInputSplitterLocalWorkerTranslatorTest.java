package net.sf.taverna.t2.activities.localworker.xmlsplitter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.testutils.DummyProcessor;
import net.sf.taverna.t2.activities.testutils.LocationConstants;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLInputSplitterActivity;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLSplitterConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.java.EchoList;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.embl.ebi.escience.scuflworkers.java.XMLInputSplitter;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.junit.Ignore;
import org.junit.Test;

public class XMLInputSplitterLocalWorkerTranslatorTest implements LocationConstants{
    
	@Test
	public void testLocalWorkerTranslatorCanHandle() throws Exception {
		XMLInputSplitterLocalWorkerTranslator translator = new XMLInputSplitterLocalWorkerTranslator();
		LocalServiceProcessor processor = new LocalServiceProcessor(
				null, "XMLInputSplitter",
				new XMLInputSplitter());
		assertTrue("should be able to handle XMLInputSplitter",translator.canHandle(processor));
	}
	
	@Test
	public void testLocalWorkerTranslatorCanHandleNegative() throws Exception {
		XMLInputSplitterLocalWorkerTranslator translator = new XMLInputSplitterLocalWorkerTranslator();
		Processor processor = new DummyProcessor();
		assertFalse("should be not be able ot handle the dummy processor",translator.canHandle(processor));
		assertFalse("should be not be able ot handle null processor",translator.canHandle(null));
	}
	
	@Test
	public void testCantHandleOtherLocalWorkers() throws Exception {
		XMLInputSplitterLocalWorkerTranslator translator = new XMLInputSplitterLocalWorkerTranslator();
		LocalServiceProcessor processor = new LocalServiceProcessor(null, "EchoList", new EchoList());
		assertFalse("should not be able to handle the EchoList localworker",translator.canHandle(processor));
	}
	
    @Ignore("Integration test")
	@Test
	public void testTranslation() throws Exception {
		WSDLBasedProcessor wsdlProcessor = new WSDLBasedProcessor(null,"test_wsdl",WSDL_TEST_BASE+"TestServices-wrapped.wsdl","personToString");
		
		XMLInputSplitter splitter = new XMLInputSplitter();
		splitter.setUpInputs(wsdlProcessor.getInputPorts()[0]);
		
		XMLInputSplitterLocalWorkerTranslator translator = new XMLInputSplitterLocalWorkerTranslator();
		LocalServiceProcessor processor = new LocalServiceProcessor(
				null, "XMLInputSplitter",splitter);
		Activity<XMLSplitterConfigurationBean> activity = translator.doTranslation(processor);
		
		assertNotNull("The activity is null",activity);
		assertTrue("It should be an XMLInputSplitterActivity",activity instanceof XMLInputSplitterActivity);
		
		assertNotNull("configuration should not be null",activity.getConfiguration());
		assertTrue("configuration should be an XMLSplitterConfigurationBean",activity.getConfiguration() instanceof XMLSplitterConfigurationBean);
		
	}
}
