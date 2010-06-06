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
import java.rmi.NotBoundException;
import java.security.Principal;
import java.util.Calendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.taverna.server.localworker.remote.RemoteRunFactory;
import org.taverna.server.localworker.remote.RemoteSingleRun;
import org.taverna.server.master.SCUFL;

/**
 * A simple factory for workflow runs that forks runs from a subprocess.
 * 
 * @author Donal Fellows
 */
@ManagedResource(objectName = "Taverna:group=Server,name=ForkSingletonFactory", description = "The factory for simple singleton forked run.")
public class ForkingRunFactory extends AbstractRemoteRunFactory {
	protected String[] firstArgs;
	private JAXBContext context;
	private int waitSeconds = 40;
	private int sleepMS = 1000;
	private int lastStartupCheckCount;
	private Integer lastExitCode;
	private int totalRuns;
	private String javaBinary = getProperty("java.home") + separator + "bin"
			+ separator + "java";
	private RemoteRunFactory factory;
	Process factoryProcess;

	public ForkingRunFactory() throws JAXBException {
		context = JAXBContext.newInstance(SCUFL.class);
	}

	@ManagedAttribute(description = "Which java executable to run.", currencyTimeLimit = 300)
	public String getJavaBinary() {
		return javaBinary;
	}

	@ManagedAttribute(description = "Which java executable to run.", currencyTimeLimit = 300)
	public void setJavaBinary(String javaBinary) {
		this.javaBinary = javaBinary;
		killFactory();
		try {
			if (firstArgs != null)
				factory();
		} catch (Exception e) {
			log.fatal("failed to make connection to remote run factory", e);
		}
	}

	@ManagedAttribute(description = "The list of arguments used to make a worker process.", currencyTimeLimit = 300)
	public String[] getFirstArguments() {
		return firstArgs.clone();
	}

	@ManagedAttribute(description = "The list of arguments used to make a worker process.", currencyTimeLimit = 300)
	public void setFirstArguments(String[] firstArguments) {
		this.firstArgs = firstArguments.clone();
		killFactory();
		try {
			if (firstArgs != null)
				factory();
		} catch (Exception e) {
			log.fatal("failed to make connection to remote run factory", e);
		}
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

	/**
	 * Makes the subprocess that manufactures runs.
	 * 
	 * @return RMI handle to the factory.
	 * @throws Exception
	 *             If anything goes wrong.
	 */
	private RemoteRunFactory factory() throws Exception {
		if (factory != null)
			return factory;
		// Generate the arguments to use when spawning the subprocess
		final String uuid = "TavernaRunServer.WorkerFactory." + randomUUID();
		ProcessBuilder p = new ProcessBuilder(javaBinary);
		p.command().addAll(asList(firstArgs));
		p.command().add(uuid);
		p.redirectErrorStream(true);

		// Spawn the subprocess
		log.info("about to create subprocess: " + p.command());
		factoryProcess = p.start();
		Thread logger = new Thread(uuid + ".Logger") {
			public void run() {
				BufferedReader r = new BufferedReader(new InputStreamReader(
						factoryProcess.getInputStream()));
				try {
					String line;
					while (true) {
						line = r.readLine();
						if (line == null)
							break;
						log.info(uuid + ".Logger: subprocess output: " + line);
					}
				} catch (Exception e) {
					log.warn("failure in reading from " + uuid, e);
				}
			}
		};
		logger.setDaemon(true);
		logger.start();

		// Wait for the subprocess to register itself in the RMI registry
		Calendar deadline = Calendar.getInstance();
		deadline.add(SECOND, waitSeconds);
		Exception lastException = null;
		lastStartupCheckCount = 0;
		while (deadline.after(Calendar.getInstance())) {
			try {
				Thread.sleep(sleepMS);
				lastStartupCheckCount++;
				log.info("about to look up resource called " + uuid);
				factory = (RemoteRunFactory) registry.lookup(uuid);
				return factory;
			} catch (InterruptedException ie) {
				continue;
			} catch (NotBoundException nbe) {
				lastException = nbe;
				log.info("resource \"" + uuid + "\" not yet registered...");
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
	private void killFactory() {
		if (factory != null)
			factory = null;
		if (factoryProcess != null) {
			int code = -1;
			try {
				lastExitCode = code = factoryProcess.exitValue();
			} catch (Exception e) {
				try {
					factoryProcess.destroy();
					sleep(350); // takes a little time, even normally
					lastExitCode = code = factoryProcess.exitValue();
				} catch (Exception e2) {
				}
			}
			if (code > 128) {
				log.info("TavernaRunServer.WorkerFactory died with signal="
						+ (code - 128));
			} else if (code >= 0) {
				log.info("TavernaRunServer.WorkerFactory process killed: code="
						+ code);
			} else {
				log.warn("TavernaRunServer.WorkerFactory not yet dead");
			}
			factoryProcess = null;
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
		RemoteSingleRun rsr = factory().make(sw.toString());
		totalRuns++;
		return rsr;
	}
}