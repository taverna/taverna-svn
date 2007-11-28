package net.sf.taverna.t2.workflowmodel.processor.iteration.impl;

import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.invocation.impl.TestInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DiagnosticIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import junit.framework.TestCase;
import static net.sf.taverna.t2.workflowmodel.processor.iteration.impl.CrossProductTest.nextID;

public class DotProductTest extends TestCase {
	
	InvocationContext context = new TestInvocationContext();
	
	public void testBasic() {
		NamedInputPortNode nipn1 = new NamedInputPortNode("Input1", 0);
		NamedInputPortNode nipn2 = new NamedInputPortNode("Input2", 0);
		
		DotProduct dp = new DotProduct();
		nipn1.setParent(dp);
		nipn2.setParent(dp);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		dp.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn1);
		is.addInput(nipn2);
		try {
			is.receiveData("Input1", "Process1", new int[] {0}, nextID(), context);
			is.receiveCompletion("Input1", "Process1", new int[]{}, context);
			assertTrue(disn.jobsReceived("Process1") == 0);
			is.receiveData("Input2", "Process1", new int[] {0}, nextID(), context);
			is.receiveCompletion("Input2", "Process1", new int[]{}, context);
			assertTrue(disn.jobsReceived("Process1") == 1);
			System.out.println(disn);
		} catch (WorkflowStructureException e) {
			fail("Unknown structure exception");
		}
	}
	
	public void testMultipleProcess() {
		NamedInputPortNode nipn1 = new NamedInputPortNode("Input1", 0);
		NamedInputPortNode nipn2 = new NamedInputPortNode("Input2", 0);
		
		DotProduct dp = new DotProduct();
		nipn1.setParent(dp);
		nipn2.setParent(dp);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		dp.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn1);
		is.addInput(nipn2);
		try {
			is.receiveData("Input1", "Process1", new int[] {}, nextID(), context);
			assertTrue(disn.jobsReceived("Process1") == 0);
			is.receiveCompletion("Input1", "Process1", new int[]{}, context);
			is.receiveData("Input2", "Process2", new int[] {}, nextID(), context);
			assertTrue(disn.jobsReceived("Process1") == 0);
			
			is.receiveData("Input2", "Process1", new int[] {}, nextID(), context);
			assertTrue(disn.jobsReceived("Process1") == 1);
			is.receiveCompletion("Input2", "Process1", new int[]{}, context);
			System.out.println(disn);
		} catch (WorkflowStructureException e) {
			fail("Unknown structure exception");
		}
	}
	
	public void testMutipleData() throws MalformedIdentifierException {
		
		NamedInputPortNode nipn1 = new NamedInputPortNode("a", 0);
		NamedInputPortNode nipn2 = new NamedInputPortNode("b", 0);
		
		DotProduct dp = new DotProduct();
		nipn1.setParent(dp);
		nipn2.setParent(dp);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		dp.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn1);
		is.addInput(nipn2);
		
		String owningProcess = "Process1";
		for (int i = 0; i < 4; i++) {
			EntityListIdentifier dataReference = new EntityListIdentifier("urn:t2data:list://foo.bar/alist"+i+"/1");
			is.receiveData("a", owningProcess, new int[]{i}, dataReference, context);
		}
		is.receiveCompletion("a", owningProcess, new int[]{}, context);
		
		for (int i = 0; i < 4; i++) {
			EntityListIdentifier dataReference = new EntityListIdentifier("urn:t2data:list://foo.bar/blist"+i+"/1");
			is.receiveData("b", owningProcess, new int[]{i}, dataReference, context);
		}
		is.receiveCompletion("b", owningProcess, new int[]{}, context);
		assertTrue(disn.jobsReceived("Process1")==4);
		System.out.println(disn);
	}
	
}
