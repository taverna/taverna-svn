package net.sf.taverna.t2.cyclone;

import net.sf.taverna.t2.cyclone.translators.ActivityTranslator;
import net.sf.taverna.t2.cyclone.translators.BeanshellActivityTranslator;
import net.sf.taverna.t2.cyclone.translators.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AbstractActivityTranslatorTest {

	private ActivityTranslator<BeanshellActivityConfigurationBean> translator;
	
	@Before
	public void createTranslator() {
		translator=new BeanshellActivityTranslator();
	}
	
	@Test
	public void testPorts() throws Exception {
		BeanshellProcessor processor = new BeanshellProcessor(null,"simplebeanshell","",new String[]{"input1","input2"},new String[]{"output"});
		processor.getInputPorts()[0].setSyntacticType("l('text/plain')");
		
		Activity<?> activity = translator.doTranslation(processor);
		
		assertEquals(2,activity.getInputPorts().size());
		assertEquals(1, activity.getOutputPorts().size());
		
		Port in1 = null;
		Port in2 = null;
		
		//don't rely on the order. Look for each expected port, then check neither are still null.
		for (Port inPort : activity.getInputPorts()) {
			if (inPort.getName().equals("input1")) in1=inPort;
			if (inPort.getName().equals("input2")) in2=inPort;
		}
		assertNotNull("No input named input1 found",in1);
		assertNotNull("No input named input2 found",in2);
		
		Port out1=(Port)activity.getOutputPorts().toArray()[0];
		
		assertEquals("input1",in1.getName());
		assertEquals(1,in1.getDepth());
		
		assertEquals("input2",in2.getName());
		assertEquals(0,in2.getDepth());
		
		assertEquals("output",out1.getName());
		assertEquals(0,out1.getDepth());
		
		assertNotNull(activity.getConfiguration());
	}
}
