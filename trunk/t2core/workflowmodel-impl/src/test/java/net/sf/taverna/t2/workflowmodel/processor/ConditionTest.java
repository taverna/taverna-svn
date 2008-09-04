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

import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Test the Condition facility, verify that it does indeed force processors to
 * execute in series when no data dependency exists.
 * 
 * @author Tom Oinn
 * 
 */
public class ConditionTest {

	private DiagnosticEventHandler deh1, deh2;

	private Processor p1, p2;

	private Edits edits = new EditsImpl();

	private InvocationContext context = new DummyInvocationContext();

	private Processor createProcessor() throws ActivityConfigurationException,
			EditException {
		AsynchEchoActivity activity = new AsynchEchoActivity();
		activity.configure(new EchoConfig("blah"));
		Processor processor = Tools.buildFromActivity(activity);
		return processor;
	}

	private void create() throws ActivityConfigurationException, EditException {
		p1 = createProcessor();
		edits.getRenameProcessorEdit(p1, "processor1").doEdit();
		p2 = createProcessor();
		edits.getRenameProcessorEdit(p2, "processor2").doEdit();
		edits.getCreateConditionEdit(p1, p2).doEdit();
		deh1 = new DiagnosticEventHandler() {
			@Override
			public void receiveEvent(WorkflowDataToken t) {
				eventCount++;
				System.out.println("1 : " + t.toString());
			}
		};
		edits.getConnectProcessorOutputEdit(p1, "output", deh1).doEdit();
		deh2 = new DiagnosticEventHandler() {
			@Override
			public void receiveEvent(WorkflowDataToken t) {
				eventCount++;
				System.out.println("2 : " + t.toString());
			}
		};
		edits.getConnectProcessorOutputEdit(p2, "output", deh2).doEdit();
	}

	@Test
	public void testCreation() throws ActivityConfigurationException,
			EditException {
		create();
		assertTrue(p1.getControlledPreconditionList().size() == 1);
		assertTrue(p2.getPreconditionList().size() == 1);
	}

	@Test
	public void testLock() throws UnsupportedEncodingException,
			ActivityConfigurationException, EditException {
		create();
		System.out.println("Lock (should produce no output) :");
		ReferenceService rs = context.getReferenceService();
		WorkflowDataToken token = new WorkflowDataToken("outerProcess1",
				new int[0], rs.register("A string", 0, true, context), context);
		p2.getInputPorts().get(0).receiveEvent(token);
		// p1.getInputPorts().get(0).receiveEvent(token);
		assertTrue(deh2.getEventCount() == 0);
	}

	@Test
	public void testLockUnlock() throws UnsupportedEncodingException,
			ActivityConfigurationException, EditException, InterruptedException {
		testLock();
		System.out.println("Unlock (should produce both tokens) :");
		Thread.sleep(200);
		ReferenceService rs = context.getReferenceService();
		WorkflowDataToken token2 = new WorkflowDataToken("outerProcess1",
				new int[0], rs.register("Another string", 0, true, context),
				context);
		p1.getInputPorts().get(0).receiveEvent(token2);
		assertTrue(deh2.getEventCount() == 1);
	}

	@Test
	public void testLockUnlockWithDifferentProcess()
			throws UnsupportedEncodingException,
			ActivityConfigurationException, EditException, InterruptedException {
		testLock();
		System.out
				.println("Unlock with diffent process, only output from p1 :");
		Thread.sleep(200);
		ReferenceService rs = context.getReferenceService();
		WorkflowDataToken token2 = new WorkflowDataToken("outerProcess2",
				new int[0], rs.register("Another string", 0, true, context), context);
		p1.getInputPorts().get(0).receiveEvent(token2);
		assertTrue(deh2.getEventCount() == 0);
		assertTrue(deh1.getEventCount() == 1);
	}

}
