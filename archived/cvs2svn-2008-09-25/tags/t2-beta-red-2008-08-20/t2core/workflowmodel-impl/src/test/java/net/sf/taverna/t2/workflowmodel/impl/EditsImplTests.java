package net.sf.taverna.t2.workflowmodel.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.impl.WorkflowInstanceFacadeImpl;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.invocation.impl.DummyInvocationContext;
import net.sf.taverna.t2.workflowmodel.processor.AsynchEchoActivity;
import net.sf.taverna.t2.workflowmodel.processor.EchoConfig;

import org.junit.BeforeClass;
import org.junit.Test;

public class EditsImplTests {
	public class InvalidDummyDataflow extends DummyDataflow {
		@Override
		public DataflowValidationReport checkValidity() {
			return new DummyValidationReport(false);
		}
	}

	private static Edits edits;
	
	@BeforeClass
	public static void createEditsInstance() {
		edits=new EditsImpl();
	}
	
	@Test
	public void createWorkflowInstanceFacade() throws InvalidDataflowException {
		WorkflowInstanceFacade facade = edits.createWorkflowInstanceFacade(new DummyDataflow(), new DummyInvocationContext(), "");
		assertTrue("Should be a WorkflowInstanceFacadeImpl",facade instanceof WorkflowInstanceFacadeImpl);
	}
	
	@Test
	public void createWorkflowInstanceFacadeFails() throws InvalidDataflowException {
		InvalidDummyDataflow invalidDummyDataflow = new InvalidDummyDataflow();
		try {
			edits.createWorkflowInstanceFacade(invalidDummyDataflow, new DummyInvocationContext(), "");
			fail("Did not throw InvalidDataflowException");
		} catch (InvalidDataflowException ex) {
			assertSame(invalidDummyDataflow, ex.getDataflow());
			assertTrue(ex.getDataflowValidationReport() instanceof DummyValidationReport);
		}
	}
	
	
	@Test
	public void createDataflow() {
		Dataflow df = edits.createDataflow();
		assertNotNull(df.getInternalIdentier());
	}
	
	@Test
	public void testGetConfigureActivityEdit() {
		Edit<?> edit = edits.getConfigureActivityEdit(new AsynchEchoActivity(), new EchoConfig());
		assertTrue(edit instanceof ConfigureActivityEdit);
	}
}
