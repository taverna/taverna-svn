package net.sf.taverna.t2.workflowmodel.processor;

import java.io.IOException;
import java.util.HashSet;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.cloudone.Literal;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.impl.InMemoryDataManager;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.ContextManager;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorInputPortImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.service.ServiceConfigurationException;

import org.jdom.JDOMException;

import junit.framework.TestCase;

public class ProcessorLevelInvocationTest extends TestCase {

	// Create a diagnostic event receiver
	DiagnosticEventHandler deh = new DiagnosticEventHandler();

	ProcessorImpl processor;

	DataManager dManager = new InMemoryDataManager("dataNS",
			new HashSet<LocationalContext>());

	public void createProcessor() throws EditException, JDOMException,
			IOException, ServiceConfigurationException,
			MalformedIdentifierException {

		// Create a processor from the simple echo service
		AsynchEchoService service = new AsynchEchoService();
		service.configure(new EchoConfig("blah"));
		processor = Tools.buildFromService(service);

		// Set up data manager
		ContextManager.baseManager = this.dManager;

		// Get an edit factory
		Edits edits = new EditsImpl();

		// Get the connect edit and apply to link the output port of the echo
		// process to a diagnostic input port
		edits.getConnectProcessorOutputEdit(processor,
				processor.getOutputPorts().get(0).getName(), deh).doEdit();

	}

	public void testSingleDataTokenBehaviour()
			throws MalformedIdentifierException, EditException, JDOMException,
			IOException, ServiceConfigurationException {
		System.out.println("Single token, 'A string'");
		createProcessor();
		// Build an input data token
		WorkflowDataToken token = new WorkflowDataToken("outerProcess1",
				new int[0], Literal.buildLiteral("A string"));
		processor.getInputPorts().get(0).receiveEvent(token);
		assertTrue(deh.getEventCount() == 1);
	}

	public void testSingleEmptyListDataTokenBehaviour()
			throws MalformedIdentifierException, EditException, JDOMException,
			IOException, ServiceConfigurationException {
		createProcessor();
		System.out.println("Empty list");
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
		assertTrue(deh.getEventCount() == 1);
	}

	public void testSingleListDataTokenWithFilterConfigured()
			throws EditException, JDOMException, IOException,
			ServiceConfigurationException, MalformedIdentifierException {
		createProcessor();
		System.out.println("List with two items, 'foo' and 'bar'");
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
		assertTrue(deh.getEventCount() == 3);
	}

	public void testTopLevelEmptyCollection() throws EditException,
			JDOMException, IOException, ServiceConfigurationException,
			MalformedIdentifierException {
		createProcessor();
		System.out.println("Top level empty list (depth 2)");
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
		assertTrue(deh.getEventCount() == 1);
	}

	public void testPartiallyEmptyCollection() throws EditException,
			JDOMException, IOException, ServiceConfigurationException,
			MalformedIdentifierException {
		createProcessor();
		EntityIdentifier emptyListIdentifier = ContextManager.baseManager
				.registerEmptyList(1);
		System.out.println("Partially empty collection : {{}{foo, bar}}");
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
		assertTrue(deh.getEventCount() == 5);
	}

}
