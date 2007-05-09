package net.sf.taverna.service.rest.client;

import static org.junit.Assert.assertEquals;

import net.sf.taverna.service.rest.ClientTest;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;



public class TestTavernaService extends ClientTest {
	private static Logger logger = Logger.getLogger(TestTavernaService.class);
	
	private RESTContext context;
	
	@Before
	public void findUser() throws NotSuccessException {
		context = RESTContext.register(BASE_URL);
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
