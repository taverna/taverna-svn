package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.rest.ClientTest;

import org.apache.log4j.Logger;
import org.junit.Before;

public class ContextTest extends ClientTest {
	private static Logger logger = Logger.getLogger(ContextTest.class);

	RESTContext context;

	UserREST user;

	@Before
	public void findUser() throws NotSuccessException {
		context = RESTContext.register(BASE_URL);
		user = context.getUser();
	}

}
