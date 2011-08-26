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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

/**
 * This class, used as a singleton by the various servlets, handles the business
 * logic of creating and servicing interaction requests
 * 
 * @author Tom Oinn
 */
public class InteractionServer {

	private File repository;

	private Map statusMap;

	private static Logger log = Logger.getLogger(InteractionServer.class);

	private String mailHost, mailFrom;

	private URL baseURL = null;

	private boolean useHTML = false;

	/**
	 * Create a new InteractionServer with the specified directory to use as
	 * space for indices and temporary data
	 */
	public InteractionServer(File repository, String smtpHost, String mailFrom) {
		this.mailHost = smtpHost;
		this.mailFrom = mailFrom;
		if (repository.exists() == false) {
			log.error("Specified repository doesn't exist");
		}
		if (repository.isDirectory() == false) {
			log.error("Repository must be a directory");
		}
		this.repository = repository;
		this.statusMap = new HashMap();
		InteractionState[] states = InteractionState
				.getPreviousStates(repository);
		for (int i = 0; i < states.length; i++) {
			statusMap.put(states[i].getID(), states[i]);
		}
		// Create a new Timer to run the expiry method every minute
		Timer timer = new Timer(true);
		timer.schedule(new TimerTask() {
			public void run() {
				InteractionServer.this.expireSessions();
			}
		}, (long) 0, (long) (1000 * 60));
	}

	/**
	 * Call this to enable the sending of HTML mail including wrapping all
	 * messages in &lt;html>&lt;body>..&lt;/body>&lt;/html> tags.
	 */
	public void enableHTML() {
		this.useHTML = true;
	}

	/**
	 * Is the server sending HTML mail?
	 */
	public boolean isUsingHTML() {
		return this.useHTML;
	}

	/**
	 * Get the InteractionState object for the specified job ID
	 */
	public InteractionState getInteraction(String jobID) {
		return (InteractionState) statusMap.get(jobID);
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
	 * Set the base URL for this interaction server, needed for the construction
	 * of callback URLs in the email messages sent on interaction submissions
	 */
	public void setBaseURL(String stringURL) {
		try {
			if (this.baseURL == null) {
				log.debug("Call to set base URL to '" + stringURL + "'");
				stringURL = stringURL.replaceAll("client/.*", "");
				stringURL = stringURL.replaceAll("workflow/.*", "");
				log.debug("Stripped to '" + stringURL + "'");
				this.baseURL = new URL(stringURL);
				log.debug("Set base URL to '" + stringURL + "'");
			}
		} catch (MalformedURLException mue) {
			log.error("Unable to assemble base URL", mue);
		}
	}

	/**
	 * Send the email invitation to interact with the specified session
	 */
	public void sendEmail(InteractionState state) {
		log.debug("About to send email for '" + state.getID() + "'");
		try {
			Properties mailProps = System.getProperties();
			mailProps.put("mail.smtp.host", this.mailHost);
			Session session = Session.getDefaultInstance(mailProps, null);
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(this.mailFrom));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					state.getEmail()));
			message.setSubject("Taverna interaction request");
			String body = state.getMessageBody(baseURL);
			if (useHTML) {
				body = "<html><body>" + body + "</body></html>";
			}
			message.setText(body);
			Transport.send(message);
			log.debug("Sent successfuly");
		} catch (Throwable ex) {
			log.error("Cannot send invitation email", ex);
		}
	}

	/**
	 * Get the mailfrom address
	 */
	public String getMailFrom() {
		return this.mailFrom;
	}

	/**
	 * Get the base URL as a string
	 */
	public String getBaseURLString() {
		if (this.baseURL == null) {
			return "Not defined!";
		}
		return this.baseURL.toString();
	}

	/**
	 * Get the address of the SMTP relay
	 */
	public String getSMTPRelayAddress() {
		return this.mailHost;
	}

	/**
	 * Create a new interaction request
	 */
	public String createInteractionRequest(String metadata, FileItem data) {
		synchronized (statusMap) {
			InteractionState state = InteractionState.createInteractionState(
					repository, data, metadata);
			if (state != null) {
				String jobID = state.getID();
				statusMap.put(jobID, state);
				sendEmail(state);
				log.debug("Returning state ID '" + jobID + "'");
				return jobID;
			} else {
				log.error("State was null");
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
				InteractionState state = (InteractionState) i.next();
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
			InteractionState state = (InteractionState) statusMap.get(id);
			if (state != null) {
				// Remove from the map
				statusMap.remove(id);
				// Destroy the on disk session
				InteractionState.destroyState(id, repository);
			}
		}
	}

}
