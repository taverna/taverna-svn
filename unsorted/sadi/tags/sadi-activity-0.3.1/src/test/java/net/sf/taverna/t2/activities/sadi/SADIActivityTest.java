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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.ModelFactory;

import ca.wilkinsonlab.sadi.common.SADIException;

/**
 * Unit tests for {@link SADIActivity}.
 * 
 * @author David Withers
 * 
 */
public class SADIActivityTest {

	private SADIActivity activity;

	private SADIActivityConfigurationBean configurationBean;
	

	@Before
	public void setUp() throws Exception {
		activity = new SADIActivity() {
			@Override
			public RestrictionNode getInputRestrictionTree() throws SADIException, IOException {
				return new RestrictionNode(ModelFactory.createOntologyModel().createClass()) {
					private static final long serialVersionUID = 1L;
					@Override
					public String toString() {
						return "input";
					}
				};
			}
			@Override
			public RestrictionNode getOutputRestrictionTree() throws SADIException,  IOException {
				return new RestrictionNode(ModelFactory.createOntologyModel().createClass()) {
					private static final long serialVersionUID = 1L;
					@Override
					public String toString() {
						return "output";
					}
				};
			}
		};
		configurationBean = new SADIActivityConfigurationBean();
	}

	@Test
	public void testSADIActivity() {
		new SADIActivity();
	}

	@Test
	public void testConfigure() throws Exception {
		Set<String> expectedInputs = new HashSet<String>();
		expectedInputs.add("input");
		Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("output");

		activity.configure(configurationBean);

		Set<ActivityInputPort> inputPorts = activity.getInputPorts();
		assertEquals(expectedInputs.size(), inputPorts.size());
		for (ActivityInputPort inputPort : inputPorts) {
			assertTrue("Wrong output : " + inputPort.getName(), expectedInputs.remove(inputPort
					.getName()));
			assertEquals(1, inputPort.getDepth());
		}
		assertTrue(expectedInputs.size() == 0);

		Set<OutputPort> outputPorts = activity.getOutputPorts();
		assertEquals(expectedOutputs.size(), outputPorts.size());
		for (OutputPort outputPort : outputPorts) {
			assertTrue("Wrong output : " + outputPort.getName(), expectedOutputs.remove(outputPort
					.getName()));
			assertEquals(1, outputPort.getDepth());
		}
		assertTrue(expectedOutputs.size() == 0);
	}

	@Test
	public void testGetConfiguration() throws Exception {
		activity.configure(configurationBean);
		assertEquals(configurationBean, activity.getConfiguration());
	}
		
}
