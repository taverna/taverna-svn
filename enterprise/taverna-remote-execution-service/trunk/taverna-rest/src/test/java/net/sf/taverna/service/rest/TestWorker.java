package net.sf.taverna.service.rest;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.DAOFactory;

import org.junit.Before;
import org.junit.Test;

public class TestWorker extends ClientTest {
	
	@Before
	public void makeWorker() {
		DAOFactory daoFactory = DAOFactory.getFactory();
		Worker worker = new Worker();
		password = User.generatePassword();
		worker.setPassword(password);
		daoFactory.getWorkerDAO().create(worker);
		username = worker.getUsername();
		useruri = BASE_URL + "users/" + username;
		daoFactory.commit();
	}
	
	@Test
	public void fish() {
		// no-op
	}
	
}
