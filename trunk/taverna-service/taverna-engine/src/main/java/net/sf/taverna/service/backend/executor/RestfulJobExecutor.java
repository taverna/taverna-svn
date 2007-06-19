package net.sf.taverna.service.backend.executor;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Worker;

public class RestfulJobExecutor implements JobExecutor {

	public void executeJob(Job job, Worker worker) {
		RestfulExecutionThread thread = new RestfulExecutionThread(job.getId(),worker.getUsername());
		thread.setDaemon(true);
		thread.start();
	}
}

