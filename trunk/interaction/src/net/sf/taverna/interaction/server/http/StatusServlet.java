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

import net.sf.taverna.interaction.server.*;

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.*;
import org.jdom.output.*;
import org.jdom.*;

import org.apache.log4j.Logger;
import org.apache.commons.fileupload.*;

import java.util.*;

/**
 * Fetches the XML status document containing all unsent events for
 * the specified Job ID POSTed to it
 * @author Tom Oinn
 */
public class StatusServlet extends HttpServlet {
    
    private static Logger log = Logger.getLogger(StatusServlet.class);
    
    public void doPost(HttpServletRequest request,
		       HttpServletResponse response)
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
		FileItem item = (FileItem)i.next();
		if (item.getFieldName().equals("id")) {
		    jobID = item.getString();
		}
	    }
	}
	catch (Exception ex) {
	    log.error("Exception",ex);
	}
	if (jobID == null) {
	    throw new ServletException("Job ID must be specified as the 'id' parameter in the POST request");
	}
	try {
	    Document statusDoc = SubmitServlet.getServer().
		getInteraction(jobID).getUnsentEvents(true);
	    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
	    String status = xo.outputString(statusDoc);
	    PrintWriter out = response.getWriter();
	    response.setContentType("text/xml");
	    out.write(status);
	    out.flush();
	}
	catch (IOException ioe) {
	    log.error(ioe);
	}
    }

}
