package org.taverna.server.master.localworker;

import static java.io.File.separator;
import static java.lang.System.getProperty;
import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Calendar.SECOND;
import static java.util.UUID.randomUUID;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.Principal;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.web.context.ServletContextAware;
import org.taverna.server.localworker.remote.RemoteRunFactory;
import org.taverna.server.localworker.remote.RemoteSingleRun;
import org.taverna.server.master.SCUFL;

/**
 * A simple factory for workflow runs that forks runs from a subprocess.
 * 
 * @author Donal Fellows
 */
@ManagedResource(objectName = "Taverna:group=Server,name=ForkRunFactory", description = "The factory for simple singleton forked run.")
public class ForkRunFactory extends AbstractRemoteRunFactory implements
		ServletContextAware {
	private String executeWorkflowScript;
	protected String[] extraArgs;
	private JAXBContext context;
	private int waitSeconds = 40;
	private int sleepMS = 1000;
	private int lastStartupCheckCount;
	private Integer lastExitCode;
	private int totalRuns;
	private String javaBinary = getProperty("java.home") + separator + "bin"
			+ separator + "java";
	private String serverWorkerJar;
	private RemoteRunFactory factory;
	private Process factoryProcess;
	private String factoryProcessName;

	public ForkRunFactory() throws JAXBException {
		ClassLoader cl = ForkRunFactory.class.getClassLoader();
		serverWorkerJar = cl.getResource("util/server.worker.jar").getFile();
		context = JAXBContext.newInstance(SCUFL.class);
	}

	private void reinitFactory() {
		boolean makeFactory = factory != null;
		killFactory();
		try {
			if (makeFactory)
				initFactory();
		} catch (Exception e) {
			log.fatal("failed to make connection to remote run factory", e);
		}
	}

	@ManagedAttribute(description = "Which java executable to run.", currencyTimeLimit = 300)
	public String getJavaBinary() {
		return javaBinary;
	}

	@ManagedAttribute(description = "Which java executable to run.", currencyTimeLimit = 300)
	public void setJavaBinary(String javaBinary) {
		this.javaBinary = javaBinary;
		reinitFactory();
	}

	@ManagedAttribute(description = "The list of additional arguments used to make a worker process.", currencyTimeLimit = 300)
	public String[] getExtraArguments() {
		return extraArgs.clone();
	}

	@ManagedAttribute(description = "The list of additional arguments used to make a worker process.", currencyTimeLimit = 300)
	public void setExtraArguments(String[] firstArguments) {
		if (firstArguments == null)
			extraArgs = null;
		else
			extraArgs = firstArguments.clone();
		reinitFactory();
	}

	@ManagedAttribute(description = "The location of the JAR implementing the server worker process.")
	public String getServerWorkerJar() {
		return this.serverWorkerJar;
	}

	@ManagedAttribute(description = "The location of the JAR implementing the server worker process.")
	public void setServerWorkerJar(String serverWorkerJar) {
		this.serverWorkerJar = serverWorkerJar;
		reinitFactory();
	}

	@ManagedAttribute(description = "The script to run to start running a workflow.", currencyTimeLimit = 300)
	public String getExecuteWorkflowScript() {
		return executeWorkflowScript;
	}

	@ManagedAttribute(description = "The script to run to start running a workflow.", currencyTimeLimit = 300)
	public void setExecuteWorkflowScript(String executeWorkflowScript) {
		this.executeWorkflowScript = executeWorkflowScript;
		reinitFactory();
	}

	@ManagedAttribute(description = "How many seconds to wait for a worker process to register itself.", currencyTimeLimit = 300)
	public int getWaitSeconds() {
		return waitSeconds;
	}

	@ManagedAttribute(description = "How many seconds to wait for a worker process to register itself.", currencyTimeLimit = 300)
	public void setWaitSeconds(int seconds) {
		this.waitSeconds = seconds;
	}

	@ManagedAttribute(description = "How many milliseconds to wait between checks to see if a worker process has registered.", currencyTimeLimit = 300)
	public int getSleepTime() {
		return sleepMS;
	}

	@ManagedAttribute(description = "How many milliseconds to wait between checks to see if a worker process has registered.", currencyTimeLimit = 300)
	public void setSleepTime(int sleepTime) {
		sleepMS = sleepTime;
	}

	@ManagedAttribute(description = "How many checks were done for the worker process the last time a spawn was tried.", currencyTimeLimit = 10)
	public int getLastStartupCheckCount() {
		return lastStartupCheckCount;
	}

	@ManagedAttribute(description = "How many times has a subprocess been spawned by this engine.", currencyTimeLimit = 10)
	public int getTotalRuns() {
		return totalRuns;
	}

	@ManagedAttribute(description = "What was the exit code from the last time the factory subprocess was killed?")
	public Integer getLastExitCode() {
		return lastExitCode;
	}

	@ManagedAttribute(description = "What the factory subprocess's main RMI interface is registered as.", currencyTimeLimit = 60)
	public String getFactoryProcessName() {
		return factoryProcessName;
	}

	/**
	 * Makes the subprocess that manufactures runs.
	 * 
	 * @throws Exception
	 *             If anything goes wrong.
	 */
	public void initFactory() throws Exception {
		if (factory != null)
			return;
		// Generate the arguments to use when spawning the subprocess
		factoryProcessName = "ForkRunFactory." + randomUUID();
		ProcessBuilder p = new ProcessBuilder(javaBinary);
		if (extraArgs != null)
			p.command().addAll(asList(extraArgs));
		p.command().add("-jar");
		p.command().add(serverWorkerJar);
		p.command().add(executeWorkflowScript);
		p.command().add(factoryProcessName);
		p.redirectErrorStream(true);

		// Spawn the subprocess
		log.info("about to create subprocess: " + p.command());
		factoryProcess = p.start();
		Thread logger = new Thread(new OutputLogger(factoryProcessName,
				factoryProcess), factoryProcessName + ".Logger");
		logger.setDaemon(true);
		logger.start();

		// Wait for the subprocess to register itself in the RMI registry
		Calendar deadline = Calendar.getInstance();
		deadline.add(SECOND, waitSeconds);
		Exception lastException = null;
		lastStartupCheckCount = 0;
		while (deadline.after(Calendar.getInstance())) {
			try {
				sleep(sleepMS);
				lastStartupCheckCount++;
				log.info("about to look up resource called "
						+ factoryProcessName);
				try {
					registry.list(); // Validate registry connection first
				} catch (ConnectException ce) {
					log.warn("connection problems with registry", ce);
				}
				factory = (RemoteRunFactory) registry
						.lookup(factoryProcessName);
				log.info("successfully connected to factory subprocess "
						+ factoryProcessName);
				return;
			} catch (InterruptedException ie) {
				continue;
			} catch (NotBoundException nbe) {
				lastException = nbe;
				log.info("resource \"" + factoryProcessName
						+ "\" not yet registered...");
				continue;
			} catch (RuntimeException re) {
				// Unpack a remote exception if we can
				lastException = re;
				try {
					if (re.getCause() != null)
						lastException = (Exception) re.getCause();
				} catch (Throwable t) {
					// Ignore!
				}
			} catch (Exception e) {
				lastException = e;
			}
		}
		throw lastException;
	}

	/**
	 * Destroys the subprocess that manufactures runs.
	 */
	public void killFactory() {
		if (factory != null) {
			log.info("requesting shutdown of " + factoryProcessName);
			try {
				factory.shutdown();
				sleep(700);
			} catch (RemoteException e) {
				log.warn(factoryProcessName + " failed to shut down nicely", e);
			} catch (InterruptedException e) {
				log.debug("interrupted during wait after asking "
						+ factoryProcessName + " to shut down", e);
			} finally {
				factory = null;
			}
		}
		if (factoryProcess != null) {
			int code = -1;
			try {
				lastExitCode = code = factoryProcess.exitValue();
				log.info(factoryProcessName + " already dead?");
			} catch (Exception e) {
				log.info("trying to force death of " + factoryProcessName);
				try {
					factoryProcess.destroy();
					sleep(350); // takes a little time, even normally
					lastExitCode = code = factoryProcess.exitValue();
				} catch (Exception e2) {
				}
			} finally {
				factoryProcess = null;
			}
			if (code > 128) {
				log.info(factoryProcessName + " died with signal="
						+ (code - 128));
			} else if (code >= 0) {
				log.info(factoryProcessName + " process killed: code=" + code);
			} else {
				log.warn(factoryProcessName + " not yet dead");
			}
		}
	}

	@Override
	protected void finalize() {
		killFactory();
		super.finalize();
	}

	@Override
	protected RemoteSingleRun getRealRun(Principal creator, SCUFL workflow)
			throws Exception {
		StringWriter sw = new StringWriter();
		context.createMarshaller().marshal(workflow, sw);
		for (int i = 0; i < 3; i++) {
			if (factory == null)
				initFactory();
			try {
				RemoteSingleRun rsr = factory.make(sw.toString());
				totalRuns++;
				return rsr;
			} catch (ConnectException e) {
				// factory was lost; try to recreate
			} catch (ConnectIOException e) {
				// factory was lost; try to recreate
			}
			killFactory();
		}
		throw new Exception("total failure to connect to factory "
				+ factoryProcessName + "despite attempting restart");
	}

	@Override
	public void setServletContext(ServletContext servletContext) {
		if (executeWorkflowScript == null && servletContext != null) {
			executeWorkflowScript = servletContext
					.getInitParameter("executeWorkflowScript");
			if (executeWorkflowScript != null)
				log.info("configured executeWorkflowScript from context as "
						+ executeWorkflowScript);
		}
	}
}

class OutputLogger implements Runnable {
	OutputLogger(String name, Process process) {
		this.uniqueName = name;
		this.process = process;
	}

	private String uniqueName;
	private Process process;

	@Override
	public void run() {
		BufferedReader r = new BufferedReader(new InputStreamReader(process
				.getInputStream()));
		try {
			String line;
			while (true) {
				line = r.readLine();
				if (line == null)
					break;
				AbstractRemoteRunFactory.log.info(uniqueName
						+ " subprocess output: " + line);
			}
		} catch (Exception e) {
			AbstractRemoteRunFactory.log.warn("failure in reading from "
					+ uniqueName, e);
		}
	}
}