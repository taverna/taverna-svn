package net.sf.taverna.t2.provenance.query;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.naming.NamingException;

import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.provenance.CaptureResultsListener;
import net.sf.taverna.t2.provenance.DataflowTimeoutException;
import net.sf.taverna.t2.provenance.ProvenanceTestHelper;
import net.sf.taverna.t2.provenance.api.NativeAnswer;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.api.Query;
import net.sf.taverna.t2.provenance.api.QueryAnswer;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.provenance.lineageservice.Dependencies;
import net.sf.taverna.t2.provenance.lineageservice.LineageQueryResultRecord;
import net.sf.taverna.t2.provenance.lineageservice.ProvenanceAnalysis;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProcessorEnactment;
import net.sf.taverna.t2.provenance.lineageservice.utils.ProvenanceProcessor;
import net.sf.taverna.t2.provenance.lineageservice.utils.Port;
import net.sf.taverna.t2.provenance.lineageservice.utils.QueryPort;
import net.sf.taverna.t2.provenance.lineageservice.utils.WorkflowInstance;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.MergePort;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import static net.sf.taverna.t2.provenance.database.AbstractDbTestHelper.*;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test provenance queries after a dataflow run.
 * <p>
 * Subclass this test to add the workflow specific details and tests
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public abstract class AbstractQueryTestHelper {

	protected static Dataflow dataflow;

	protected static ProvenanceTestHelper testHelper;

	@BeforeClass
	public static void clearDataflow() {
		dataflow = null; 
		workflowIdToPaths.clear();
		workflowPaths.clear();
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

	protected abstract Set<String> getExpectedProcesses();
	
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
		synchronized (AbstractQueryTestHelper.class) {
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
	public void testSimpleQuery() throws SQLException {
		Query provenanceQuery = new Query();
		provenanceQuery.setRunIDList(Arrays.asList(getFacade()
				.getWorkflowRunId()));
		QueryPort queryPort = new QueryPort();
		queryPort.setWfName(dataflow.getInternalIdentier());
		queryPort.setPath(ProvenanceAnalysis.ALL_PATHS_KEYWORD);
		provenanceQuery.setTargetPorts(Arrays.asList(queryPort));
		QueryAnswer answer = getProvenanceAccess()
				.executeQuery(provenanceQuery);
		NativeAnswer provenanceAnswer = answer.getNativeAnswer();
		System.out.println(provenanceAnswer.getAnswer());
	}

	@Test
	public void fetchInputPortData() throws Exception {
		Map<String, Object> expectedWorkflowInputs = getExpectedWorkflowInputs();
		Map<String, Object> recordedInputs = new HashMap<String, Object>();
		String wfInstance = getFacade().getWorkflowRunId();
		String workflowId = dataflow.getInternalIdentier();
		String pname = dataflow.getLocalName();
		for (InputPort inputPort : dataflow.getInputPorts()) {
			String port = inputPort.getName();
			Dependencies fetchPortData = getProvenanceAccess().fetchPortData(
					wfInstance, workflowId, pname, port, null);
			for (LineageQueryResultRecord record : fetchPortData.getRecords()) {
				String value = record.getValue();
				T2Reference referenceValue = getReferenceService()
						.referenceFromString(value);
				String iteration = record.getIteration();

				String key = port + iteration;
				Object resolved = getReferenceService().renderIdentifier(
						referenceValue, Object.class, getContext());

				recordedInputs.put(key, resolved);
			}
		}
		assertEquals(expectedWorkflowInputs, recordedInputs);
	}
	
	private static Timestamp notExecutedBefore;
	
	@BeforeClass
	public static void findEarlyTimestamp() {
		notExecutedBefore = new Timestamp(System.currentTimeMillis());
	}
	
	
	@Test
	public void listEnactments() {
		Timestamp notExecutedAfter = new Timestamp(System.currentTimeMillis());
		assertTrue(notExecutedAfter.after(notExecutedBefore));
		Set<String> processors = new HashSet<String>();

		String wfRunId = getFacade().getWorkflowRunId();
		List<ProcessorEnactment> enactments = getProvenanceAccess().getProcessorEnactments(wfRunId);
		assertTrue(! enactments.isEmpty());
		
		for (ProcessorEnactment enactment : enactments) {
			ProvenanceProcessor proc = getProvenanceAccess().getProvenanceProcessor(enactment.getProcessorId());
			String pName = proc.getPname();
			String processorKey = pName + enactment.getIteration();
			processors.add(processorKey);
			
			Timestamp enactmentStarted = enactment.getEnactmentStarted();
			Timestamp enactmentEnded = enactment.getEnactmentEnded();
			
			assertTrue(enactmentStarted.after(notExecutedBefore));
			assertTrue(enactmentEnded.after(enactmentStarted));
			assertTrue(notExecutedAfter.after(enactmentEnded));
		}			
		Set<String> expectedProcesses = getExpectedProcesses();
		assertEquals(expectedProcesses, processors);
	}
	
	@Test
	public void listEnactmentsByProcessor() {
		// TODO: Check processor paths from nested workflow 
		for (Processor p : dataflow.getProcessors()) {		
			String pName = p.getLocalName();
			String wfRunId = getFacade().getWorkflowRunId();
			List<ProcessorEnactment> enactments = getProvenanceAccess().getProcessorEnactments(wfRunId, pName);
			assertTrue(! enactments.isEmpty());
			for (ProcessorEnactment enactment : enactments) {
				ProvenanceProcessor proc = getProvenanceAccess().getProvenanceProcessor(enactment.getProcessorId());
				assertEquals(pName, proc.getPname());
			}			
		}

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
	
	@Test
	public void fetchEnactmentValues() {
		// TODO: Check processors in nested workflow 
		Map<String, Object>  intermediateValues = new HashMap<String, Object>();

		String wfRunId = getFacade().getWorkflowRunId();
		List<ProcessorEnactment> enactments = getProvenanceAccess().getProcessorEnactments(wfRunId);
		assertTrue(! enactments.isEmpty());
		for (ProcessorEnactment enactment : enactments) {
			ProvenanceProcessor proc = getProvenanceAccess().getProvenanceProcessor(enactment.getProcessorId());	
			String inputsId = enactment.getInitialInputsDataBindingId();
			String outputsId = enactment.getFinalOutputsDataBindingId();
			
			Map<Port, T2Reference> bindings = getProvenanceAccess().getDataBindings(inputsId);
			if (! outputsId.equals(inputsId)) {
				bindings.putAll(getProvenanceAccess().getDataBindings(outputsId));
			}			
			for (Entry<Port,T2Reference> binding : bindings.entrySet()) {
				Port port = binding.getKey();
				assertEquals(port.getProcessorName(), proc.getPname());
				assertEquals(port.getWorkflowId(), proc.getWfInstanceRef());
				//assertEquals(port.getProcessorId(), proc.getIdentifier());
				String key = proc.getWfInstanceRef() + "/" + proc.getPname()
				+ "/" + (port.isInputPort() ? "i:" : "o:") + port.getPortName()
				+ enactment.getIteration();
				Object resolved = getReferenceService().renderIdentifier(
						binding.getValue(), Object.class, getContext());
				intermediateValues.put(key, resolved);
			}
			
		}			
		
		Map<String, Object> expectedIntermediateValues = getExpectedIntermediates();
		
		assertMapsEquals("Unexpected intermediate values", 
				expectedIntermediateValues, intermediateValues);
	}
	
	

	
	@Test
	public void listRuns() throws JDOMException, IOException,
			DeserializationException, EditException {
		List<WorkflowInstance> runs = getProvenanceAccess()
				.listRuns(null, null);
		Set<String> wfIds = new HashSet<String>();
		for (WorkflowInstance run : runs) {
			assertEquals(getFacade().getWorkflowRunId(), run.getInstanceID());

			String workflowIdentifier = run.getWorkflowIdentifier();
			wfIds.add(workflowIdentifier);

			String externalName = run.getWorkflowExternalName();
			if (workflowIdentifier.equals(dataflow.getInternalIdentier())) {
				assertEquals(dataflow.getLocalName(), externalName);
				assertTrue(getProvenanceAccess().isTopLevelDataflow(
						workflowIdentifier));
			} else {
				List<String> paths = workflowIdToPaths.get(workflowIdentifier);
				assertNotNull("Could not find workflow " + workflowIdentifier,
						paths);
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
			}

			byte[] blob = run.getDataflowBlob();
			Dataflow loadedDf = testHelper
					.loadDataflow(new ByteArrayInputStream(blob));
			assertEquals(run.getWorkflowIdentifier(), loadedDf
					.getInternalIdentier());
		}
		assertEquals(workflowIdToPaths.keySet(), wfIds);
	}

	@Test
	public void listRunsSpecificWfId() throws JDOMException, IOException,
			DeserializationException, EditException {
		for (String dfId : workflowIdToPaths.keySet()) {
			List<WorkflowInstance> runs = getProvenanceAccess().listRuns(dfId,
					null);			
			assertEquals(1, runs.size());
			for (WorkflowInstance run : runs) {
				assertEquals(getFacade().getWorkflowRunId(), run
						.getInstanceID());
				assertEquals(dfId, run.getWorkflowIdentifier());
			}
		}
	}

	@Test
	public void fetchOutputPortData() throws Exception {
		Map<String, Object> expectedWorkflowOutputs = getExpectedWorkflowOutputs();
		Map<String, Object> recordedOutputs = new HashMap<String, Object>();
		String wfInstance = getFacade().getWorkflowRunId();
		String workflowId = dataflow.getInternalIdentier();
		String pname = dataflow.getLocalName();
		for (OutputPort outputPort : dataflow.getOutputPorts()) {
			String port = outputPort.getName();
			// FIXME: How to specify output port vs. input port?
			Dependencies fetchPortData = getProvenanceAccess().fetchPortData(
					wfInstance, workflowId, pname, port, null);
			System.out.println(fetchPortData);
			for (LineageQueryResultRecord record : fetchPortData.getRecords()) {
				String value = record.getValue();
				T2Reference referenceValue = getReferenceService()
						.referenceFromString(value);
				String iteration = record.getIteration();

				String key = port + iteration;
				Object resolved = getReferenceService().renderIdentifier(
						referenceValue, Object.class, getContext());

				recordedOutputs.put(key, resolved);
			}
		}
		assertEquals(expectedWorkflowOutputs, recordedOutputs);
	}

	@Test
	public void fetchProcessorData() throws Exception {
		Map<String, Object> expectedWorkflowOutputs = getExpectedIntermediateValues();
		Map<String, Object> recordedOutputs = new HashMap<String, Object>();
		String wfInstance = getFacade().getWorkflowRunId();
		for (Dataflow df : workflowPaths.values()) {
			String workflowId = df.getInternalIdentier();
			for (Processor p : df.getProcessors()) {
				String pName = p.getLocalName();
				List<ProcessorPort> ports = new ArrayList<ProcessorPort>();
				for (ProcessorPort ip : p.getInputPorts()) {
					ports.add(ip);
				}
				for (ProcessorPort ip : p.getOutputPorts()) {
					ports.add(ip);
				}
				for (ProcessorPort port : ports) {
					String portName = port.getName();
					// FIXME: How to specify output port vs. input port?
					Dependencies fetchPortData = getProvenanceAccess()
							.fetchPortData(wfInstance, workflowId, pName,
									portName, null);
					for (LineageQueryResultRecord record : fetchPortData
							.getRecords()) {
						String value = record.getValue();
						T2Reference referenceValue = getReferenceService()
								.referenceFromString(value);
						String iteration = record.getIteration();
						boolean isInput = port instanceof InputPort;
						Object resolved = getReferenceService()
								.renderIdentifier(referenceValue, Object.class,
										getContext());
						String key = df.getInternalIdentier() + "/" + pName
								+ "/" + (isInput ? "i:" : "o:") + portName
								+ iteration;
						recordedOutputs.put(key, resolved);
					}
				}
			}
		}

		assertMapsEquals("Mismatch workflow outputs ", expectedWorkflowOutputs, recordedOutputs);
	}
	
	

	public void waitForCompletion() throws InterruptedException,
			DataflowTimeoutException {
		testHelper.waitForCompletion();
	}

}
