package net.sf.taverna.service.rest.client;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;



public class TestTavernaService {
	private static Logger logger = Logger.getLogger(TestTavernaService.class);
	private RESTContext context;
	
	@Before
	public void findUser() throws NotSuccessException {
		context = RESTContext.register("http://localhost:8976/v1/");
	}
	
	@Test
	public void findWorkflows() {
		WorkflowsREST workflows = context.getUser().getWorkflows();
		System.out.println("Received " + workflows);
		for (WorkflowREST wf : workflows) {
			System.out.println(wf);
			System.out.println(wf.getOwner());
			System.out.println("s:: " + wf.getScufl());
			assertEquals(context.getUser(), wf.getOwner());
		}
	}
	
	
	
}
