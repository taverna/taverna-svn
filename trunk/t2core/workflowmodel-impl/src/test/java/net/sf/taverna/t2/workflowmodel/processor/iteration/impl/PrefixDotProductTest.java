package net.sf.taverna.t2.workflowmodel.processor.iteration.impl;

import junit.framework.TestCase;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.invocation.impl.TestInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DiagnosticIterationStrategyNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.PrefixDotProduct;

public class PrefixDotProductTest extends TestCase {

	InvocationContext context = new TestInvocationContext();

	/**
	 * Test that the prefix node copes when we feed it two inputs with different
	 * cardinalities. Output should be four jobs with index arrays length 2
	 * 
	 * @throws MalformedIdentifierException
	 * 
	 */
	public void testMutipleData() {

		NamedInputPortNode nipn1 = new NamedInputPortNode("a", 0);
		NamedInputPortNode nipn2 = new NamedInputPortNode("b", 0);

		PrefixDotProduct pdp = new PrefixDotProduct();
		nipn1.setParent(pdp);
		nipn2.setParent(pdp);
		DiagnosticIterationStrategyNode disn = new DiagnosticIterationStrategyNode();
		pdp.setParent(disn);
		IterationStrategyImpl is = new IterationStrategyImpl();
		is.addInput(nipn1);
		is.addInput(nipn2);

		String owningProcess = "Process1";
		for (int i = 0; i < 2; i++) {
			is.receiveData("a", owningProcess, new int[] { i },
					TestInvocationContext.nextListReference(1), context);
		}
		is.receiveCompletion("a", owningProcess, new int[] {}, context);

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				is.receiveData("b", owningProcess, new int[] { i, j },
						TestInvocationContext.nextListReference(1), context);
			}
			is.receiveCompletion("b", owningProcess, new int[] { i }, context);
		}
		is.receiveCompletion("b", owningProcess, new int[] {}, context);
		assertTrue(disn.jobsReceived("Process1") == 4);
		System.out.println(disn);
	}

}
