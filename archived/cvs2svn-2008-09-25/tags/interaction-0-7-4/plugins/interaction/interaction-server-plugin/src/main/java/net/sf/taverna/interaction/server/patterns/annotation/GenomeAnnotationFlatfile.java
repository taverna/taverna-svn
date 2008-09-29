/*
 * Copyright 2005 Anders Lanzén CBU, BCCS
 * 
 * Created on Sep 13, 2005 by Anders Lanzén, Computational Biology Group, 
 * Bergen Center for Computationoal Science, UiB Norway.
 * 
 *    
 */

package net.sf.taverna.interaction.server.patterns.annotation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.taverna.interaction.server.AbstractServerInteractionPattern;
import net.sf.taverna.interaction.server.InteractionServer;
import net.sf.taverna.interaction.server.InteractionState;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Implementation of the ServerInteractionPattern for Genome flatfiles (such as
 * EMBL, Genbank, GFF etc.). This Pattern supports launching of a modified
 * version of Artemis via Java Web Start for manipulation of flatfiles. Uploaded
 * data includes: Accept or Reject, Manual changes (yes / no in case of Accept),
 * Modified data and comments from the Expert Reviewer.
 * 
 * @author andersl
 * 
 */
public class GenomeAnnotationFlatfile extends AbstractServerInteractionPattern {

	public GenomeAnnotationFlatfile() {
		super();
	}

	private static Logger log = Logger
			.getLogger(GenomeAnnotationFlatfile.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.interaction.server.ServerInteractionPattern#getMessageBody(java.net.URL,
	 *      net.sf.taverna.interaction.server.InteractionState)
	 */
	public String getMessageBody(URL baseURL, InteractionState state) {

		StringBuffer message = new StringBuffer();
		log.debug("Base URL is " + baseURL);
		try {
			String id = state.getID();

			URL acceptURL = new URL(baseURL, "client/upload?id=" + id
					+ "&response=accept&manualchange=no");
			URL rejectURL = new URL(baseURL, "client/upload?id=" + id
					+ "&response=reject");

			URL artemisAppletURL = new URL(baseURL, "client/download?id=" + id
					+ "&launch=Artemis");
			URL plainTextDataURL = new URL(baseURL, "client/download?id=" + id);

			Document doc = state.getInputDocument();
			Map dataThings = DataThingXMLFactory.parseDataDocument(doc);
			String title = (String) ((DataThing) dataThings.get("title"))
					.getDataObject();
			String comment = (String) ((DataThing) dataThings.get("comment"))
					.getDataObject();

			message
					.append("This is an interaction request for genome annotation flatfiles. ");
			message
					.append("The workflow designer has requested that you to review the annotation "
							+ title + ".");
			message
					.append("\n\nThe data can be reviewed using a modified version of Artemis simply by following the below link. (Requires Java Runtime Environment 1.4.1 or newer). ");
			message
					.append("When you have finished reviewing the data you can use the Artemis client to accept the data, submit modifications or reject the data.\n\n");
			message.append("Comments: \n" + comment + "\n\n");
			message.append(" Review data : " + artemisAppletURL + "\n\n");
			message.append(" Review data as plain text : " + plainTextDataURL
					+ "\n\n");
			message.append(" Accept : " + acceptURL.toString() + "\n");
			message.append(" Reject : " + rejectURL.toString() + "\n");
		} catch (MalformedURLException mue) {
			log.error("Couldn't create context URLs", mue);
		}

		return message.toString();
	}

	public void handleInputDownload(HttpServletRequest request,
			HttpServletResponse response, InteractionState state,
			InteractionServer server) throws ServletException {

		// TODO For coming multifile version: serve files one after another
		// during request.getParameter(fileIndex)

		try {
			// The default behaviour is to launch the Artemis applet which
			// handles data download
			if (request.getParameter("launch") != null) {
				response.setContentType("application/x-java-jnlp-file");
				Document jnlp = new ArtemisInteractionJNLP(state.getID(),
						server.getBaseURLString());
				OutputStream out = response.getOutputStream();
				XMLOutputter outputter = new XMLOutputter(Format
						.getPrettyFormat());
				try {
					outputter.output(jnlp, out);
				} catch (IOException e) {
					System.err.println(e);
				}
			}
			// If the launch parameter is not given the result will be
			// downloaded as plain text
			else {
				response.setContentType("text/plain");
				PrintWriter writer = response.getWriter();
				Document doc = state.getInputDocument();
				Map dataThings = DataThingXMLFactory.parseDataDocument(doc);
				String flatfileContent = (String) ((DataThing) dataThings
						.get("annotation")).getDataObject();
				writer.print(flatfileContent);
				writer.flush();
				writer.close();
			}
		} catch (Exception e) {
			try {
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e
								.getMessage());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	/**
	 * Handles upload of genome flatfile result and takes care of modified data.
	 * If an interaction request is accepted without changes, the original input
	 * data is written as modifiedData output.
	 */

	public void handleResultUpload(HttpServletRequest request,
			HttpServletResponse response, InteractionState state,
			InteractionServer server) throws ServletException {

		/*
		 * The HTTP Post is made up by exactly 5 parameters all sent as UTF-8
		 * encoded Strings: id: the ID of the interaction request response:
		 * "accept" or "reject" manualchange: "yes" or "no" depending on if
		 * manual changes are submitted modified: the modified result, if it
		 * exists notes: the expert reviewers notes about modifications made
		 * 
		 * All of these should be present and only contain the values described.
		 * However, the interaction will only be failed if any of the following
		 * criteria are met:
		 * 
		 * 1) Response does not exist
		 * 
		 * 2) manualchange equals "yes" AND modified does not exist
		 * 
		 */

		try {

			Map dataThingMap = new HashMap();

			String manualChanges = request.getParameter("manualchange");
			dataThingMap.put("modified", new DataThing(manualChanges));
			String modified = request.getParameter("modified");
			boolean manualChangeMade = (manualChanges != null && manualChanges
					.equals("yes"));
			if (manualChangeMade && modified == null) {
				badRequest(
						"Manual changes indicated but modified data parameter does not exist.",
						response, state);
				return;
			}

			String responseValue = request.getParameter("response");
			if (responseValue == null) {
				badRequest("No response value, failing the interaction.",
						response, state);
				return;
			} else
				dataThingMap.put("decision", new DataThing(responseValue));

			String notes = request.getParameter("notes");
			if (notes == null)
				notes = "";
			dataThingMap.put("review_notes", new DataThing(notes));

			if (responseValue.toLowerCase().equals("accept")) {
				// Handle manual modifications

				if (modified == null
						|| (manualChanges != null && manualChanges
								.toLowerCase().equals("no"))) {

					// Modified undeclared or manualchange set to "no".
					// Put input data to modified annotation

					Document inputDoc = state.getInputDocument();
					Map inputThings = DataThingXMLFactory
							.parseDataDocument(inputDoc);
					dataThingMap.put("reviewed_result", inputThings
							.get("annotation"));
				} else if (modified != null)
					dataThingMap
							.put("reviewed_result", new DataThing(modified));
			} else
				dataThingMap.put("reviewed_result", new DataThing(
						"request rejected"));

			// Save results to disk
			File resultsFile = new File(server.getRepository(), state.getID()
					+ "-results.xml");
			resultsFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(resultsFile);
			Document doc = DataThingXMLFactory.getDataDocument(dataThingMap);
			XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
			xo.output(doc, fos);
			fos.flush();
			fos.close();
			state.complete();
		} catch (Exception ex) {
			log.error("Exception thrown handling upload, failing interaction.",
					ex);
			state.fail();
			try {
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex
								.getMessage());
			} catch (Exception ex2) {
				ex2.printStackTrace();
			}
			return;
		}
	}

	private static void badRequest(String message,
			HttpServletResponse response, InteractionState state)
			throws IOException {
		log.error(message);
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
		state.fail();
	}
}
