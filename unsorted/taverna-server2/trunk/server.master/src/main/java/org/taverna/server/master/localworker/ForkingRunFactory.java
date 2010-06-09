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
import java.rmi.RemoteException;
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
	private Process factoryProcess;

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
				initFactory();
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
				initFactory();
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
	 * @throws Exception
	 *             If anything goes wrong.
	 */
	public void initFactory() throws Exception {
		if (factory != null)
			return;
		// Generate the arguments to use when spawning the subprocess
		final String uuid = "TavernaRunServer.WorkerFactory." + randomUUID();
		ProcessBuilder p = new ProcessBuilder(javaBinary);
		p.command().addAll(asList(firstArgs));
		p.command().add(uuid);
		p.redirectErrorStream(true);

		// Spawn the subprocess
		log.info("about to create subprocess: " + p.command());
		final Process fp = factoryProcess = p.start();
		Thread logger = new Thread(new Runnable() {
			@Override
			public void run() {
				BufferedReader r = new BufferedReader(new InputStreamReader(fp
						.getInputStream()));
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
		}, uuid + ".Logger");
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
				log.info("about to look up resource called " + uuid);
				factory = (RemoteRunFactory) registry.lookup(uuid);
				return;
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
	public void killFactory() {
		if (factory != null) {
			log.info("requesting shutdown of TavernaRunServer.WorkerFactory");
			try {
				factory.shutdown();
				sleep(700);
			} catch (RemoteException e) {
				log.warn("TavernaRunServer.WorkerFactory"
						+ " failed to shut down nicely", e);
			} catch (InterruptedException e) {
				log.debug("interrupted during wait after asking "
						+ "TavernaRunServer.WorkerFactory to shut down", e);
			} finally {
				factory = null;
			}
		}
		if (factoryProcess != null) {
			int code = -1;
			try {
				lastExitCode = code = factoryProcess.exitValue();
				log.info("TavernaRunServer.WorkerFactory already dead?");
			} catch (Exception e) {
				log
						.info("trying to force death of TavernaRunServer.WorkerFactory");
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
				log.info("TavernaRunServer.WorkerFactory died with signal="
						+ (code - 128));
			} else if (code >= 0) {
				log.info("TavernaRunServer.WorkerFactory process killed: code="
						+ code);
			} else {
				log.warn("TavernaRunServer.WorkerFactory not yet dead");
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
		if (factory == null)
			initFactory();
		RemoteSingleRun rsr = factory.make(sw.toString());
		totalRuns++;
		return rsr;
	}
}