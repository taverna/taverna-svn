package net.sf.taverna.service.rest.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TestWorkflows extends ContextTest {
	private static Logger logger = Logger.getLogger(TestWorkflows.class);

	static WorkflowREST wf;
	
	@Test
	public void uploadWorkflow() throws NotSuccessException {
		WorkflowsREST workflows = context.getUser().getWorkflows();
		wf = context.getUser().getWorkflows().add(workflow);
		assertTrue(wf.getURI().contains("workflows/"));
		// FIXME: Can't check equality comparison with workflow because the XML
		// has different indentions and name spaces..
		assertTrue(wf.getScufl().contains("ShapeAnimals"));
		workflows = context.getUser().getWorkflows();
		WorkflowREST wf2 = workflows.getWorkflows().get(0);
		assertEquals(wf, wf2);
	}

	@Test
	public void findWorkflows() throws NotSuccessException {
		if (wf == null) {
			uploadWorkflow();
		}
		WorkflowsREST workflows = context.getUser().getWorkflows();
		for (WorkflowREST wf : workflows) {
			assertEquals(context.getUser(), wf.getOwner());
		}
	}
	
}
