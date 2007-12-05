package net.sf.taverna.t2.workflowmodel.processor.iteration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.invocation.Event;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.workflowmodel.invocation.impl.TestInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.iteration.CrossProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.DotProduct;
import net.sf.taverna.t2.workflowmodel.processor.iteration.NamedInputPortNode;

import org.junit.Ignore;
import org.junit.Test;

public class TestStagedIteration  {

	public final DataManager dManager = new InMemoryDataManager("foo.bar",
			Collections.<LocationalContext> emptySet());
	
	public InvocationContext context = new TestInvocationContext() {
		public DataManager getDataManager() {
			return dManager;
		}
	};
	
	@Test
	public void testStaging() throws MalformedIdentifierException {
		IterationStrategyStackImpl iss = new IterationStrategyStackImpl() {
			protected void receiveEventFromStrategy(Event e) {
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
				DataDocumentIdentifier ddocIdentifier = context.getDataManager()
						.registerDocument(Collections
								.<ReferenceScheme> emptySet());
				idsInList.add(ddocIdentifier);
			}
			EntityListIdentifier dataReference = context.getDataManager().registerList(idsInList.toArray(new EntityIdentifier[0]));
			is1.receiveData("a", owningProcess, new int[] { i }, dataReference, context);
		}
		is1.receiveCompletion("a", owningProcess, new int[] {}, context);

		for (int i = 0; i < 4; i++) {
			List<EntityIdentifier> idsInList = new ArrayList<EntityIdentifier>();
			for (int j = 0; j < 2; j++) {
				DataDocumentIdentifier ddocIdentifier = context.getDataManager()
						.registerDocument(Collections
								.<ReferenceScheme> emptySet());
				idsInList.add(ddocIdentifier);
			}
			EntityListIdentifier dataReference = context.getDataManager().registerList(idsInList.toArray(new EntityIdentifier[0]));

			is1.receiveData("b", owningProcess, new int[] { i }, dataReference, context);
		}
		is1.receiveCompletion("b", owningProcess, new int[] {}, context);

	}

}
