package net.sf.taverna.t2.workflowmodel.processor.iteration.impl;

import junit.framework.TestCase;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.invocation.impl.TestInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DiagnosticIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import static net.sf.taverna.t2.workflowmodel.processor.iteration.impl.CrossProductTest.nextID;

public class NamedInputPortNodeTest extends TestCase {

	InvocationContext context = new TestInvocationContext();
	
	public void testBasic() {
		NamedInputPortNode nipn = new NamedInputPortNode("Input", 0);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		nipn.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn);
		try {
			is.receiveData("Input", "Process1", new int[]{}, nextID(), context);
		} catch (WorkflowStructureException e) {
			fail("Should be able to find input named 'Input' in this test case");
		}
		assertTrue(disn.jobsReceived("Process1") == 1);	
	}
	
	public void testMultipleProcesses() {
		NamedInputPortNode nipn = new NamedInputPortNode("Input", 0);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		nipn.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn);
		try {
			is.receiveData("Input", "Process1", new int[]{0}, nextID(), context);
			is.receiveData("Input", "Process1", new int[]{1}, nextID(), context);
			is.receiveData("Input", "Process2", new int[]{}, nextID(), context);
		} catch (WorkflowStructureException e) {
			fail("Should be able to find input named 'Input' in this test case");
		}
		assertTrue(disn.jobsReceived("Process1") == 2);
		assertTrue(disn.jobsReceived("Process2") == 1);
	}
	
}
