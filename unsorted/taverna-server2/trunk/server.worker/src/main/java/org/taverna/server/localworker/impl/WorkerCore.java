package org.taverna.server.localworker.impl;

import static java.io.File.createTempFile;
import static java.lang.Runtime.getRuntime;
import static org.apache.commons.io.IOUtils.copy;
import static org.taverna.server.localworker.remote.RemoteStatus.Finished;
import static org.taverna.server.localworker.remote.RemoteStatus.Initialized;
import static org.taverna.server.localworker.remote.RemoteStatus.Operating;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;

import org.taverna.server.localworker.remote.RemoteListener;
import org.taverna.server.localworker.remote.RemoteStatus;

public class WorkerCore extends UnicastRemoteObject implements RemoteListener {
	Process subprocess;
	private boolean finished;
	StringWriter stdout;
	StringWriter stderr;

	public WorkerCore() throws RemoteException {
		super();
		stdout = new StringWriter();
		stderr = new StringWriter();
	}

	private static class AsyncCopy extends Thread {
		private InputStream from;
		private Writer to;

		AsyncCopy(InputStream from, Writer to) {
			this.from = from;
			this.to = to;
			setDaemon(true);
			start();
		}

		public void run() {
			try {
				copy(from, to);
			} catch (IOException e) {
			}
		}
	}

	public void initWorker(String executeWorkflowCommand, String workflow,
			File workingDir, String inputBaclava,
			Map<String, String> inputFiles, Map<String, String> inputValues,
			String outputBaclava) throws RemoteException, IOException {
		ArrayList<String> args = new ArrayList<String>();
		args.add(executeWorkflowCommand);
		if (inputBaclava != null) {
			args.add("-inputdoc");
			args.add(inputBaclava);
		} else {
			for (String port : inputFiles.keySet()) {
				String f = inputFiles.get(port);
				if (f != null) {
					args.add("-inputfile");
					args.add(port);
					args.add(f);
				}
			}
			for (String port : inputValues.keySet()) {
				String v = inputValues.get(port);
				if (v != null) {
					args.add("-inputvalue");
					args.add(port);
					args.add(v);
				}
			}
		}
		if (outputBaclava != null) {
			args.add("-outputdoc");
			args.add(outputBaclava);
		} else {
			args.add("-outputdir");
			args.add("out");
		}
		File tmp = createTempFile("taverna", null);
		FileWriter w = new FileWriter(tmp);
		w.write(workflow);
		w.close();
		args.add(tmp.getAbsolutePath());
		subprocess = getRuntime().exec(args.toArray(new String[args.size()]),
				null, workingDir);
		new AsyncCopy(subprocess.getInputStream(), stdout);
		new AsyncCopy(subprocess.getErrorStream(), stderr);
	}

	public void killWorker() throws RemoteException {
		if (!finished && subprocess != null) {
			try {
				subprocess.exitValue();
			} catch (IllegalThreadStateException e) {
				subprocess.destroy();
				finished = true;
			}
		}
	}

	public void startWorker() throws RemoteException {
		throw new RemoteException("starting unsupported");
	}

	public void stopWorker() throws RemoteException {
		throw new RemoteException("stopping unsupported");
	}

	public RemoteStatus getWorkerStatus() {
		if (subprocess == null)
			return Initialized;
		if (finished)
			return Finished;
		try {
			subprocess.exitValue();
			finished = true;
			return Finished;
		} catch (IllegalThreadStateException e) {
			return Operating;
		}
	}

	@Override
	public String getConfiguration() {
		return "";
	}

	@Override
	public String getName() {
		return "io";
	}

	@Override
	public String getProperty(String propName) throws RemoteException {
		if (propName.equals("stdout"))
			return stdout.toString();
		if (propName.equals("stderr"))
			return stderr.toString();
		throw new RemoteException("unknown property");
	}

	@Override
	public String getType() {
		return "io";
	}

	@Override
	public String[] listProperties() {
		return new String[] { "stdout", "stderr" };
	}

	@Override
	public void setProperty(String propName, String value)
			throws RemoteException {
		throw new RemoteException("property is read only");
	}
}
