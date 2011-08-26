package net.sf.taverna.service.backend.executor;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Worker;

public interface JobExecutor {
	public void executeJob(Job job, Worker worker);
	
	/**
	 * Kill the job processing.
	 * 
	 * @param job
	 *            Job to be killed
	 * @return true if the process was killed or in other ways finished, or
	 *         false if it is still running or can't be killed.
	 */
	public boolean killJob(Job job);
}
