package net.sf.taverna.t2.provenance.database;

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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector.DataBinding;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector.DataflowInvocation;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector.ProcessorEnactment;
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
 * Subclass this test to add the workflow specific details and tests
 * 
 * @author Stian Soiland-Reyes
 * @author Paolo Missier
 * 
 */
public abstract class AbstractDbTestHelper {

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

	private static Timestamp notExecutedBefore;

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
		List<String> paths = workflowIdToPaths.get(df.getInternalIdentifier());
		if (paths == null) {
			paths = new ArrayList<String>();
			workflowIdToPaths.put(df.getInternalIdentifier(), paths);
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
		Map<String, Object> workflowInputs = new HashMap<String, Object>();
		for (Entry<String,Object> entry : getWorkflowInputs().entrySet()) {
			workflowInputs.put(entry.getKey() + "[]", entry.getValue());
		}
		return workflowInputs;
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
		synchronized (AbstractDbTestHelper.class) {
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
	public void testDataLink() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT sourcePortName,sourceProcessorName,destinationPortName,destinationProcessorName from Datalink WHERE workflowId=?");
		for (Dataflow df : workflowPaths.values()) {
			Set<String> expectedLinks = describeDataLinks(df);
			// Magic processor names meaning workflow ports
			Set<String> myProcessorNames = new HashSet<String>();
			for (String path : workflowIdToPaths.get(df.getInternalIdentifier())) {
				if (path.equals("/")) {
					myProcessorNames.add(df.getLocalName());
				} else {
					String[] split = path.split("/");
					myProcessorNames.add(split[split.length - 1]);
				}
			}

			Set<String> links = new HashSet<String>();

			statement.setString(1, df.getInternalIdentifier());
			ResultSet resultSet = statement.executeQuery();
			try {
				while (resultSet.next()) {
					String sourcePortName = resultSet
							.getString("sourcePortName");
					String sourceProcessorName = resultSet
							.getString("sourceProcessorName");
					String destinationPortName = resultSet
							.getString("destinationPortName");
					String destinationProcessorName = resultSet.getString("destinationProcessorName");

					StringBuilder link = new StringBuilder();
					if (!myProcessorNames.contains(sourceProcessorName)) {
						link.append(sourceProcessorName);
						link.append(".");
					}
					link.append(sourcePortName);

					link.append("->");

					if (!myProcessorNames.contains(destinationProcessorName)) {
						link.append(destinationProcessorName);
						link.append(".");
					}
					link.append(destinationPortName);
					links.add(link.toString());
				}
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
								+ "  Collection.processorNameRef AS processorNameRef,"
								+ "  Collection.portName AS portName,"
								+ "   iteration, isInputPort "
								+ "FROM Collection " + "INNER JOIN Port "
								+ "  ON Collection.portName = Port.portName "
								+ "  AND Collection.processorNameRef = Port.processorName " +
								// "  AND Collection.workflowId = Port.workflowId "
								// +
								"WHERE Collection.workflowRunId=? AND Port.workflowId=?");
		statement.setString(1, getFacade().getWorkflowRunId());
		// FIXME: Collections don't support nested workflows
		statement.setString(2, dataflow.getInternalIdentifier());

		Map<String, Object> collections = new HashMap<String, Object>();

		ResultSet resultSet = statement.executeQuery();
		// debugOutput(resultSet);
		try {
			while (resultSet.next()) {
				String collId = resultSet.getString("collId");
				String parentCollIdRef = resultSet.getString("parentCollIdRef");
				String processorNameRef = resultSet.getString("processorNameRef");
				String portName = resultSet.getString("portName");
				String iteration = resultSet.getString("iteration");
				boolean isInput = resultSet.getBoolean("isInputPort");

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

				String key = dataflow.getInternalIdentifier() + "/" + processorNameRef
						+ "/" + (isInput ? "i:" : "o:") + portName
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
	public void testProcessors() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT processorName,firstActivityClass,isTopLevel from Processor WHERE workflowId=?");
		for (Dataflow df : workflowPaths.values()) {
			Map<String, Processor> expectedProcessors = new HashMap<String, Processor>();
			for (Processor p : df.getProcessors()) {
				expectedProcessors.put(p.getLocalName(), p);
			}
			if (df.getInternalIdentifier().equals(dataflow.getInternalIdentifier())) {
				// Also expect the top-level workflow name
				// NOTE: Might be in conflict with a processor in the workflow
				expectedProcessors.put(dataflow.getLocalName(), null);
			}
			statement.setString(1, df.getInternalIdentifier());
			ResultSet resultSet = statement.executeQuery();
			Set<String> processors = new HashSet<String>();
			try {
				while (resultSet.next()) {
					String pName = resultSet.getString("processorName");
					String type = resultSet.getString("firstActivityClass");

					boolean isTopLevel = resultSet.getBoolean("isTopLevel");
					processors.add(pName);
					if (isTopLevel) {
						assertEquals(dataflow.getInternalIdentifier(), df
								.getInternalIdentifier());
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

	public static void assertMapsEquals(String msg, 
			Map<? extends Object, ? extends Object> expected,
			Map<? extends Object, ? extends Object> actual) {
		assertSetsEquals(msg, expected.keySet(), actual.keySet());
		for (Object key : expected.keySet()) {
			assertEquals(msg + " values for key " + key,
					expected.get(key), actual.get(key));
		}
	}

	public static void assertSetsEquals(String msg,
			Collection<? extends Object> expected, Collection<? extends Object> actual) {
		Set<Object> missing = new HashSet<Object>(expected);
		missing.removeAll(actual);
		Set<Object> extra = new HashSet<Object>(actual);
		extra.removeAll(expected);
		assertTrue(msg + " extra: " + extra, extra.isEmpty());
		assertTrue(msg + " missing: " + missing, missing.isEmpty());
	}

	
	protected abstract Set<String> getExpectedProcesses();


	@Test
	public void testProcessorEnactments() throws Exception {
		PreparedStatement statement = getConnection().prepareStatement(
				"SELECT " + ProcessorEnactment.enactmentStarted + ","
						+ ProcessorEnactment.enactmentEnded + ","
						+ ProcessorEnactment.iteration + ","
						+ "Processor.processorName" + " FROM "
						+ ProcessorEnactment.ProcessorEnactment
						+ " INNER JOIN " + "Processor" + " ON "
						+ ProcessorEnactment.ProcessorEnactment + "."
						+ ProcessorEnactment.processorId + " = "
						+ "Processor.processorId" + " WHERE "
						+ ProcessorEnactment.workflowRunId + "=?");
		statement.setString(1, getFacade().getWorkflowRunId());
		ResultSet resultSet = statement.executeQuery();
		
		Map<String, Timestamp> processorStarted = new HashMap<String, Timestamp>();
		Map<String, Timestamp> processorEnded = new HashMap<String, Timestamp>();
		
		Timestamp notExecutedAfter = new Timestamp(System.currentTimeMillis());
		assertTrue(notExecutedAfter.after(notExecutedBefore));
		
		try {
			while (resultSet.next()) {
				Timestamp enactmentStarted = resultSet.getTimestamp(ProcessorEnactment.enactmentStarted.name());
				Timestamp enactmentEnded = resultSet.getTimestamp(ProcessorEnactment.enactmentEnded.name());
				String iteration = resultSet.getString(ProcessorEnactment.iteration.name());
				String pName = resultSet.getString("processorName");
				String processorKey = pName + iteration;
				processorStarted.put(processorKey, enactmentStarted);
				processorEnded.put(processorKey, enactmentEnded);
				assertTrue(enactmentStarted.after(notExecutedBefore));
//				assertTrue(enactmentEnded.after(enactmentStarted));
//				assertTrue(notExecutedAfter.after(enactmentEnded));
			}
		} finally {
			resultSet.close();
		}
		
		
		
		Set<String> expectedProcesses = getExpectedProcesses();
		assertSetsEquals("Unexpected process ", expectedProcesses, processorStarted.keySet());

		
	}
	
	@Test
	public void testProcessorEnactmentDataBindings() throws Exception {
		testProcessorEnactmentDataBindingsForWorkflowRun(getFacade().getWorkflowRunId());
	}

	@Test
	public void testProcessorEnactmentDataBindingsTwoRuns() throws Exception {
		String firstRunId = getFacade().getWorkflowRunId();
		testProcessorEnactmentDataBindingsForWorkflowRun(firstRunId);
		// Force a second run
		dataflow = null;
		runWorkflow();
		String secondRunId = getFacade().getWorkflowRunId();
		assertTrue("Did not run again, run IDs are both " + firstRunId, 
				! firstRunId.equals(secondRunId));
		testProcessorEnactmentDataBindingsForWorkflowRun(secondRunId);
		
		// Should still work for firstRunId
		testProcessorEnactmentDataBindingsForWorkflowRun(firstRunId);
	}
	
	
	protected void testProcessorEnactmentDataBindingsForWorkflowRun(
			String workflowRunId) throws Exception {
		Map<String, Object> intermediateValues = new HashMap<String, Object>();
		PreparedStatement statement = getConnection().prepareStatement(
				"SELECT " + DataBinding.t2Reference + " AS t2ref, "
						+ ProcessorEnactment.processIdentifier + ", "
						+ ProcessorEnactment.iteration + ", "
						+ "Port.portName AS portName, "
						+ "Port.isInputPort AS isInputPort, "
						+ "Processor.workflowId AS workflowId, "
						+ "Processor.processorName AS processorName" + " FROM "
						+ ProcessorEnactment.ProcessorEnactment
						+ " INNER JOIN " + "Processor" + " ON "
						+ ProcessorEnactment.ProcessorEnactment + "."
						+ ProcessorEnactment.processorId + " = "
						+ "Processor.processorId" + " INNER JOIN "
						+ DataBinding.DataBinding + " ON "
						+ DataBinding.dataBindingId + " IN ("
						+ ProcessorEnactment.initialInputsDataBindingId + ","
						+ ProcessorEnactment.finalOutputsDataBindingId + ") "
						+ " INNER JOIN Port " + " ON Port.portId="
						+ DataBinding.DataBinding + "." + DataBinding.portId
						+ " WHERE " + ProcessorEnactment.ProcessorEnactment
						+ "." + ProcessorEnactment.workflowRunId + "=?");
		statement.setString(1, workflowRunId);
		ResultSet resultSet = statement.executeQuery();

		try {
			while (resultSet.next()) {
				String processorName = resultSet.getString("processorName");
				String workflowId = resultSet.getString("workflowId");
				String iteration = resultSet.getString("iteration");
				String t2ref = resultSet.getString("t2ref");
				String processIdentifier = resultSet
						.getString("processIdentifier");

				String[] processIdentifierSplit = processIdentifier.split(":");
				if (workflowId.equals(dataflow.getInternalIdentifier())) {
					assertEquals("Expected processIdentifier length 3, but is " + processIdentifier, 
							3, processIdentifierSplit.length);
				} else {
					assertTrue("Expected processIdentifier longer than 5, but is " + processIdentifier, 
							processIdentifierSplit.length > 5);
				}
				
				assertEquals("Expected last processIdentifier element to be processor name, but processId is " + processIdentifier,
						processorName, processIdentifierSplit[processIdentifierSplit.length-1]);

				
				String portName = resultSet.getString("portName");

				boolean isInput = resultSet.getBoolean("isInputPort");
				String key = workflowId + "/" + processorName + "/"
						+ (isInput ? "i:" : "o:") + portName + iteration;

				T2Reference ref = getReferenceService().referenceFromString(
						t2ref);
				Object resolved = getReferenceService().renderIdentifier(ref,
						Object.class, getContext());
				intermediateValues.put(key, resolved);
			}
		} finally {
			resultSet.close();
		}

		Map<String, Object> expectedIntermediateValues = getExpectedIntermediates();
		assertMapsEquals("Unexpected intermediate values",
				expectedIntermediateValues, intermediateValues);
	}	


	protected Map<String, Object> getExpectedIntermediates() {
		Map<String, Object> intermediates = new HashMap<String, Object>();
		
		Map<String, Object> expectedIntermediateValues = getExpectedIntermediateValues(); 
			
		Map<String, Object> expectedCollections = getExpectedCollections();
		intermediates.putAll(expectedCollections);
		
		for (String collectionKey : expectedCollections.keySet()) {
			String collectionPrefix = collectionKey.split("\\]", 2)[0];
			if (! collectionPrefix.endsWith("[")) {
				collectionPrefix = collectionPrefix + ",";
			}
			Set<String> removeItems = new HashSet<String>();
			for (String itemKey : expectedIntermediateValues.keySet()) {
				String itemPrefix = itemKey.split("\\]", 2)[0];
				if (itemPrefix.startsWith(collectionPrefix)) {
					removeItems.add(itemKey);
				}
			}
			expectedIntermediateValues.keySet().removeAll(removeItems);
		}
		intermediates.putAll(expectedIntermediateValues);
		return intermediates;
		
	}

	@BeforeClass
	public static void findEarlyTimestamp() {
		notExecutedBefore = new Timestamp(System.currentTimeMillis());
	}
	
	@Test
	public void testPortBinding() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT "
								+ "  Port.portName,value,collIdRef,positionInColl,valueType,"
								+ "  iteration,ref,isInputPort "
								+ "FROM PortBinding "
								+ "INNER JOIN Port "
								+ "  ON PortBinding.portName = Port.portName "
								+ "  AND PortBinding.processorNameRef = Port.processorName "
								+
								// FIXME: Non-unique foreign key to PortBinding
								"  AND PortBinding.workflowId = Port.workflowId "
								+ "WHERE PortBinding.workflowRunId=? "
								+ "  AND PortBinding.workflowId=? "
								+ "  AND PortBinding.processorNameRef=?");
		statement.setString(1, getFacade().getWorkflowRunId());

		Map<String, Object> intermediateValues = new HashMap<String, Object>();

		for (Dataflow df : workflowPaths.values()) {
			statement.setString(2, df.getInternalIdentifier());
			for (Processor p : df.getProcessors()) {
				statement.setString(3, p.getLocalName());

				ResultSet resultSet = statement.executeQuery();
				try {
					while (resultSet.next()) {
						String portName = resultSet.getString("portName");
						String value = resultSet.getString("value");
						String collIdRef = resultSet.getString("collIdRef");
						String positionInColl = resultSet
								.getString("positionInColl");
						String valueType = resultSet.getString("valueType");
						String iteration = resultSet.getString("iteration");
						boolean isInput = resultSet.getBoolean("isInputPort");
						if (collIdRef != null) {
							// Last bit of iteration should include reference
							int expectedEnd = Integer.parseInt(positionInColl)-1;
							
							// TODO: Enable test for iteration position
							
//							assertTrue("Expected iteration to end with "
//									+ expectedEnd + "] but iteration is "
//									+ iteration, iteration
//									.endsWith(expectedEnd + "]"));intermediateValues
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

						String key = df.getInternalIdentifier() + "/"
								+ p.getLocalName() + "/"
								+ (isInput ? "i:" : "o:") + portName
								+ iteration;
						intermediateValues.put(key, resolved);
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
	public void testPortBindingsWorkflowPorts() throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT value,portName,iteration " +
						"FROM PortBinding " +
						"WHERE processorNameRef=? AND workflowId=? AND workflowRunId=?");
		statement.setString(1, dataflow.getLocalName());
		statement.setString(2, dataflow.getInternalIdentifier());
		statement.setString(3, getFacade().getWorkflowRunId());
		Map<String, Object> values = new HashMap<String, Object>();
		ResultSet resultSet = statement.executeQuery();
		try {
			while (resultSet.next()) {
				String value = resultSet.getString("value");
				String portName = resultSet.getString("portName");
				String iteration = resultSet.getString("iteration");
				T2Reference ref = getReferenceService().referenceFromString(
						value);

				Object rendered = getReferenceService().renderIdentifier(ref,
						Object.class, getContext());
				values.put(portName + iteration, rendered);
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
	public void testDataflowInvocation() throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		Map<String, Object> values = new HashMap<String, Object>();
		String sql = "SELECT " + "portName," + DataBinding.t2Reference + ",Port.isInputPort"
				+ "\n FROM " + DataflowInvocation.DataflowInvocation
				+ " AS DI " + "\n INNER JOIN " + DataBinding.DataBinding
				+ "\n ON " + DataBinding.dataBindingId + " IN (" + DataflowInvocation.inputsDataBinding + "," 
				+ DataflowInvocation.outputsDataBinding + ")" 
				+ "\n INNER JOIN Port " + "\n ON Port.portId="
				+ DataBinding.DataBinding + "." + DataBinding.portId
				+ "\n WHERE " + "DI." + DataflowInvocation.workflowRunId
				+ "=? AND DI." + DataflowInvocation.workflowId + "=?";
		//System.out.println(sql);
		PreparedStatement statement = getConnection()
				.prepareStatement(
						sql );
		statement.setString(1, getFacade().getWorkflowRunId());
		statement.setString(2, dataflow.getInternalIdentifier());
		
		ResultSet resultSet = statement.executeQuery();
		try {
			while (resultSet.next()) {
				String t2Reference = resultSet.getString("t2Reference");
				String portName = resultSet.getString("portName");
				boolean isInput = resultSet.getBoolean("isInputPort");
				T2Reference ref = getReferenceService().referenceFromString(
						t2Reference);

				Object rendered = getReferenceService().renderIdentifier(ref,
						Object.class, getContext());

				String key = (isInput ? "i:" : "o:") + portName + "[]";
				values.put(key, rendered);
			}
		} finally {
			resultSet.close();
		}			
	
		
		Map<String, Object> expected = getExpectedWorkflowPortCollections();
		assertMapsEquals("Unexpected workflow port values", expected, values);
	}
	
	protected Map<String, Object> getExpectedWorkflowPortCollections() {
		Map<String, Object> expected = new HashMap<String, Object>();
		for (Entry<String,Object> entry : getExpectedWorkflowInputs().entrySet()) {
			expected.put("i:" + entry.getKey(), entry.getValue());
		}
		for (Entry<String,Object> entry : getExpectedWorkflowOutputs().entrySet()) {
			expected.put("o:" + entry.getKey(), entry.getValue());
		}
		return expected;
	}

	@Test
	public void testPort() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT portId,portName,isInputPort,depth,resolvedDepth,iterationStrategyOrder from Port WHERE workflowId=? AND processorName=?");
		Set<UUID> portIds = new HashSet<UUID>();
		for (Dataflow df : workflowPaths.values()) {
			statement.setString(1, df.getInternalIdentifier());
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
						String portName = resultSet.getString("portName");
						boolean isInput = resultSet.getBoolean("isInputPort");

						String depth = resultSet
								.getString("depth");
						String resolvedDepth = resultSet
								.getString("resolvedDepth");
						String iterationStrategyOrder = resultSet.getString("iterationStrategyOrder");

						String portId = resultSet.getString("portId");
						assertTrue("Port ID not unique: " + portId + " for port " + portName, portIds.add(UUID.fromString(portId)));
						
						
						// TODO: Test iterationStrategyOrder

						if (isInput) {
							ProcessorInputPort inputPort = inputPorts
									.get(portName);
							assertNotNull(inputPort);
							foundInputs.add(portName);
							assertEquals(
									Integer.toString(inputPort.getDepth()),
									depth);
							Datalink incoming = inputPort.getIncomingLink();
							if (incoming == null) {
								assertNull(resolvedDepth);
							} else {
								assertEquals(Integer.toString(incoming
										.getResolvedDepth()),
										resolvedDepth);
							}
						} else {
							ProcessorOutputPort outputPort = outputPorts
									.get(portName);
							assertNotNull(outputPort);
							foundOutputs.add(portName);
							assertEquals(Integer
									.toString(outputPort.getDepth()),
									depth);
							if (outputPort.getOutgoingLinks().isEmpty()) {
								assertNull(resolvedDepth);
							} else {
								Datalink datalink = outputPort
										.getOutgoingLinks().iterator().next();
								// TODO: Fix so this is not null for dataflow
								// output ports
								// assertEquals(Integer.toString(datalink
								// .getResolvedDepth()),
								// resolvedDepth);
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
	public void testWorkflowPorts() throws SQLException,
			InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		PreparedStatement statement = getConnection()
				.prepareStatement(
						"SELECT portName,isInputPort,depth,resolvedDepth,iterationStrategyOrder from Port WHERE workflowId=? AND processorName=?");
		Dataflow df = dataflow;
		statement.setString(1, df.getInternalIdentifier());
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
				String portName = resultSet.getString("portName");
				boolean isInput = resultSet.getBoolean("isInputPort");

				String depth = resultSet.getString("depth");
				String resolvedDepth = resultSet
						.getString("resolvedDepth");
				String iterationStrategyOrder = resultSet.getString("iterationStrategyOrder");


				// TODO: Test iterationStrategyOrder

				if (isInput) {
					DataflowInputPort inputPort = inputPorts.get(portName);
					assertNotNull(inputPort);
					foundInputs.add(portName);
					assertEquals(Integer.toString(inputPort.getDepth()),
							depth);
					Datalink incoming = inputPort.getIncomingLink();
					if (incoming == null) {
						assertNull(resolvedDepth);
					} else {
						assertEquals(Integer.toString(incoming
								.getResolvedDepth()), resolvedDepth);
					}
				} else {
					DataflowOutputPort outputPort = outputPorts.get(portName);
					assertNotNull(outputPort);
					foundOutputs.add(portName);
					assertEquals(Integer.toString(outputPort.getDepth()),
							depth);
					if (outputPort.getOutgoingLinks().isEmpty()) {
						assertNull(resolvedDepth);
					} else {
						Datalink datalink = outputPort.getOutgoingLinks()
								.iterator().next();
						// TODO: Fix so this is not null for dataflow
						// output ports
						// assertEquals(Integer.toString(datalink
						// .getResolvedDepth()),
						// resolvedDepth);
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
	public void testWorkflowRun() throws SQLException, InstantiationException,
			IllegalAccessException, ClassNotFoundException {
		java.util.Date now = new java.util.Date();
		PreparedStatement statement = getConnection().prepareStatement(
				"SELECT workflowId,timestamp from WorkflowRun WHERE workflowRunId=?");
		statement.setString(1, getFacade().getWorkflowRunId());
		ResultSet resultSet = statement.executeQuery();
		Map<String, Timestamp> timestamps = new HashMap<String, Timestamp>();
		try {
			// debugOutput(resultSet);
			Set<String> wfIds = new HashSet<String>();
			while (resultSet.next()) {
				String workflowId = resultSet.getString("workflowId");
				Timestamp timestamp = resultSet.getTimestamp("timestamp");
				assertTrue(timestamp.before(now));
				timestamps.put(workflowId, timestamp);
				wfIds.add(workflowId);
			}
			Set<String> expectedWfIds = new HashSet<String>();
			for (Dataflow df : workflowPaths.values()) {
				expectedWfIds.add(df.getInternalIdentifier());
			}
			assertEquals(expectedWfIds, wfIds);
			Timestamp topTimestamp = timestamps.get(dataflow
					.getInternalIdentifier());

			// Check that nested workflows have later timestamps
			for (Entry<String, Timestamp> entry : timestamps.entrySet()) {
				if (!entry.getKey().equals(dataflow.getInternalIdentifier())) {
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
				"SELECT workflowId,parentWorkflowId,externalName from Workflow");
		ResultSet resultSet = statement.executeQuery();
		try {
			// debugOutput(resultSet);
			Set<String> wfIds = new HashSet<String>();
			while (resultSet.next()) {
				String workflowId = resultSet.getString("workflowId");
				String parentWorkflowId = resultSet.getString("parentWorkflowId");
				String externalName = resultSet.getString("externalName");
				wfIds.add(workflowId);
				if (workflowId.equals(dataflow.getInternalIdentifier())) {
					assertNull(parentWorkflowId);
					assertEquals(dataflow.getLocalName(), externalName);
				} else {
					List<String> paths = workflowIdToPaths.get(workflowId);
					assertNotNull("Could not find workflow " + workflowId, paths);
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
					assertEquals(parentDataflow.getInternalIdentifier(),
							parentWorkflowId);
				}
			}
			Set<String> expectedWfIds = new HashSet<String>();
			for (Dataflow df : workflowPaths.values()) {
				expectedWfIds.add(df.getInternalIdentifier());
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
