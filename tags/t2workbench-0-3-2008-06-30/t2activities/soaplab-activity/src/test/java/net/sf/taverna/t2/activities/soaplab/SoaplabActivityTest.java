package net.sf.taverna.t2.activities.soaplab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.OutputPort;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for SoaplabActivity.
 * 
 * @author David Withers
 */
public class SoaplabActivityTest {

	private SoaplabActivity activity;

	private SoaplabActivityConfigurationBean configurationBean;

	@Before
	public void setUp() throws Exception {
		activity = new SoaplabActivity();
		configurationBean = new SoaplabActivityConfigurationBean();
		configurationBean
				.setEndpoint("http://www.ebi.ac.uk/soaplab/emboss4/services/utils_misc.embossversion");
	}

	@Test
	public void testExecuteAsynch() throws Exception {
		Map<String, Object> inputs = new HashMap<String, Object>();
		// inputs.put("full", "true");
		List<String> expectedOutputs = new ArrayList<String>();
		expectedOutputs.add("report");
		expectedOutputs.add("outfile");

		activity.configure(configurationBean);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputs);
		assertTrue(outputs.containsKey("report"));
		// assertTrue(outputs.get("report") instanceof String);
		assertTrue(outputs.containsKey("outfile"));
		assertTrue(outputs.get("outfile") instanceof String);
		System.out.println(outputs.get("outfile"));

		// test with polling
		configurationBean.setPollingInterval(5);
		configurationBean.setPollingIntervalMax(6);
		configurationBean.setPollingBackoff(1.2);
		activity.configure(configurationBean);

		outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs,
				expectedOutputs);
		assertTrue(outputs.containsKey("report"));
		assertTrue(outputs.containsKey("outfile"));
	}

	@Test
	public void testSoaplabActivity() {
		assertNotNull(new SoaplabActivity());
	}

	@Test
	public void testConfigureSoaplabActivityConfigurationBean()
			throws Exception {
		Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("report");
		expectedOutputs.add("outfile");

		activity.configure(configurationBean);
		Set<OutputPort> ports = activity.getOutputPorts();
		assertEquals(expectedOutputs.size(), ports.size());
		for (OutputPort outputPort : ports) {
			assertTrue("Wrong output : " + outputPort.getName(),
					expectedOutputs.remove(outputPort.getName()));
		}
	}

	@Test
	public void testIsPollingDefined() throws Exception {
		assertFalse(activity.isPollingDefined());
		activity.configure(configurationBean);
		assertFalse(activity.isPollingDefined());
		configurationBean.setPollingInterval(1000);
		activity.configure(configurationBean);
		assertTrue(activity.isPollingDefined());
	}

}
