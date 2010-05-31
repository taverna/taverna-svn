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

import org.taverna.server.master.SCUFL;
import org.taverna.server.master.localworker.rmi.RemoteSingleRun;

public class ForkingRunFactory extends AbstractRemoteRunFactory {
	protected List<String> firstArgs;
	private JAXBContext context;
	private int waitSeconds = 40;
	public ForkingRunFactory() throws JAXBException {
		context = JAXBContext.newInstance(SCUFL.class);
	}

	public List<String> getFirstArguments() {
		return firstArgs;
	}
	public void setFirstArguments(List<String> firstArguments) {
		firstArgs = firstArguments;
	}
	public int getWaitSeconds() {
		return waitSeconds;
	}
	public void setWaitSeconds(int seconds) {
		waitSeconds = seconds;
	}

	@Override
	protected RemoteSingleRun getRealRun(Principal creator, SCUFL workflow)
			throws Exception {
		// Generate the arguments to use when spawning the subprocess
		ArrayList<String> args = new ArrayList<String>(firstArgs);
		String uuid = "TavernaRun." + randomUUID();
		args.add(uuid);

		// Spawn the subprocess and serialize the SCUFL to it
		Process p = getRuntime().exec(args.toArray(new String[0]));
		context.createMarshaller().marshal(workflow, p.getOutputStream());

		// Wait for the subprocess to register itself in the RMI registry
		Calendar deadline = Calendar.getInstance();
		deadline.add(SECOND, waitSeconds);
		Exception lastException = null;
		while (deadline.after(Calendar.getInstance())) {
			try {
				Thread.sleep(1000);
				return (RemoteSingleRun) registry.lookup(uuid);
			} catch (Exception e) {
				lastException = e;
			}
		}
		throw lastException;
	}

}
