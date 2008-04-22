package net.sf.taverna.t2.workflowmodel.processor;

import java.io.UnsupportedEncodingException;

import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.invocation.impl.TestInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import junit.framework.TestCase;

/**
 * Test the Condition facility, verify that it does indeed force processors to
 * execute in series when no data dependency exists.
 * 
 * @author Tom Oinn
 * 
 */
public class ConditionTest extends TestCase {

	private DiagnosticEventHandler deh1, deh2;

	private Processor p1, p2;

	private Edits edits = new EditsImpl();
	
	private InvocationContext context = new TestInvocationContext();

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

	public void testCreation() throws ActivityConfigurationException,
			EditException {
		create();
		assertTrue(p1.getControlledPreconditionList().size() == 1);
		assertTrue(p2.getPreconditionList().size() == 1);
	}

	public void testLock() throws UnsupportedEncodingException,
			MalformedIdentifierException, ActivityConfigurationException,
			EditException {
		create();
		System.out.println("Lock (should produce no output) :");
		WorkflowDataToken token = new WorkflowDataToken("outerProcess1",
				new int[0], Literal.buildLiteral("A string"), context);
		p2.getInputPorts().get(0).receiveEvent(token);
		// p1.getInputPorts().get(0).receiveEvent(token);
		assertTrue(deh2.getEventCount() == 0);
	}

	public void testLockUnlock() throws UnsupportedEncodingException,
			MalformedIdentifierException, ActivityConfigurationException,
			EditException, InterruptedException {
		testLock();
		System.out.println("Unlock (should produce both tokens) :");
		Thread.sleep(200);
		WorkflowDataToken token2 = new WorkflowDataToken("outerProcess1",
				new int[0], Literal.buildLiteral("Another string"),context);
		p1.getInputPorts().get(0).receiveEvent(token2);
		assertTrue(deh2.getEventCount() == 1);
	}
	
	public void testLockUnlockWithDifferentProcess()
			throws UnsupportedEncodingException, MalformedIdentifierException,
			ActivityConfigurationException, EditException, InterruptedException {
		testLock();
		System.out.println("Unlock with diffent process, only output from p1 :");
		Thread.sleep(200);
		WorkflowDataToken token2 = new WorkflowDataToken("outerProcess2",
				new int[0], Literal.buildLiteral("Another string"),context);
		p1.getInputPorts().get(0).receiveEvent(token2);
		assertTrue(deh2.getEventCount() == 0);
		assertTrue(deh1.getEventCount() == 1);
	}


}
