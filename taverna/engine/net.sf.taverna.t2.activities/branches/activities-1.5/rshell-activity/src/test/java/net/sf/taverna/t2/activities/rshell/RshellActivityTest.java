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
package net.sf.taverna.t2.activities.rshell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for RshellActivity.
 * 
 */
public class RshellActivityTest {

	private RshellActivity activity;

	private RshellActivityConfigurationBean configurationBean;

	@Before
	public void setUp() throws Exception {
		activity = new RshellActivity();
		configurationBean = new RshellActivityConfigurationBean();

		ActivityInputPortDefinitionBean inputPortBean = new ActivityInputPortDefinitionBean();
		inputPortBean.setDepth(0);
		inputPortBean.setName("example_input");
		inputPortBean.setHandledReferenceSchemes(new ArrayList<Class<? extends ExternalReferenceSPI>>());
		inputPortBean.setTranslatedElementType(String.class);
		inputPortBean.setAllowsLiteralValues(true);
		configurationBean.setInputPortDefinitions(Collections.singletonList(inputPortBean));
		
		ActivityOutputPortDefinitionBean outputPortBean = new ActivityOutputPortDefinitionBean();
		outputPortBean.setDepth(0);
		outputPortBean.setName("example_output");
		outputPortBean.setMimeTypes(new ArrayList<String>());
		configurationBean.setOutputPortDefinitions(Collections.singletonList(outputPortBean));
	}

	@Test
	public void testRshellActivity() {
		assertNotNull(new RshellActivity());
	}

	@Test
	@Ignore
	public void testExecuteAsynch() throws Exception {
		Map<String, Object> inputs = new HashMap<String, Object>();
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("example_output", String.class);

		activity.configure(configurationBean);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
				activity, inputs, expectedOutputs);
		assertTrue(outputs.containsKey("example_output"));
		assertEquals("hello_example", outputs.get("example_output"));
	}

	@Test
	public void testConfigureRshellActivityConfigurationBean()
			throws Exception {
		Set<String> expectedInputs = new HashSet<String>();
		expectedInputs.add("example_input");
		Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("example_output");

		activity.configure(configurationBean);
		
		Set<ActivityInputPort> inputPorts = activity.getInputPorts();
		assertEquals(expectedInputs.size(), inputPorts.size());
		for (ActivityInputPort inputPort : inputPorts) {
			assertTrue("Wrong output : " + inputPort.getName(),
					expectedInputs.remove(inputPort.getName()));
		}
		
		Set<OutputPort> outputPorts = activity.getOutputPorts();
		assertEquals(expectedOutputs.size(), outputPorts.size());
		for (OutputPort outputPort : outputPorts) {
			assertTrue("Wrong output : " + outputPort.getName(),
					expectedOutputs.remove(outputPort.getName()));
		}
	}

}
