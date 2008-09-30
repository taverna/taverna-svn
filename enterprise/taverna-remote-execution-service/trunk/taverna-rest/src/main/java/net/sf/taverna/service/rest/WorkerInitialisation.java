package net.sf.taverna.service.rest;

import org.apache.log4j.Logger;

import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.DAOFactory;

public class WorkerInitialisation {
	private static Logger logger = Logger.getLogger(WorkerInitialisation.class);
	
	public static void createNew() {
		DAOFactory daoFactory=DAOFactory.getFactory();
		Worker worker = new Worker();
		worker.setPassword(Worker.generatePassword()); 
		worker.setQueue(daoFactory.getQueueDAO().defaultQueue());
		daoFactory.getWorkerDAO().create(worker);
		daoFactory.commit();
		logger.info("Created new worker " + worker);
	}
}
