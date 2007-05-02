package net.sf.taverna.t2.workflowmodel.processor.iteration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.cloudone.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.EntityIdentifier;
import net.sf.taverna.t2.cloudone.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.impl.InMemoryDataManager;
import net.sf.taverna.t2.invocation.ContextManager;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyImpl;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;
import junit.framework.TestCase;

public class TestStagedIteration extends TestCase {

	public void testStaging() throws MalformedIdentifierException {
		IterationStrategyStackImpl iss = new IterationStrategyStackImpl() {
			protected void receiveEventFromStrategy(Event e) {
				System.out.println(e);
			}
		};

		// Configure a dummy data manager to handle the traversal of collections
		ContextManager.baseManager = new InMemoryDataManager("foo.bar",
				Collections.<LocationalContext> emptySet());

		IterationStrategyImpl is1 = new IterationStrategyImpl();
		NamedInputPortNode nipn1 = new NamedInputPortNode("a", 1);
		NamedInputPortNode nipn2 = new NamedInputPortNode("b", 1);
		is1.addInput(nipn1);
		is1.addInput(nipn2);
		DotProduct dp = new DotProduct();
		nipn1.setParent(dp);
		nipn2.setParent(dp);
		dp.setParent(is1.getTerminal());

		IterationStrategyImpl is2 = new IterationStrategyImpl();
		NamedInputPortNode nipn3 = new NamedInputPortNode("a", 0);
		NamedInputPortNode nipn4 = new NamedInputPortNode("b", 0);
		is2.addInput(nipn3);
		is2.addInput(nipn4);
		CrossProduct cp = new CrossProduct();
		nipn3.setParent(cp);
		nipn4.setParent(cp);
		cp.setParent(is2.getTerminal());

		iss.addStrategy(is1);
		iss.addStrategy(is2);

		// Directly inject events into is1 to test
		String owningProcess = "parent";
		for (int i = 0; i < 4; i++) {
			List<EntityIdentifier> idsInList = new ArrayList<EntityIdentifier>();
			for (int j = 0; j < 2; j++) {
				DataDocumentIdentifier ddocIdentifier = ContextManager.baseManager
						.registerDocument(Collections
								.<ReferenceScheme> emptySet()).getIdentifier();
				idsInList.add(ddocIdentifier);
			}
			EntityListIdentifier dataReference = ContextManager.baseManager.registerList(idsInList.toArray(new EntityIdentifier[0]));
			is1.receiveData("a", owningProcess, new int[] { i }, dataReference);
		}
		is1.receiveCompletion("a", owningProcess, new int[] {});

		for (int i = 0; i < 4; i++) {
			List<EntityIdentifier> idsInList = new ArrayList<EntityIdentifier>();
			for (int j = 0; j < 2; j++) {
				DataDocumentIdentifier ddocIdentifier = ContextManager.baseManager
						.registerDocument(Collections
								.<ReferenceScheme> emptySet()).getIdentifier();
				idsInList.add(ddocIdentifier);
			}
			EntityListIdentifier dataReference = ContextManager.baseManager.registerList(idsInList.toArray(new EntityIdentifier[0]));

			is1.receiveData("b", owningProcess, new int[] { i }, dataReference);
		}
		is1.receiveCompletion("b", owningProcess, new int[] {});

	}

}
