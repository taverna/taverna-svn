package net.sf.taverna.t2.workflowmodel.processor.dispatch.layers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.impl.EventKeeper;
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;

import org.junit.Before;
import org.junit.Test;

/**
 * Test {@link Loop} layer.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class TestLoop {

	protected static InvocationContext context = new DummyInvocationContext();
	protected static final String HELLO = "hello";
	protected static final String HELLO_THERE = "hello there";
	protected static final String PORT_A = "portA";
	protected static final String PORT_B = "portB";
	protected static final String NULL_MSG = "Can't invoke conditional activity: null";
	protected static final String OTHER_PROCESS = "otherProcess";
	protected static final String PROCESS = "someProcess";

	// Lazy trick: Sleep 150 ms after pushing token before checking the
	// eventKeeper for the results
	protected static final int SLEEP = 150;
	protected DummyActivity activity;
	protected LoopConfiguration configuration;
	protected Edits edits = EditsRegistry.getEdits();

	protected EventKeeper eventKeeper = new EventKeeper();

	protected WorkflowDataToken helloToken;
	protected WorkflowDataToken listItem0Token;

	protected WorkflowDataToken listItem1Token;

	protected WorkflowDataToken listToken;

	protected Loop loopLayer;
	protected Processor processor;
	protected ProcessorInputPort processorInputPort;
	protected ReferenceService rs;

	@Test
	public void conditionFalse() throws ActivityConfigurationException,
			InterruptedException {
		assertEquals("Activity should not have been invoked yet", 0, activity
				.getInvocationCount());

		DummyActivity condition = new DummyActivity();
		DummyActivityConfiguration conditionConf = new DummyActivityConfiguration();
		conditionConf.setOutputPortName("loop");
		conditionConf.setOutputValue("false");
		condition.configure(conditionConf);

		configuration.setCondition(condition);

		assertTrue("runFirst not true", configuration.isRunFirst());
		assertEquals(0, eventKeeper.events.size());
		processorInputPort.receiveEvent(helloToken);
		Thread.sleep(SLEEP);
		assertEquals(1, eventKeeper.events.size());
		String output = (String) tokenTostring(eventKeeper.events.get(0));
		assertEquals(HELLO_THERE, output);
		assertEquals("Activity should have been invoked once", 1, activity
				.getInvocationCount());
		assertEquals("Condition should have been invoked once", 1, condition
				.getInvocationCount());
		assertEquals("Should not be any incoming jobs cached", 0,
				loopLayer.incomingJobs.size());
		assertEquals("Should not be any outgoing jobs cached", 0,
				loopLayer.outgoingJobs.size());
	}

	@Test
	public void conditionFalseExtraCondInputs()
			throws ActivityConfigurationException, InterruptedException {
		assertEquals("Activity should not have been invoked yet", 0, activity
				.getInvocationCount());

		DummyActivity condition = new DummyActivity() {
			@Override
			public void configure(DummyActivityConfiguration conf)
					throws ActivityConfigurationException {
				super.configure(conf);
				// Should be filled by the activity
				addInput(PORT_B, 0, true,
						new ArrayList<Class<? extends ExternalReferenceSPI>>(),
						String.class);
			}
		};
		DummyActivityConfiguration conditionConf = new DummyActivityConfiguration();
		conditionConf.setOutputPortName("loop");
		conditionConf.setOutputValue("false");
		condition.configure(conditionConf);

		configuration.setCondition(condition);

		assertTrue("runFirst not true", configuration.isRunFirst());
		assertEquals(0, eventKeeper.events.size());
		processorInputPort.receiveEvent(helloToken);
		Thread.sleep(SLEEP);
		assertEquals(1, eventKeeper.events.size());

		assertEquals("Unexpected number of input collections", 1,
				condition.calls.size());
		Map<String, T2Reference> condInputs = condition.calls.get(0);
		assertEquals("Unexpected number of input data", 2, condInputs.size());
		assertEquals("Didn't pass input " + PORT_A, helloToken.getData(),
				condInputs.get(PORT_A));
		T2Reference portBRef = condInputs.get(PORT_B);
		Object portB = rs.renderIdentifier(portBRef, String.class, context);
		assertEquals("Didn't pass input " + PORT_B, HELLO_THERE, portB);
	}

	@Test
	public void conditionTrueExtraOutputs()
			throws ActivityConfigurationException, InterruptedException {
		assertEquals("Activity should not have been invoked yet", 0, activity
				.getInvocationCount());
		final String YO = "Yo";

		DummyActivity condition = new DummyActivity() {
			@Override
			public void configure(DummyActivityConfiguration conf)
					throws ActivityConfigurationException {
				super.configure(conf);
				// Extra output should go down to invoking actual activity
				addOutput(PORT_A, 0);
			}

			@Override
			protected Map<String, T2Reference> createOutputs(
					AsynchronousActivityCallback callback) {
				Map<String, T2Reference> outputs = super
						.createOutputs(callback);
				InvocationContext invocationContext = callback.getContext();
				ReferenceService referenceService = invocationContext
						.getReferenceService();
				outputs.put(PORT_A, referenceService.register(YO, 0, true,
						invocationContext));
				return outputs;
			}
		};
		DummyActivityConfiguration conditionConf = new DummyActivityConfiguration();
		conditionConf.setOutputPortName("loop");
		conditionConf.setOutputValue("true");
		condition.configure(conditionConf);

		configuration.setCondition(condition);
		assertEquals(0, eventKeeper.events.size());
		processorInputPort.receiveEvent(helloToken);
		Thread.sleep(SLEEP);
		conditionConf.setOutputValue("false");

		assertTrue("Expected at least 5 calls ", activity.calls.size() > 5);

		// First call should have the original inputs
		Map<String, T2Reference> activityInputs = activity.calls.get(0);
		System.out.println(activityInputs);
		assertEquals("Unexpected number of input data", 1, activityInputs
				.size());

		T2Reference portARef = activityInputs.get(PORT_A);
		assertNotNull("Did not pass port " + PORT_A, portARef);
		Object portA = rs.renderIdentifier(portARef, String.class, context);
		assertEquals("Didn't pass original input " + PORT_A, HELLO, portA);

		// Second call should have got the modified input from the conditional
		// activity
		Map<String, T2Reference> secondInputs = activity.calls.get(1);
		System.out.println(secondInputs);
		assertEquals("Unexpected number of input data", 1, secondInputs.size());

		T2Reference secondPortARef = secondInputs.get(PORT_A);
		assertNotNull("Did not pass mport " + PORT_A, secondPortARef);
		Object secondPortA = rs.renderIdentifier(secondPortARef, String.class,
				context);
		assertEquals("Didn't pass modified input " + PORT_A, YO, secondPortA);
	}

	@Test
	public void conditionFalseIteration()
			throws ActivityConfigurationException, InterruptedException {
		assertEquals("Activity should not have been invoked yet", 0, activity
				.getInvocationCount());

		DummyActivity condition = new DummyActivity();
		DummyActivityConfiguration conditionConf = new DummyActivityConfiguration();
		conditionConf.setOutputPortName("loop");
		conditionConf.setOutputValue("false");
		condition.configure(conditionConf);

		configuration.setCondition(condition);

		assertTrue("runFirst not true", configuration.isRunFirst());
		assertEquals(0, eventKeeper.events.size());

		processorInputPort.receiveEvent(listItem0Token);
		processorInputPort.receiveEvent(listItem1Token);
		Thread.sleep(SLEEP);
		assertEquals(2, eventKeeper.events.size());
		assertEquals("Should not be two incoming jobs cached", 2,
				loopLayer.incomingJobs.size());
		assertEquals("Should not be two outgoing jobs cached", 2,
				loopLayer.outgoingJobs.size());

		processorInputPort.receiveEvent(listToken);
		Thread.sleep(SLEEP);

		assertEquals(3, eventKeeper.events.size());
		List<String> outputs = (List<String>) tokenTostring(eventKeeper.events
				.get(2));
		assertEquals(2, outputs.size());
		assertEquals(HELLO_THERE, outputs.get(0));
		assertEquals(HELLO_THERE, outputs.get(1));
		assertEquals("Invoked the activity more than two times", 2, activity
				.getInvocationCount());
		assertEquals("Condition should have been invoked twice", 2, condition
				.getInvocationCount());
		assertEquals("Should not be any incoming jobs cached", 0,
				loopLayer.incomingJobs.size());
		assertEquals("Should not be any outgoing jobs cached", 0,
				loopLayer.outgoingJobs.size());
	}

	@Test
	public void conditionTrueNotRunFirst()
			throws ActivityConfigurationException, InterruptedException {
		assertEquals("Activity should not have been invoked yet", 0, activity
				.getInvocationCount());

		DummyActivity condition = new DummyActivity();
		DummyActivityConfiguration conditionConf = new DummyActivityConfiguration();
		conditionConf.setOutputPortName("loop");
		conditionConf.setOutputValue("true");
		condition.configure(conditionConf);

		configuration.setCondition(condition);
		configuration.setRunFirst(false);
		assertFalse("runFirst not false", configuration.isRunFirst());
		assertEquals(0, eventKeeper.events.size());

		processorInputPort.receiveEvent(helloToken);
		Thread.sleep(SLEEP);
		assertEquals("Should not have returned yet", 0, eventKeeper.events
				.size());
		assertTrue("Activity should have been invoked more than 5 times",
				activity.getInvocationCount() > 5);
		assertTrue("Condition should have been invoked more than 5 times",
				condition.getInvocationCount() > 5);

		// Stop the loop
		conditionConf.setOutputValue("false");
		Thread.sleep(SLEEP);
		// Should now have returned
		assertEquals(1, eventKeeper.events.size());
		String output = (String) tokenTostring(eventKeeper.events.get(0));
		assertEquals(HELLO_THERE, output);
		// One extra conditional check when we started
		assertEquals("Invocation counts mismatch", condition
				.getInvocationCount() - 1, activity.getInvocationCount());

		assertEquals("Should not be any incoming jobs cached", 0,
				loopLayer.incomingJobs.size());
		assertEquals("Should not be any outgoing jobs cached", 0,
				loopLayer.outgoingJobs.size());
	}

	@Test
	public void conditionTrueRunFirst() throws ActivityConfigurationException,
			InterruptedException {
		assertEquals("Activity should not have been invoked yet", 0, activity
				.getInvocationCount());

		DummyActivity condition = new DummyActivity();
		DummyActivityConfiguration conditionConf = new DummyActivityConfiguration();
		conditionConf.setOutputPortName("loop");
		conditionConf.setOutputValue("true");
		condition.configure(conditionConf);

		configuration.setCondition(condition);

		assertTrue("runFirst not true", configuration.isRunFirst());
		assertEquals(0, eventKeeper.events.size());
		processorInputPort.receiveEvent(helloToken);
		Thread.sleep(SLEEP);
		assertEquals("Should not have returned yet", 0, eventKeeper.events
				.size());
		assertTrue("Activity should have been invoked more than 5 times",
				activity.getInvocationCount() > 5);
		assertTrue("Condition should have been invoked more than 5 times",
				condition.getInvocationCount() > 5);

		// Stop the loop
		conditionConf.setOutputValue("false");
		Thread.sleep(SLEEP);
		// Should now have returned
		assertEquals(1, eventKeeper.events.size());
		String output = (String) tokenTostring(eventKeeper.events.get(0));
		assertEquals(HELLO_THERE, output);
		assertEquals("Invocation counts mismatch", activity
				.getInvocationCount(), condition.getInvocationCount());
		assertEquals("Should not be any incoming jobs cached", 0,
				loopLayer.incomingJobs.size());
		assertEquals("Should not be any outgoing jobs cached", 0,
				loopLayer.outgoingJobs.size());
	}

	public void makeLayer() {
		loopLayer = new Loop();
		configuration = new LoopConfiguration();
		loopLayer.configure(configuration);
	}

	@Before
	public void makeProcessor() throws EditException,
			ActivityConfigurationException {
		createActivity();
		createProcessor();
		connectInputPort();
		connectOutputPort();
		createLink();
	}

	@Test
	public void nullConditional() throws Exception {
		assertEquals("Activity should not have been invoked yet", 0, activity
				.getInvocationCount());
		configuration = new LoopConfiguration();
		configuration.setRunFirst(false);
		loopLayer.configure(configuration);
		processorInputPort.receiveEvent(helloToken);
		assertEquals(1, eventKeeper.events.size());
		T2Reference data = eventKeeper.events.get(0).getData();
		assertTrue("Should fail", data.containsErrors());
		assertEquals(T2ReferenceType.ErrorDocument, data.getReferenceType());
		ErrorDocument errorDoc = (ErrorDocument) rs.resolveIdentifier(data,
				null, context);
		System.out.println(errorDoc.getMessage());

		assertTrue("Error document didn't contain '" + NULL_MSG + "'", errorDoc
				.getMessage().contains(NULL_MSG));
		assertEquals("Activity should not have been invoked", 0, activity
				.getInvocationCount());
	}

	@Test
	public void nullConditionalRunFirst() throws Exception {
		assertEquals("Activity should not have been invoked yet", 0, activity
				.getInvocationCount());
		assertEquals("Should not be any incoming jobs cached", 0,
				loopLayer.incomingJobs.size());
		assertEquals("Should not be any outgoing jobs cached", 0,
				loopLayer.outgoingJobs.size());

		assertTrue("run first not true by default", configuration.isRunFirst());
		assertEquals(0, eventKeeper.events.size());
		processorInputPort.receiveEvent(helloToken);
		Thread.sleep(SLEEP);
		assertEquals(1, eventKeeper.events.size());
		String output = (String) tokenTostring(eventKeeper.events.get(0));
		assertEquals(HELLO_THERE, output);
		assertEquals("Invoked the activity more than once", 1, activity
				.getInvocationCount());

		assertEquals("Should not be any incoming jobs cached", 0,
				loopLayer.incomingJobs.size());
		assertEquals("Should not be any outgoing jobs cached", 0,
				loopLayer.outgoingJobs.size());
	}

	@Test
	public void nullConditionalRunFirstIteration() throws Exception {
		assertEquals("Activity should not have been invoked yet", 0, activity
				.getInvocationCount());
		assertEquals("Should not be any incoming jobs cached", 0,
				loopLayer.incomingJobs.size());
		assertEquals("Should not be any outgoing jobs cached", 0,
				loopLayer.outgoingJobs.size());

		assertTrue("run first not true by default", configuration.isRunFirst());
		assertEquals(0, eventKeeper.events.size());

		processorInputPort.receiveEvent(listItem0Token);
		processorInputPort.receiveEvent(listItem1Token);
		Thread.sleep(SLEEP);
		assertEquals(2, eventKeeper.events.size());

		processorInputPort.receiveEvent(listToken);
		Thread.sleep(SLEEP);

		assertEquals(3, eventKeeper.events.size());
		List<String> outputs = (List<String>) tokenTostring(eventKeeper.events
				.get(2));
		assertEquals(2, outputs.size());
		assertEquals(HELLO_THERE, outputs.get(0));
		assertEquals(HELLO_THERE, outputs.get(1));
		assertEquals("Invoked the activity more than two times", 2, activity
				.getInvocationCount());
		assertEquals("Should not be any incoming jobs cached", 0,
				loopLayer.incomingJobs.size());
		assertEquals("Should not be any outgoing jobs cached", 0,
				loopLayer.outgoingJobs.size());
	}

	@Before
	public void registerData() {
		rs = context.getReferenceService();
		helloToken = new WorkflowDataToken(PROCESS, new int[0], rs.register(
				HELLO, 0, true, context), context);

		listItem0Token = new WorkflowDataToken(OTHER_PROCESS, new int[] { 0 },
				rs.register(HELLO, 0, true, context), context);
		listItem1Token = new WorkflowDataToken(OTHER_PROCESS, new int[] { 1 },
				rs.register(HELLO_THERE, 0, true, context), context);

		listToken = new WorkflowDataToken(OTHER_PROCESS, new int[0], rs
				.register(Arrays.asList(listItem0Token.getData(),
						listItem1Token.getData()), 1, false, context), context);

	}

	protected void connectInputPort() throws EditException {
		ActivityInputPort activityInput = activity.getInputPorts().iterator()
				.next();
		ProcessorInputPort processorInputPort = edits.createProcessorInputPort(
				processor, activityInput.getName(), activityInput.getDepth());
		edits.getAddProcessorInputPortEdit(processor, processorInputPort)
				.doEdit();
		edits.getAddActivityInputPortMappingEdit(activity,
				activityInput.getName(), activityInput.getName()).doEdit();
	}

	protected void connectOutputPort() throws EditException {
		OutputPort activityOutput = activity.getOutputPorts().iterator().next();
		String portName = activityOutput.getName();
		ProcessorOutputPort processorOutputPort = edits
				.createProcessorOutputPort(processor, portName, activityOutput
						.getDepth(), activityOutput.getGranularDepth());
		edits.getAddProcessorOutputPortEdit(processor, processorOutputPort)
				.doEdit();
		edits.getAddActivityOutputPortMappingEdit(activity, portName, portName)
				.doEdit();
	}

	protected void createActivity() throws ActivityConfigurationException {
		activity = new DummyActivity();
		DummyActivityConfiguration config = new DummyActivityConfiguration();
		config.setOutputValue(HELLO_THERE);
		activity.configure(config);
	}

	protected void createLink() throws EditException {
		processorInputPort = processor.getInputPorts().get(0);
		ProcessorOutputPort processorOutputPort = processor.getOutputPorts()
				.get(0);
		Datalink link = edits.createDatalink(processorOutputPort, eventKeeper);
		edits.getConnectDatalinkEdit(link).doEdit();
	}

	protected void createProcessor() throws EditException {
		makeLayer();
		processor = edits.createProcessor("proc");
		edits.getDefaultDispatchStackEdit(processor).doEdit();
		DispatchStack dispatchStack = processor.getDispatchStack();
		// TODO: Make a real Edit for inserting layer
		List<DispatchLayer<?>> layers = dispatchStack.getLayers();
		int loopLayerPosition = 0;
		for (int layerPosition = 0; layerPosition < layers.size(); layerPosition++) {
			if (layers.get(layerPosition) instanceof ErrorBounce) {
				loopLayerPosition = layerPosition + 1;
			}
		}

		edits.getAddDispatchLayerEdit(dispatchStack, loopLayer,
				loopLayerPosition).doEdit();
		edits.getAddActivityEdit(processor, activity).doEdit();
	}

	protected Object tokenTostring(WorkflowDataToken workflowDataToken) {
		return rs.renderIdentifier(workflowDataToken.getData(), String.class,
				context);
	}

	public class DummyActivity extends
			AbstractAsynchronousActivity<DummyActivityConfiguration> implements
			AsynchronousActivity<DummyActivityConfiguration> {

		public List<Map<String, T2Reference>> calls = new ArrayList<Map<String, T2Reference>>();
		private DummyActivityConfiguration config;

		public HealthReport checkActivityHealth() {
			return new HealthReport("AsynchEchoActivity",
					"Everything is hunky dorey", Status.OK);
		}

		@Override
		public void configure(DummyActivityConfiguration conf)
				throws ActivityConfigurationException {
			this.config = conf;
			addInput(PORT_A, 0, true,
					new ArrayList<Class<? extends ExternalReferenceSPI>>(),
					String.class);
			addOutput(config.getOutputPortName(), config.getOutputPortDepth(),
					config.getOutputPortDepth());
		}

		@Override
		public void executeAsynch(final Map<String, T2Reference> data,
				final AsynchronousActivityCallback callback) {
			callback.requestRun(new Runnable() {
				public void run() {
					execute(data, callback);
				}
			});
		}

		@Override
		public DummyActivityConfiguration getConfiguration() {
			return config;
		}

		public int getInvocationCount() {
			return calls.size();
		}

		protected Map<String, T2Reference> createOutputs(
				final AsynchronousActivityCallback callback) {
			Map<String, T2Reference> outputMap = new HashMap<String, T2Reference>();
			T2Reference valueId = callback.getContext().getReferenceService()
					.register(config.getOutputValue(), 0, true,
							callback.getContext());

			outputMap.put(config.getOutputPortName(), valueId);
			return outputMap;
		}

		protected void execute(final Map<String, T2Reference> data,
				final AsynchronousActivityCallback callback) {
			calls.add(data);
			Map<String, T2Reference> outputMap = createOutputs(callback);
			callback.receiveResult(outputMap, new int[0]);
		}

	}

	public class DummyActivityConfiguration {

		private int outputPortDepth = 0;
		private String outputPortName = PORT_B;
		private String outputValue = "hello";

		public int getOutputPortDepth() {
			return outputPortDepth;
		}

		public String getOutputPortName() {
			return outputPortName;
		}

		public String getOutputValue() {
			return outputValue;
		}

		public void setOutputPortDepth(int outputPortDepth) {
			this.outputPortDepth = outputPortDepth;
		}

		public void setOutputPortName(String outputPortName) {
			this.outputPortName = outputPortName;
		}

		public void setOutputValue(String outputValue) {
			this.outputValue = outputValue;
		}
	}

}
