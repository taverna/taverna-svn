package net.sf.taverna.service.backend;

public class JobExecutorFactory {
	private static JobExecutorFactory instance = new JobExecutorFactory();
	private JobExecutorFactory() {
		
	}
	
	public static JobExecutorFactory getInstance() {
		return instance;
	}
	
	public JobExecutor createExecutor() {
		//the only executor that presently exists.
		return new RavenJobExecutor();
	}
	
}
