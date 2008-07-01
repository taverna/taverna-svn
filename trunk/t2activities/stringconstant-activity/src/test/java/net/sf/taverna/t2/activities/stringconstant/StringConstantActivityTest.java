package net.sf.taverna.t2.activities.stringconstant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.AbstractPort;

import org.junit.Test;

/**
 * Tests the StringConstantActivity
 * @author Stuart Owen
 *
 */
public class StringConstantActivityTest {

	/**
	 * Simple invocation test. Also tests Activity.configure sets up the correct output port.
	 * @throws Exception
	 */
	@Test
	public void testInvoke() throws Exception {
		StringConstantConfigurationBean bean = new StringConstantConfigurationBean();
		bean.setValue("this_is_a_string");
		StringConstantActivity activity = new StringConstantActivity();
		activity.configure(bean);
		
		assertEquals("there should be no inputs",0,activity.getInputPorts().size());
		assertEquals("there should be 1 output",1,activity.getOutputPorts().size());
		assertEquals("the output port name should be value","value",((AbstractPort)activity.getOutputPorts().toArray()[0]).getName());
		
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("value", String.class);

		Map<String,Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, new HashMap<String, Object>(), expectedOutputs);
		
		assertEquals("there should be 1 output",1,outputs.size());
		assertTrue("there should be an output named value",outputs.containsKey("value"));
		assertEquals("The value of the output should be 'this_is_a_string'","this_is_a_string",outputs.get("value"));
		assertTrue("The output type should be String",outputs.get("value") instanceof String);
	}
}
