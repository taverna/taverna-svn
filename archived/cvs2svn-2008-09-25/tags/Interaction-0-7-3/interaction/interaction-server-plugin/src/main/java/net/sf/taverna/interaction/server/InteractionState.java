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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Represents the persistant state of a single interaction job
 * 
 * @author Tom Oinn
 */
public class InteractionState {

	private String jobID;

	private List unsentEvents = new ArrayList();

	private File repository = null;

	private static Logger log = Logger.getLogger(InteractionState.class);

	private Date expiry;

	private String email;

	private ServerInteractionPattern pattern;

	private int currentState = InteractionState.WAITING;

	public static int REJECTED = 0;

	public static int COMPLETED = 1;

	public static int FAILED = 2;

	public static int TIMEOUT = 3;

	public static int WAITING = 4;

	/**
	 * Scan the specified directory for the state files created by interaction
	 * state objects and recreate the list of prior states
	 */
	public static InteractionState[] getPreviousStates(File repository) {
		if (repository.exists() == false) {
			log.warn("Repository at " + repository.toString() + " not found");
			return new InteractionState[0];
		}
		File[] entries = repository.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return (pathname.getName().endsWith("-metadata.xml"));
			}
		});
		log.debug("Found " + entries.length + " saved interaction states in "
				+ repository.toString());
		InteractionState[] result = new InteractionState[entries.length];
		for (int i = 0; i < entries.length; i++) {
			String jobID = entries[i].getName().replaceAll("-metadata\\.xml",
					"");
			log.debug("  " + jobID);
			InteractionState state = new InteractionState(jobID, repository);
			state.fetchEventState();
			result[i] = state;
		}
		return result;
	}

	/**
	 * Given a repository use the supplied jobID to resurrect this
	 * InteractionState
	 */
	private InteractionState(String jobID, File repository) {
		this.jobID = jobID;
		this.repository = repository;
		// Read a JDOM document from the metadata file to get the
		// various other properties such as expiry, email and
		// build the interaction pattern from the interaction SPI
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder(false);
			File metadataFile = new File(repository, jobID + "-metadata.xml");
			doc = builder.build(new FileInputStream(metadataFile));
		} catch (IOException ioe) {
			log.error("Failed to read metadata file for '" + jobID + "'");
		} catch (JDOMException jde) {
			log.error("Failed to parse metadata file for '" + jobID + "'");
		}
		if (doc != null) {
			// Expect something like
			// <request>
			// <to email="..."/>
			// <pattern name="..."/>
			// <expires date="..."/>
			// </request>
			Element request = doc.getRootElement();
			email = request.getChild("to").getAttributeValue("email");
			expiry = new Date(Long.parseLong(request.getChild("expires")
					.getAttributeValue("date")));
			String patternName = request.getChild("pattern").getAttributeValue(
					"name");
			pattern = PatternRegistry.patternForName(patternName);
		}
	}

	/**
	 * Consume the supplied FileItem containing submitted data and String with
	 * request metadata and generate a new InteractionState object backed by the
	 * specified repository
	 * <p>
	 * Implementation writes the supplied data and metadata to disk then
	 * delegates to the private constructor to actually create the new object
	 */
	public static InteractionState createInteractionState(File repository,
			FileItem data, String metadata) {
		String jobID = createID();
		try {
			// Write input data to a file
			File inputDataFile = new File(repository, jobID + "-input.xml");
			inputDataFile.createNewFile();
			data.write(inputDataFile);
			// Write metadata file
			File metadataFile = new File(repository, jobID + "-metadata.xml");
			metadataFile.createNewFile();
			PrintWriter out = new PrintWriter(new FileWriter(metadataFile));
			out.println(metadata);
			out.flush();
			out.close();
			return new InteractionState(jobID, repository);
		} catch (IOException ioe) {
			log.error("IOException when trying to create state object", ioe);
		} catch (Exception e) {
			log.error("Some other problem occured when creating the state", e);
		}
		return null;
	}

	private static int count = 0;

	private static String createID() {
		return new Date().getTime() + "-" + (count++);
	}

	/**
	 * Get the state of this interaction
	 */
	public int getState() {
		return this.currentState;
	}

	/**
	 * Remove all files for the given Job ID
	 */
	public static void destroyState(String theJobID, File repository) {
		final String jobID = theJobID;
		File[] entries = repository.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return (pathname.getName().startsWith(jobID));
			}
		});
		for (int i = 0; i < entries.length; i++) {
			entries[i].delete();
		}
	}

	/**
	 * Return the Job ID for this InteractionState
	 */
	public String getID() {
		return this.jobID;
	}

	/**
	 * Return the expiry time of this InteractionState
	 */
	public Date getExpiry() {
		return this.expiry;
	}

	/**
	 * Return the target email to interact with
	 */
	public String getEmail() {
		return this.email;
	}

	/**
	 * Create the body of an invitation email, use the specified URL as the base
	 * of this interaction service installation in case we need to generate
	 * links back to the server within the mail
	 */
	public String getMessageBody(URL baseURL) {
		return this.pattern.getMessageBody(baseURL, this);
	}

	/**
	 * Return the interaction request defining this interaction state -
	 * particularly useful as it can determine whether we should be attempting
	 * to handle output from the interaction
	 */
	public ServerInteractionPattern getInteractionPattern() {
		return this.pattern;
	}

	/**
	 * Get a file handle to which results should be written, delete the file if
	 * it exists.
	 */
	public File getResultsFile() throws IOException {
		File resultsFile = new File(repository, jobID + "-results.xml");
		if (resultsFile.exists() == false) {
			resultsFile.createNewFile();
		} else {
			resultsFile.delete();
			resultsFile.createNewFile();
		}
		return resultsFile;
	}

	/**
	 * Complete this interaction
	 */
	public void complete() {
		currentState = COMPLETED;
		addEvent("completed");
	}

	/**
	 * Fail this interaction
	 */
	public void fail() {
		currentState = FAILED;
		addEvent("failure");
	}

	/**
	 * User rejected interaction
	 */
	public void reject() {
		currentState = REJECTED;
		addEvent("rejected");
	}

	/**
	 * Interaction timed out
	 */
	public void timeout() {
		currentState = TIMEOUT;
		addEvent("timeout");
	}

	/**
	 * Get an in memory JDOM document representation of the data for this
	 * request
	 * 
	 * @return the document, or null if an error occurs
	 */
	public Document getInputDocument() {
		try {
			SAXBuilder builder = new SAXBuilder();
			File dataFile = new File(repository, jobID + "-input.xml");
			return builder.build(new FileInputStream(dataFile));
		} catch (Exception ex) {
			log.error("Unable to fetch data document for request '" + getID()
					+ "'");
			return null;
		}
	}

	/**
	 * Return a JDOM document containing all the unsent events
	 * 
	 * @param clear
	 *            If true then remove all items from the list after returning
	 *            it, if false then leave the current state intact.
	 */
	public Document getUnsentEvents(boolean clear) {
		synchronized (unsentEvents) {
			Element rootElement = new Element("events");
			for (Iterator i = unsentEvents.iterator(); i.hasNext();) {
				String eventString = (String) i.next();
				rootElement.addContent(new Element(eventString));
			}
			if (clear) {
				// Clear in memory representation
				unsentEvents.clear();
				// Write empty event file
				writeEventState();
			}
			return new Document(rootElement);
		}
	}

	/**
	 * Push an event onto the queue and write the state to disk
	 */
	private void addEvent(Object o) {
		synchronized (unsentEvents) {
			unsentEvents.add(o);
			writeEventState();
		}
	}

	/**
	 * Write the event state to disk
	 */
	private void writeEventState() {
		synchronized (unsentEvents) {
			try {
				XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
				String currentEvents = xo.outputString(getUnsentEvents(false));
				File eventFile = new File(repository, jobID + "-events.xml");
				if (eventFile.createNewFile() == false) {
					// Destroy the old file if it existed - not entirely safe!
					// Should write to a new file then swap the original and the
					// new
					eventFile.delete();
					eventFile.createNewFile();
				}
				PrintWriter out = new PrintWriter(new FileWriter(eventFile));
				out.println(currentEvents);
				out.flush();
				out.close();
			} catch (IOException ioe) {
				log.error("Unable to write event file", ioe);
			}
		}
	}

	/**
	 * Fetch the event state from disk
	 */
	private void fetchEventState() {
		try {
			synchronized (unsentEvents) {
				unsentEvents.clear();
				File eventFile = new File(repository, jobID + "-events.xml");
				if (eventFile.exists() == false) {
					log.warn("No event file found for '" + jobID + "'");
					return;
				}
				SAXBuilder builder = new SAXBuilder(false);
				Document eventDoc = builder
						.build(new FileInputStream(eventFile));
				for (Iterator i = eventDoc.getRootElement().getChildren()
						.iterator(); i.hasNext();) {
					Element e = (Element) i.next();
					unsentEvents.add(e.getName());
					if (e.getName().equals("timeout")) {
						currentState = TIMEOUT;
					} else if (e.getName().equals("rejected")) {
						currentState = REJECTED;
					} else if (e.getName().equals("failure")) {
						currentState = FAILED;
					} else if (e.getName().equals("completed")) {
						currentState = COMPLETED;
					}
				}
			}
		} catch (IOException ioe) {
			log.error("IO Exception when attempting to read event file for '"
					+ jobID + "'", ioe);
		} catch (JDOMException jde) {
			log.error("JDOM Exception when parsing event file for '" + jobID
					+ "'", jde);
		}
	}
}
