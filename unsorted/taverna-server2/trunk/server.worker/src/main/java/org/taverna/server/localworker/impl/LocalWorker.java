package org.taverna.server.localworker.impl;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.setProperty;
import static java.lang.System.setSecurityManager;
import static java.rmi.registry.LocateRegistry.getRegistry;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.taverna.server.localworker.remote.RemoteStatus.Finished;
import static org.taverna.server.localworker.remote.RemoteStatus.Initialized;
import static org.taverna.server.localworker.remote.RemoteStatus.Operating;
import static org.taverna.server.localworker.remote.RemoteStatus.Stopped;

import java.io.File;
import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.taverna.server.localworker.remote.RemoteDirectory;
import org.taverna.server.localworker.remote.RemoteInput;
import org.taverna.server.localworker.remote.RemoteListener;
import org.taverna.server.localworker.remote.RemoteRunFactory;
import org.taverna.server.localworker.remote.RemoteSecurityContext;
import org.taverna.server.localworker.remote.RemoteSingleRun;
import org.taverna.server.localworker.remote.RemoteStatus;

/**
 * This class implements one side of the connection between the Taverna Server
 * master server and this process. It delegates to a {@link WorkerCore} instance
 * the handling of actually running a workflow.
 * 
 * @author Donal Fellows
 * @see DirectoryDelegate
 * @see FileDelegate
 */
public class LocalWorker extends UnicastRemoteObject implements RemoteSingleRun {
	private String executeWorkflowCommand;
	private String workflow;
	private File base;
	private DirectoryDelegate baseDir;
	RemoteStatus status;
	String inputBaclava, outputBaclava;
	Map<String, String> inputFiles;
	Map<String, String> inputValues;
	private WorkerCore core;

	protected LocalWorker(String executeWorkflowCommand, String workflow)
			throws RemoteException {
		super();
		this.workflow = workflow;
		this.executeWorkflowCommand = executeWorkflowCommand;
		base = new File(randomUUID().toString());
		try {
			FileUtils.forceMkdir(base);
		} catch (IOException e) {
			RemoteException re = new RemoteException(e.getMessage());
			re.initCause(e);
			throw re;
		}
		baseDir = new DirectoryDelegate(base, null);
		inputFiles = new HashMap<String, String>();
		inputValues = new HashMap<String, String>();
		core = new WorkerCore();
		status = Initialized;
	}

	private static final String usage = "java -jar "
			+ "TavernaServer.Worker.0.0.1-SNAPSHOT-jar-with-dependencies.jar"
			+ " workflowExecScript UUID";

	static class RRF extends UnicastRemoteObject implements RemoteRunFactory {
		String command;

		public RRF(String command) throws RemoteException {
			this.command = command;
		}

		@Override
		public RemoteSingleRun make(String scufl) throws RemoteException {
			return new LocalWorker(command, scufl);
		}
	}

	public static final String SECURITY_POLICY_FILE = "security.policy";

	/**
	 * @param args
	 *            The arguments from the command line invocation.
	 * @throws Exception
	 *             If we can't connect to the RMI registry, or if we can't read
	 *             the workflow, or if we can't build the worker instance, or
	 *             register it. Also if the arguments are wrong.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			throw new Exception("wrong # args: must be \"" + usage + "\"");
		}
		setProperty("java.security.policy", LocalWorker.class.getClassLoader()
				.getResource(SECURITY_POLICY_FILE).toExternalForm());
		setSecurityManager(new RMISecurityManager());
		String command = args[0];
		final String factoryname = args[1];
		final Registry registry = getRegistry();
		registry.bind(factoryname, new RRF(command));
		getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					registry.unbind(factoryname);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		System.out.println("registered RemoteRunFactory with ID " + factoryname);
	}

	@Override
	public void destroy() throws RemoteException {
		if (status != Finished && status != Initialized)
			core.killWorker();
		// Is this it?
		try {
			if (base != null)
				forceDelete(base);
			base = null;
		} catch (IOException e) {
			RemoteException r = new RemoteException(e.getMessage());
			r.initCause(e);
			throw r;
		}
	}

	@Override
	public void addListener(RemoteListener listener) throws RemoteException {
		throw new RemoteException("not implemented");
	}

	@Override
	public String getInputBaclavaFile() throws RemoteException {
		return inputBaclava;
	}

	@Override
	public List<RemoteInput> getInputs() throws RemoteException {
		ArrayList<RemoteInput> result = new ArrayList<RemoteInput>();
		for (String name : inputFiles.keySet())
			result.add(new InputDelegate(name));
		return result;
	}

	@Override
	public List<String> getListenerTypes() throws RemoteException {
		return emptyList();
	}

	@Override
	public List<RemoteListener> getListeners() {
		return singletonList((RemoteListener) core);
	}

	@Override
	public String getOutputBaclavaFile() throws RemoteException {
		return outputBaclava;
	}

	class SecurityDelegate extends UnicastRemoteObject implements
			RemoteSecurityContext {
		protected SecurityDelegate() throws RemoteException {
			super();
		}
	}

	@Override
	public RemoteSecurityContext getSecurityContext() throws RemoteException {
		return new SecurityDelegate();
	}

	@Override
	public RemoteStatus getStatus() {
		// only state that can spontaneously change to another
		if (status == Operating) {
			status = core.getWorkerStatus();
		}
		return status;
	}

	@Override
	public RemoteDirectory getWorkingDirectory() {
		return baseDir;
	}

	void validateFilename(String filename) throws RemoteException {
		if (filename.startsWith("/") || filename.contains("//"))
			throw new RemoteException("invalid filename");
		if (Arrays.asList(filename.split("/")).contains(".."))
			throw new RemoteException("invalid filename");
	}

	class InputDelegate extends UnicastRemoteObject implements RemoteInput {
		private String name;

		InputDelegate(String name) throws RemoteException {
			super();
			this.name = name;
			if (!inputFiles.containsKey(name)) {
				if (status != RemoteStatus.Initialized)
					throw new RemoteException("not initializing");
				inputFiles.put(name, null);
				inputValues.put(name, null);
			}
		}

		@Override
		public String getFile() {
			return inputFiles.get(name);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getValue() {
			return inputValues.get(name);
		}

		@Override
		public void setFile(String file) throws RemoteException {
			if (status != RemoteStatus.Initialized)
				throw new RemoteException("not initializing");
			validateFilename(file);
			inputValues.put(name, null);
			inputFiles.put(name, file);
			inputBaclava = null;
		}

		@Override
		public void setValue(String value) throws RemoteException {
			if (status != RemoteStatus.Initialized)
				throw new RemoteException("not initializing");
			inputValues.put(name, value);
			inputFiles.put(name, null);
			LocalWorker.this.inputBaclava = null;
		}
	}

	@Override
	public RemoteInput makeInput(String name) throws RemoteException {
		return new InputDelegate(name);
	}

	@Override
	public RemoteListener makeListener(String type, String configuration)
			throws RemoteException {
		throw new RemoteException("listener manufacturing unsupported");
	}

	@Override
	public void setInputBaclavaFile(String filename) throws RemoteException {
		if (status != RemoteStatus.Initialized)
			throw new RemoteException("not initializing");
		validateFilename(filename);
		for (String input : inputFiles.keySet()) {
			inputFiles.put(input, null);
			inputValues.put(input, null);
		}
		inputBaclava = filename;
	}

	@Override
	public void setOutputBaclavaFile(String filename) throws RemoteException {
		if (status != RemoteStatus.Initialized)
			throw new RemoteException("not initializing");
		if (filename != null)
			validateFilename(filename);
		outputBaclava = filename;
	}

	@Override
	public void setStatus(RemoteStatus newStatus) throws RemoteException {
		if (status == newStatus)
			return;

		switch (newStatus) {
		case Initialized:
			throw new RemoteException("may not move back to start");
		case Operating:
			switch (status) {
			case Initialized:
				try {
					core.initWorker(executeWorkflowCommand, workflow, base,
							inputBaclava, inputFiles, inputValues,
							outputBaclava);
				} catch (IOException e) {
					RemoteException re = new RemoteException(e.getMessage());
					re.initCause(e);
					throw re;
				}
				break;
			case Stopped:
				core.startWorker();
				break;
			case Finished:
				throw new RemoteException("already finished");
			}
			status = Operating;
			break;
		case Stopped:
			switch (status) {
			case Initialized:
				throw new RemoteException("may only stop from Operating");
			case Operating:
				core.stopWorker();
				break;
			case Finished:
				throw new RemoteException("already finished");
			}
			status = Stopped;
			break;
		case Finished:
			switch (status) {
			case Operating:
			case Stopped:
				core.killWorker();
				break;
			}
			status = Finished;
			break;
		}
	}
}
