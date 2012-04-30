/*
 * Copyright (C) 2010-2011 The University of Manchester
 * 
 * See the file "LICENSE.txt" for license terms.
 */
package org.taverna.server.localworker.impl;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.exit;
import static java.lang.System.getProperty;
import static java.lang.System.out;
import static java.lang.System.setProperty;
import static java.lang.System.setSecurityManager;
import static java.rmi.registry.LocateRegistry.getRegistry;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Holder;

import org.taverna.server.localworker.remote.RemoteRunFactory;
import org.taverna.server.localworker.remote.RemoteSingleRun;
import org.taverna.server.localworker.server.UsageRecordReceiver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * The registered factory for runs, this class is responsible for constructing
 * runs that are suitable for particular users. It is also the entry point for
 * this whole process.
 * 
 * @author Donal Fellows
 * @see LocalWorker
 */
@SuppressWarnings({ "SE_BAD_FIELD", "SE_NO_SERIALVERSIONID" })
public class TavernaRunManager extends UnicastRemoteObject implements
		RemoteRunFactory {
	DocumentBuilderFactory dbf;
	TransformerFactory tf;
	String command;
	Constructor<? extends RemoteSingleRun> cons;
	Class<? extends Worker> workerClass;
	// Hacks!
	public static String interactionHost;
	public static String interactionPort;
	public static String interactionWebdavPath;
	public static String interactionFeedPath;

	/**
	 * How to get the actual workflow document from the XML document that it is
	 * contained in.
	 * 
	 * @param containerDocument
	 *            The document sent from the web interface.
	 * @return The element describing the workflow, as expected by the Taverna
	 *         command line executor.
	 */
	protected Element unwrapWorkflow(Document containerDocument) {
		return (Element) containerDocument.getDocumentElement().getFirstChild();
	}

	private static final String usage = "java -jar server.worker.jar workflowExecScript UUID";

	/**
	 * An RMI-enabled factory for runs.
	 * 
	 * @param command
	 *            What command to call to actually run a run.
	 * @param constructor
	 *            What constructor to call to instantiate the RMI server object
	 *            for the run. The constructor <i>must</i> be able to take two
	 *            strings (the execution command, and the workflow document) and
	 *            a class (the <tt>workerClass</tt> parameter, below) as
	 *            arguments.
	 * @param workerClass
	 *            What class to create to actually manufacture and manage the
	 *            connection to the workflow engine.
	 * @throws RemoteException
	 *             If anything goes wrong during creation of the instance.
	 */
	public TavernaRunManager(String command,
			Constructor<? extends RemoteSingleRun> constructor,
			Class<? extends Worker> workerClass) throws RemoteException {
		this.command = command;
		this.dbf = DocumentBuilderFactory.newInstance();
		this.dbf.setNamespaceAware(true);
		this.dbf.setCoalescing(true);
		this.tf = TransformerFactory.newInstance();
		this.cons = constructor;
		this.workerClass = workerClass;
	}

	/**
	 * Do the unwrapping of a workflow to extract the contents of the file to
	 * feed into the Taverna core.
	 * 
	 * @param workflow
	 *            The string containing the workflow to extract.
	 * @param wfid
	 *            A place to store the extracted workflow ID.
	 * @return The extracted workflow description.
	 * @throws RemoteException
	 *             If anything goes wrong.
	 */
	@SuppressWarnings("REC_CATCH_EXCEPTION")
	private String unwrapWorkflow(String workflow, Holder<String> wfid)
			throws RemoteException {
		StringReader sr = new StringReader(workflow);
		StringWriter sw = new StringWriter();
		try {
			Document doc = dbf.newDocumentBuilder().parse(new InputSource(sr));
			// Try to extract the t2flow's ID.
			NodeList nl = doc.getElementsByTagNameNS(
					"http://taverna.sf.net/2008/xml/t2flow", "dataflow");
			if (nl.getLength() > 0) {
				Node n = nl.item(0).getAttributes().getNamedItem("id");
				if (n != null)
					wfid.value = n.getTextContent();
			}
			tf.newTransformer().transform(new DOMSource(unwrapWorkflow(doc)),
					new StreamResult(sw));
			return sw.toString();
		} catch (Exception e) {
			throw new RemoteException("failed to extract contained workflow", e);
		}
	}

	@Override
	public RemoteSingleRun make(String workflow, String creator,
			UsageRecordReceiver urReceiver, UUID id) throws RemoteException {
		if (creator == null)
			throw new RemoteException("no creator");
		try {
			Holder<String> wfid = new Holder<String>("???");
			workflow = unwrapWorkflow(workflow, wfid);
			out.println("Creating run from workflow <" + wfid.value + "> for <"
					+ creator + ">");
			return cons.newInstance(command, workflow, workerClass, urReceiver,
					id);
		} catch (RemoteException e) {
			throw e;
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof RemoteException)
				throw (RemoteException) e.getTargetException();
			throw new RemoteException("unexpected exception",
					e.getTargetException());
		} catch (Exception e) {
			throw new RemoteException("bad instance construction", e);
		}
	}

	private static boolean shuttingDown;
	private static String factoryName;
	private static Registry registry;

	static synchronized void unregisterFactory() {
		if (!shuttingDown) {
			shuttingDown = true;
			try {
				if (factoryName != null && registry != null)
					registry.unbind(factoryName);
			} catch (Exception e) {
				e.printStackTrace(out);
			}
		}
	}

	@Override
	public void shutdown() {
		unregisterFactory();
		new Thread(new DelayedDeath()).start();
	}

	static class DelayedDeath implements Runnable {
		@Override
		@SuppressWarnings("DM_EXIT")
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			} finally {
				exit(0);
			}
		}
	}

	/**
	 * The name of the file (in this code's resources) that provides the default
	 * security policy that we use.
	 */
	public static final String SECURITY_POLICY_FILE = "security.policy";

	private static final String SEC_POLICY_PROP = "java.security.policy";
	private static final String UNSECURE_PROP = "taverna.suppressrestrictions.rmi";
	private static final String RMI_HOST_PROP = "java.rmi.server.hostname";

	/**
	 * @param args
	 *            The arguments from the command line invocation.
	 * @throws Exception
	 *             If we can't connect to the RMI registry, or if we can't read
	 *             the workflow, or if we can't build the worker instance, or
	 *             register it. Also if the arguments are wrong.
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 2)
			throw new Exception("wrong # args: must be \"" + usage + "\"");
		if (!getProperty(UNSECURE_PROP, "no").equals("yes")) {
			setProperty(SEC_POLICY_PROP, LocalWorker.class.getClassLoader()
					.getResource(SECURITY_POLICY_FILE).toExternalForm());
			setProperty(RMI_HOST_PROP, "127.0.0.1");
		}
		setSecurityManager(new RMISecurityManager());
		String command = args[0];
		factoryName = args[1];
		registry = getRegistry();
		registry.bind(
				factoryName,
				new TavernaRunManager(command, LocalWorker.class
						.getDeclaredConstructor(String.class, String.class,
								Class.class, UsageRecordReceiver.class,
								UUID.class), WorkerCore.class));
		getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				unregisterFactory();
			}
		});
		out.println("registered RemoteRunFactory with ID " + factoryName);
	}

	@Override
	public void setInteractionServiceDetails(String host, String port,
			String webdavPath, String feedPath) throws RemoteException {
		interactionHost = host;
		interactionPort = port;
		interactionWebdavPath = webdavPath;
		interactionFeedPath = feedPath;
	}
}
