package net.sf.taverna.t2.provenance.capture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.naming.NamingException;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.provenance.CaptureResultsListener;
import net.sf.taverna.t2.provenance.DataflowTimeoutException;
import net.sf.taverna.t2.provenance.ProvenanceTestHelper;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.MergePort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
public abstract class AbstractDatabaseTestHelper {

	protected static Dataflow dataflow;

	protected static ProvenanceTestHelper testHelper;

	@BeforeClass
	public static void clearDataflow() {
		dataflow = null;
	}

	@BeforeClass
	public static void makeTestHelper() throws NamingException {
		testHelper = new ProvenanceTestHelper();
	}

	protected static Map<String, List<String>> workflowIdToPaths = new HashMap<String, List<String>>();

	protected static Map<String, Dataflow> workflowPaths = new HashMap<String, Dataflow>();

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

	protected static void findNestedWorkflows(Dataflow df, String path) {
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

	public Connection getConnection() throws InstantiationException,
			IllegalAccessException, ClassNotFoundException, SQLException {
		return testHelper.getConnection();
	}

	public InvocationContext getContext() {
		return testHelper.getContext();
	}

	protected abstract Map<String, Object> getExpectedCollections();

	protected Map<String, Object> getExpectedIntermediateValues() {
		Map<String, Object> expectedIntermediateValues = new HashMap<String, Object>();

		return expectedIntermediateValues;
	}

	protected Map<String, Object> getExpectedWorkflowInputs() {
		return getWorkflowInputs();
	}

	protected abstract Map<String, Object> getExpectedWorkflowOutputs();

	public WorkflowInstanceFacade getFacade() {
		return testHelper.getFacade();
	}

	public CaptureResultsListener getListener() {
		return testHelper.getListener();
	}

	public ProvenanceAccess getProvenanceAccess() {
		return testHelper.getProvenanceAccess();
	}

	public ProvenanceConnector getProvenanceConnector() {
		return testHelper.getProvenanceConnector();
	}

	public ReferenceService getReferenceService() {
		return testHelper.getReferenceService();
	}

	protected abstract Map<String, Object> getWorkflowInputs();

	protected abstract String getWorkflowName();

	public Dataflow prepareDataflowRun(String dataflowFile) throws IOException,
			JDOMException, DeserializationException, EditException,
			InvalidDataflowException {
		return testHelper.prepareDataflowRun(dataflowFile);
	}

	@Before
	public void runWorkflow() throws Exception {
		synchronized (AbstractDatabaseTestHelper.class) {
			if (dataflow != null) {
				return;
			}
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
	}

	@Test
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

	@Test
	public void testCollection() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT collId,parentCollIdRef,"
								+ "  Collection.pNameRef AS pNameRef,"
								+ "  Collection.varNameRef AS varNameRef,"
								+ "   iteration, inputOrOutput "
								+ "FROM Collection " + "INNER JOIN Var "
								+ "  ON Collection.varNameRef = Var.varName "
								+ "  AND Collection.pNameRef = Var.pNameRef " +
								// "  AND Collection.wfNameRef = Var.wfInstanceRef "
								// +
								"WHERE Collection.wfInstanceRef=? AND Var.wfInstanceRef=?");
		statement.setString(1, getFacade().getWorkflowRunId());
		// FIXME: Collections don't support nested workflows
		statement.setString(2, dataflow.getInternalIdentier());

		Map<String, Object> collections = new HashMap<String, Object>();

		ResultSet resultSet = statement.executeQuery();
		// debugOutput(resultSet);
		try {
			while (resultSet.next()) {
				String collId = resultSet.getString("collId");
				String parentCollIdRef = resultSet.getString("parentCollIdRef");
				String pNameRef = resultSet.getString("pNameRef");
				String varNameRef = resultSet.getString("varNameRef");
				String iteration = resultSet.getString("iteration");
				boolean isInput = resultSet.getBoolean("inputOrOutput");

				T2Reference ref = getReferenceService().referenceFromString(
						collId);
				Object resolved;
				try {
					resolved = getReferenceService().renderIdentifier(ref,
							Object.class, getContext());
				} catch (ReferenceServiceException ex) {
					resolved = ref;
				}
				if (parentCollIdRef != null && !parentCollIdRef.equals("TOP")) {
					// If there's a parent, are we there as well?
					T2Reference parentRef = getReferenceService()
							.referenceFromString(parentCollIdRef);
					IdentifiedList<T2Reference> parentList = getReferenceService()
							.getListService().getList(parentRef);
					assertTrue(parentList.contains(ref));
				} else {
					// TODO: Assert that this is supposed to be a "TOP"
					// collection
				}

				String key = dataflow.getInternalIdentier() + "/" + pNameRef
						+ "/" + (isInput ? "i:" : "o:") + varNameRef
						+ iteration;
				collections.put(key, resolved);

			}
		} finally {
			resultSet.close();
		}
		Map<String, Object> expectedCollections = getExpectedCollections();
		assertEquals(expectedCollections, collections);

	}

	@Test
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

	@Test
	@SuppressWarnings("unused")
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

	@Test
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

	@Test
	public void testVarBindings() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT "
								+ "  varNameRef,value,collIdRef,positionInColl,valueType,"
								+ "  iteration,ref,inputOrOutput "
								+ "FROM VarBinding "
								+ "INNER JOIN Var "
								+ "  ON VarBinding.varNameRef = Var.varName "
								+ "  AND VarBinding.pNameRef = Var.pNameRef "
								+
								// FIXME: Non-unique foreign key to VarBinding
								"  AND VarBinding.wfNameRef = Var.wfInstanceRef "
								+ "WHERE VarBinding.wfInstanceRef=? "
								+ "  AND VarBinding.wfNameRef=? "
								+ "  AND VarBinding.pNameRef=?");
		statement.setString(1, getFacade().getWorkflowRunId());

		Map<String, Object> intermediateValues = new HashMap<String, Object>();

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
						boolean isInput = resultSet.getBoolean("inputOrOutput");
						if (collIdRef != null) {
							System.out.println(positionInColl + " " + iteration + " " + collIdRef);
							// Last bit of iteration should include reference
							int expectedEnd = Integer.parseInt(positionInColl)-1;
							
							// TODO: Enable test for iteration position
							
//							assertTrue("Expected iteration to end with "
//									+ expectedEnd + "] but iteration is "
//									+ iteration, iteration
//									.endsWith(expectedEnd + "]"));
						} else {
							assertEquals("1", positionInColl);
							// TODO: Enable test for []
//							 assertEquals("[]", iteration);
						}
						// TODO: Test REF and valueType

						T2Reference ref = getReferenceService()
								.referenceFromString(value);
						Object resolved = getReferenceService()
								.renderIdentifier(ref, Object.class,
										getContext());

						if (collIdRef != null) {
							// Check the position in the list
							T2Reference listRef = getReferenceService()
									.referenceFromString(collIdRef);
							IdentifiedList<T2Reference> l = getReferenceService()
									.getListService().getList(listRef);
							T2Reference t2Reference = l.get(Integer
									.parseInt(positionInColl)-1);
							assertEquals(t2Reference, ref);
						}

						String key = df.getInternalIdentier() + "/"
								+ p.getLocalName() + "/"
								+ (isInput ? "i:" : "o:") + varNameRef
								+ iteration;
						intermediateValues.put(key, resolved);
						// System.out.println(key + " " + resolved);
					}
				} finally {
					resultSet.close();
				}
			}
		}

		// Naive test of just the set of values
		Map<String, Object> expectedValues = getExpectedIntermediateValues();

		assertEquals(expectedValues, intermediateValues);

	}

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	@Test
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

	public void waitForCompletion() throws InterruptedException,
			DataflowTimeoutException {
		testHelper.waitForCompletion();
	}

}
