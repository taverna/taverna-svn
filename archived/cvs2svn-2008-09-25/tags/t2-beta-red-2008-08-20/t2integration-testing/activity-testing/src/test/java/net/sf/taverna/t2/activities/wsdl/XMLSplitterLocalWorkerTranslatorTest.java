package net.sf.taverna.t2.activities.wsdl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.localworker.xmlsplitter.XMLInputSplitterLocalWorkerTranslator;
import net.sf.taverna.t2.activities.localworker.xmlsplitter.XMLOutputSplitterLocalWorkerTranslator;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLInputSplitterActivity;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLOutputSplitterActivity;
import net.sf.taverna.t2.activities.wsdl.xmlsplitter.XMLSplitterConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.embl.ebi.escience.scuflworkers.java.XMLInputSplitter;
import org.embl.ebi.escience.scuflworkers.java.XMLOutputSplitter;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.junit.Ignore;
import org.junit.Test;

public class XMLSplitterLocalWorkerTranslatorTest extends
		WSDLTestConstants {

	@Test
	public void inputSplitterTranslation() throws Exception {
		WSDLBasedProcessor wsdlProcessor = new WSDLBasedProcessor(null,
				"test_wsdl", WSDL_TEST_BASE + "TestServices-wrapped.wsdl",
				"personToString");

		XMLInputSplitter splitter = new XMLInputSplitter();
		splitter.setUpInputs(wsdlProcessor.getInputPorts()[0]);

		XMLInputSplitterLocalWorkerTranslator translator = new XMLInputSplitterLocalWorkerTranslator();
		LocalServiceProcessor processor = new LocalServiceProcessor(null,
				"XMLInputSplitter", splitter);
		Activity<XMLSplitterConfigurationBean> activity = translator
				.doTranslation(processor);

		assertNotNull("The activity is null", activity);
		assertTrue("It should be an XMLInputSplitterActivity",
				activity instanceof XMLInputSplitterActivity);

		assertNotNull("configuration should not be null", activity
				.getConfiguration());
		assertTrue(
				"configuration should be an XMLSplitterConfigurationBean",
				activity.getConfiguration() instanceof XMLSplitterConfigurationBean);
	}
	
	 @Ignore("Integration test")
		@Test
		public void outputSplitterTranslation() throws Exception {
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
