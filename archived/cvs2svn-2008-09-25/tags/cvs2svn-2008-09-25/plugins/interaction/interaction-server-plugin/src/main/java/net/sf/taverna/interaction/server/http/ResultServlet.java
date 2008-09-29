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

package net.sf.taverna.interaction.server.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.taverna.interaction.server.InteractionServer;
import net.sf.taverna.interaction.server.InteractionState;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Fetch the result document for the session ID specified in the 'id' parameter
 * to the POST request
 * 
 * @author Tom Oinn
 */
public class ResultServlet extends HttpServlet {

	private static Logger log = Logger.getLogger(ResultServlet.class);

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		String jobID = null;
		try {
			boolean isMultipart = FileUpload.isMultipartContent(request);
			if (!isMultipart) {
				log.error("Form data is not in multipart format");
				return;
			}
			DiskFileUpload upload = new DiskFileUpload();
			List items = upload.parseRequest(request);
			for (Iterator i = items.iterator(); i.hasNext();) {
				FileItem item = (FileItem) i.next();
				if (item.getFieldName().equals("id")) {
					jobID = item.getString();
				}
			}
		} catch (Exception ex) {
			log.error("Exception", ex);
		}
		if (jobID == null) {
			throw new ServletException(
					"Job ID must be specified as the 'id' parameter in the POST request");
		}
		log.debug("Fetching results for '" + jobID + "'");
		InteractionServer server = SubmitServlet.getServer();
		InteractionState state = server.getInteraction(jobID);
		if (state == null) {
			log.error("Attempt to retrieve results for state '" + jobID
					+ "' but it doesn't exist!");
			throw new ServletException("No such state on the server!");
		}
		if (state.getState() != InteractionState.COMPLETED) {
			log.error("Interaction '" + jobID
					+ "' isn't in state COMPLETED, can't fetch results.");
			throw new ServletException(
					"Can't fetch results, this session isn't finished.");
		}
		try {
			File resultsFile = new File(server.getRepository(), jobID
					+ "-results.xml");
			FileInputStream fis = new FileInputStream(resultsFile);
			response.setContentType("text/xml");
			PrintWriter out = response.getWriter();
			IOUtils.copy(fis, out);
			out.flush();
			out.close();
			// Got to here, this means we've sent the results off safely
			// so destroy the session on the server
			server.removeSession(jobID);
		} catch (IOException ioe) {
			log.error("Unable to read from results file for '" + jobID + "'");
			throw new ServletException(ioe);
		}
	}

}
