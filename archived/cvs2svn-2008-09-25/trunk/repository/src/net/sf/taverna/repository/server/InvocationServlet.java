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

import java.io.*;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.enactor.*;
import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateListener;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.main.WorkflowState;
import org.apache.commons.fileupload.*;
import org.embl.ebi.escience.scufl.*;
import java.util.*;
import org.embl.ebi.escience.scuflui.*;
import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

/**
 * Receives a request POSTed to it to launch a particular workflow, processes
 * the request to build the appropriate Map of DataThing objects, launches 
 * the enactor if not already running, emails a message to the user with a link
 * to the workflow status page then redirects to that status page.
 * @author Tom Oinn
 */
public class InvocationServlet extends HttpServlet {
    
    // Singleton instance of the EnactorProxy to create new workflow instances
    public static EnactorProxy ENACTOR;
    
    // Logger
    private static Logger log = Logger.getLogger(InvocationServlet.class);
    
    // Map of instanceid -> WorkflowInstance object
    static Map WORKFLOWS = new HashMap();
    // Map of instanceid -> EnactorStatusTableModel object
    static Map STATUS = new HashMap();

    // Initialize the enactor proxy, get email properties from servlet config
    public void init(ServletConfig sc) {
	ENACTOR = new FreefluoEnactorProxy();
	log.debug("Created new workflow engine singleton");
    }

    public void doGet(HttpServletRequest request,
		       HttpServletResponse response)
	throws ServletException {
	doPost(request, response);
    }

    public void doPost(HttpServletRequest request,
		       HttpServletResponse response)
	throws ServletException {
	DiskFileUpload upload = new DiskFileUpload();

	// Pull out an 'workflowID' field from the request, use this to locate
	// a workflow in the repository.
	String id = null;
	try {
	    boolean isMultipart = FileUpload.isMultipartContent(request);
	    if (!isMultipart) {
		log.error("Form data is not in multipart format");
		return;
	    }
	    List items = upload.parseRequest(request);
	    for (Iterator i = items.iterator(); i.hasNext();) {
		FileItem item = (FileItem)i.next();
		if (item.getFieldName().equals("workflowID")) {
		    id = item.getString();
		}
	    }
	}
	catch (Exception ex) {
	    log.error("Exception",ex);
	}
	if (id == null) {
	    log.error("No workflow ID specified!");
	    try {
		response.sendRedirect("noidspecified.jsp");
	    }
	    catch (IOException ioe) {
		//
	    }
	    return;
	}
	Repository rep = SubmitServlet.getRepository();
	ScuflModel model = rep.getModel(id, true);
	if (model == null) {
	    log.error("Unable to find or parse the specified model with id '"+id+"'.");
	    try {
		response.sendRedirect("workflowloaderror.jsp?id="+id);
	    }
	    catch (IOException ioe) {
		//
	    }
	    return;
	}
	// Now have a non null ScuflModel, need to create an appropriate
	// input map from the other parameters and complain bitterly if
	// there isn't enough data to launch the workflow
	// We use <inputname> as the actual data to process and optionally
	// handle <inputname>.<param> as a processing directive, this is
	// passed off to the createData method to build an appropriate DataThing
	Map inputMap = new HashMap();
	Map categories = new HashMap();
	List items;
	try {
	    items = upload.parseRequest(request);
	}
	catch (FileUploadException fue) {
	    log.error("Failed to handle file upload", fue);
	    try {
		response.sendRedirect("invalidupload.jsp");
	    }
	    catch (IOException ioe) {
		//
	    }
	    return;
	}
	for (Iterator i = items.iterator(); i.hasNext();) {
	    FileItem item = (FileItem)i.next();
	    String name = item.getFieldName();
	    // Ignore the workflow ID
	    if (name.equals("workflowID") == false) {
		String[] s = name.split("\\.");
		String itemName = null;
		String paramName = null;
		if (s.length == 1) {
		    itemName = s[0];
		    paramName = "data";
		}
		else if (s.length > 1) {
		    itemName = s[0];
		    paramName = s[1];
		}
		// Create or find the map of param->value using the magic param string data
		// to handle the actual input data item
		Map categoryMap = (Map)categories.get(itemName);
		if (categoryMap == null) {
		    categoryMap = new HashMap();
		    categories.put(itemName, categoryMap);
		}
		categoryMap.put(paramName, item);		
	    }
	}
	for (Iterator i = categories.keySet().iterator(); i.hasNext();) {
	    String inputName = (String)i.next();
	    Map data = (Map)categories.get(inputName);
	    inputMap.put(inputName, buildDataThing(data, inputName, model));
	}
	// Now have an inputMap object containing named DataThing objects
	String instanceID;
	try {
	    WorkflowInstance instance = ENACTOR.compileWorkflow(model, inputMap, null);
	    instanceID = instance.getID();
	    log.debug("Created workflow instance with instance ID '"+instanceID+"'");
	    WORKFLOWS.put(instanceID, instance);
	    STATUS.put(instanceID, new EnactorStatusTableModel(model));
	    instance.run();
	}
	catch (WorkflowSubmissionException e) {
	    log.error("Unable to compile the workflow, aborting.",e);
	    try {
		response.sendRedirect("compilefailed.jsp");
	    }
	    catch (IOException ioe) {
		//
	    }
	    return;
	}
	catch (InvalidInputException e) {
	    log.error("Unable to compile the workflow input, aborting.",e);
	    try {
		response.sendRedirect("badinput.jsp");
	    }
	    catch (IOException ioe) {
		//
	    }
	    return;
	}
	
	try {
	    response.sendRedirect("status.jsp?instance="+instanceID);
	    return;
	}
	catch (IOException ioe) {
	    //
	}
    }

    private DataThing buildDataThing(Map data, String inputName, ScuflModel model) {
	if (data.size() == 1) {
	    // Naive handler that just takes the string value and puts
	    // it into a string datathing
	    FileItem item = (FileItem)data.get("data");
	    return new DataThing(item.getString());
	}
	else {
	    log.error("Can't handle any compound input types yet.");
	    return null;
	}
    }

    public static EnactorStatusTableModel getUpdatedModel(String instance) {
	EnactorStatusTableModel t = (EnactorStatusTableModel)STATUS.get(instance);
	if (t == null) {
	    return null;
	}
	synchronized (t) {
	    WorkflowInstance w = (WorkflowInstance)WORKFLOWS.get(instance);
	    String progressReport = w.getProgressReportXMLString();
	    try {
		t.update(progressReport);
	    }
	    catch (Exception isre) {
		log.error("Unable to parse status report",isre);
		return null;
	    }
	    return t;
	}
    }

}
