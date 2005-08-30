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

import net.sf.taverna.interaction.server.AbstractServerInteractionPattern;
import net.sf.taverna.interaction.server.InteractionState;
import net.sf.taverna.interaction.server.InteractionServer;
import java.net.URL;
import java.net.MalformedURLException;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.baclava.factory.*;
import org.apache.log4j.Logger;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * Simple pattern, user accepts or rejects a single item of data
 * @author Tom Oinn
 */
public class AcceptReject extends AbstractServerInteractionPattern {

    private static Logger log = Logger.getLogger(AcceptReject.class);

    public String getMessageBody(URL baseURL, InteractionState state) {
	StringBuffer sb = new StringBuffer();
	log.debug("Base URL is "+baseURL);
	try {
	    URL acceptURL = new URL(baseURL, "client/upload?id="+state.getID()+"&response=accept");
	    URL rejectURL = new URL(baseURL, "client/upload?id="+state.getID()+"&response=reject");
	    Document doc = state.getInputDocument();
	    Map dataThings = DataThingXMLFactory.parseDataDocument(doc);
	    DataThing text = (DataThing)dataThings.get("data");
	    String data = (String)text.getDataObject();
	    sb.append("Interaction request for the accept / reject interaction pattern, the workflow designer has requested that you either accept or reject the following text : ");
	    sb.append("\n\n" + data + "\n\n");
	    sb.append(" Accept : "+acceptURL.toString()+"\n");
	    sb.append(" Reject : "+acceptURL.toString()+"\n");
	}
	catch (MalformedURLException mue) {
	    log.error("Couldn't create context URLs, very strange!", mue);
	}
	return sb.toString();
    }

    public void handleResultUpload(HttpServletRequest request,
				   HttpServletResponse response,
				   InteractionState state,
				   InteractionServer server) 
	throws ServletException {
	try {
	    File resultsFile = new File(server.getRepository(),
					state.getID()+"-results.xml");
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
	}
	catch (Exception ex) {
	    log.error("Exception thrown handling upload, failing interaction.", ex);
	    state.fail();
	     try {
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
				   ex.getMessage());
	    }
	    catch (Exception ex2) {
		//
	    }
	    return;
	}
    }

}
