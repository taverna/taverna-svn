package net.sf.taverna.service.backend.executor;

import org.apache.log4j.Logger;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.rest.utils.URIFactory;

public class RestfulJobExecutor implements JobExecutor {
	private static Logger logger = Logger.getLogger(RestfulJobExecutor.class);

	private URIFactory uriFactory;

	public RestfulJobExecutor(URIFactory uriFactory) {
		this.uriFactory = uriFactory;
	}

	public void executeJob(Job job, Worker worker) {
		RestfulExecutionThread thread =
			new RestfulExecutionThread(uriFactory.getURI(job),
				uriFactory.getApplicationRoot().toString(), worker.getUsername(), worker.getWorkerPasswordStr());
		thread.setDaemon(true);
		thread.start();
	}
}
