package net.sf.taverna.service.backend;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Worker;

public interface JobExecutor {
	public void executeJob(Job job, Worker worker);
}
