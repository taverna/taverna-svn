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
import java.net.*;
import java.util.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.apache.log4j.Logger;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * This class, used as a singleton by the various servlets, handles
 * the business logic of creating and servicing interaction requests
 * @author Tom Oinn
 */
public class InteractionServer {
    
    private File repository;
    private Map statusMap;
    private static Logger log = Logger.getLogger(InteractionServer.class);
    private String mailHost, mailFrom;
    private URL baseURL = null;

    /**
     * Create a new InteractionServer with the specified directory
     * to use as space for indices and temporary data
     */
    public InteractionServer(File repository, String smtpHost, String mailFrom) {
	this.mailHost = mailHost;
	this.mailFrom = mailFrom;
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
		    InteractionServer.this.expireSessions();
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
     * Get the repository location for this InteractionServer
     */
    public File getRepository() {
	return this.repository;
    }

    /**
     * Get all keys for interaction jobs within this server
     */
    public Set getCurrentJobs() {
	return this.statusMap.keySet();
    }

    /**
     * Set the base URL for this interaction server, needed for
     * the construction of callback URLs in the email messages
     * sent on interaction submissions
     */
    public void setBaseURL(String stringURL) {
	try {
	    if (this.baseURL != null) {
		this.baseURL = new URL(stringURL);
		log.debug("Set base URL to '"+stringURL+"'");
	    }
	}
	catch (MalformedURLException mue) {
	    log.error("Unable to assemble base URL", mue);
	}
    }

    /**
     * Send the email invitation to interact with the specified session
     */
    public void sendEmail(InteractionState state) {
	Properties mailProps = System.getProperties();
	mailProps.put("mail.smtp.host",this.mailHost);
	try {
	    Session session = Session.getDefaultInstance(mailProps, null);
	    MimeMessage message = new MimeMessage(session);
	    message.setFrom(new InternetAddress(this.mailFrom));
	    message.addRecipient(Message.RecipientType.TO,
				 new InternetAddress(state.getEmail()));
	    message.setSubject("Taverna interaction request");
	    String body = state.getMessageBody(baseURL);
	    Transport.send(message);	    
	}
	catch (Exception ex) {
	    log.error("Cannot send invitation email", ex);
	}	
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
		sendEmail(state);
		return jobID;
	    }
	    else {
		return null;
	    }
	}
    }

    /**
     * Run the expiry test, message all sessions that have expired
     */
    public void expireSessions() {
	long currentTime = new Date().getTime();
	synchronized (statusMap) {
	    for (Iterator i = statusMap.values().iterator(); i.hasNext();) {
		InteractionState state = (InteractionState)i.next();
		if (state.getExpiry().getTime() < currentTime) {
		    state.timeout();
		}
	    }
	}	
    }

    /**
     * Remove a state from this server
     */
    public void removeSession(String id) {
	synchronized (statusMap) {
	    InteractionState state = 
		(InteractionState)statusMap.get(id);
	    if (state != null) {
		// Remove from the map
		statusMap.remove(id);
		// Destroy the on disk session
		InteractionState.destroyState(id, repository);
	    }
	}
    }

}
