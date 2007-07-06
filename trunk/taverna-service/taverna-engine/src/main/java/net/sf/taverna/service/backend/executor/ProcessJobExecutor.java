package net.sf.taverna.service.backend.executor;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.util.RavenProcess;

import org.apache.log4j.Logger;

public class ProcessJobExecutor implements JobExecutor {

	private static Logger logger = Logger.getLogger(ProcessJobExecutor.class);

	private URIFactory uriFactory;

	public ProcessJobExecutor(URIFactory uriFactory) {
		this.uriFactory = uriFactory;
	}

	public void executeJob(Job job, Worker worker) {
		String jobUri = uriFactory.getURI(job);
		String baseUri = uriFactory.getApplicationRoot().toString();
		String username = worker.getUsername();
		// FIXME: Use slightly better passwords, and don't hardcode
		String password = "Bob";
		
		RavenProcess raven = new RavenProcess("uk.org.mygrid.tavernaservice", "taverna-engine", "1.0.0", "net.sf.taverna.service.RestfulExecutionProcess", "main");
		raven.addArguments("-base", baseUri);
		raven.addArguments("-username",username);
		// FIXME: Don't expose our password on command line
		raven.addArguments("-password", password);

		raven.addArguments(jobUri);
		
		raven.run();
	}

	


}
