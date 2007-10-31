package net.sf.taverna.t2.workflowmodel.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorInputPortImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class ProcessorLevelInvocationTest {

	// Create a diagnostic event receiver
	DiagnosticEventHandler deh;
	ProcessorImpl processor;

	DataManager dManager = new InMemoryDataManager("dataNS",
			new HashSet<LocationalContext>());

	@Before
	public void createProcessor() throws EditException, JDOMException,
			IOException, ActivityConfigurationException,
			MalformedIdentifierException {

		// Create a processor from the simple echo activity
		AsynchEchoActivity activity = new AsynchEchoActivity();
		activity.configure(new EchoConfig("blah"));
		processor = Tools.buildFromActivity(activity);
		deh = new DiagnosticEventHandler();

		// Set up data manager
		ContextManager.baseManager = this.dManager;

		// Get an edit factory
		Edits edits = new EditsImpl();

		// Get the connect edit and apply to link the output port of the echo
		// process to a diagnostic input port
		edits.getConnectProcessorOutputEdit(processor,
				processor.getOutputPorts().get(0).getName(), deh).doEdit();

	}

	@Test
	public synchronized void singleDataTokenBehaviour()
			throws MalformedIdentifierException, EditException, JDOMException,
			IOException, ActivityConfigurationException {
		//System.out.println("Single token, 'A string'");
		// Build an input data token
		WorkflowDataToken token = new WorkflowDataToken("outerProcess1",
				new int[0], Literal.buildLiteral("A string"));
		processor.getInputPorts().get(0).receiveEvent(token);
		assertEquals(1, deh.getEventCount());
	}

	@Test
	public void singleEmptyListDataTokenBehaviour()
			throws MalformedIdentifierException, EditException, JDOMException,
			IOException, ActivityConfigurationException, InterruptedException {
		//System.out.println("Empty list");
		EntityIdentifier listIdentifier = ContextManager.baseManager
				.registerEmptyList(1);
		// Build an input data token of a single list
		WorkflowDataToken token = new WorkflowDataToken("outerProcess2",
				new int[0], listIdentifier);

		// This is not a public API! We're using it here because we need some
		// way to push the configuration data into the processor that would
		// normally be created during the typecheck operation. In this case
		// we're declaring that we've given a list to the processor where it
		// wanted a single item and that it therefore has a wrapping level of 1
		// after the iteration strategy has been applied.
		processor.resultWrappingDepth = 1;

		processor.getInputPorts().get(0).receiveEvent(token);
		// This shouldn't do anything as we've got the filter by default set to
		// 0 so it ignores this list which has depth 1. It will now, however,
		// correctly iterate over what it thinks is an empty set and return a
		// single empty result on the output
		assertEquals(1, deh.getEventCount());
	}

	@Test
	public void singleListDataTokenWithFilterConfigured()
			throws EditException, JDOMException, IOException,
			ActivityConfigurationException, MalformedIdentifierException, InterruptedException {
		//System.out.println("List with two items, 'foo' and 'bar'");
		EntityIdentifier listIdentifier = ContextManager.baseManager
				.registerList(new EntityIdentifier[] {
						Literal.buildLiteral("foo"),
						Literal.buildLiteral("bar") });

		WorkflowDataToken token = new WorkflowDataToken("outerProcess3",
				new int[0], listIdentifier);
		// Reconfigure processor to set the filter to level 1, i.e. consume
		// lists and iterate over them using the data manager's traversal
		// functionality
		((ProcessorInputPortImpl) (processor.getInputPorts().get(0)))
				.setFilterDepth(1);
		processor.getInputPorts().get(0).receiveEvent(token);

		// Should produce two outputs followed by a collection
		assertEquals(3, deh.getEventCount());
	}

	@Test
	public void topLevelEmptyCollection() throws EditException,
			JDOMException, IOException, ActivityConfigurationException,
			MalformedIdentifierException, InterruptedException {
		//System.out.println("Top level empty list (depth 2)");
		EntityIdentifier listIdentifier = ContextManager.baseManager
				.registerEmptyList(2);
		((ProcessorInputPortImpl) (processor.getInputPorts().get(0)))
				.setFilterDepth(0);
		WorkflowDataToken token = new WorkflowDataToken("outerProcess4",
				new int[0], listIdentifier);

		// This is not a public API! We're using it here because we need some
		// way to push the configuration data into the processor that would
		// normally be created during the typecheck operation. In this case
		// we're declaring that we've given a list of lists to the processor
		// where it wanted a single item and that it therefore has a wrapping
		// level of 2 after the iteration strategy has been applied.
		processor.resultWrappingDepth = 2;

		processor.getInputPorts().get(0).receiveEvent(token);
		assertEquals(1, deh.getEventCount());
	}

	@Test
	public void testPartiallyEmptyCollection() throws EditException,
			JDOMException, IOException, ActivityConfigurationException,
			MalformedIdentifierException, InterruptedException {
		EntityIdentifier emptyListIdentifier = ContextManager.baseManager
				.registerEmptyList(1);
		//System.out.println("Partially empty collection : {{}{foo, bar}}");
		EntityIdentifier populatedListIdentifier = ContextManager.baseManager
				.registerList(new EntityIdentifier[] {
						Literal.buildLiteral("foo"),
						Literal.buildLiteral("bar") });
		EntityIdentifier listIdentifier = ContextManager.baseManager
				.registerList(new EntityIdentifier[] { emptyListIdentifier,
						populatedListIdentifier });
		((ProcessorInputPortImpl) (processor.getInputPorts().get(0)))
				.setFilterDepth(2);
		WorkflowDataToken token = new WorkflowDataToken("outerProcess5",
				new int[0], listIdentifier);
		
		// This is not a public API! We're using it here because we need some
		// way to push the configuration data into the processor that would
		// normally be created during the typecheck operation. In this case
		// we're declaring that we've given a list of lists to the processor
		// where it wanted a single item and that it therefore has a wrapping
		// level of 2 after the iteration strategy has been applied.
		processor.resultWrappingDepth = 2;
		
		processor.getInputPorts().get(0).receiveEvent(token);
		Thread.sleep(1);
		assertEquals(5, deh.getEventCount());
	}

}
