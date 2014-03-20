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
package net.sf.taverna.t2.activities.wsdl.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.activities.wsdl.WSDLTestConstants;
import net.sf.taverna.t2.activities.wsdl.translator.WSDLActivityTranslator;
import net.sf.taverna.t2.activities.wsdl.translator.WSDLActivityTranslatorTest;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.junit.BeforeClass;
import org.junit.Test;

public class WSDLActivityTranslatorTest {
	
	private static WSDLBasedProcessor processor = null;
	private static String wsdlPath;

	@Test
	public void testTranslateAndInvoke() throws Exception {
		System.setProperty("raven.eclipse", "true");
		WSDLBasedProcessor proc = new WSDLBasedProcessor(null,"test",WSDLTestConstants.WSDL_TEST_BASE+"menagerie-complex-rpc.wsdl","createPerson");
		WSDLActivity activity = (WSDLActivity) new WSDLActivityTranslator().doTranslation(proc);
		Map<String,Object> inputMap = new HashMap<String,Object>();
		Map<String, Class<?>> outputMap = new HashMap<String, Class<?>>();
		outputMap.put("out", String.class);
		Map<String,Object> results = ActivityInvoker.invokeAsyncActivity(activity, inputMap, outputMap);
		assertEquals(1,results.size());
		assertNotNull(results.get("out"));
		assertTrue(results.get("out") instanceof String);
	}
    
    @BeforeClass
    public static void createProcessor() throws Exception {
    	wsdlPath = WSDLActivityTranslatorTest.class.getResource("/dbfetch.wsdl").toURI().toURL().toExternalForm();
        processor = new WSDLBasedProcessor(null,"test_wsdl",wsdlPath ,"getSupportedDBs");
    }
    
    @Test
    public void testCanHandleTrue() throws Exception {
        WSDLActivityTranslator translator = new WSDLActivityTranslator();
        assertTrue(translator.canHandle(processor));
    }

	@SuppressWarnings("serial")
	private class DummyProcessor extends BeanshellProcessor {
		public DummyProcessor() throws ProcessorCreationException, DuplicateProcessorNameException {
			super(null,"beanshell","",new String[]{},new String[]{});
		}
	};
    
    @Test
    public void testCanHandleFalse() throws Exception {
        WSDLActivityTranslator translator = new WSDLActivityTranslator();        
        assertFalse(translator.canHandle(new DummyProcessor()));
    }
    
    @Test
    public void testConfig() throws Exception {
        WSDLActivityTranslator translator = new WSDLActivityTranslator();
        WSDLActivityConfigurationBean bean = translator.createConfigType(processor);
        
        assertEquals("The wsdl in the config bean is wrong",wsdlPath,bean.getWsdl());
        assertEquals("The operation in the config bean is wrong","getSupportedDBs",bean.getOperation());
    }
    
    @Test
    public void testSimplePorts() throws Exception {
        WSDLActivityTranslator translator = new WSDLActivityTranslator();
        Activity<?> activity = translator.doTranslation(processor);
        assertEquals("no inputs were expected",0,activity.getInputPorts().size());
        assertEquals("2 outputs were expected (remember 1 extra for attachment list!).",2,activity.getOutputPorts().size());
       
        List<String> portNames = new ArrayList<String>();
        portNames.add("attachmentList");
        portNames.add("getSupportedDBsReturn");
        boolean found=false;
        for (OutputPort port : activity.getOutputPorts()) {
        	if (port.getName().equals("getSupportedDBsReturn")) {
        		found=true;
        	}
        	assertEquals("the port '"+port.getName()+"' should have a depth of 1 (i.e. a list)",1,port.getDepth());
        }
        if (!found) {
        	fail("There should be an output port named getSupportedDBsReturn");
        }
        
    }
    
    @Test
    public void getTypeDescriptorForPorts() throws Exception {
    	WSDLActivityTranslator translator = new WSDLActivityTranslator();
        WSDLActivity activity = (WSDLActivity)translator.doTranslation(processor);
        assertEquals("no inputs were expected",0,activity.getInputPorts().size());
        assertEquals("2 outputs were expected (remember 1 extra for attachment list!).",2,activity.getOutputPorts().size());
       
        assertNotNull("The descriptor should exist for output getSupportedDBsReturn",activity.getTypeDescriptorForOutputPort("getSupportedDBsReturn"));
        assertNull("The descriptor should not exist for output fred",activity.getTypeDescriptorForOutputPort("fred"));
        assertNull("The descriptor should not exist for input getSupportedDBsReturn",activity.getTypeDescriptorForInputPort("getSupportedDBsReturn"));
    }
	
}
