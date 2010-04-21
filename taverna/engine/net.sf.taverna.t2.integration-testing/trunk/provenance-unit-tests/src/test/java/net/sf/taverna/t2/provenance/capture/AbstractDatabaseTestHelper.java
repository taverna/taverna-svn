package net.sf.taverna.t2.provenance.capture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.provenance.ProvenanceTestHelper;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.MergePort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;

import org.junit.Before;
import org.junit.Test;

/**
 * Test database content for provenance captured after a dataflow run.
 * <p>
 * Subclass this test to add the workflow specific tests
 * 
 * @author Stian Soiland-Reyes
 * @author Paolo Missier
 * 
 */
public abstract class AbstractDatabaseTestHelper extends ProvenanceTestHelper {

	protected Dataflow dataflow;

	protected Map<String, Dataflow> workflowPaths = new HashMap<String, Dataflow>();
	protected Map<String, List<String>> workflowIdToPaths = new HashMap<String, List<String>>();

	@Before
	public void runWorkflow() throws Exception {

		dataflow = prepareDataflowRun(getWorkflowName());
		findNestedWorkflows(dataflow, "/");

		for (Entry<String, Object> entry : getWorkflowInputs().entrySet()) {
			String portName = entry.getKey();
			Object value = entry.getValue();
			int depth = 0;
			if (value instanceof List) {
				depth = 1;
				// TODO: Support deeper input lists
			}
			T2Reference ref = getContext().getReferenceService().register(
					value, depth, true, getContext());
			WorkflowDataToken inputToken1 = new WorkflowDataToken("",
					new int[] {}, ref, getContext());
			getFacade().pushData(inputToken1, portName);
		}
		waitForCompletion();
	}

	protected abstract Map<String, Object> getWorkflowInputs();

	protected abstract Map<String, Object> getExpectedWorkflowOutputs();

	protected abstract String getWorkflowName();

	public void testWorkflows() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection().prepareStatement(
				"SELECT wfName,parentWfName,externalName from Workflow");
		ResultSet resultSet = statement.executeQuery();
		try {
			// debugOutput(resultSet);
			Set<String> wfIds = new HashSet<String>();
			while (resultSet.next()) {
				String wfName = resultSet.getString("wfName");
				String parentWfName = resultSet.getString("parentWfName");
				String externalName = resultSet.getString("externalName");
				wfIds.add(wfName);
				if (wfName.equals(dataflow.getInternalIdentier())) {
					assertNull(parentWfName);
					assertEquals(dataflow.getLocalName(), externalName);
				} else {
					List<String> paths = workflowIdToPaths.get(wfName);
					assertNotNull("Could not find workflow " + wfName, paths);
					assertNotNull("externalName not defined", externalName);
					String foundPath = null;
					for (String path : paths) {
						if (path.endsWith("/" + externalName + "/")) {
							foundPath = path;
							break;
						}
					}
					assertNotNull("Unexpected externalName " + externalName
							+ ", expected from " + paths, foundPath);
					URI parentPath = URI.create(foundPath).resolve("../");
					Dataflow parentDataflow = workflowPaths.get(parentPath
							.toString());
					assertEquals(parentDataflow.getInternalIdentier(),
							parentWfName);
				}
			}
			Set<String> expectedWfIds = new HashSet<String>();
			for (Dataflow df : workflowPaths.values()) {
				expectedWfIds.add(df.getInternalIdentier());
			}
			assertEquals(wfIds, expectedWfIds);
		} finally {
			resultSet.close();
		}
	}

	public void testWfInstance() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		java.util.Date now = new java.util.Date();
		PreparedStatement statement = getConnection().prepareStatement(
				"SELECT instanceID,wfNameRef,timestamp from WfInstance");
		ResultSet resultSet = statement.executeQuery();
		Map<String, Timestamp> timestamps = new HashMap<String, Timestamp>();
		try {
			// debugOutput(resultSet);
			Set<String> wfIds = new HashSet<String>();
			while (resultSet.next()) {
				String instanceId = resultSet.getString("instanceID");
				String wfNameRef = resultSet.getString("wfNameRef");
				Timestamp timestamp = resultSet.getTimestamp("timestamp");
				assertTrue(timestamp.before(now));
				timestamps.put(wfNameRef, timestamp);
				wfIds.add(wfNameRef);
				assertEquals(getFacade().getWorkflowRunId(), instanceId);
			}
			Set<String> expectedWfIds = new HashSet<String>();
			for (Dataflow df : workflowPaths.values()) {
				expectedWfIds.add(df.getInternalIdentier());
			}
			assertEquals(wfIds, expectedWfIds);
			Timestamp topTimestamp = timestamps.get(dataflow
					.getInternalIdentier());

			// Check that nested workflows have later timestamps
			for (Entry<String, Timestamp> entry : timestamps.entrySet()) {
				if (!entry.getKey().equals(dataflow.getInternalIdentier())) {
					assertTrue(entry.getValue().after(topTimestamp));
				}
			}
		} finally {
			resultSet.close();
		}
	}

	/**
	 * All tests in one go, to avoid multiple workflow runs etc.
	 * 
	 * @throws Exception
	 */
	@Test
	public void allTests() throws Exception {

		testWorkflows();
		testProcessors();
		testVars();
		testVarsWorkflowPorts();
		testArc();

		testWfInstance();
		testCollection();
		testVarBindings();
		testVarBindingsWorkflowPorts();
		testProcBinding();

		testData();
	}

	public void testArc() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT sourceVarNameRef,sourcePNameRef,sinkVarNameRef,sinkPNameRef from Arc WHERE wfInstanceRef=?");
		for (Dataflow df : workflowPaths.values()) {
			Set<String> expectedLinks = describeDataLinks(df);
			// System.out.println(expectedLinks);

			// Magic processor names meaning workflow ports
			Set<String> myProcessorNames = new HashSet<String>();
			for (String path : workflowIdToPaths.get(df.getInternalIdentier())) {
				if (path.equals("/")) {
					myProcessorNames.add(df.getLocalName());
				} else {
					String[] split = path.split("/");
					myProcessorNames.add(split[split.length - 1]);
				}
			}

			Set<String> links = new HashSet<String>();

			statement.setString(1, df.getInternalIdentier());
			ResultSet resultSet = statement.executeQuery();
			try {
				while (resultSet.next()) {
					String sourceVarNameRef = resultSet
							.getString("sourceVarNameRef");
					String sourcePNameRef = resultSet
							.getString("sourcePNameRef");
					String sinkVarNameRef = resultSet
							.getString("sinkVarNameRef");
					String sinkPNameRef = resultSet.getString("sinkPNameRef");

					StringBuilder link = new StringBuilder();
					if (!myProcessorNames.contains(sourcePNameRef)) {
						link.append(sourcePNameRef);
						link.append(".");
					}
					link.append(sourceVarNameRef);

					link.append("->");

					if (!myProcessorNames.contains(sinkPNameRef)) {
						link.append(sinkPNameRef);
						link.append(".");
					}
					link.append(sinkVarNameRef);
					links.add(link.toString());
				}
				// System.out.println(links);
			} finally {
				resultSet.close();
			}
			assertEquals(expectedLinks, links);

		}

	}

	protected Set<String> describeDataLinks(Dataflow df) {

		Set<String> foundLinks = new HashSet<String>();

		for (Datalink link : df.getLinks()) {
			StringBuilder linkRef = new StringBuilder();
			EventForwardingOutputPort source = link.getSource();
			if (source instanceof ProcessorPort) {
				linkRef.append(((ProcessorPort) source).getProcessor()
						.getLocalName());
				linkRef.append('.');
			} else if (source instanceof MergePort) {
				MergePort mergePort = (MergePort) source;
				linkRef.append(mergePort.getMerge().getLocalName());
				linkRef.append(':'); // : indicates merge ..
			}
			linkRef.append(source.getName());

			linkRef.append("->");

			EventHandlingInputPort sink = link.getSink();
			if (sink instanceof ProcessorPort) {
				linkRef.append(((ProcessorPort) sink).getProcessor()
						.getLocalName());
				linkRef.append('.');
			} else if (sink instanceof MergePort) {
				MergePort mergePort = (MergePort) sink;
				linkRef.append(mergePort.getMerge().getLocalName());
				linkRef.append(':');
			}
			linkRef.append(sink.getName());

			String linkStr = linkRef.toString();
			foundLinks.add(linkStr);
		}
		return foundLinks;
	}

	public void testData() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection().prepareStatement(
				"SELECT * from Data");
		ResultSet resultSet = statement.executeQuery();
		try {
			// Table is always empty..
			assertTrue(!resultSet.next());
		} finally {
			resultSet.close();
		}
	}

	public void testProcBinding() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT pNameRef,iteration,actName from ProcBinding WHERE execIDRef=? AND wfNameRef=?");
		statement.setString(1, getFacade().getWorkflowRunId());
		for (Dataflow df : workflowPaths.values()) {
			Map<String, Processor> expectedProcessors = new HashMap<String, Processor>();
			for (Processor p : df.getProcessors()) {
				expectedProcessors.put(p.getLocalName(), p);
			}
			statement.setString(2, df.getInternalIdentier());
			Set<String> processors = new HashSet<String>();
			ResultSet resultSet = statement.executeQuery();
			try {
				while (resultSet.next()) {
					String pNameRef = resultSet.getString("pNameRef");
					String iteration = resultSet.getString("iteration");
					String actName = resultSet.getString("actName");
					processors.add(pNameRef);
					// TODO: Test iteration, actName
				}
			} finally {
				resultSet.close();
			}
		}
	}

	public void testCollection() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT collId,parentCollIdRef,pNameRef,varNameRef,iteration from Collection WHERE wfInstanceRef=?");
		statement.setString(1, getFacade().getWorkflowRunId());

		ResultSet resultSet = statement.executeQuery();
		// debugOutput(resultSet);
		try {
			while (resultSet.next()) {
				String collId = resultSet.getString("collId");
				String parentCollIdRef = resultSet.getString("parentCollIdRef");
				String pNameRef = resultSet.getString("pNameRef");
				String varNameRef = resultSet.getString("varNameRef");
				String iteration = resultSet.getString("iteration");
				
				
			}
		} finally {
			resultSet.close();
		}
	}

	public void testProcessors() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT pName,type,isTopLevel from Processor WHERE wfInstanceRef=?");
		for (Dataflow df : workflowPaths.values()) {
			Map<String, Processor> expectedProcessors = new HashMap<String, Processor>();
			for (Processor p : df.getProcessors()) {
				expectedProcessors.put(p.getLocalName(), p);
			}
			if (df.getInternalIdentier().equals(dataflow.getInternalIdentier())) {
				// Also expect the top-level workflow name
				// NOTE: Might be in conflict with a processor in the workflow
				expectedProcessors.put(dataflow.getLocalName(), null);
			}
			statement.setString(1, df.getInternalIdentier());
			ResultSet resultSet = statement.executeQuery();
			Set<String> processors = new HashSet<String>();
			try {
				while (resultSet.next()) {
					String pName = resultSet.getString("pName");
					String type = resultSet.getString("type");

					boolean isTopLevel = resultSet.getBoolean("isTopLevel");
					processors.add(pName);
					if (isTopLevel) {
						assertEquals(dataflow.getInternalIdentier(), df
								.getInternalIdentier());
						// Aliasing as a DataflowActivity
						assertEquals(DataflowActivity.class.getCanonicalName(),
								type);
						assertEquals(dataflow.getLocalName(), pName);
					} else {
						Processor expectedProcessor = expectedProcessors
								.get(pName);
						assertNotNull(expectedProcessor);
						String expectedType = expectedProcessor
								.getActivityList().get(0).getClass()
								.getCanonicalName();
						assertEquals(expectedType, type);
					}
				}
			} finally {
				resultSet.close();
			}
			assertEquals(expectedProcessors.keySet(), processors);
		}
	}

	public void testVars() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT varName,inputOrOutput,nestingLevel,actualNestingLevel,anlSet,reorder from Var WHERE wfInstanceRef=? AND pNameRef=?");
		for (Dataflow df : workflowPaths.values()) {
			statement.setString(1, df.getInternalIdentier());
			for (Processor p : df.getProcessors()) {
				statement.setString(2, p.getLocalName());
				ResultSet resultSet = statement.executeQuery();
				Map<String, ProcessorInputPort> inputPorts = new HashMap<String, ProcessorInputPort>();
				for (ProcessorInputPort ip : p.getInputPorts()) {
					inputPorts.put(ip.getName(), ip);
				}
				Map<String, ProcessorOutputPort> outputPorts = new HashMap<String, ProcessorOutputPort>();
				for (ProcessorOutputPort op : p.getOutputPorts()) {
					outputPorts.put(op.getName(), op);
				}
				try {
					Set<String> foundInputs = new HashSet<String>();
					Set<String> foundOutputs = new HashSet<String>();

					while (resultSet.next()) {
						String varName = resultSet.getString("varName");
						boolean isInput = resultSet.getBoolean("inputOrOutput");

						String nestingLevel = resultSet
								.getString("nestingLevel");
						String actualNestingLevel = resultSet
								.getString("actualNestingLevel");
						String anlSet = resultSet.getString("anlSet");
						String reorder = resultSet.getString("reorder");

						// System.out.print(df.getLocalName() + " " +
						// p.getLocalName() + " " + (isInput ? "i" : "o"));
						// System.out.println(":" + varName + " " + nestingLevel
						// + " " + actualNestingLevel + " " + anlSet + " " +
						// reorder);
						//						
						// TODO: Test anlset, reorder

						if (isInput) {
							ProcessorInputPort inputPort = inputPorts
									.get(varName);
							assertNotNull(inputPort);
							foundInputs.add(varName);
							assertEquals(
									Integer.toString(inputPort.getDepth()),
									nestingLevel);
							Datalink incoming = inputPort.getIncomingLink();
							if (incoming == null) {
								assertNull(actualNestingLevel);
							} else {
								assertEquals(Integer.toString(incoming
										.getResolvedDepth()),
										actualNestingLevel);
							}
						} else {
							ProcessorOutputPort outputPort = outputPorts
									.get(varName);
							assertNotNull(outputPort);
							foundOutputs.add(varName);
							assertEquals(Integer
									.toString(outputPort.getDepth()),
									nestingLevel);
							if (outputPort.getOutgoingLinks().isEmpty()) {
								assertNull(actualNestingLevel);
							} else {
								Datalink datalink = outputPort
										.getOutgoingLinks().iterator().next();
								// TODO: Fix so this is not null for dataflow
								// output ports
								// assertEquals(Integer.toString(datalink
								// .getResolvedDepth()),
								// actualNestingLevel);
							}
						}
					}
					assertEquals(inputPorts.keySet(), foundInputs);
					assertEquals(outputPorts.keySet(), foundOutputs);

				} finally {
					resultSet.close();
				}
			}
		}
	}

	public void testVarsWorkflowPorts() throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT varName,inputOrOutput,nestingLevel,actualNestingLevel,anlSet,reorder from Var WHERE wfInstanceRef=? AND pNameRef=?");
		Dataflow df = dataflow;
		statement.setString(1, df.getInternalIdentier());
		statement.setString(2, df.getLocalName());
		ResultSet resultSet = statement.executeQuery();
		Map<String, DataflowInputPort> inputPorts = new HashMap<String, DataflowInputPort>();
		for (DataflowInputPort ip : df.getInputPorts()) {
			inputPorts.put(ip.getName(), ip);
		}
		Map<String, DataflowOutputPort> outputPorts = new HashMap<String, DataflowOutputPort>();
		for (DataflowOutputPort op : df.getOutputPorts()) {
			outputPorts.put(op.getName(), op);
		}
		try {
			Set<String> foundInputs = new HashSet<String>();
			Set<String> foundOutputs = new HashSet<String>();

			while (resultSet.next()) {
				String varName = resultSet.getString("varName");
				boolean isInput = resultSet.getBoolean("inputOrOutput");

				String nestingLevel = resultSet.getString("nestingLevel");
				String actualNestingLevel = resultSet
						.getString("actualNestingLevel");
				String anlSet = resultSet.getString("anlSet");
				String reorder = resultSet.getString("reorder");

				// System.out.print(df.getLocalName() + " " +
				// p.getLocalName() + " " + (isInput ? "i" : "o"));
				// System.out.println(":" + varName + " " + nestingLevel
				// + " " + actualNestingLevel + " " + anlSet + " " +
				// reorder);
				//						
				// TODO: Test anlset, reorder

				if (isInput) {
					DataflowInputPort inputPort = inputPorts.get(varName);
					assertNotNull(inputPort);
					foundInputs.add(varName);
					assertEquals(Integer.toString(inputPort.getDepth()),
							nestingLevel);
					Datalink incoming = inputPort.getIncomingLink();
					if (incoming == null) {
						assertNull(actualNestingLevel);
					} else {
						assertEquals(Integer.toString(incoming
								.getResolvedDepth()), actualNestingLevel);
					}
				} else {
					DataflowOutputPort outputPort = outputPorts.get(varName);
					assertNotNull(outputPort);
					foundOutputs.add(varName);
					assertEquals(Integer.toString(outputPort.getDepth()),
							nestingLevel);
					if (outputPort.getOutgoingLinks().isEmpty()) {
						assertNull(actualNestingLevel);
					} else {
						Datalink datalink = outputPort.getOutgoingLinks()
								.iterator().next();
						// TODO: Fix so this is not null for dataflow
						// output ports
						// assertEquals(Integer.toString(datalink
						// .getResolvedDepth()),
						// actualNestingLevel);
					}
				}
			}
			assertEquals(inputPorts.keySet(), foundInputs);
			assertEquals(outputPorts.keySet(), foundOutputs);

		} finally {
			resultSet.close();
		}

	}

	public void testVarBindings() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT varNameRef,value,collIdRef,positionInColl,valueType,iteration,ref from VarBinding WHERE wfInstanceRef=? AND wfNameRef=? AND pNameRef=?");
		statement.setString(1, getFacade().getWorkflowRunId());

		Map<String, Object> values = new HashMap<String, Object>();

		for (Dataflow df : workflowPaths.values()) {
			statement.setString(2, df.getInternalIdentier());
			for (Processor p : df.getProcessors()) {
				statement.setString(3, p.getLocalName());

				ResultSet resultSet = statement.executeQuery();
				try {
					while (resultSet.next()) {
						String varNameRef = resultSet.getString("varNameRef");
						String value = resultSet.getString("value");
						String collIdRef = resultSet.getString("collIdRef");
						String positionInColl = resultSet
								.getString("positionInColl");
						String valueType = resultSet.getString("valueType");
						String iteration = resultSet.getString("iteration");

						// assertNull(collIdRef);
						// assertEquals("1", positionInColl);
						// assertEquals("[]", iteration);
						// assertEquals("referenceSet", valueType);
						// TODO: Test REF

						T2Reference ref = getReferenceService()
								.referenceFromString(value);
						Object resolved = getReferenceService()
								.renderIdentifier(ref, Object.class,
										getContext());

						String key = df.getInternalIdentier() + "/"
								+ p.getLocalName() + "/" + varNameRef
								+ iteration;
						values.put(key, resolved);
						// System.out.println(key + " " + resolved);
					}
				} finally {
					resultSet.close();
				}
			}
		}

		// Naive test of just the set of values
		Map<String, Object> expectedValues = getExpectedIntermediateValues();

		System.out.println(values);		
		assertEquals(expectedValues, values);

	}

	protected Map<String, Object> getExpectedIntermediateValues() {
		Map<String, Object> expectedIntermediateValues = new HashMap<String, Object>();

		return expectedIntermediateValues;
	}

	protected void findNestedWorkflows(Dataflow df, String path) {
		if (path == null || path.equals("")) {
			path = "/";
		}
		workflowPaths.put(path, df);
		List<String> paths = workflowIdToPaths.get(df.getInternalIdentier());
		if (paths == null) {
			paths = new ArrayList<String>();
			workflowIdToPaths.put(df.getInternalIdentier(), paths);
		}
		paths.add(path);

		for (Processor p : df.getProcessors()) {
			for (Activity<?> a : p.getActivityList()) {
				if (a instanceof NestedDataflow) {
					NestedDataflow nestedDataflowActivity = (NestedDataflow) a;
					Dataflow nestedDataflow = nestedDataflowActivity
							.getNestedDataflow();
					findNestedWorkflows(nestedDataflow, path + p.getLocalName()
							+ "/");
					continue;
				}
			}
		}
	}

	public void testVarBindingsWorkflowPorts() throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT value,varNameRef,iteration from VarBinding WHERE pNameRef=? AND wfNameRef=?");
		statement.setString(1, dataflow.getLocalName());
		statement.setString(2, dataflow.getInternalIdentier());
		Map<String, Object> values = new HashMap<String, Object>();
		ResultSet resultSet = statement.executeQuery();
		try {
			while (resultSet.next()) {
				String value = resultSet.getString("value");
				String varNameRef = resultSet.getString("varNameRef");
				String iteration = resultSet.getString("iteration");
				T2Reference ref = getReferenceService().referenceFromString(
						value);

				Object rendered = getReferenceService().renderIdentifier(ref,
						Object.class, getContext());
				values.put(varNameRef + iteration, rendered);
			}
		} finally {
			resultSet.close();
		}

		Map<String, Object> expected = new HashMap<String, Object>();

		expected.putAll(getExpectedWorkflowOutputs());
		expected.putAll(getExpectedWorkflowInputs());
		assertEquals(expected, values);
	}

	protected Map<String, Object> getExpectedWorkflowInputs() {
		return getWorkflowInputs();
	}

	protected void debugOutput(ResultSet resultSet) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		int columnCount = metaData.getColumnCount();
		for (int i = 1; i <= columnCount; i++) {
			System.out.print(metaData.getColumnLabel(i) + "\t");
		}
		System.out.println();

		while (resultSet.next()) {
			for (int i = 1; i <= columnCount; i++) {
				System.out.print(resultSet.getString(i) + "\t");
			}
			System.out.println();
		}
		resultSet.close();
	}

}
