/*******************************************************************************
 * Copyright (C) 2009 Ingo Wassink of University of Twente, Netherlands and
 * The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/

/**
 * @author Ingo Wassink
 * @author Ian Dunlop
 * @author Alan R Williams
 */
package net.sf.taverna.t2.activities.rshell;

import java.util.HashMap;

import org.rosuda.REngine.Rserve.RserveException;


/**
 * Class for managing connections with RServe Now, it is possible to persist a
 * connection and to keep the session
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
	 * @throws RserveException 
	 */
	public RshellConnection createConnection(RshellConnectionSettings settings) throws RserveException {

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
