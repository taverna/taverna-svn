package net.sf.taverna.service.backend.executor;

import java.io.File;
import java.io.IOException;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.util.JavaProcess;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ProcessJobExecutor implements JobExecutor {

	public class ConsoleReaderThread extends Thread {

		DAOFactory daoFactory = DAOFactory.getFactory();
		
		private final Job job;

		private final Process process;

		private ConsoleReaderThread(Job job, Process process) {
			super("Console reader for " + job);
			this.job = daoFactory.getJobDAO().read(job.getId());
			this.process = process;
		}

		@Override
		public void run() {
			String stdout;
			try {
				stdout = IOUtils.toString(process.getInputStream());
			} catch (IOException e) {
				logger.warn("Could not read stdout for " + job, e);
				return;
			}
			int status;
			try {
				status = process.waitFor();
			} catch (InterruptedException e) {
				logger.info("Thread interrupted while waiting for " + job);
				System.out.println(stdout);
				job.setConsole(stdout);
				Thread.currentThread().interrupt();
				return;
			}
			if (status == 0) {
				logger.info("Completed process for " + job);
			} else {
				logger.warn("Error code " + status + " from process for " + job);
			}
			System.out.println(stdout);
			job.setConsole(stdout);
		}
	}


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

		// FIXME don't hardcode the path to the taverna distribution (and is it needed?)
		javaProcess.addSystemProperty("taverna.startup", 
			"/Users/stain/download/taverna-1.5.2");
		
		javaProcess.addSystemProperty("java.awt.headless", "true");
		
		javaProcess.addSystemProperty("raven.profile", "http://www.mygrid.org.uk/taverna/updates/1.5.2/taverna-1.5.2.0-profile.xml");
		
//		RavenProcess javaProcess =
//			new RavenProcess("uk.org.mygrid.tavernaservice", "taverna-engine",
//				"1.0.0", "net.sf.taverna.service.RestfulExecutionProcess",
//				"main");

		javaProcess.addArguments("-base", baseUri);
		javaProcess.addArguments("-username", username);
		// FIXME: Don't expose our password on command line
		javaProcess.addArguments("-password", password);
		
		javaProcess.setRedirectingError(true);
		
		javaProcess.addArguments(jobUri);
		logger.info("Starting process " + javaProcess);
		Process process = javaProcess.run();
		new ConsoleReaderThread(job, process).start();
	}

}
