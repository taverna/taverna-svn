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
package net.sf.taverna.t2.activities.soaplab;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.OutputPort;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit tests for SoaplabActivity.
 * 
 * @author David Withers
 */
public class SoaplabActivityTest {

	private SoaplabActivity activity;

	private SoaplabActivityConfigurationBean configurationBean;

	@Ignore("Integration test")
	@Before
	public void setUp() throws Exception {
		activity = new SoaplabActivity();
		configurationBean = new SoaplabActivityConfigurationBean();
		configurationBean
				.setEndpoint("http://www.ebi.ac.uk/soaplab/emboss4/services/utils_misc.embossversion");
	}

	@Ignore("Integration test")
	@Test
	public void testExecuteAsynch() throws Exception {
		Map<String, Object> inputs = new HashMap<String, Object>();
		// inputs.put("full", "true");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("report", String.class);
		expectedOutputs.put("outfile", String.class);

//		activity.configure(configurationBean);

//		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(
//				activity, inputs, expectedOutputs);
//		assertTrue(outputs.containsKey("report"));
//		// assertTrue(outputs.get("report") instanceof String);
//		assertTrue(outputs.containsKey("outfile"));
//		assertTrue(outputs.get("outfile") instanceof String);
//		System.out.println(outputs.get("outfile"));

		// test with polling
		configurationBean.setPollingInterval(5);
		configurationBean.setPollingIntervalMax(6);
		configurationBean.setPollingBackoff(1.2);
//		activity.configure(configurationBean);

//		outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs,
//				expectedOutputs);
//		assertTrue(outputs.containsKey("report"));
//		assertTrue(outputs.containsKey("outfile"));
	}

	@Ignore("Integration test")
	@Test
	public void testSoaplabActivity() {
		assertNotNull(new SoaplabActivity());
	}

	@Ignore("Integration test")
	@Test
	public void testConfigureSoaplabActivityConfigurationBean()
			throws Exception {
		Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("report");
		expectedOutputs.add("outfile");

//		activity.configure(configurationBean);
		Set<OutputPort> ports = activity.getOutputPorts();
		assertEquals(expectedOutputs.size(), ports.size());
		for (OutputPort outputPort : ports) {
			assertTrue("Wrong output : " + outputPort.getName(),
					expectedOutputs.remove(outputPort.getName()));
		}
	}

	@Ignore("Integration test")
	@Test
	public void testIsPollingDefined() throws Exception {
		assertFalse(activity.isPollingDefined());
//		activity.configure(configurationBean);
		assertFalse(activity.isPollingDefined());
		configurationBean.setPollingInterval(1000);
//		activity.configure(configurationBean);
		assertTrue(activity.isPollingDefined());
	}

}
