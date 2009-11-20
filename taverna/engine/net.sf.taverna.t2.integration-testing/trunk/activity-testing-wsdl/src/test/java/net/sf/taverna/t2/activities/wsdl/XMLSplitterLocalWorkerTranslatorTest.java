/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
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
