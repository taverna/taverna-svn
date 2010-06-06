package org.taverna.server.localworker.impl;

import static java.io.File.createTempFile;
import static java.lang.Thread.sleep;
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
import java.util.Map;

import org.taverna.server.localworker.remote.RemoteListener;
import org.taverna.server.localworker.remote.RemoteStatus;

/**
 * The core class that connects to a Taverna command-line workflow execution
 * engine. This implementation always registers a single listener, &lquo;
 * <tt>io</tt> &rquo;, with two properties representing the stdout and stderr of
 * the run. The listener is remote-accessible. It does not support attaching any
 * other listeners.
 * 
 * @author Donal Fellows
 */
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

	/**
	 * An engine for asynchronously copying from an {@link InputStream} to a
	 * {@link Writer}.
	 * 
	 * @author Donal Fellows
	 */
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

	/**
	 * Fire up the workflow. This causes a transition into the operating state.
	 * 
	 * @param executeWorkflowCommand
	 *            The command to run to execute the workflow.
	 * @param workflow
	 *            The workflow document to execute.
	 * @param workingDir
	 *            What directory to use as the working directory.
	 * @param inputBaclava
	 *            The baclava file to use for inputs, or <tt>null</tt> to use
	 *            the other <b>input*</b> arguments' values.
	 * @param inputFiles
	 *            A mapping of input names to files that supply them. Note that
	 *            we assume that nothing mapped here will be mapped in
	 *            <b>inputValues</b>.
	 * @param inputValues
	 *            A mapping of input names to values to supply to them. Note
	 *            that we assume that nothing mapped here will be mapped in
	 *            <b>inputFiles</b>.
	 * @param outputBaclava
	 *            What baclava file to write the output from the workflow into,
	 *            or <tt>null</tt> to have it written into the <tt>out</tt>
	 *            subdirectory.
	 * @throws IOException
	 *             If any of quite a large number of things goes wrong.
	 */
	public void initWorker(String executeWorkflowCommand, String workflow,
			File workingDir, String inputBaclava,
			Map<String, String> inputFiles, Map<String, String> inputValues,
			String outputBaclava) throws IOException {
		ProcessBuilder pb = new ProcessBuilder()
				.command(executeWorkflowCommand);
		if (inputBaclava != null) {
			pb.command().add("-inputdoc");
			pb.command().add(inputBaclava);
		} else {
			for (String port : inputFiles.keySet()) {
				String f = inputFiles.get(port);
				if (f != null) {
					pb.command().add("-inputfile");
					pb.command().add(port);
					pb.command().add(f);
				}
			}
			for (String port : inputValues.keySet()) {
				String v = inputValues.get(port);
				if (v != null) {
					pb.command().add("-inputvalue");
					pb.command().add(port);
					pb.command().add(v);
				}
			}
		}
		if (outputBaclava != null) {
			pb.command().add("-outputdoc");
			pb.command().add(outputBaclava);
		} else {
			pb.command().add("-outputdir");
			pb.command().add("out");
		}
		File tmp = createTempFile("taverna", null);
		FileWriter w = new FileWriter(tmp);
		w.write(workflow);
		w.close();
		pb.command().add(tmp.getAbsolutePath());
		pb.directory(workingDir);
		subprocess = pb.start();
		if (subprocess == null)
			throw new IOException("unknown failure creating process");
		new AsyncCopy(subprocess.getInputStream(), stdout);
		new AsyncCopy(subprocess.getErrorStream(), stderr);
	}

	/**
	 * Kills off the subprocess if it exists and is alive.
	 */
	public void killWorker() {
		if (!finished && subprocess != null) {
			int code;
			try {
				code = subprocess.exitValue();
			} catch (IllegalThreadStateException e) {
				subprocess.destroy();
				try {
					sleep(350);
				} catch (InterruptedException e1) {
					e1.printStackTrace(); // not expected
				}
				code = subprocess.exitValue();
				finished = true;
			}
			if (code > 128) {
				System.out.println("workflow aborted, signal=" + (code - 128));
			} else {
				System.out.println("workflow exited, code=" + code);
			}
		}
	}

	/**
	 * Move the worker out of the stopped state and back to operating.
	 * 
	 * @throws RemoteException
	 *             if it fails (which it always does; operation currently
	 *             unsupported).
	 */
	public void startWorker() throws RemoteException {
		throw new RemoteException("starting unsupported");
	}

	/**
	 * Move the worker into the stopped state from the operating state.
	 * 
	 * @throws RemoteException
	 *             if it fails (which it always does; operation currently
	 *             unsupported).
	 */
	public void stopWorker() throws RemoteException {
		throw new RemoteException("stopping unsupported");
	}

	/**
	 * @return The status of the workflow run. Note that this can be an
	 *         expensive operation.
	 */
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
