package net.sf.taverna.service.backend.executor;

import org.apache.log4j.Logger;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.rest.utils.URIFactory;

public class RestfulJobExecutor implements JobExecutor {
	private static Logger logger = Logger.getLogger(RestfulJobExecutor.class);

	public void executeJob(Job job, Worker worker) {
		URIFactory factory = URIFactory.getInstance();
		while (factory.getRoot()==null || factory.getRoot().equals("")) {
			logger.info("Waiting for URIFactory to be initialised");
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		String jobUri = URIFactory.getInstance().getURI(job);
		String baseUri = URIFactory.getInstance().getRoot();
		
		logger.info("JobURI="+jobUri);
		logger.info("BaseUri="+baseUri);
		
		RestfulExecutionThread thread = new RestfulExecutionThread(jobUri,baseUri,worker.getUsername());
		thread.setDaemon(true);
		thread.start();
	}
}

