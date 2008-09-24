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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Partial implementation of the ServerInteractionPattern interface that derives
 * the various port names, description and service name from an XML
 * configuration file. For a class 'FooClass' this xml file should be named
 * FooClass.metadata.xml and located in the same package, this will then be used
 * by the default constructor in this superclass to populate the basic fields.
 * <p>
 * The XML file should contain something like the following:
 * 
 * <pre>
 *  &lt;pattern name=&quot;base.AcceptReject&quot;&gt;
 *    &lt;description&gt;Accept or reject a single item of data&lt;/description&gt;
 *    &lt;input name=&quot;data&quot; type=&quot;'text/plain'&quot;/&gt;
 *    &lt;output name=&quot;decision&quot; type=&quot;'text/plain'&quot;/&gt;
 *  &lt;/pattern&gt;
 * </pre>
 * 
 * In addition this class implements a naive version of the request data
 * download and results upload plugins. The default behaviour for the request
 * data download is to serve back the xml data file directly without any
 * transformation, for the results upload it just dumps the filepart named
 * 'results' into the results file and marks the interaction as completed.
 * <p>
 * This behaviour is almost certainly too simple for many cases - it inherently
 * requires the Taverna DataThing library on the client which is probably
 * overkill. The intention is therefore that almost all pattern implementations
 * will override these methods to perform some manner of data marshalling and
 * unmarshalling, moving the burden of handling the complex encoded XML onto the
 * server and allowing extremely lightweight clients, the only real requirement
 * from the client is that it should be able to send HTTP POST requests (this
 * being the only sane way of getting data back to the server), or, in the
 * degenerate case, HTTP GET to send a simple 'yes / no' type response.
 * 
 * @author Tom Oinn
 */
public abstract class AbstractServerInteractionPattern implements
		ServerInteractionPattern {

	private List inputNames, outputNames, inputTypes, outputTypes;

	private String description, patternName;

	private static Logger log = Logger
			.getLogger(AbstractServerInteractionPattern.class);

	/**
	 * Locate the appropriate XML config file, parse it and populate the simple
	 * textual metadata from it
	 */
	public AbstractServerInteractionPattern() {
		inputNames = new ArrayList();
		outputNames = new ArrayList();
		inputTypes = new ArrayList();
		outputTypes = new ArrayList();
		description = "No description available";
		patternName = "noname";
		String[] nameParts = getClass().getName().split("\\.");
		String name = nameParts[nameParts.length - 1];
		try {
			SAXBuilder builder = new SAXBuilder(false);
			InputStream is = getClass().getResourceAsStream(
					name + ".metadata.xml");
			if (is == null) {
				log.error("Unable to locate metadata file '" + name
						+ ".metadata.xml'");
				return;
			}
			Document metadata = builder.build(is);
			// Expect document of form...
			// <pattern name="foo.bar.PatternName">
			// <description>....</description>
			// <input name="..." type="...">*
			// <output name="..." type="...">*
			// </pattern>
			Element pattern = metadata.getRootElement();
			this.patternName = pattern.getAttributeValue("name");
			this.description = pattern.getChild("description").getTextTrim();
			List inputs = pattern.getChildren("input");
			for (Iterator i = inputs.iterator(); i.hasNext();) {
				Element e = (Element) i.next();
				String inputName = e.getAttributeValue("name");
				String inputType = e.getAttributeValue("type");
				inputNames.add(inputName);
				inputTypes.add(inputType);
			}
			List outputs = pattern.getChildren("output");
			for (Iterator i = outputs.iterator(); i.hasNext();) {
				Element e = (Element) i.next();
				String outputName = e.getAttributeValue("name");
				String outputType = e.getAttributeValue("type");
				outputNames.add(outputName);
				outputTypes.add(outputType);
			}
		} catch (JDOMException jde) {
			log.error("Can't parse xml metadata document for '" + name
					+ ".metadata.xml'");
		} catch (IOException ioe) {
			log.error("Unable to open stream for '" + name + ".metadata.xml'");
		} catch (NullPointerException npe) {
			log.error("Unexpected document structure for '" + name
					+ ".metadata.xml'");
		}
	}

	/**
	 * Handle a data request, this method is invoked when the client side
	 * interaction code needs to fetch the original input data for the
	 * interaction job. By default this implementation just returns the input
	 * document as an XML stream.
	 */
	public void handleInputDownload(HttpServletRequest request,
			HttpServletResponse response, InteractionState state,
			InteractionServer server) throws ServletException {
		try {
			response.setContentType("text/xml");
			File dataFile = new File(server.getRepository(), state.getID()
					+ "-input.xml");
			FileInputStream fis = new FileInputStream(dataFile);
			PrintWriter writer = response.getWriter();
			IOUtils.copy(fis, writer);
			fis.close();
			writer.flush();
			writer.close();
			return;
		} catch (Exception ex) {
			try {
				response.sendError(
						HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex
								.getMessage());
			} catch (Exception ex2) {
				//
			}
		}
	}

	/**
	 * Handle a result upload. By default this method just dumps the contents of
	 * the POSTed data, assuming a data item named 'data' and writing it to the
	 * result file before messaging the state object that it's been completed.
	 * If anything goes wrong the state object is messaged as failing.
	 */
	public void handleResultUpload(HttpServletRequest request,
			HttpServletResponse response, InteractionState state,
			InteractionServer server) throws ServletException {
		try {
			boolean isMultipart = FileUpload.isMultipartContent(request);
			if (!isMultipart) {
				log
						.debug("Response isn't a multipart file upload, failing the interaction");
				state.fail();
				return;
			}
			DiskFileUpload upload = new DiskFileUpload();
			List items = upload.parseRequest(request);
			boolean found = false;
			for (Iterator i = items.iterator(); i.hasNext() && !found;) {
				FileItem item = (FileItem) i.next();
				if (item.getFieldName().equals("data")) {
					File resultsFile = new File(server.getRepository(), state
							.getID()
							+ "-results.xml");
					resultsFile.createNewFile();
					item.write(resultsFile);
					state.complete();
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);
					log.debug("Completed interaction '" + state.getID() + "'");
					return;
				}
			}
			log.debug("No file upload part named 'data', failing interaction");
			state.fail();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"No file upload part named 'data', failing interaction");
		} catch (Exception ex) {
			log
					.error(
							"Exception thrown whilst trying to handle upload, failing interaction",
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

	/**
	 * Return an array of names for the inputs defined in getInputTypes
	 */
	public final String[] getInputNames() {
		return (String[]) inputNames.toArray(new String[0]);
	}

	/**
	 * Return an array of Taverna style syntactic type strings corresponding to
	 * the input data for this interaction pattern
	 */
	public final String[] getInputTypes() {
		return (String[]) inputTypes.toArray(new String[0]);
	}

	/**
	 * Return an array of output names
	 */
	public final String[] getOutputNames() {
		return (String[]) outputNames.toArray(new String[0]);
	}

	/**
	 * Return an array of Taverna style syntactic type strings corresponding to
	 * the output data for this interaction pattern
	 */
	public final String[] getOutputTypes() {
		return (String[]) outputTypes.toArray(new String[0]);
	}

	/**
	 * Return a free text description
	 */
	public final String getDescription() {
		return this.description;
	}

	/**
	 * Return a name for this interaction pattern. If the name contains a '.'
	 * character this may be interpreted by a browser interface as representing
	 * categories, so for example the name 'edit.sequence.Artemis' could be
	 * placed in an 'edit' category with subcategory 'sequence'. Names MUST be
	 * unique within a given interaction server.
	 */
	public final String getName() {
		return this.patternName;
	}

}
