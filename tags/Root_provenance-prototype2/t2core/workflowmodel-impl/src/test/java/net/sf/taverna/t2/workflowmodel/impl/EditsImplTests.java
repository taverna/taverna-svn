package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;

import org.junit.BeforeClass;
import org.junit.Test;

public class EditsImplTests {
	private static Edits edits;
	
	@BeforeClass
	public static void createEditsInstance() {
		edits=new EditsImpl();
	}
	
	@Test
	public void createWorkflowInstanceFacade() {
		WorkflowInstanceFacade facade = edits.createWorkflowInstanceFacade(new DummyDataflow(), new DummyInvocationContext(), "");
		
		assertTrue("Should be a WorkflowInstanceFacadeImpl",facade instanceof WorkflowInstanceFacadeImpl);
	}
	
	@Test
	public void createDataflow() {
		Dataflow df = edits.createDataflow();
		assertNotNull(df.getInternalIdentier());
	}
	
	@Test
	public void testGetConfigureActivityEdit() {
		Edit<?> edit = edits.getConfigureActivityEdit(null, "");
		assertTrue(edit instanceof ConfigureActivityEdit);
	}
}
