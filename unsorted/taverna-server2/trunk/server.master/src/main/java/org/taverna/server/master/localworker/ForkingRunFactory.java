package org.taverna.server.master.localworker;

import static java.lang.Runtime.getRuntime;
import static java.util.Calendar.SECOND;
import static java.util.UUID.randomUUID;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.taverna.server.localworker.remote.RemoteSingleRun;
import org.taverna.server.master.SCUFL;

@ManagedResource(objectName = "Taverna:group=Server,name=ForkSingletonFactory", description = "The factory for simple singleton forked run.")
public class ForkingRunFactory extends AbstractRemoteRunFactory {
	protected List<String> firstArgs;
	private JAXBContext context;
	private int waitSeconds = 40;
	private int sleepMS = 1000;
	private int lastStartupCheckCount;
	private int totalRuns;

	public ForkingRunFactory() throws JAXBException {
		context = JAXBContext.newInstance(SCUFL.class);
	}

	@ManagedAttribute(description = "The list of arguments used to make a worker process.", currencyTimeLimit = 300)
	public List<String> getFirstArguments() {
		return firstArgs;
	}

	@ManagedAttribute(description = "The list of arguments used to make a worker process.", currencyTimeLimit = 300)
	public void setFirstArguments(List<String> firstArguments) {
		firstArgs = firstArguments;
	}

	@ManagedAttribute(description = "How many seconds to wait for a worker process to register itself.", currencyTimeLimit = 300)
	public int getWaitSeconds() {
		return waitSeconds;
	}

	@ManagedAttribute(description = "How many seconds to wait for a worker process to register itself.", currencyTimeLimit = 300)
	public void setWaitSeconds(int seconds) {
		waitSeconds = seconds;
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

	@Override
	protected RemoteSingleRun getRealRun(Principal creator, SCUFL workflow)
			throws Exception {
		// Generate the arguments to use when spawning the subprocess
		ArrayList<String> args = new ArrayList<String>(firstArgs);
		String uuid = "TavernaRun." + randomUUID();
		args.add(uuid);
		totalRuns++;

		// Spawn the subprocess and serialize the SCUFL to it
		Process p = getRuntime().exec(args.toArray(new String[0]));
		context.createMarshaller().marshal(workflow, p.getOutputStream());

		// Wait for the subprocess to register itself in the RMI registry
		Calendar deadline = Calendar.getInstance();
		deadline.add(SECOND, waitSeconds);
		Exception lastException = null;
		lastStartupCheckCount = 0;
		while (deadline.after(Calendar.getInstance())) {
			try {
				Thread.sleep(sleepMS);
				lastStartupCheckCount++;
				return (RemoteSingleRun) registry.lookup(uuid);
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
}
