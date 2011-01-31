package org.taverna.server.localworker.impl;

import java.io.File;
import java.util.Map;

import org.taverna.server.localworker.remote.RemoteListener;
import org.taverna.server.localworker.remote.RemoteStatus;

/**
 * The interface between the connectivity layer and the thunk to the
 * subprocesses.
 * 
 * @author Donal Fellows
 */
public interface Worker {
	/**
	 * Fire up the workflow. This causes a transition into the operating state.
	 * 
	 * @param executeWorkflowCommand
	 *            The command to run to execute the workflow.
	 * @param workflow
	 *            The workflow document to execute.
	 * @param workingDir
	 *            What directory to use as the working directory.
	 * @param inputBaclavaFile
	 *            The baclava file to use for inputs, or <tt>null</tt> to use
	 *            the other <b>input*</b> arguments' values.
	 * @param inputRealFiles
	 *            A mapping of input names to files that supply them. Note that
	 *            we assume that nothing mapped here will be mapped in
	 *            <b>inputValues</b>.
	 * @param inputValues
	 *            A mapping of input names to values to supply to them. Note
	 *            that we assume that nothing mapped here will be mapped in
	 *            <b>inputFiles</b>.
	 * @param outputBaclavaFile
	 *            What baclava file to write the output from the workflow into,
	 *            or <tt>null</tt> to have it written into the <tt>out</tt>
	 *            subdirectory.
	 * @param contextDirectory
	 *            The directory containing the keystore and truststore. May be
	 *            <tt>null</tt> if no security information is provided.
	 * @param keystorePassword
	 *            The password to the keystore and truststore. May be
	 *            <tt>null</tt> if no security information is provided.
	 * @throws Exception
	 *             If any of quite a large number of things goes wrong.
	 */
	public void initWorker(String executeWorkflowCommand, String workflow,
			File workingDir, File inputBaclavaFile,
			Map<String, File> inputRealFiles, Map<String, String> inputValues,
			File outputBaclavaFile, File contextDirectory,
			char[] keystorePassword) throws Exception;

	/**
	 * Kills off the subprocess if it exists and is alive.
	 * 
	 * @throws Exception
	 *             if anything goes badly wrong when the worker is being killed
	 *             off.
	 */
	public void killWorker() throws Exception;

	/**
	 * Move the worker out of the stopped state and back to operating.
	 * 
	 * @throws Exception
	 *             if it fails (which it always does; operation currently
	 *             unsupported).
	 */
	public void startWorker() throws Exception;

	/**
	 * Move the worker into the stopped state from the operating state.
	 * 
	 * @throws Exception
	 *             if it fails (which it always does; operation currently
	 *             unsupported).
	 */
	public void stopWorker() throws Exception;

	/**
	 * @return The status of the workflow run. Note that this can be an
	 *         expensive operation.
	 */
	public RemoteStatus getWorkerStatus();

	/**
	 * @return The listener that is registered by default, in addition to all
	 *         those that are explicitly registered by the user.
	 */
	public RemoteListener getDefaultListener();
}
