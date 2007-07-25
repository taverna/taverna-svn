package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.rest.ClientTest;

import org.junit.Before;

public abstract class ContextTest extends ClientTest {
	
	RESTContext context;

	UserREST user;

	@Before
	public void findUser() throws NotSuccessException {
		context = RESTContext.register(BASE_URL);
		user = context.getUser();
	}

}
