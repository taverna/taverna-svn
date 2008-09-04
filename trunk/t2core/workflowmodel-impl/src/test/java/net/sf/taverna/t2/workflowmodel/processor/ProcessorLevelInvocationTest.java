/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.t2.workflowmodel.processor;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorInputPortImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;

public class ProcessorLevelInvocationTest {

	/**
	 * sleep for given milliseconds before checking deh.getEventCount().
	 * 
	 */
	private static final int COMPLETION_SLEEP = 100;
	// Create a diagnostic event receiver
	DiagnosticEventHandler deh;
	ProcessorImpl processor;

	InvocationContext context = new DummyInvocationContext();

	@Before
	public void createProcessor() throws EditException, JDOMException,
			IOException, ActivityConfigurationException {

		// Create a processor from the simple echo activity
		AsynchEchoActivity activity = new AsynchEchoActivity();
		activity.configure(new EchoConfig("blah"));
		processor = Tools.buildFromActivity(activity);
		deh = new DiagnosticEventHandler();

		// Get an edit factory
		Edits edits = new EditsImpl();

		// Get the connect edit and apply to link the output port of the echo
		// process to a diagnostic input port
		edits.getConnectProcessorOutputEdit(processor,
				processor.getOutputPorts().get(0).getName(), deh).doEdit();

	}

	@Test
	public synchronized void singleDataTokenBehaviour() throws EditException,
			JDOMException, IOException, ActivityConfigurationException,
			InterruptedException {
		// System.out.println("Single token, 'A string'");
		// Build an input data token
		ReferenceService rs = context.getReferenceService();
		WorkflowDataToken token = new WorkflowDataToken("outerProcess1",
				new int[0], rs.register("A string", 0, true, context), context);
		processor.getInputPorts().get(0).receiveEvent(token);
		Thread.sleep(250);
		assertEquals(1, deh.getEventCount());
	}

	@Test
	public void singleEmptyListDataTokenBehaviour() throws EditException,
			JDOMException, IOException, ActivityConfigurationException,
			InterruptedException {
		// System.out.println("Empty list");
		ReferenceService rs = context.getReferenceService();
		T2Reference listIdentifier = rs.getListService().registerEmptyList(1)
				.getId();
		// Build an input data token of a single list
		WorkflowDataToken token = new WorkflowDataToken("outerProcess2",
				new int[0], listIdentifier, context);

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
		Thread.sleep(100);
		assertEquals(1, deh.getEventCount());
	}

	@Test
	public void singleListDataTokenWithFilterConfigured() throws EditException,
			JDOMException, IOException, ActivityConfigurationException,
			InterruptedException {
		// System.out.println("List with two items, 'foo' and 'bar'");

		List<String> itemList = new ArrayList<String>();
		itemList.add("foo");
		itemList.add("bar");
		T2Reference listIdentifier = context.getReferenceService().register(
				itemList, 1, true, context);

		WorkflowDataToken token = new WorkflowDataToken("outerProcess3",
				new int[0], listIdentifier, context);
		// Reconfigure processor to set the filter to level 1, i.e. consume
		// lists and iterate over them using the data manager's traversal
		// functionality
		((ProcessorInputPortImpl) (processor.getInputPorts().get(0)))
				.setFilterDepth(1);
		processor.getInputPorts().get(0).receiveEvent(token);

		// Should produce two outputs followed by a collection
		Thread.sleep(100);
		assertEquals(3, deh.getEventCount());
	}

	@Test
	public void topLevelEmptyCollection() throws EditException, JDOMException,
			IOException, ActivityConfigurationException, InterruptedException {
		// System.out.println("Top level empty list (depth 2)");
		ReferenceService rs = context.getReferenceService();
		T2Reference listIdentifier = rs.getListService().registerEmptyList(2)
				.getId();

		((ProcessorInputPortImpl) (processor.getInputPorts().get(0)))
				.setFilterDepth(0);
		WorkflowDataToken token = new WorkflowDataToken("outerProcess4",
				new int[0], listIdentifier, context);

		// This is not a public API! We're using it here because we need some
		// way to push the configuration data into the processor that would
		// normally be created during the typecheck operation. In this case
		// we're declaring that we've given a list of lists to the processor
		// where it wanted a single item and that it therefore has a wrapping
		// level of 2 after the iteration strategy has been applied.
		processor.resultWrappingDepth = 2;

		processor.getInputPorts().get(0).receiveEvent(token);
		Thread.sleep(100);
		assertEquals(1, deh.getEventCount());
	}

	@Test
	public void testPartiallyEmptyCollection() throws EditException,
			JDOMException, IOException, ActivityConfigurationException,
			InterruptedException {
		List<Object> sourceList = new ArrayList<Object>();
		sourceList.add(new ArrayList<Object>());
		List<String> itemList = new ArrayList<String>();
		itemList.add("foo");
		itemList.add("bar");
		sourceList.add(itemList);
		T2Reference listIdentifier = context.getReferenceService().register(
				sourceList, 2, true, context);

		// System.out.println("Partially empty collection : {{}{foo, bar}}");

		((ProcessorInputPortImpl) (processor.getInputPorts().get(0)))
				.setFilterDepth(2);
		WorkflowDataToken token = new WorkflowDataToken("outerProcess5",
				new int[0], listIdentifier, context);

		// This is not a public API! We're using it here because we need some
		// way to push the configuration data into the processor that would
		// normally be created during the typecheck operation. In this case
		// we're declaring that we've given a list of lists to the processor
		// where it wanted a single item and that it therefore has a wrapping
		// level of 2 after the iteration strategy has been applied.
		processor.resultWrappingDepth = 2;

		processor.getInputPorts().get(0).receiveEvent(token);
		Thread.sleep(COMPLETION_SLEEP);
		assertEquals(5, deh.getEventCount());
	}

}
