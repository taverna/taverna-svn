package net.sf.taverna.t2.workflowmodel.processor.iteration.impl;

import net.sf.taverna.t2.cloudone.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.MalformedIdentifierException;
import net.sf.taverna.t2.workflowmodel.WorkflowStructureException;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DiagnosticIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import junit.framework.TestCase;

public class CrossProductTest extends TestCase {

	private static int counter = 0;
	public static DataDocumentIdentifier nextID() {
		try {
			return new DataDocumentIdentifier("urn:t2data:ddoc://foo.bar/testid"+(counter++));
		}
		catch (MalformedIdentifierException mie) {
			return null;
		}
	}
	
	public void testBasic() {
		NamedInputPortNode nipn1 = new NamedInputPortNode("Input1", 0);
		NamedInputPortNode nipn2 = new NamedInputPortNode("Input2", 0);
		
		CrossProduct cp = new CrossProduct();
		nipn1.setParent(cp);
		nipn2.setParent(cp);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		cp.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn1);
		is.addInput(nipn2);
		try {
			is.receiveData("Input1", "Process1", new int[] {0}, nextID());
			assertTrue(disn.jobsReceived("Process1") == 0);
			is.receiveData("Input1", "Process1", new int[] {1}, nextID());
			assertTrue(disn.jobsReceived("Process1") == 0);
			is.receiveData("Input1", "Process1", new int[] {2}, nextID());
			assertTrue(disn.jobsReceived("Process1") == 0);
			is.receiveCompletion("Input1", "Process1", new int[]{});
			is.receiveData("Input2", "Process1", new int[] {0}, nextID());
			System.out.println(disn);
			assertTrue(disn.jobsReceived("Process1") == 3);
			assertTrue(disn.containsJob("Process1", new int[]{0,0}));
			assertTrue(disn.containsJob("Process1", new int[]{1,0}));
			assertTrue(disn.containsJob("Process1", new int[]{2,0}));
			is.receiveData("Input2", "Process1", new int[] {1}, nextID());
			is.receiveCompletion("Input2", "Process1", new int[]{});
			System.out.println(disn);
			assertTrue(disn.jobsReceived("Process1") == 6);
		} catch (WorkflowStructureException e) {
			fail("Unknown structure exception");
		}
	}
	
}
