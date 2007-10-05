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

package net.sf.taverna.interaction.server.patterns;

import java.io.File;
import java.io.FileOutputStream;
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
 * Simple pattern, user accepts or rejects a single item of data. It does this
 * directly within the email response rather than requiring some external server
 * or set of jsp, this is probably the simplest possible interaction pattern and
 * is included more as an example than anything else.
 * 
 * @author Tom Oinn
 */
public class AcceptReject extends AbstractServerInteractionPattern {

	private static Logger log = Logger.getLogger(AcceptReject.class);

	/**
	 * Constructs an email body consisting of a copy of the interaction data
	 * specified as an input in the workflow along with two links, both to the
	 * upload servlet in GET mode with different values for the 'response'
	 * parameter, this will be processed by the handleResultUpload method in
	 * this class.
	 */
	public String getMessageBody(URL baseURL, InteractionState state) {
		StringBuffer sb = new StringBuffer();
		log.debug("Base URL is " + baseURL);
		try {
			URL acceptURL = new URL(baseURL, "client/upload?id="
					+ state.getID() + "&response=accept");
			URL rejectURL = new URL(baseURL, "client/upload?id="
					+ state.getID() + "&response=reject");
			Document doc = state.getInputDocument();
			Map dataThings = DataThingXMLFactory.parseDataDocument(doc);
			DataThing text = (DataThing) dataThings.get("data");
			String data = (String) text.getDataObject();
			sb
					.append("Interaction request for the accept / reject interaction pattern, the workflow designer has requested that you either accept or reject the following text : ");
			sb.append("\n\n" + data + "\n\n");
			sb.append(" Accept : " + acceptURL.toString() + "\n");
			sb.append(" Reject : " + rejectURL.toString() + "\n");
		} catch (MalformedURLException mue) {
			log.error("Couldn't create context URLs, very strange!", mue);
		}
		return sb.toString();
	}

	/**
	 * This interaction pattern uploads the results in the form of a GET query
	 * to the upload servlet. This is then passed onto this method which pulls
	 * out the 'response' parameter, bakes it into a DataThing and writes the
	 * Map containing the single DataThing object (with a key of 'decision' as
	 * per the metadata for this pattern) into the <ID>-results.xml file in the
	 * server repository. It then messages the state object to signal completion
	 * of the interaction.
	 */
	public void handleResultUpload(HttpServletRequest request,
			HttpServletResponse response, InteractionState state,
			InteractionServer server) throws ServletException {
		try {
			File resultsFile = new File(server.getRepository(), state.getID()
					+ "-results.xml");
			resultsFile.createNewFile();
			FileOutputStream fos = new FileOutputStream(resultsFile);
			Map dataThingMap = new HashMap();
			String result = request.getParameter("response");
			if (result == null) {
				log.error("No response value, failing the interaction.");
				state.fail();
				return;
			}
			dataThingMap.put("decision", new DataThing(result));
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
				//
			}
			return;
		}
	}

}
