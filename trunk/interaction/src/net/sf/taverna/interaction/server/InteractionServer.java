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

import org.apache.commons.fileupload.*;
import java.io.*;
import java.util.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.apache.log4j.Logger;

/**
 * This class, used as a singleton by the various servlets, handles
 * the business logic of creating and servicing interaction requests
 * @author Tom Oinn
 */
public class InteractionServer {
    
    private File temp;
    private int count = 0;
    private Map statusMap = new HashMap();
    private static Logger log = Logger.getLogger(InteractionServer.class);
    
    /**
     * Create a new InteractionServer with the specified directory
     * to use as space for indices and temporary data
     */
    public InteractionServer(File tempLocation) {
	this.temp = tempLocation;
    }
    
    /**
     * Get a unique ID within this InteractionServer instance
     */
    private synchronized String getID() {
	return new Date().getTime()+"-"+(count++);
    }
    
    /**
     * Get the PendingInteraction object for the specified job ID
     */
    public PendingInteraction getInteraction(String jobID) {
	return (PendingInteraction)statusMap.get(jobID);
    }
    
    /**
     * Create a new interaction request
     */
    public String createInteractionRequest(String metadata,
					   FileItem data) {
	String jobID = getID();
	
	try {
	    // Write the intput data to a file
	    File inputDataFile = new File(temp, jobID+"-input.xml");
	    inputDataFile.createNewFile();
	    data.write(inputDataFile);
	    
	    // Write metadata to a file as well
	    File metadataFile = new File(temp, jobID+"-request.xml");
	    metadataFile.createNewFile();
	    PrintWriter out = new PrintWriter(new FileWriter(metadataFile));
	    out.println(metadata);
	    out.flush();
	    out.close();
	    
	    // Parse the metadata document
	    SAXBuilder builder = new SAXBuilder(false);
	    Document metadataDoc = builder.build(new StringReader(metadata));
	}
	catch (JDOMException jde) {
	    log.error(jde);
	}
	catch (IOException ioe) {
	    log.error(ioe);
	}
	catch (Exception ex) {
	    log.error(ex);
	}
	
	PendingInteraction pi = new PendingInteraction(jobID);
	statusMap.put(jobID, pi);
	
	return jobID;
    }
	
    /**
     * Holds metadata about a current interaction job
     */
    public class PendingInteraction {
	
	private List eventList = new ArrayList();

	public PendingInteraction(String jobID) {
	    
	}
	
	public void complete() {
	    addEvent("completed");
	}
	
	public void fail() {
	    addEvent("failure");
	}
	
	public void reject() {
	    addEvent("rejected");
	}
	
	public void timeout() {
	    addEvent("timeout");
	}
	
	private void addEvent(Object o) {
	    synchronized(eventList) {
		eventList.add(o);
	    }
	}
	
	/**
	 * Return a JDOM document containing all the unsent events
	 */
	public Document getUnsentEvents() {
	    synchronized(eventList) {
		Element rootElement = new Element("events");
		for (Iterator i = eventList.iterator(); i.hasNext();) {
		    String eventString = (String)i.next();
		    rootElement.addContent(new Element(eventString));
		}
		eventList.clear();
		return new Document(rootElement);
	    }
	}
    }

}
