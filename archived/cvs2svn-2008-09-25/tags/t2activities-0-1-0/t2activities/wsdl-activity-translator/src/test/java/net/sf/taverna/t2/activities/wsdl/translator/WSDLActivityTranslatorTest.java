package net.sf.taverna.t2.activities.wsdl.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.testutils.DummyProcessor;
import net.sf.taverna.t2.activities.testutils.LocationConstants;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Stuart Owen
 */
public class WSDLActivityTranslatorTest  implements LocationConstants {
    private static WSDLBasedProcessor processor = null;
    
    @BeforeClass
    public static void createProcessor() throws Exception {
        processor = new WSDLBasedProcessor(null,"test_wsdl",WSDL_TEST_BASE+"DBfetch.wsdl","getSupportedDBs");
    }
    
    @Test
    public void testCanHandleTrue() throws Exception {
        WSDLActivityTranslator translator = new WSDLActivityTranslator();
        assertTrue(translator.canHandle(processor));
    }
    
    @Test
    public void testCanHandleFalse() throws Exception {
        WSDLActivityTranslator translator = new WSDLActivityTranslator();
        assertFalse(translator.canHandle(new DummyProcessor()));
    }
    
    @Test
    public void testConfig() throws Exception {
        WSDLActivityTranslator translator = new WSDLActivityTranslator();
        WSDLActivityConfigurationBean bean = translator.createConfigType(processor);
        
        assertEquals("The wsdl in the config bean is wrong",WSDL_TEST_BASE+"DBfetch.wsdl",bean.getWsdl());
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
   
}
