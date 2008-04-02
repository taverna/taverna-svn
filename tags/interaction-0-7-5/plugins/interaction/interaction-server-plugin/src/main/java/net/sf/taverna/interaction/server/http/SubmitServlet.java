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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.taverna.interaction.server.InteractionServer;

import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.log4j.Logger;

/**
 * Servlet to handle interaction request submissions, all servlets are stateless
 * and rely on a singleton instance of the SubmissionServer class to do the real
 * work, this is just a presentation layer.
 * 
 * @author Tom Oinn
 */
public class SubmitServlet extends HttpServlet {

	private static Logger log = Logger.getLogger(SubmitServlet.class);

	private static InteractionServer server = null;

	public static InteractionServer getServer() throws ServletException {
		if (server != null) {
			return server;
		}
		log.error("The interaction server singleton is still null, failing");
		throw new ServletException("InteractionServer singleton not created!");
	}

	public void init(ServletConfig sc) {
		String tempLocation = sc.getInitParameter("temp_location");
		if (tempLocation == null) {
			log.error("Must define a temp location in the web.xml file.");
			return;
		}
		File f = new File(tempLocation);
		if (f.isDirectory() == false) {
			log.error("Temp location must point to a directory.");
			return;
		}
		String mailHost = sc.getInitParameter("smtp_host");
		String mailFrom = sc.getInitParameter("mail_from");
		server = new InteractionServer(new File(tempLocation), mailHost,
				mailFrom);
		try {
			if (sc.getInitParameter("use_html").equals("true")) {
				server.enableHTML();
			}
		} catch (Throwable ex) {
			log.debug("Not using HTML");
		}

		try {
			server.setBaseURL(sc.getInitParameter("base_url"));
		} catch (Throwable ex) {
			log.debug("Base URL not defined, will try to guess from requests.",
					ex);
		}
		log.debug("Created InteractionServer singleton with temp location of '"
				+ tempLocation + "'.");
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
		server.setBaseURL(request.getRequestURL().toString());
		String jobID = null;
		try {
			FileItem dataItem = null;
			String metadata = null;
			// Check whether the request is a file upload
			boolean isMultipart = FileUpload.isMultipartContent(request);
			if (!isMultipart) {
				log.error("Form data is not in multipart format");
				return;
			}

			DiskFileUpload upload = new DiskFileUpload();
			List items = upload.parseRequest(request);
			for (Iterator i = items.iterator(); i.hasNext();) {
				FileItem item = (FileItem) i.next();
				if (item.isFormField()) {
					log.debug("Item '" + item.getFieldName()
							+ "' is a form field");
					log.debug(item.getString());
				} else {
					log.debug("Item '" + item.getFieldName()
							+ "' is a file upload");
					log.debug("  held in memory? " + item.isInMemory());
					log.debug("  size? " + item.getSize());
				}

				if (item.getFieldName().equals("data")) {
					dataItem = item;
				} else if (item.getFieldName().equals("metadata")) {
					metadata = item.getString();
				}
			}
			if (dataItem != null && metadata != null) {
				jobID = server.createInteractionRequest(metadata, dataItem);
				log.debug("Submitted job to server, received ID '" + jobID
						+ "'.");
			} else {
				throw new ServletException(
						"Request must contain both data and metadata parts.");
			}
		} catch (FileUploadException fue) {
			log.error("Exception handling file upload", fue);
			throw new ServletException("Can't handle file upload", fue);
		}
		try {
			// Got a Job ID here, create the response
			response.setContentType("text/xml");
			PrintWriter out = response.getWriter();
			out.println("<jobID>" + jobID + "</jobID>");
			out.flush();
		} catch (IOException ioe) {
			log.error("Unable to write output", ioe);
			throw new ServletException("Can't create output writer", ioe);
		}
	}

}
