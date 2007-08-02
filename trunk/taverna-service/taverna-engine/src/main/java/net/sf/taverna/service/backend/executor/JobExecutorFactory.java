package net.sf.taverna.service.backend.executor;

import net.sf.taverna.service.rest.utils.URIFactory;

public class JobExecutorFactory {
	private static JobExecutorFactory instance = new JobExecutorFactory();
	private JobExecutorFactory() {
		
	}
	
	public static JobExecutorFactory getInstance() {
		return instance;
	}
	
	public JobExecutor createExecutor(URIFactory uriFactory) {
		return new ProcessJobExecutor(uriFactory);
	}
	
}
