package net.sf.taverna.service.queue;

import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.DAOFactory;

/**
 * A testing class for uploading some data for testing purposes.
 * @author Stuart Owen
 *
 */
public class TestingTool {
	
	public static void main(String[] args) throws Exception {
		DAOFactory daoFactory = DAOFactory.getFactory();
		
		addWorker(daoFactory);
		
		daoFactory.commit();
		daoFactory.close();
	}
	
	private static void addWorker(DAOFactory daoFactory) throws Exception {
		Worker worker = new Worker();
		String password = "Bob";
		worker.setPassword(password);
		worker.setQueue(daoFactory.getQueueDAO().defaultQueue());
		daoFactory.getWorkerDAO().create(worker);
	}
}
