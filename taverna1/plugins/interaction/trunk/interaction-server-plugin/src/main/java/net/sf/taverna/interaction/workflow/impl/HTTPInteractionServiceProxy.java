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

package net.sf.taverna.interaction.workflow.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.taverna.interaction.workflow.InteractionEvent;
import net.sf.taverna.interaction.workflow.InteractionPattern;
import net.sf.taverna.interaction.workflow.InteractionReceipt;
import net.sf.taverna.interaction.workflow.InteractionRequest;
import net.sf.taverna.interaction.workflow.InteractionService;
import net.sf.taverna.interaction.workflow.InteractionStateListener;
import net.sf.taverna.interaction.workflow.InteractionStatus;
import net.sf.taverna.interaction.workflow.SubmissionException;
import net.sf.taverna.interaction.workflow.TerminalInteractionStatus;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Implementation of InteractionService based on a remote set of Servlets using
 * the HTTP transport.
 * <p>
 * This client proxy assumes that there is the following structure beneath the
 * specified base URL :
 * <ul>
 * <li>/patterns.xml - <em>XML file containing all the patterns this
 * interaction server supports</em></li>
 * <li>/workflow/submit - <em>Submission handler servlet to which interaction
 * request data and metadata can be POSTed to in multipart form</em></li>
 * <li>/workflow/status - <em>Status handler servlet used to fetch interaction
 * request status for a previously submitted request</em></li>
 * <li>/workflow/results - <em>Result handler capable of streaming a serialized
 * Map of DataThing objects back to the proxy</em></li>
 * </ul>
 * 
 * @author Tom Oinn
 */
public class HTTPInteractionServiceProxy implements InteractionService {

	static Logger log = Logger.getLogger(HTTPInteractionServiceProxy.class);

	static Map proxyCache = new HashMap();

	static HttpClient client;

	private InteractionPattern[] patterns = null;

	private URL baseURL = null;

	/**
	 * Set up the HttpClient singleton
	 */
	static {
		client = new HttpClient();
		client.getHttpConnectionManager().getParams()
				.setConnectionTimeout(5000);
	}

	/**
	 * Get a connection to the interaction service at the specified URL
	 */
	public static InteractionService connectTo(URL targetURL) {
		synchronized (proxyCache) {
			if (proxyCache.containsKey(targetURL)) {
				return (InteractionService) proxyCache.get(targetURL);
			} else {
				InteractionService i = new HTTPInteractionServiceProxy(
						targetURL);
				proxyCache.put(targetURL, i);
				return i;
			}
		}
	}

	/**
	 * Private constructor, this class should never be directly constructed, use
	 * the 'connectTo' static method instead
	 */
	private HTTPInteractionServiceProxy(URL targetURL) {
		baseURL = targetURL;
	}

	/**
	 * Get the XML document from the server describing all possible interaction
	 * patterns, parse it and create InteractionPattern implementations for each
	 * pattern
	 */
	public InteractionPattern[] getInteractionPatterns() {
		if (this.patterns != null) {
			return this.patterns;
		} else {
			List patternElements = null;
			try {
				URL metadataURL = new URL(baseURL, "patterns.xml");
				InputStream is = metadataURL.openStream();
				SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(is);
				patternElements = doc.getRootElement().getChildren();
			} catch (MalformedURLException mue) {
				log.error(mue);
			} catch (JDOMException jde) {
				log.error(jde);
			} catch (IOException ioe) {
				log.error(ioe);
			}
			// Fail silently, return no interaction patterns if anything
			// goes wrong. Arguably this isn't the best way to cope here..
			if (patternElements == null) {
				return new InteractionPattern[0];
			}
			List patternObjects = new ArrayList();
			for (Iterator i = patternElements.iterator(); i.hasNext();) {
				try {
					patternObjects.add(new XMLBasedInteractionPattern(
							(Element) i.next()));
				} catch (Exception ex) {
					//
				}
			}
			this.patterns = (InteractionPattern[]) patternObjects
					.toArray(new InteractionPattern[0]);
			return this.patterns;
		}
	}

	/**
	 * Submit the specified request to this Interaction Service proxy, returning
	 * an InteractionReceipt that can be used to access ongoing events and
	 * results from the interaction process
	 */
	public InteractionReceipt submitRequest(InteractionRequest request)
			throws SubmissionException {
		try {
			// URL to submit the request to by POST
			URL submitURL = new URL(baseURL, "workflow/submit");

			// Build the request metadata document
			XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
			String requestMetadata = xo
					.outputString(elementForRequest(request));

			// Post data to the URL
			PostMethod post = new PostMethod(submitURL.toString());

			// Use two parts, a metadata part containing the request email,
			// pattern
			// and other properties and a data part containing the serialized
			// Map
			// of DataThing objects extracted from the InteractionRequest
			// object.
			Part[] parts = {
					new StringPart("metadata", requestMetadata),
					new FilePart("data", new ByteArrayPartSource("data",
							request.getData())) };
			post.setRequestEntity(new MultipartRequestEntity(parts, post
					.getParams()));
			int status = client.executeMethod(post);
			if (status == HttpStatus.SC_OK) {
				log.info("Submitted request...");
			} else {
				SubmissionException se = new SubmissionException();
				se.setMessage("Upload failed, response was "
						+ HttpStatus.getStatusText(status));
				throw se;
			}

			// Get the response, extract the job ID from it and create a new
			// implementation of the InteractionReceipt
			Document responseDoc;
			try {
				SAXBuilder sb = new SAXBuilder();
				responseDoc = sb.build(post.getResponseBodyAsStream());
			} catch (IOException ioe) {
				// Failed to fetch the IO stream contents, could potentially
				// be caused by network issues I guess or somesuch inbetween
				// getting the reponse code and this point
				SubmissionException se = new SubmissionException();
				se.setMessage("Error reading response stream "
						+ ioe.getMessage());
				se.initCause(ioe);
				throw se;
			} catch (JDOMException jde) {
				// Failed to parse the document, probably because this isn't
				// a valid XML response. Should never happen in a production
				// environment but you never know, safest to catch it in case
				SubmissionException se = new SubmissionException();
				se.setMessage("Unable to build JDOM Document from stream "
						+ jde.getMessage());
				se.initCause(jde);
				throw se;
			} finally {
				// Release the HTTP connection whether it worked or not
				post.releaseConnection();
			}
			return new HTTPPollingInteractionReceipt(responseDoc, request);
		} catch (Exception ex) {
			if (ex instanceof SubmissionException) {
				throw (SubmissionException) ex;
			}
			SubmissionException se = new SubmissionException();
			se.setMessage("Failed to submit interaction request "
					+ ex.getMessage());
			se.initCause(ex);
			throw se;
		}
	}

	/**
	 * Get all InteractionStatus objects from the status service for the
	 * specified ID - removes such events from the server in the process.
	 */
	InteractionStatus[] getNewEventsForID(String requestID) {
		PostMethod post = null;
		try {
			URL statusURL = new URL(baseURL, "workflow/status");
			post = new PostMethod(statusURL.toString());
			Part[] parts = { new StringPart("id", requestID) };
			post.setRequestEntity(new MultipartRequestEntity(parts, post
					.getParams()));
			int status = client.executeMethod(post);
			if (status == HttpStatus.SC_OK) {
				log.info("requested status...");
			} else {
				throw new Exception("Unable to fetch status, error was "
						+ HttpStatus.getStatusText(status));
			}
			// Expect an XML document containing elements for each event under
			// the top level element, if we can't parse the document for some
			// reason then complain bitterly
			SAXBuilder sb = new SAXBuilder();
			Document responseDoc = sb.build(post.getResponseBodyAsStream());
			List eventElementList = responseDoc.getRootElement().getChildren();
			List eventObjectList = new ArrayList();
			for (Iterator i = eventElementList.iterator(); i.hasNext();) {
				// TODO - create InteractionStatus objects here
				Element eventElement = (Element) i.next();
				if (eventElement.getName().equals("timeout")) {
					eventObjectList.add(new InteractionTimedOutEvent());
				} else if (eventElement.getName().equals("failure")) {
					eventObjectList.add(new InteractionFailedEvent());
				} else if (eventElement.getName().equals("rejected")) {
					eventObjectList.add(new InteractionRejectedEvent());
				} else if (eventElement.getName().equals("completed")) {
					try {
						Map result = getResultObject(requestID);
						if (result != null) {
							eventObjectList.add(new InteractionCompletionEvent(
									result));
						} else {
							eventObjectList.add(new InteractionFailedEvent());
						}
					} catch (Exception ex) {
						eventObjectList.add(new InteractionFailedEvent());
					}
				}
			}
			return (InteractionStatus[]) eventObjectList
					.toArray(new InteractionStatus[0]);
		} catch (Exception ex) {
			log.error("Failed to get new events", ex);
			return new InteractionStatus[] { new InteractionFailedEvent() };
		} finally {
			// Release the HTTP connection whether it worked or not
			if (post != null) {
				post.releaseConnection();
			}
		}
	}

	/**
	 * Connect to the results servlet and get the XML document containing any
	 * results from the interaction process.
	 */
	Map getResultObject(String requestID) {
		PostMethod post = null;
		try {
			URL resultsURL = new URL(baseURL, "workflow/results");
			post = new PostMethod(resultsURL.toString());
			Part[] parts = { new StringPart("id", requestID) };
			post.setRequestEntity(new MultipartRequestEntity(parts, post
					.getParams()));
			int status = client.executeMethod(post);
			if (status == HttpStatus.SC_OK) {
				log.info("fetched results okay...");
			} else {
				throw new Exception("Unable to fetch results, error was "
						+ HttpStatus.getStatusText(status));
			}
			// Construct a JDOM Document from the stream
			SAXBuilder sb = new SAXBuilder();
			Document responseDoc = sb.build(post.getResponseBodyAsStream());
			// Use the DataThingFactory to build a Map of DataThing objects
			return DataThingXMLFactory.parseDataDocument(responseDoc);
		} catch (Exception ex) {
			log.error("Error fetching results", ex);
			return null;
		} finally {
			if (post != null) {
				post.releaseConnection();
			}
		}
	}

	/**
	 * Return the hostname that this class is proxying
	 */
	public String getHostName() {
		return this.baseURL.getHost();
	}

	public static Element elementForRequest(InteractionRequest r) {
		Element requestElement = new Element("request");
		Element patternElement = new Element("pattern");
		patternElement.setAttribute("name", r.getPattern().getName());
		Element emailElement = new Element("to");
		emailElement.setAttribute("email", r.getEmail());
		Element expiryElement = new Element("expires");
		expiryElement.setAttribute("date", Long.toString(r.getExpiryTime()
				.getTime()));
		requestElement.addContent(patternElement);
		requestElement.addContent(emailElement);
		requestElement.addContent(expiryElement);
		return requestElement;
	}

	/**
	 * Implementation of InteractionReceipt that polls the HTTP server for
	 * status messages via the getStatusForID method in the enclosing class and
	 * pushes any responses out to all listeners
	 */
	class HTTPPollingInteractionReceipt implements InteractionReceipt {

		private List statusChangeMessages = new ArrayList();

		private Set listeners = new HashSet();

		private InteractionStatus currentStatus;

		private InteractionRequest interactionRequest;

		private String requestID;

		/**
		 * Create a new Receipt based on the JDOM Document parsed from the job
		 * submission response from the HTTP server
		 */
		public HTTPPollingInteractionReceipt(Document receiptDoc,
				InteractionRequest request) {
			this.interactionRequest = request;
			// Assume a response document like :
			// <jobID>foo</jobID>
			// Actually we just extract the text content of the
			// top level element here to get the job ID but hey.
			this.requestID = receiptDoc.getRootElement().getTextTrim();
			statusChangeMessages.add(new RequestSubmittedEvent());

			// Create a new TimerTask to poll the status service via
			// the HTTPInteractionServiceProxy
			final String requestID = this.requestID;
			final Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run() {
					InteractionStatus[] statii = HTTPInteractionServiceProxy.this
							.getNewEventsForID(requestID);
					for (int i = 0; i < statii.length; i++) {
						pushMessage(statii[i]);
					}
					if (statii.length > 0
							&& statii[statii.length - 1] instanceof TerminalInteractionStatus) {
						// Cancel this task if the last event is a terminal one
						timer.cancel();
					}
				}
			}, (long) 0, (long) (1000 * 30));
		}

		/**
		 * Push a new message onto the queue and send it to all interested
		 * listeners
		 */
		void pushMessage(InteractionStatus message) {
			synchronized (statusChangeMessages) {
				statusChangeMessages.add(message);
				synchronized (listeners) {
					for (Iterator i = listeners.iterator(); i.hasNext();) {
						InteractionStateListener listener = (InteractionStateListener) i
								.next();
						sendMessage(message, listener);
					}
				}
			}
		}

		/**
		 * Send all messages on the history list to the specified client, used
		 * when new listeners join so that all listeners have a full replay of
		 * the message history
		 */
		void sendAllMessagesToClient(InteractionStateListener client) {
			synchronized (statusChangeMessages) {
				for (Iterator i = statusChangeMessages.iterator(); i.hasNext();) {
					InteractionStatus message = (InteractionStatus) i.next();
					sendMessage(message, client);
				}
			}
		}

		/**
		 * Send the specified message to the specified listener
		 */
		void sendMessage(InteractionStatus message,
				InteractionStateListener client) {
			client.stateChanged(new InteractionEvent() {
				public InteractionReceipt getReceipt() {
					return HTTPPollingInteractionReceipt.this;
				}
			});
		}

		/**
		 * Add a listener to respond to interaction events, replay any prior
		 * messages to the new listener if it wasn't there before
		 */
		public void addInteractionStateListener(
				InteractionStateListener listener) {
			synchronized (listeners) {
				if (listeners.contains(listener) == false) {
					listeners.add(listener);
					sendAllMessagesToClient(listener);
				}
			}
		}

		/**
		 * Return the enclosing instance
		 */
		public InteractionService getService() {
			return HTTPInteractionServiceProxy.this;
		}

		/**
		 * Get the current status
		 */
		public InteractionStatus getInteractionStatus() {
			synchronized (this.statusChangeMessages) {
				InteractionStatus latestStatus = (InteractionStatus) statusChangeMessages
						.get(statusChangeMessages.size() - 1);
				return latestStatus;
			}
		}

		/**
		 * Return the InteractionRequest that created this instance
		 */
		public InteractionRequest getRequest() {
			return this.interactionRequest;
		}

	}

	/**
	 * Submitted messages
	 */
	class RequestSubmittedEvent implements InteractionStatus {
		//
	}

	/**
	 * Completion message
	 */
	class InteractionCompletionEvent implements TerminalInteractionStatus {
		private Map results;

		public InteractionCompletionEvent(Map results) {
			this.results = results;
		}

		public int getStatusCode() {
			return TerminalInteractionStatus.COMPLETED;
		}

		public Object getResultData() {
			return this.results;
		}
	}

	/**
	 * Failure message
	 */
	class InteractionFailedEvent implements TerminalInteractionStatus {
		public int getStatusCode() {
			return TerminalInteractionStatus.FAILED;
		}

		public Object getResultData() {
			return null;
		}
	}

	/**
	 * Rejection message
	 */
	class InteractionRejectedEvent implements TerminalInteractionStatus {
		public int getStatusCode() {
			return TerminalInteractionStatus.REJECTED;
		}

		public Object getResultData() {
			return null;
		}
	}

	/**
	 * Timeout message
	 */
	class InteractionTimedOutEvent implements TerminalInteractionStatus {
		public int getStatusCode() {
			return TerminalInteractionStatus.TIMEOUT;
		}

		public Object getResultData() {
			return null;
		}
	}

	/**
	 * Simple implementation of the InteractionPattern interface driven off an
	 * XML definition fragment fetched from the HTTP server
	 */
	class XMLBasedInteractionPattern implements InteractionPattern {

		private String name, description;

		private String[] inputTypes, outputTypes, inputNames, outputNames;

		XMLBasedInteractionPattern(Element e) {
			this.name = e.getAttributeValue("name", "No name!");
			Element descriptionElement = e.getChild("description");
			this.description = descriptionElement.getTextTrim();
			List inputs = e.getChildren("input");
			inputTypes = new String[inputs.size()];
			inputNames = new String[inputs.size()];
			List outputs = e.getChildren("output");
			outputTypes = new String[outputs.size()];
			outputNames = new String[outputs.size()];
			for (int i = 0; i < inputTypes.length; i++) {
				Element input = (Element) inputs.get(i);
				inputTypes[i] = input.getAttributeValue("type");
				inputNames[i] = input.getAttributeValue("name");
			}
			for (int i = 0; i < outputTypes.length; i++) {
				Element output = (Element) outputs.get(i);
				outputTypes[i] = output.getAttributeValue("type");
				outputNames[i] = output.getAttributeValue("name");
			}
		}

		public String getName() {
			return this.name;
		}

		public String getDescription() {
			return this.description;
		}

		public String[] getInputTypes() {
			return this.inputTypes;
		}

		public String[] getOutputTypes() {
			return this.outputTypes;
		}

		public String[] getInputNames() {
			return this.inputNames;
		}

		public String[] getOutputNames() {
			return this.outputNames;
		}

	}

}
