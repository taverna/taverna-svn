/*
 * Copyright (C) 2010-2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.master.localworker;

import static java.io.File.separator;
import static java.lang.System.getProperty;
import static java.rmi.registry.Registry.REGISTRY_PORT;
import static org.taverna.server.master.localworker.LocalWorkerManagementState.KEY;
import static org.taverna.server.master.localworker.LocalWorkerManagementState.makeInstance;

import javax.annotation.PostConstruct;
import javax.jdo.annotations.PersistenceAware;

import org.springframework.beans.factory.annotation.Required;
import org.taverna.server.master.common.Status;
import org.taverna.server.master.utils.JDOSupport;

/**
 * The persistent state of a local worker factory.
 * 
 * @author Donal Fellows
 */
@PersistenceAware
public class LocalWorkerState extends JDOSupport<LocalWorkerManagementState> {
	public LocalWorkerState() {
		super(LocalWorkerManagementState.class);
	}

	private LocalWorkerState self;

	@Required
	public void setSelf(LocalWorkerState self) {
		this.self = self;
	}

	/**
	 * The name of the resource that is the implementation of the subprocess
	 * that this class will fork off.
	 */
	public static final String SERVER_WORKER_IMPLEMENTATION_JAR = "util/server.worker.jar";

	/**
	 * The name of the resource that is the implementation of the subprocess
	 * that manages secure forking.
	 */
	public static final String SECURE_FORK_IMPLEMENTATION_JAR = "util/secure.fork.jar";

	/** Initial lifetime of runs, in minutes. */
	int defaultLifetime;
	private static final int DEFAULT_DEFAULT_LIFE = 20;
	/**
	 * Maximum number of runs to exist at once. Note that this includes when
	 * they are just existing for the purposes of file transfer (
	 * {@link Status#Initialized}/{@link Status#Finished} states).
	 */
	int maxRuns;
	private static final int DEFAULT_MAX = 5;
	/**
	 * Prefix to use for RMI names.
	 */
	String factoryProcessNamePrefix;
	private static final String DEFAULT_PREFIX = "ForkRunFactory.";
	/**
	 * Full path name of the script used to start running a workflow; normally
	 * expected to be "<i>somewhere/</i><tt>executeWorkflow.sh</tt>".
	 */
	String executeWorkflowScript;
	/** Default value for {@link #executeWorkflowScript}. */
	private transient String defaultExecuteWorkflowScript;
	/**
	 * Full path name of the file containing the password used to launch workers
	 * as other users. The file is normally expected to contain a single line,
	 * the password, and to be thoroughly locked down so only the user running
	 * the server (e.g., "<tt>tomcat</tt>") can read it; it will probably reside
	 * in either the user's home directory or in a system configuration
	 * directory.
	 */
	String passwordFile;
	/** Default value for {@link #passwordFile}. */
	private transient String defaultPasswordFile;
	/**
	 * The extra arguments to pass to the subprocess.
	 */
	String[] extraArgs;
	private static final String[] DEFAULT_EXTRA_ARGS = new String[0];
	/**
	 * How long to wait for subprocess startup, in seconds.
	 */
	int waitSeconds;
	private static final int DEFAULT_WAIT = 40;
	/**
	 * Polling interval to use during startup, in milliseconds.
	 */
	int sleepMS;
	private static final int DEFAULT_SLEEP = 1000;
	/**
	 * Full path name to the worker process's implementation JAR.
	 */
	String serverWorkerJar;
	private static final String DEFAULT_WORKER_JAR = LocalWorkerState.class
			.getClassLoader().getResource(SERVER_WORKER_IMPLEMENTATION_JAR)
			.getFile();
	/**
	 * Full path name to the Java binary to use to run the subprocess.
	 */
	String javaBinary;
	private static final String DEFAULT_JAVA_BINARY = getProperty("java.home")
			+ separator + "bin" + separator + "java";
	/**
	 * Full path name to the secure fork process's implementation JAR.
	 */
	String serverForkerJar;
	private static final String DEFAULT_FORKER_JAR = LocalWorkerState.class
			.getClassLoader().getResource(SECURE_FORK_IMPLEMENTATION_JAR)
			.getFile();

	String registryHost;
	int registryPort;

	/**
	 * @param defaultLifetime
	 *            how long a workflow run should live by default, in minutes.
	 */
	public void setDefaultLifetime(int defaultLifetime) {
		this.defaultLifetime = defaultLifetime;
		if (loadedState)
			self.store();
	}

	/**
	 * @return how long a workflow run should live by default, in minutes.
	 */
	public int getDefaultLifetime() {
		return defaultLifetime < 1 ? DEFAULT_DEFAULT_LIFE : defaultLifetime;
	}

	/**
	 * @param maxRuns
	 *            the maxRuns to set
	 */
	public void setMaxRuns(int maxRuns) {
		this.maxRuns = maxRuns;
		if (loadedState)
			self.store();
	}

	/**
	 * @return the maxRuns
	 */
	public int getMaxRuns() {
		return maxRuns < 1 ? DEFAULT_MAX : maxRuns;
	}

	/**
	 * @param factoryProcessNamePrefix
	 *            the factoryProcessNamePrefix to set
	 */
	public void setFactoryProcessNamePrefix(String factoryProcessNamePrefix) {
		this.factoryProcessNamePrefix = factoryProcessNamePrefix;
		if (loadedState)
			self.store();
	}

	/**
	 * @return the factoryProcessNamePrefix
	 */
	public String getFactoryProcessNamePrefix() {
		return factoryProcessNamePrefix == null ? DEFAULT_PREFIX
				: factoryProcessNamePrefix;
	}

	/**
	 * @param executeWorkflowScript
	 *            the executeWorkflowScript to set
	 */
	public void setExecuteWorkflowScript(String executeWorkflowScript) {
		this.executeWorkflowScript = executeWorkflowScript;
		if (loadedState)
			self.store();
	}

	/**
	 * @return the executeWorkflowScript
	 */
	public String getExecuteWorkflowScript() {
		return executeWorkflowScript == null ? defaultExecuteWorkflowScript
				: executeWorkflowScript;
	}

	void setDefaultExecuteWorkflowScript(String defaultExecuteWorkflowScript) {
		this.defaultExecuteWorkflowScript = defaultExecuteWorkflowScript;
	}

	String getDefaultExecuteWorkflowScript() {
		return defaultExecuteWorkflowScript;
	}

	/**
	 * @param extraArgs
	 *            the extraArgs to set
	 */
	public void setExtraArgs(String[] extraArgs) {
		this.extraArgs = extraArgs.clone();
		if (loadedState)
			self.store();
	}

	/**
	 * @return the extraArgs
	 */
	public String[] getExtraArgs() {
		return extraArgs == null ? DEFAULT_EXTRA_ARGS : extraArgs.clone();
	}

	/**
	 * @param waitSeconds
	 *            the waitSeconds to set
	 */
	public void setWaitSeconds(int waitSeconds) {
		this.waitSeconds = waitSeconds;
		if (loadedState)
			self.store();
	}

	/**
	 * @return the waitSeconds
	 */
	public int getWaitSeconds() {
		return waitSeconds < 1 ? DEFAULT_WAIT : waitSeconds;
	}

	/**
	 * @param sleepMS
	 *            the sleepMS to set
	 */
	public void setSleepMS(int sleepMS) {
		this.sleepMS = sleepMS;
		if (loadedState)
			self.store();
	}

	/**
	 * @return the sleepMS
	 */
	public int getSleepMS() {
		return sleepMS < 1 ? DEFAULT_SLEEP : sleepMS;
	}

	/**
	 * @param serverWorkerJar
	 *            the serverWorkerJar to set
	 */
	public void setServerWorkerJar(String serverWorkerJar) {
		this.serverWorkerJar = serverWorkerJar;
		if (loadedState)
			self.store();
	}

	/**
	 * @return the serverWorkerJar
	 */
	public String getServerWorkerJar() {
		return serverWorkerJar == null ? DEFAULT_WORKER_JAR : serverWorkerJar;
	}

	/**
	 * @param serverForkerJar
	 *            the serverForkerJar to set
	 */
	public void setServerForkerJar(String serverForkerJar) {
		this.serverForkerJar = serverForkerJar;
		if (loadedState)
			self.store();
	}

	/**
	 * @return the serverForkerJar
	 */
	public String getServerForkerJar() {
		return serverForkerJar == null ? DEFAULT_FORKER_JAR : serverForkerJar;
	}

	/**
	 * @param javaBinary
	 *            the javaBinary to set
	 */
	public void setJavaBinary(String javaBinary) {
		this.javaBinary = javaBinary;
		if (loadedState)
			self.store();
	}

	/**
	 * @return the javaBinary
	 */
	public String getJavaBinary() {
		return javaBinary == null ? DEFAULT_JAVA_BINARY : javaBinary;
	}

	/**
	 * @param passwordFile
	 *            the passwordFile to set
	 */
	public void setPasswordFile(String passwordFile) {
		this.passwordFile = passwordFile;
		if (loadedState)
			self.store();
	}

	/**
	 * @return the passwordFile
	 */
	public String getPasswordFile() {
		return passwordFile == null ? defaultPasswordFile : passwordFile;
	}

	void setDefaultPasswordFile(String defaultPasswordFile) {
		this.defaultPasswordFile = defaultPasswordFile;
	}

	/**
	 * @param registryHost
	 *            the registryHost to set
	 */
	public void setRegistryHost(String registryHost) {
		this.registryHost = (registryHost == null ? "" : registryHost);
		if (loadedState)
			self.store();
	}

	/**
	 * @return the registryHost
	 */
	public String getRegistryHost() {
		return registryHost.isEmpty() ? null : registryHost;
	}

	/**
	 * @param registryPort
	 *            the registryPort to set
	 */
	public void setRegistryPort(int registryPort) {
		this.registryPort = ((registryPort < 1 || registryPort > 65534) ? REGISTRY_PORT
				: registryPort);
		if (loadedState)
			self.store();
	}

	/**
	 * @return the registryPort
	 */
	public int getRegistryPort() {
		return registryPort == 0 ? REGISTRY_PORT : registryPort;
	}

	// --------------------------------------------------------------

	private boolean loadedState;

	@PostConstruct
	@WithinSingleTransaction
	public void load() {
		if (loadedState || !isPersistent())
			return;
		LocalWorkerManagementState state = getById(KEY);
		if (state == null) {
			store();
			return;
		}

		defaultLifetime = state.getDefaultLifetime();
		executeWorkflowScript = state.getExecuteWorkflowScript();
		extraArgs = state.getExtraArgs();
		factoryProcessNamePrefix = state.getFactoryProcessNamePrefix();
		javaBinary = state.getJavaBinary();
		maxRuns = state.getMaxRuns();
		serverWorkerJar = state.getServerWorkerJar();
		serverForkerJar = state.getServerForkerJar();
		passwordFile = state.getPasswordFile();
		sleepMS = state.getSleepMS();
		waitSeconds = state.getWaitSeconds();
		registryHost = state.getRegistryHost();
		registryPort = state.getRegistryPort();

		loadedState = true;
	}

	@WithinSingleTransaction
	public void store() {
		if (!isPersistent())
			return;
		LocalWorkerManagementState state = getById(KEY);
		if (state == null) {
			state = persist(makeInstance());
		}

		state.setDefaultLifetime(defaultLifetime);
		state.setExecuteWorkflowScript(executeWorkflowScript);
		state.setExtraArgs(extraArgs);
		state.setFactoryProcessNamePrefix(factoryProcessNamePrefix);
		state.setJavaBinary(javaBinary);
		state.setMaxRuns(maxRuns);
		state.setServerWorkerJar(serverWorkerJar);
		state.setServerForkerJar(serverForkerJar);
		state.setPasswordFile(passwordFile);
		state.setSleepMS(sleepMS);
		state.setWaitSeconds(waitSeconds);
		state.setRegistryHost(registryHost);
		state.setRegistryPort(registryPort);

		loadedState = true;
	}
}
