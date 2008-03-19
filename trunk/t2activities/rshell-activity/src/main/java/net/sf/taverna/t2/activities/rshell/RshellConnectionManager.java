/*
 * CVS
 * $Author: davidwithers $
 * $Date: 2008-03-19 15:48:11 $
 * $Revision: 1.1 $
 * University of Twente, Human Media Interaction Group
 */
package net.sf.taverna.t2.activities.rshell;

import java.util.HashMap;

//import org.embl.ebi.escience.scufl.enactor.WorkflowEventAdapter;
//import org.embl.ebi.escience.scufl.enactor.event.WorkflowCompletionEvent;
//import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;
//import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowEventDispatcher;
import org.rosuda.JRclient.RSrvException;

/**
 * Class for managing connections with RServe Now, it is possible to persist a
 * connection and to keep the session
 * 
 * @author Ingo Wassink
 * 
 */
public class RshellConnectionManager /*extends WorkflowEventAdapter*/ {
	public static final RshellConnectionManager INSTANCE = new RshellConnectionManager();

	private HashMap<RshellConnectionSettings, RshellConnection> settingsToConnectionsMap;

//	private int numberOfWorkflowInstances;

	private RshellConnectionManager() {
		settingsToConnectionsMap = new HashMap<RshellConnectionSettings, RshellConnection>();
//		numberOfWorkflowInstances = 0;
	}

//	/**
//	 * 
//	 */
//	public void workflowCreated(WorkflowCreationEvent e) {
//		numberOfWorkflowInstances++;
//	}
//
//	/**
//	 * Method which is called when a workflow is completed
//	 */
//	public void workflowCompleted(WorkflowCompletionEvent e) {
//		assert (numberOfWorkflowInstances > 0);
//
//		if (--numberOfWorkflowInstances == 0)
//			releaseConnections();
//	}

	/**
	 * Method for creating a new r shell connection
	 * 
	 * @param settings
	 *            the connection settings
	 * @return the connection
	 */
	public RshellConnection createConnection(RshellConnectionSettings settings)
			throws RSrvException {

		RshellConnection connection;
		if (settings.isKeepSessionAlive()) {
			connection = settingsToConnectionsMap.get(settings);
			if (connection == null) {
				connection = new RshellConnection(settings);
				settingsToConnectionsMap.put(settings, connection);
			}
		} else {
			connection = new RshellConnection(settings);
		}
		return connection;
	}

	/**
	 * Method for releasing the connection
	 * 
	 * @param connection
	 *            the connection to be released
	 */
	public void releaseConnection(RshellConnection connection) {
		if (!connection.isKeepSessionAlive())
			connection.close();
	}

	/**
	 * Method for removing all connections
	 */
	public void releaseConnections() {
		for (RshellConnection connection : settingsToConnectionsMap.values()) {
			connection.close();
		}
		settingsToConnectionsMap.clear();
	}

//	/**
//	 * Initialize static instance
//	 */
//	static {
//		WorkflowEventDispatcher.DISPATCHER.addListener(INSTANCE);
//	}
}
