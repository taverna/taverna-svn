package net.sf.taverna.t2.activities.wsdl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.activities.testutils.LocationConstants;
import net.sf.taverna.t2.workflowmodel.OutputPort;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class WSDLActivityTest implements LocationConstants {

	private static WSDLActivity activity;
	private static WSDLActivityConfigurationBean configBean;
	private static String wsdlLocation = WSDL_TEST_BASE
			+ "eutils/eutils_lite.wsdl";

	@BeforeClass
	public static void setUp() throws Exception {
		activity = new WSDLActivity();
		configBean = new WSDLActivityConfigurationBean();
		configBean.setOperation("run_eInfo");
		configBean.setWsdl(wsdlLocation);
		activity.configure(configBean);
	}

	@Test
	public void testConfigureWSDLActivityConfigurationBean() throws Exception {
		assertEquals("There should be 1 input ports", 1, activity
				.getInputPorts().size());
		assertEquals("There should be 2 output ports", 2, activity
				.getOutputPorts().size());
		
		assertEquals("parameters", activity.getInputPorts().iterator().next()
				.getName());
		
		List<String> expectedOutputNames = new ArrayList<String>();
		expectedOutputNames.add("parameters");
		expectedOutputNames.add("attachmentList");
		for (OutputPort port : activity.getOutputPorts()) {
			assertTrue("Unexpected output name:"+port.getName(),expectedOutputNames.contains(port.getName()));
			expectedOutputNames.remove(port.getName());
		}
		assertEquals("Not all of the expected outputs were found, those remainng are:"+expectedOutputNames.toArray(),0,expectedOutputNames.size());
	}

	@Test
	public void testGetConfiguration() throws Exception {
		assertSame(configBean, activity.getConfiguration());
	}

	@Test
	@Ignore("Service is broken")
	public void testExecuteAsynchMapOfStringEntityIdentifierAsynchronousActivityCallback()
			throws Exception {
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put("parameters", "<parameters><db>pubmed</db></parameters>");
		Map<String, Object> outputMap = ActivityInvoker.invokeAsyncActivity(
				activity, inputMap, Collections.singletonList("parameters"));
		assertNotNull("there should be an output named parameters", outputMap
				.get("parameters"));
		String xml;
		if (outputMap.get("parameters") instanceof String) {
			xml = (String) outputMap.get("parameters");
		} else {
			byte [] bytes = (byte []) outputMap
					.get("parameters");
			xml = new String(bytes);
		}

		assertTrue("the xml is not what was expected", xml
				.contains("<DbName>pubmed</DbName>"));
	}

}
