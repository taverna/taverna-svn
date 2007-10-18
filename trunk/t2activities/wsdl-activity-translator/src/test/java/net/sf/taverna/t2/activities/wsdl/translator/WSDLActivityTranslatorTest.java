package net.sf.taverna.t2.activities.wsdl.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.testutils.DummyProcessor;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.AbstractOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Stuart Owen
 */
public class WSDLActivityTranslatorTest {
    public static final String WSDL_TEST_BASE="http://www.mygrid.org.uk/taverna-tests/testwsdls/";
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
        assertEquals("1 output was expected",1,activity.getOutputPorts().size());
        AbstractOutputPort port = (AbstractOutputPort)activity.getOutputPorts().toArray()[0];
        assertEquals("incorrect name for the port","getSupportedDBsReturn",port.getName());
        assertEquals("the port should have a depth of 1 (i.e. a list)",1,port.getDepth());
    }
   
}
