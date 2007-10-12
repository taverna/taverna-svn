package net.sf.taverna.t2.activities.beanshell;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.junit.Test;

/**
 * Beanshell Activity Tests
 * @author Stuart Owen
 *
 */
public class BeanshellActivityTest {
	
	/**
	 * Tests a simple script (String output = input + "_returned") to ensure the script is invoked correctly.
	 * @throws Exception
	 */
	@Test
	public void simpleScript() throws Exception {
		BeanshellActivity activity = new BeanshellActivity();
		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
		
		ActivityInputPortDefinitionBean inputPortBean = new ActivityInputPortDefinitionBean();
		inputPortBean.setDepth(0);
		inputPortBean.setName("input");
		inputPortBean.setMimeTypes(new ArrayList<String>());
		bean.setInputPortDefinitions(Collections.singletonList(inputPortBean));
		
		ActivityOutputPortDefinitionBean outputPortBean = new ActivityOutputPortDefinitionBean();
		outputPortBean.setDepth(0);
		outputPortBean.setName("output");
		outputPortBean.setMimeTypes(new ArrayList<String>());
		bean.setOutputPortDefinitions(Collections.singletonList(outputPortBean));
		bean.setScript("String output = input + \"_returned\";");
		
		activity.configure(bean);
		assertEquals("There should be 1 input port",1,activity.getInputPorts().size());
		assertEquals("There should be 1 output port",1,activity.getOutputPorts().size());
		
		assertEquals("The input should be called input", "input",((AbstractPort)activity.getInputPorts().toArray()[0]).getName());
		assertEquals("The output should be called output", "output",((AbstractPort)activity.getOutputPorts().toArray()[0]).getName());
		
		Map<String,Object> inputs = new HashMap<String, Object>();
		inputs.put("input", "aString");
		List<String> expectedOutputs = Collections.singletonList("output");
		
		Map<String,Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
		assertTrue("there should be an output named output",outputs.containsKey("output"));
		assertEquals("output should have the value aString_returned","aString_returned",outputs.get("output"));
	}
}
