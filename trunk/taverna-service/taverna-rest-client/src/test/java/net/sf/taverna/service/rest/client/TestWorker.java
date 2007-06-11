package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.datastore.dao.DAOFactory;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class TestWorker extends ContextTest {
	private static Logger logger = Logger.getLogger(TestWorker.class);
	
	@Before
	public void upgradeToWorker() {
		context.getWorkers();
	}
	
	
}

