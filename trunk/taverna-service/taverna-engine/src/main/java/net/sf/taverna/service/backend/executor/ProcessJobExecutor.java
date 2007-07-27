package net.sf.taverna.service.backend.executor;

import java.io.File;
import java.io.IOException;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.util.JavaProcess;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ProcessJobExecutor implements JobExecutor {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ProcessJobExecutor.class);

	private URIFactory uriFactory;

	public ProcessJobExecutor(URIFactory uriFactory) {
		this.uriFactory = uriFactory;
	}
	
	public File makeTavernaHome() {
		File tavernaHome;
		try {
			tavernaHome = File.createTempFile("taverna", "");
		} catch (IOException e) {
			logger.warn("Could not create temporary taverna home", e);
			return null;
		}
		tavernaHome.delete();
		tavernaHome.mkdir();
		if (! tavernaHome.isDirectory()) {
			logger.warn("Could not create directory " + tavernaHome);
			return null;
		}
		System.out.println("Temporary taverna.home is " + tavernaHome);
		return tavernaHome;
	}


	public void executeJob(Job job, Worker worker) {
		String jobUri = uriFactory.getURI(job);
		String baseUri = uriFactory.getApplicationRoot().toString();
		String username = worker.getUsername();
		String password = worker.getWorkerPasswordStr();
	
		JavaProcess javaProcess =
			new JavaProcess("net.sf.taverna.service.backend.executor.RestfulExecutionProcess",
				getClass().getClassLoader());
		
		File tavernaHome = makeTavernaHome();
		javaProcess.addSystemProperty("taverna.home", tavernaHome.getAbsolutePath());

//		RavenProcess javaProcess =
//			new RavenProcess("uk.org.mygrid.tavernaservice", "taverna-engine",
//				"1.0.0", "net.sf.taverna.service.RestfulExecutionProcess",
//				"main");

		javaProcess.addArguments("-base", baseUri);
		javaProcess.addArguments("-username", username);
		// FIXME: Don't expose our password on command line
		javaProcess.addArguments("-password", password);
		
		

		javaProcess.addArguments(jobUri);
		logger.info("Starting process " + javaProcess);
		Process process = javaProcess.run();
		String stdout;
		try {
			stdout = IOUtils.toString(process.getInputStream());
		} catch (IOException e) {
			logger.warn("Could not read stdout from process " + javaProcess, e);
			return;
		}
		int status;
		try {
			status = process.waitFor();
		} catch (InterruptedException e) {
			logger.info("Thread interrupted while waiting for " + javaProcess);
			System.out.println(stdout);
			Thread.currentThread().interrupt();
			return;
		}
		if (status == 0) {
			logger.info("Completed process " + javaProcess);
		} else {
			logger.warn("Error code " + status + " from process " + javaProcess);
		}
		System.out.println(stdout);
		job.setConsole(stdout);
	}

}
