/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.interaction.server;

import org.apache.commons.fileupload.*;
import java.io.*;
import java.util.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.apache.log4j.Logger;

/**
 * This class, used as a singleton by the various servlets, handles
 * the business logic of creating and servicing interaction requests
 * @author Tom Oinn
 */
public class InteractionServer {
    
    private File repository;
    private Map statusMap;
    private static Logger log = Logger.getLogger(InteractionServer.class);
    
    /**
     * Create a new InteractionServer with the specified directory
     * to use as space for indices and temporary data
     */
    public InteractionServer(File repository) {
	if (repository.exists() == false) {
	    log.error("Specified repository doesn't exist");
	}
	if (repository.isDirectory() == false) {
	    log.error("Repository must be a directory");
	}
	this.repository = repository;
	this.statusMap = new HashMap();
	InteractionState[] states = InteractionState.getPreviousStates(repository);
	for (int i = 0; i < states.length; i++) {
	    statusMap.put(states[i].getID(), states[i]);
	}
	// Create a new Timer to run the expiry method every minute
	Timer timer = new Timer();
	timer.schedule(new TimerTask() {
		public void run() {
		    InteractionServer.this.removeExpiredSessions();
		}
	    }, (long)0, (long)(1000 * 60));
    }
        
    /**
     * Get the InteractionState object for the specified job ID
     */
    public InteractionState getInteraction(String jobID) {
	return (InteractionState)statusMap.get(jobID);
    }
    
    /**
     * Get all keys for interaction jobs within this server
     */
    public Set getCurrentJobs() {
	return this.statusMap.keySet();
    }

    /**
     * Create a new interaction request
     */
    public String createInteractionRequest(String metadata,
					   FileItem data) {
	synchronized (statusMap) {
	    InteractionState state = 
		InteractionState.createInteractionState(repository,
							data,
							metadata);
	    if (state != null) {
		String jobID = state.getID();
		statusMap.put(jobID, state);
		return jobID;
	    }
	    else {
		return null;
	    }
	}
    }

    /**
     * Run the expiry test, remove all requests that
     * have expired
     */
    public void removeExpiredSessions() {
	long currentTime = new Date().getTime();
	List sessionIDsToRemove = new ArrayList();
	synchronized (statusMap) {
	    for (Iterator i = statusMap.values().iterator(); i.hasNext();) {
		InteractionState state = (InteractionState)i.next();
		if (state.getExpiry().getTime() < currentTime) {
		    sessionIDsToRemove.add(state.getID());
		}
	    }
	    for (Iterator i = sessionIDsToRemove.iterator(); i.hasNext();) {
		String id = (String)i.next();
		statusMap.remove(id);
		InteractionState.destroyState(id, repository); 
	    }
	}	
    }

}
