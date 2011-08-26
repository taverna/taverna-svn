package net.sf.taverna.service.backend.executor;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.utils.URIFactory;
import net.sf.taverna.service.util.JavaProcess;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class ProcessJobExecutor implements JobExecutor {
	
	public static final JobProcesses jobProcesses = new JobProcesses();
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ProcessJobExecutor.class);

	private URIFactory uriFactory;

	public ProcessJobExecutor(URIFactory uriFactory) {
		this.uriFactory = uriFactory;
	}
	
	private File makeTavernaHome() {
		String tavernHomePath = getConfiguredTavernaHome();
		File tavernaHome = new File(tavernHomePath);
		
		if (!tavernaHome.exists()) {
			logger.info("Creating new taverna home directory:"+tavernHomePath);
			tavernaHome.mkdir();
		}
		
		if (! tavernaHome.isDirectory()) {
			logger.error("Could not create directory " + tavernaHome);
			return null;
		}
		
		File repository = new File(tavernaHome,"repository");
		if (!repository.exists()) {
			if (!repository.mkdir()) {
				logger.error("Unable to make repository directory:"+repository.getAbsolutePath());
			}
			else {
				logger.info("Successfully made repository directory:"+repository.getAbsolutePath());
			}
		}
		else {
			if (!repository.isDirectory()) {
				logger.error("Repository directory already exists but is a file (not a directory):"+repository.getAbsolutePath());
			}
		}
		return tavernaHome;
	}
	
	private String getConfiguredTavernaHome() {
		DAOFactory daoFactory = DAOFactory.getFactory();
		Configuration config = daoFactory.getConfigurationDAO().getConfig();
		String result = config.getTavernaHome();
		return result;
	}
	
	/**
	 * Kill a process. Return true if the process was killed or in other ways
	 * finished, or false if it is still running.
	 * 
	 * @param job
	 * @return
	 */
	public boolean killJob(Job job) {
		Process process = jobProcesses.get(job);
		if (process == null) { 
			logger.info("Attempt to kill unknown job (already finished?): "
				+ job);
			return true;
		}
		try {
			process.exitValue();
			logger.info("Didn't kill already finished job " + job);
			jobProcesses.remove(job);
			return true;
		} catch (IllegalThreadStateException ex) {
			logger.debug("Attempting to kill " + job);
			process.destroy();
		}
		
		// Did it die?
		try {
			logger.info("Killed " + job);
			process.exitValue();
			jobProcesses.remove(job);
			return true;
		} catch (IllegalThreadStateException ex) {
			logger.warn("Could not kill job " + job);
			return false;
		}
	}
	

	public void executeJob(Job job, Worker worker) {
		logger.info("Executing " + job + " at " + worker);
		String jobUri = uriFactory.getURI(job);
		String baseUri = uriFactory.getApplicationRoot().toString();
		String username = worker.getUsername();
		String password = worker.getWorkerPasswordStr();
		String memory = getConfiguredMemory();
	
		JavaProcess javaProcess =
			new JavaProcess("net.sf.taverna.service.backend.executor.RestfulExecutionProcess",
				getClass().getClassLoader(),memory);
		
		File tavernaHome = makeTavernaHome();
		javaProcess.addSystemProperty("taverna.home", tavernaHome.getAbsolutePath());
		
		javaProcess.addSystemProperty("java.awt.headless", "true");
		
		// Enable for debugging on localhost:8990
		//javaProcess.addJavaArguments("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:8990");
		
		// Disable to use Raven to download artefacts
//		javaProcess.addSystemProperty("raven.eclipse", "true");
		
		// Enable to use Raven to download Taverna 1.7.0.0
		javaProcess.addSystemProperty("raven.profile", "http://www.mygrid.org.uk/taverna/updates/1.7.0/taverna-1.7.0.0-profile.xml");

		javaProcess.addArguments("-base", baseUri);
		javaProcess.addArguments("-username", username);
		// FIXME: Don't expose our password on command line!
		javaProcess.addArguments("-password", password);
		
		javaProcess.setRedirectingError(true);
		
		javaProcess.addArguments(jobUri);
		logger.info("Starting process " + javaProcess);
		Process process = javaProcess.run();
		new ConsoleReaderThread(job, process).start();
		jobProcesses.put(job, process);
	}
	
	private String getConfiguredMemory() {
		DAOFactory daoFactory = DAOFactory.getFactory();
		Configuration config = daoFactory.getConfigurationDAO().getConfig();
		if (daoFactory.hasActiveTransaction()) daoFactory.rollback();
		daoFactory.close();
		return config.getWorkerMemory();
	}
}

/**
 * Mapping between {@link Job} and {@link Process}. Primarily of use for
 * {@link ProcessJobExecutor#killJob(Job)}.
 * <p>
 * Processes that are finished (even if they failed) will be removed from the
 * mapping.
 * 
 * @author Stian Soiland
 */
class JobProcesses {

	private Map<Job, Process> jobToProcess = new HashMap<Job, Process>();
	
	public synchronized void remove(Job job) {
		jobToProcess.remove(job);
	}
	
	/**
	 * Return the process running the given {@link Job}, or return
	 * <code>null</code> if the job is unknown or the process is finished.
	 * 
	 * @param job Job which process to look up.
	 * @return {@link Process} that is currently executing the job
	 */
	public synchronized Process get(Job job) {
		removeCompleted();
		return jobToProcess.get(job);
	}

	public  void put(Job job, Process process) {
		removeCompleted();
		jobToProcess.put(job, process);
	}
	
	/**
	 * Remove mappings for processes that have completed. Called by
	 * {@link #get(Job)}, {@link #put(Job, Process)} and {@link #remove(Job)}.
	 */
	private synchronized void removeCompleted() {
		Set<Job> completed = new HashSet<Job>();
		for (Entry<Job, Process> entry : jobToProcess.entrySet()) {
			try {
				entry.getValue().exitValue();
				completed.add(entry.getKey());
			} catch (IllegalThreadStateException ex) {
				// Expected, still running
			}
		}
		for (Job job : completed) {
			jobToProcess.remove(job);
		}
	}
}

class ConsolePipingThread extends Thread {
	
	private static Logger logger = Logger.getLogger(ConsolePipingThread.class);
	
	DAOFactory daoFactory = DAOFactory.getFactory();

	private final Process process;

	public ConsolePipingThread(Job job, Process process) {
		super("Console piper for " + job);
		this.process = process;
	}
	
	@Override
	public void run() {
		try {
			IOUtils.copy(process.getInputStream(), System.err);
		} catch (IOException e) {
			logger.warn("An error occured", e);
		}
	}
}

/**
 * Read the {@link Process}'s standard output (the console output) and store it
 * with {@link Job#setConsole(String)}
 * 
 * @author Stian Soiland
 */
class ConsoleReaderThread extends Thread {
	
	private static Logger logger = Logger.getLogger(ConsoleReaderThread.class);

	DAOFactory daoFactory = DAOFactory.getFactory();
	
	private final Job job;

	private final Process process;

	public ConsoleReaderThread(Job job, Process process) {
		super("Console reader for " + job);
		this.job = job;
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
			if (status == 0) {
				logger.info("Completed process for " + job);
			} else {
				logger.warn("Error code " + status + " from process for " + job);
			}
		} catch (InterruptedException e) {
			logger.info("Thread interrupted while waiting for " + job);
			System.out.println(stdout);
			
			Thread.currentThread().interrupt();
		}
		finally {
			daoFactory.getJobDAO().reread(job).setConsole(stdout);
			daoFactory.commit();
			daoFactory.close();
		}
	}
}
