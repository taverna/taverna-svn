/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.activities.sequencefile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for SequenceFileActivity.
 * 
 * @author David Withers
 */
public class SequenceFileActivityTest {

	private SequenceFileActivity activity;

	private SequenceFileActivityConfigurationBean configurationBean;

	@Before
	public void setUp() throws Exception {
		activity = new SequenceFileActivity();
		configurationBean = new SequenceFileActivityConfigurationBean();
	}

	@Test
	public void testExampleActivity() {
		assertNotNull(new SequenceFileActivity());
	}

	@Test
	public void testExecuteAsynch() throws Exception {
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("fileurl", "src/test/resources/fasta_protein.txt");
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("name", String.class);
		expectedOutputs.put("sequences", String.class);

		configurationBean.setFileFormat(FileFormat.fasta);
		configurationBean.setSequenceType(SequenceType.protein);
		activity.configure(configurationBean);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs,
				expectedOutputs);
		assertTrue(outputs.containsKey("name"));
		assertTrue(outputs.get("name") instanceof List<?>);
		List<?> names = (List<?>) outputs.get("name");
		assertEquals(245, names.size());
		assertEquals("ENSG00000008130|ENST00000160596", names.get(7));
		assertTrue(outputs.containsKey("sequences"));
		assertTrue(outputs.get("sequences") instanceof List<?>);
		List<?> sequences = (List<?>) outputs.get("sequences");
		assertEquals(245, sequences.size());
		assertEquals(
				"MEENMIVYVEKKVLEDPAIASDESFGAVKKKFCTFREDYDDISNQIDFIICLGGDGTLLYASSLFQGTQLLFSGVG*",
				sequences.get(7));
	}

	@Test
	public void testConfigureExampleActivityConfigurationBean() throws Exception {
		activity.configure(configurationBean);

		Set<ActivityInputPort> inputPorts = activity.getInputPorts();
		assertEquals(1, inputPorts.size());
		ActivityInputPort inputPort = inputPorts.iterator().next();
		assertEquals("fileurl", inputPort.getName());
		assertEquals(0, inputPort.getDepth());

		Set<String> expectedPortNames = new HashSet(Arrays.asList("name", "sequences"));
		Set<OutputPort> outputPorts = activity.getOutputPorts();
		assertEquals(2, outputPorts.size());
		for (OutputPort outputPort : outputPorts) {
			assertTrue(expectedPortNames.remove(outputPort.getName()));
			assertEquals(1, outputPort.getDepth());
			assertEquals(0, outputPort.getGranularDepth());
		}
	}
}
