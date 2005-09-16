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

import org.apache.log4j.Logger;
import java.io.*;
import java.util.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.view.*;
import org.embl.ebi.escience.scufl.parser.*;

/**
 * Represents an on disc workflow repository
 * @author Tom Oinn
 */
public class Repository {
    
    private static Logger log = Logger.getLogger(Repository.class);
    private Map models;
    private File location;
    
    /**
     * Create a workflow repository backed by the specified
     * directory. If this directory contains an index file
     * this will be used to populate the repository object
     * otherwise a blank index will be created
     */
    public Repository(File location) {
	this.location = location;
	if (location.exists() == false) {
	    init(location);
	}
	this.models = new HashMap();
	readFromIndex();
    }
    
    /**
     * Return the location this repository uses to store files
     */
    public File getLocation() {
	return this.location;
    }

    /**
     * Initialise the repository structure
     */
    private void init(File location) {
	if (location.mkdirs() == true) {
	    new File(location,"images").mkdirs();
	    writeIndex();
	}
	else {
	    log.error("Unable to create directories for repository at '"+
		      location.toString()+"'");
	}
    }

    /**
     * Write the index to disc
     */
    private synchronized void writeIndex() {
	try {
	    Element rootElement = new Element("models");
	    for (Iterator i = models.values().iterator(); i.hasNext();) {
		ScuflModelInfo info = (ScuflModelInfo)i.next();
		Element e = new Element("model");
		e.setAttribute("id",info.getID());
		Element author = new Element("author");
		author.setText(info.getAuthor());
		e.addContent(author);
		Element title = new Element("title");
		title.setText(info.getTitle());
		e.addContent(title);
		Element description = new Element("description");
		description.setText(info.getDescription());
		e.addContent(description);
		rootElement.addContent(e);
	    }
	    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
	    File indexFile = new File(location,"index.xml");
	    if (indexFile.createNewFile() == false) {
		indexFile.delete();
		indexFile.createNewFile();
	    }
	    PrintWriter out = new PrintWriter(new FileWriter(indexFile));
	    out.println(xo.outputString(rootElement));
	    out.flush();
	    out.close();
	}
	catch (IOException ioe) {
	    log.error("Unable to write index", ioe);
	}
    }

    /**
     * Update from the index
     */
    private synchronized void readFromIndex() {
	try {
	    File indexFile = new File(location,"index.xml");
	    if (indexFile.exists() == false) {
		log.warn("No index file present, returning.");
		return;
	    }
	    this.models = new HashMap();
	    SAXBuilder builder = new SAXBuilder(false);
	    Document indexDoc = builder.build(new FileInputStream(indexFile));
	    for (Iterator i = indexDoc.getRootElement().getChildren().iterator();
		 i.hasNext();) {
		Element e = (Element)i.next();
		String id = e.getAttributeValue("id");
		String author = e.getChild("author").getTextTrim();
		String description = e.getChild("description").getTextTrim();
		String title = e.getChild("title").getTextTrim();
		this.models.put(id, new ScuflModelInfo(author, title, description, id));
	    }
	    log.info("Loaded "+this.models.values().size()+" workflow descriptions.");
	}
	catch (IOException ioe) {
	    log.error("IO exception loading index file.", ioe);
	}
	catch (JDOMException jde) {
	    log.error("JDOM exception when parsing index file.", jde);
	}		
    }

    /**
     * Get all model metadata objects
     */
    public ScuflModelInfo[] getModels() {
	return (ScuflModelInfo[])models.values().toArray(new ScuflModelInfo[0]);
    }

    /**
     * Get model info for the specified ID
     */
    public ScuflModelInfo getModelInfo(String id) {
	return (ScuflModelInfo)models.get(id);
    }

    /**
     * Get the workflow model
     * @param online set to true if you want to load this workflow in
     * full online mode, false to load offline. In general for diagram
     * generation etc you want to load in offline mode.
     */
    public ScuflModel getModel(String id, boolean online) {
	File workflowFile = new File(location, id+".xml");
	if (workflowFile.exists() == false) {
	    log.error("Unable to find a workflow file for id '"+id+"'.");
	    return null;
	}
	ScuflModel model = new ScuflModel();
	try {
	    if (online == false) {
		model.setOffline(true);
	    }
	    FileInputStream fis = new FileInputStream(workflowFile);
	    XScuflParser.populate(fis, model, null);
	}
	catch (Exception ex) {
	    log.error("Unable to process workflow definition", ex);
	    return null;
	}
	return model;
    }

    /**
     * Add a new workflow
     */
    public synchronized void submitWorkflow(ScuflModel workflow) {
	String id = getID();
	try {
	    // Write out index data
	    WorkflowDescription wd = workflow.getDescription();
	    String author = wd.getAuthor();
	    String description = wd.getText();
	    String title = wd.getTitle();
	    this.models.put(id, new ScuflModelInfo(author, title, description, id));
	    writeIndex();
	    
	    // Write out the workflow definition
	    XScuflView xsv = new XScuflView(workflow);
	    Document doc = new XScuflView(workflow).getDocument();
	    workflow.removeListener(xsv);
	    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
	    File workflowFile = new File(location,id+".xml");
	    workflowFile.createNewFile();
	    PrintWriter out = new PrintWriter(new FileWriter(workflowFile));
	    out.println(xo.outputString(doc));
	    out.flush();
	    out.close();
	    
	    // Write out the html summary (do it now while we have an online
	    // version of the workflow, saves potential network access later)
	    File summaryFile = new File(location,id+".summary.html");
	    summaryFile.createNewFile();
	    out = new PrintWriter(new FileWriter(summaryFile));
	    out.println(WorkflowSummaryAsHTML.getSummary(workflow));
	    out.flush();
	    out.close();
	}
	catch (IOException ioe) {
	    log.error("Error writing state", ioe);
	}
    }

    private int count = 0;
    /**
     * Return a unique ID
     */
    private String getID() {
	return new Date().getTime()+"-"+count++;
    }

}
