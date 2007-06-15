package net.sf.taverna.service.backend;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Job.Status;
import net.sf.taverna.service.datastore.dao.DAOFactory;

public class RavenJobExecutor implements JobExecutor {

	public void executeJob(Job job, Worker worker) {
		DAOFactory daoFactory = DAOFactory.getFactory();
		job.setStatus(Status.RUNNING);
		daoFactory.getJobDAO().update(job);
//		daoFactory.commit();
//		daoFactory.close();
	}
}
