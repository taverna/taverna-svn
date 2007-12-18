package net.sf.taverna.service.rest.client;

import org.apache.log4j.Logger;
import org.junit.Before;

public abstract class TestWorker extends ContextTest {
	private static Logger logger = Logger.getLogger(TestWorker.class);
	
	@Before
	public void upgradeToWorker() {
		context.getWorkers();
	}
	
	
}

