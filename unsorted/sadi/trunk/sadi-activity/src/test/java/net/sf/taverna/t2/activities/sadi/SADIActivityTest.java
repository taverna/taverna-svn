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
package net.sf.taverna.t2.activities.sadi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import org.junit.Before;
import org.junit.Test;

import ca.wilkinsonlab.sadi.client.Registry;
import ca.wilkinsonlab.sadi.client.RegistryImpl;
import ca.wilkinsonlab.sadi.utils.QueryExecutorFactory;

/**
 * Unit tests for {@link SADIActivity}.
 * 
 * @author David Withers
 * 
 */
public class SADIActivityTest {

	private static final String TEST_REGISTRY_PATH = "src/test/resources/registry.rdf";
	
	private SADIActivity activity;

	private SADIActivityConfigurationBean configurationBean;

	@Before
	public void setUp() throws Exception {
		activity = new SADIActivity() {
			@Override
			public Registry getRegistry() throws IOException {
				return new RegistryImpl(QueryExecutorFactory.createFileModelQueryExecutor(TEST_REGISTRY_PATH));
			}
		};
		configurationBean = new SADIActivityConfigurationBean();
		configurationBean.setServiceURI("http://sadiframework.org/examples/hello");
	}

	@Test
	public void testSADIActivity() {
		new SADIActivity();
	}

	@Test
	public void testConfigure() throws Exception {
		Map<String, String> expectedInputPorts = new HashMap<String, String>();
		expectedInputPorts.put("name", "http://xmlns.com/foaf/0.1/name some *");
		Map<String, String> expectedOutputPorts = new HashMap<String, String>();
		expectedOutputPorts.put("greeting", "http://sadiframework.org/examples/hello.owl#greeting some http://www.w3.org/2001/XMLSchema#string");
		
		activity.configure(configurationBean);

		for (ActivityInputPort inputPort : activity.getInputPorts()) {
			SADIActivityPort port = (SADIActivityPort)inputPort;
			String expectedPathSpec = expectedInputPorts.get(port.getName());
			assertNotNull(String.format("unexpected input port %s", port.getName()),
					expectedPathSpec);
			assertEquals(String.format("unexpected path for input port %s", port.getName()),
					expectedPathSpec, port.getRDFPath().toString());
			assertEquals(String.format("wrong depth for input port %s", port.getName()),
					SADIActivity.INPUT_DEPTH, port.getDepth());
		}
		for (OutputPort outputPort : activity.getOutputPorts()) {
			SADIActivityPort port = (SADIActivityPort)outputPort;
			String expectedPathSpec = expectedOutputPorts.get(port.getName());
			assertNotNull(String.format("unexpected output port %s", port.getName()),
					expectedPathSpec);
			assertEquals(String.format("unexpected path for output port %s", port.getName()),
					expectedPathSpec, port.getRDFPath().toString());
			assertEquals(String.format("wrong depth for output port %s", port.getName()),
					SADIActivity.OUTPUT_DEPTH, port.getDepth());
		}
	}

	@Test
	public void testGetConfiguration() throws Exception {
		activity.configure(configurationBean);
		assertEquals(configurationBean, activity.getConfiguration());
	}
		
}
