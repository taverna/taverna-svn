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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration tests for {@link SADIActivity}.
 * 
 * @author David Withers
 * 
 * FIXME it's impratical to run integration tests against live services;
 * they are not static (in that their definitions and their output change)
 * and they are not reliable.  In the future, we should run a couple of
 * local services with the Jetty plugin and test against those.
 * (see http://code.google.com/p/sadi/source/browse/trunk/sadi.service.examples/pom.xml
 *  for an example of binding the Jetty plugin to a particular Maven phase)
 */
public class SADIActivityIT {

	private SADIActivity activity;

	private SADIActivityConfigurationBean configurationBean;
	
	@Before
	public void setUp() throws Exception {
		activity = new SADIActivity();
		configurationBean = new SADIActivityConfigurationBean();
		configurationBean.setSparqlEndpoint("http://biordf.net/sparql");
		configurationBean.setGraphName("http://sadiframework.org/registry/");
		configurationBean.setServiceURI("http://sadiframework.org/examples/uniprotInfo");
	}

	@Test
	@Ignore(value="service definitions change all the time")
	public void testConfigure() throws Exception {
		Set<String> expectedInputs = new HashSet<String>();
		expectedInputs.add("UniProt_Record");
		Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("hasName (string)");
		expectedOutputs.add("belongsToOrganism (string)");
		expectedOutputs.add("hasSequence (string)");

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
			assertEquals(2, outputPort.getDepth());
		}
		assertTrue(expectedOutputs.size() == 0);
	}

	@Test
	@Ignore(value="service definitions change all the time")
	public void testConfigureMI() throws Exception {
		Set<String> expectedInputs = new HashSet<String>();
		expectedInputs.add("UniProt_Record");
		Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("hasMolecularInteractionWith (GI_Record)");
		expectedOutputs.add("inBINDInteraction (BIND)");

		configurationBean.setServiceURI("http://sadiframework.org/services/getMolecularInteractions");
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
			assertEquals(2, outputPort.getDepth());
		}
		assertTrue(expectedOutputs.size() == 0);
	}

	@Test
	@Ignore(value="service definitions change all the time")
	public void testConfigureErmineJ() throws Exception {
		Set<String> expectedInputs = new HashSet<String>();
		expectedInputs.add("hasGOTerm (GO_Record)");
		expectedInputs.add("expressionLevel (double)");
		Set<String> expectedOutputs = new HashSet<String>();
		expectedOutputs.add("term (GO_Record)");
		expectedOutputs.add("p (double)");

		configurationBean.setServiceURI("http://sadiframework.org/examples/ermineJgo");
		activity.configure(configurationBean);

		Set<ActivityInputPort> inputPorts = activity.getInputPorts();
		assertEquals(expectedInputs.size(), inputPorts.size());
		for (ActivityInputPort inputPort : inputPorts) {
			assertTrue("Wrong output : " + inputPort.getName(), expectedInputs.remove(inputPort
					.getName()));
			assertEquals(2, inputPort.getDepth());
		}
		assertTrue(expectedInputs.size() == 0);

		Set<OutputPort> outputPorts = activity.getOutputPorts();
		assertEquals(expectedOutputs.size(), outputPorts.size());
		for (OutputPort outputPort : outputPorts) {
			assertTrue("Wrong output : " + outputPort.getName(), expectedOutputs.remove(outputPort
					.getName()));
			assertEquals(3, outputPort.getDepth());
		}
		assertTrue(expectedOutputs.size() == 0);
	}

	@Test
	@Ignore(value="service output changes all the time")
	public void testExecuteAsynch() throws Exception {
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("UniProt_Record", Arrays.asList("P68871", "Q7Z591"));
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("hasName (string)", String.class);
		expectedOutputs.put("belongsToOrganism (string)", String.class);

		activity.configure(configurationBean);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs,
				expectedOutputs);

		assertTrue(outputs.containsKey("hasName (string)"));
		Object output = outputs.get("hasName (string)");
		assertTrue(output instanceof List<?>);
		assertEquals(2, ((List<?>) output).size());
		assertTrue(((List<?>) output).get(0) instanceof List<?>);
		assertEquals(3, ((List<?>) ((List<?>) output).get(0)).size());
		assertTrue(((List<?>) ((List<?>) output).get(0)).contains("Hemoglobin subunit beta"));
		assertTrue(((List<?>) ((List<?>) output).get(0)).contains("Hemoglobin beta chain"));
		assertTrue(((List<?>) ((List<?>) output).get(0)).contains("Beta-globin"));
		assertTrue(((List<?>) output).get(1) instanceof List<?>);
		assertEquals(1, ((List<?>) ((List<?>) output).get(1)).size());
		assertTrue(((List<?>) ((List<?>) output).get(1)).contains("AT-hook-containing transcription factor"));

		assertTrue(outputs.containsKey("belongsToOrganism (string)"));
		output = outputs.get("belongsToOrganism (string)");
		assertTrue(output instanceof List<?>);
		assertEquals(2, ((List<?>) output).size());
		assertTrue(((List<?>) output).get(0) instanceof List<?>);
		assertEquals(1, ((List<?>) ((List<?>) output).get(0)).size());
		assertTrue(((List<?>) ((List<?>) output).get(0)).contains("Homo sapiens"));
		assertTrue(((List<?>) output).get(1) instanceof List<?>);
		assertEquals(1, ((List<?>) ((List<?>) output).get(1)).size());
		assertTrue(((List<?>) ((List<?>) output).get(1)).contains("Homo sapiens"));
	}
	
	@Test
	@Ignore(value="service output changes all the time")
	public void testExecuteAsynchGO() throws Exception {
		configurationBean.setServiceURI("http://sadiframework.org/services/getGOTerm");
		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("GO_Record", Arrays.asList("0005515", "0005524"));
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("hasTermName (Literal)", String.class);

		activity.configure(configurationBean);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs,
				expectedOutputs);

		assertTrue(outputs.containsKey("hasTermName (Literal)"));
		Object output = outputs.get("hasTermName (Literal)");
		System.out.println(output);
		assertTrue(output instanceof List<?>);
		assertEquals(2, ((List<?>) output).size());
		assertTrue(((List<?>) output).get(0) instanceof List<?>);
		assertEquals(1, ((List<?>) ((List<?>) output).get(0)).size());
		assertTrue(((List<?>) ((List<?>) output).get(0)).contains("protein binding"));
		assertTrue(((List<?>) output).get(1) instanceof List<?>);
		assertEquals(1, ((List<?>) ((List<?>) output).get(1)).size());
		assertTrue(((List<?>) ((List<?>) output).get(1)).contains("ATP binding"));
	}
	
	@Test
	@Ignore(value="service output changes all the time")
	public void testExecuteAsynchGO2() throws Exception {
		configurationBean.setServiceURI("http://sadiframework.org/services/getGOTerm");
		Map<String, Object> inputs1 = new HashMap<String, Object>();
		inputs1.put("GO_Record", Arrays.asList("0005515"));
		Map<String, Object> inputs2 = new HashMap<String, Object>();
		inputs2.put("GO_Record", Arrays.asList("0005524"));
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("hasTermName (Literal)", String.class);

		activity.configure(configurationBean);

		Map<String, Object> outputs1 = ActivityInvoker.invokeAsyncActivity(activity, inputs1,
				expectedOutputs);
		Map<String, Object> outputs2 = ActivityInvoker.invokeAsyncActivity(activity, inputs2,
				expectedOutputs);

		assertTrue(outputs1.containsKey("hasTermName (Literal)"));
		Object output = outputs1.get("hasTermName (Literal)");
		System.out.println(output);
		assertTrue(output instanceof List<?>);
		assertEquals(1, ((List<?>) output).size());
		assertTrue(((List<?>) output).get(0) instanceof List<?>);
		assertEquals(1, ((List<?>) ((List<?>) output).get(0)).size());
		assertTrue(((List<?>) ((List<?>) output).get(0)).contains("protein binding"));

		assertTrue(outputs2.containsKey("hasTermName (Literal)"));
		output = outputs2.get("hasTermName (Literal)");
		System.out.println(output);
		assertTrue(output instanceof List<?>);
		assertEquals(1, ((List<?>) output).size());
		assertTrue(((List<?>) output).get(0) instanceof List<?>);
		assertEquals(1, ((List<?>) ((List<?>) output).get(0)).size());
		assertTrue(((List<?>) ((List<?>) output).get(0)).contains("ATP binding"));
}
	
	@Test
	@Ignore(value="service output changes all the time")
	public void testExecuteAsynchErmineJ() throws Exception {
		configurationBean.setServiceURI("http://sadiframework.org/examples/ermineJgo");
		List<List<String>> hasGoTerm = new ArrayList<List<String>>();
		List<String> goTerms = new ArrayList<String>();
		Collections.addAll(goTerms, "0000166", "0003677", "0017111", "0005515", "0005524",
				"0005663", "0005634", "0003689", "0006260");
		hasGoTerm.add(goTerms);
		goTerms = new ArrayList<String>();
		Collections.addAll(goTerms, "0016740", "0006468", "0007155", "0005515", "0004672",
				"0007169", "0016301", "0004713", "0004872", "0004714", "0000166", "0005524",
				"0016020", "0005887", "0016021");
		hasGoTerm.add(goTerms);
		goTerms = new ArrayList<String>();
		Collections.addAll(goTerms, "0006986", "0006950", "0005524", "0000166");
		hasGoTerm.add(goTerms);

		List<List<String>> expressionLevels = new ArrayList<List<String>>();
		List<String> expressionLevel = new ArrayList<String>();
		expressionLevel.add("0.903370423");
		expressionLevels.add(expressionLevel);
		expressionLevel = new ArrayList<String>();
		expressionLevel.add("1.903370423");
		expressionLevels.add(expressionLevel);
		expressionLevel = new ArrayList<String>();
		expressionLevel.add("1.2");
		expressionLevels.add(expressionLevel);

		Map<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("hasGOTerm (GO_Record)", hasGoTerm);
		inputs.put("expressionLevel (double)", expressionLevels);
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("term (GO_Record)", String.class);
		expectedOutputs.put("p (double)", String.class);

		activity.configure(configurationBean);

		Map<String, Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs,
				expectedOutputs);

		assertTrue(outputs.containsKey("term (GO_Record)"));
		Object output = outputs.get("term (GO_Record)");
		assertTrue(output instanceof List<?>);
		assertEquals(1, ((List<?>) output).size());
		assertTrue(((List<?>) output).get(0) instanceof List<?>);
		assertEquals(2, ((List<?>) ((List<?>) output).get(0)).size());
		System.out.println((List<?>) ((List<?>) output).get(0));
		assertTrue(((List<?>) ((List<?>) output).get(0)).contains(Collections.singletonList("http://lsrn.org/GO:0005515")));
		assertTrue(((List<?>) ((List<?>) output).get(0)).contains(Collections.singletonList("http://lsrn.org/GO:0005524")));

		assertTrue(outputs.containsKey("p (double)"));
		output = outputs.get("p (double)");
		assertTrue(output instanceof List<?>);
		assertEquals(1, ((List<?>) output).size());
		assertTrue(((List<?>) output).get(0) instanceof List<?>);
		assertEquals(2, ((List<?>) ((List<?>) output).get(0)).size());
		assertTrue(((List<?>) ((List<?>) output).get(0)).contains(Collections.singletonList("0.33333333333333326")));
		assertTrue(((List<?>) ((List<?>) output).get(0)).contains(Collections.singletonList("0.0")));
	}

}
