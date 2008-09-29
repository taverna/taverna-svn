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
import org.apache.log4j.Logger;

/**
 * Superclass of the client upload / download servlets, takes care of extracting
 * the session ID, finding the state etc from the request object
 * 
 * @author Tom Oinn
 */
public abstract class AbstractClientServlet extends HttpServlet {

	private static Logger log = Logger.getLogger(AbstractClientServlet.class);

	public final void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		String jobID = null;
		try {
			if (FileUpload.isMultipartContent(request)) {
				DiskFileUpload upload = new DiskFileUpload();
				List items = upload.parseRequest(request);
				for (Iterator i = items.iterator(); i.hasNext();) {
					FileItem item = (FileItem) i.next();
					if (item.isFormField()) {
						if (item.getFieldName().equals("id")) {
							jobID = item.getString();
						}
					}
				}
			} else {
				jobID = request.getParameter("id");
			}
			if (jobID == null) {
				log.error("Unable to locate a job id in the POSTed request");
				throw new ServletException(
						"No job ID specified in POST request, failing");
			}
			InteractionServer server = SubmitServlet.getServer();
			InteractionState state = server.getInteraction(jobID);
			if (state == null) {
				log.error("No extant state for id '" + jobID + "'");
				throw new ServletException("No interaction state for id '"
						+ jobID + "'");
			}
			log.debug("Delegating POST request for '" + jobID + "' to "
					+ getClass().toString() + ".handleRequest(..)");
			handleRequest(request, response, state, server);
		} catch (Exception ex) {
			if (ex instanceof ServletException) {
				throw (ServletException) ex;
			} else {
				throw new ServletException("Failure in handling POST request",
						ex);
			}
		}
	}

	public final void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		String jobID = request.getParameter("id");
		if (jobID == null) {
			log.error("Unable to locate a job ID in the GET request.");
			throw new ServletException(
					"No job ID specified in GET request, failing");
		}
		InteractionServer server = SubmitServlet.getServer();
		InteractionState state = server.getInteraction(jobID);
		if (state == null) {
			log.error("No extant state for id '" + jobID + "'");
			throw new ServletException("No interaction state for id '" + jobID
					+ "'");
		}
		log.debug("Delegating GET request for '" + jobID + "' to "
				+ getClass().toString() + ".handleRequest(..)");
		handleRequest(request, response, state, server);
	}

	/**
	 * Handle the request in the context of the specified server and state
	 * object.
	 */
	protected abstract void handleRequest(HttpServletRequest request,
			HttpServletResponse response, InteractionState state,
			InteractionServer server) throws ServletException;

}
