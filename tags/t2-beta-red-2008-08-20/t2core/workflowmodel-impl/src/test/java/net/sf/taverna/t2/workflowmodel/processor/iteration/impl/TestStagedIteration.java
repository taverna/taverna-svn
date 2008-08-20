package net.sf.taverna.t2.workflowmodel.processor.iteration.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.IterationInternalEvent;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;

import org.junit.Test;

public class TestStagedIteration {

	public InvocationContext context = new DummyInvocationContext();

	@SuppressWarnings("unchecked")
	@Test
	public void testStaging() {
		IterationStrategyStackImpl iss = new IterationStrategyStackImpl() {
			@Override
			protected void receiveEventFromStrategy(
					IterationInternalEvent<? extends IterationInternalEvent<?>> e) {
				System.out.println(e);
			}
		};

		IterationStrategyImpl is1 = new IterationStrategyImpl();
		NamedInputPortNode nipn1 = new NamedInputPortNode("a", 1);
		NamedInputPortNode nipn2 = new NamedInputPortNode("b", 1);
		is1.addInput(nipn1);
		is1.addInput(nipn2);
		DotProduct dp = new DotProduct();
		nipn1.setParent(dp);
		nipn2.setParent(dp);
		dp.setParent(is1.getTerminalNode());

		IterationStrategyImpl is2 = new IterationStrategyImpl();
		NamedInputPortNode nipn3 = new NamedInputPortNode("a", 0);
		NamedInputPortNode nipn4 = new NamedInputPortNode("b", 0);
		is2.addInput(nipn3);
		is2.addInput(nipn4);
		CrossProduct cp = new CrossProduct();
		nipn3.setParent(cp);
		nipn4.setParent(cp);
		cp.setParent(is2.getTerminalNode());

		iss.addStrategy(is1);
		iss.addStrategy(is2);

		// Directly inject events into is1 to test
		String owningProcess = "parent";
		for (int i = 0; i < 4; i++) {
			List<String> items = new ArrayList<String>();
			for (int j = 0; j < 2; j++) {
				items.add("bar-" + i + "-" + j);
			}
			T2Reference listReference = context.getReferenceService().register(
					items, 1, true, context);
			is1.receiveData("a", owningProcess, new int[] { i }, listReference,
					context);
		}
		is1.receiveCompletion("a", owningProcess, new int[] {}, context);

		for (int i = 0; i < 4; i++) {

			List<String> items = new ArrayList<String>();
			for (int j = 0; j < 2; j++) {
				items.add("foo-" + i + "-" + j);
			}
			T2Reference listReference = context.getReferenceService().register(
					items, 1, true, context);
			is1.receiveData("b", owningProcess, new int[] { i }, listReference,
					context);
		}
		is1.receiveCompletion("b", owningProcess, new int[] {}, context);

	}
}
