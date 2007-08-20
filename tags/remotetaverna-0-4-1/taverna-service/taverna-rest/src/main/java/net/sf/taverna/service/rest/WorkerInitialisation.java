package net.sf.taverna.service.rest;

import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.DAOFactory;

public class WorkerInitialisation {
	public static void createNew() {
		DAOFactory daoFactory=DAOFactory.getFactory();
		Worker worker = new Worker();
		worker.setPassword(Worker.generatePassword()); 
		worker.setQueue(daoFactory.getQueueDAO().defaultQueue());
		daoFactory.getWorkerDAO().create(worker);
		daoFactory.commit();
	}
}
