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

package net.sf.taverna.repository.server;

import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;

import java.io.*;

import org.apache.log4j.Logger;
import org.apache.commons.fileupload.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.*;

import java.util.*;
import java.net.*;

/**
 * Servlet to handle submission of a workflow script to the repository
 * @author Tom Oinn
 */
public class SubmitServlet extends HttpServlet {
    
    private static Logger log = Logger.getLogger(SubmitServlet.class);
    private static Repository repository = null;
    
    public static Repository getRepository() throws ServletException {
	if (repository != null) {
	    return repository;
	}
	log.error("The workflow repository singleton is still null, failing");
	throw new ServletException("Repository singleton not created!");
    }

    public void init(ServletConfig sc) {
	String location = sc.getInitParameter("location");	
	if (location == null) {
	    log.error("Must define a repository location in the web.xml file.");
	    return;
	}
	String dotLocation = sc.getInitParameter("dot");
	File f = new File(location);
	repository = new Repository(f, dotLocation);
	log.debug("Created Repository singleton with temp location of '"+location+"'.");
    }

    public void doPost(HttpServletRequest request,
		       HttpServletResponse response)
	throws ServletException {
	FileItem workflowItem = null;
	try {
	    // Check whether the request is a file upload
	    boolean isMultipart = FileUpload.isMultipartContent(request);
	    if (!isMultipart) {
		log.error("Form data is not in multipart format");
		return;
	    }
	    DiskFileUpload upload = new DiskFileUpload();
	    List items = upload.parseRequest(request);
	    for (Iterator i = items.iterator(); i.hasNext();) {
		FileItem item = (FileItem)i.next();
		if (item.isFormField()) {
		    log.debug("Item '"+item.getFieldName()+"' is a form field");
		    log.debug(item.getString());
		}
		else {
		    log.debug("Item '"+item.getFieldName()+"' is a file upload");
		    log.debug("  held in memory? "+item.isInMemory());
		    log.debug("  size? "+item.getSize());
		}
		if (item.getFieldName().equals("workflow")) {
		    workflowItem = item;
		}
	    }
	    if (workflowItem == null) {
		throw new ServletException("Request doesn't contain a part named 'workflow', aborting.");
	    }
	}
	catch (FileUploadException fue) {
	    log.error("Exception handling file upload",fue);
	    throw new ServletException("Can't handle file upload",fue);
	}
	try {
	    ScuflModel model = new ScuflModel();
	    XScuflParser.populate(workflowItem.getInputStream(), model, null);
	    repository.submitWorkflow(model);
	    response.sendRedirect("index.jsp");
	}
	catch (Exception e) {
	    log.error("Unable to parse uploaded workflow", e);
	    throw new ServletException("Can't parse workflow", e);
	}
    }

    public void doGet(HttpServletRequest request,
		       HttpServletResponse response)
	throws ServletException {
	try {
	    String url = request.getParameter("workflowURL");
	    ScuflModel model = new ScuflModel();
	    XScuflParser.populate(new URL(url).openStream(), model, null);
	    repository.submitWorkflow(model);
	    response.sendRedirect("index.jsp");
	}
	catch (Exception e) {
	    log.error("Unable to parse uploaded workflow", e);
	    throw new ServletException("Can't parse workflow", e);
	}
    }

}
