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

package net.sf.taverna.ocula;

import net.sf.taverna.ocula.validation.PageValidator;

import org.jdom.*;
import org.jdom.input.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.log4j.Logger;

/**
 * A page definition within the Ocula system. There is a one to one
 * correspondance between Page objects and pages in the user interface
 * @author Tom Oinn
 */
public class Page {
    
    URL pageLocation;
    private Document pageDefinition;
    private boolean pageValid;
    private PageValidator validator = new PageValidator();
    static Map pageDefinitionCache = new HashMap();
    private static Logger log = Logger.getLogger(Page.class);

    /**
     * Create a new Page from the definition at the specified URL
     * @param pageLocation the location of an xml definition file,
     * subsequent internal references will be resolved relative to
     * this URL
     * @throws IOException if an IO error occurs when loading the
     * page definition
     */
    public Page(URL pageLocation) throws IOException {
	this();
	try {
	    setLocation(pageLocation);
	}
	catch (IOException ioe) {
	    log.info("IOException when loading page", ioe);
	    throw ioe;
	}
	catch (JDOMException jde) {
	    log.info("JDOM Exception when loading page", jde);
	    IOException ioe = new IOException();
	    ioe.initCause(ioe);
	    throw ioe;
	}
    }
    
    /**
     * Create a blank Page with no content, must call setLocation before
     * doing anything with it
     */
    public Page() {
	this.pageValid = false;
    }
    
    /**
     * Clear the page cache
     */
    public static void clearDefinitionCache() {
	pageDefinitionCache = new HashMap();
    }

    /**
     * Set the page location
     * @param pageLocation the location of the page definition to load
     */
    public synchronized void setLocation(URL pageLocation) 
	throws IOException, JDOMException {
	assert(pageLocation!=null);
	pageValid = false;
	this.pageLocation = pageLocation;
	loadPage();
	pageValid = true;
    }
    
    /**
     * Flags whether the page is ready for display and has a valid
     * definition with no loading errors
     */
    public synchronized boolean isValid() {
	return this.pageValid;
    }

    private synchronized void loadPage() throws IOException, JDOMException {
	// Test whether the cache hits
	assert(pageLocation!=null);
	if (pageLocation == null) {
	    throw new IOException("Location was null");
	}
	InputStream is = pageLocation.openStream();
	InputStreamReader isr = new InputStreamReader(is);
	SAXBuilder builder = new SAXBuilder(false);
	pageDefinition = builder.build(isr);
	this.pageValid = true;
    }

    /**
     * Return the List of Element objects within the &lt;contents&gt; tag
     */
    public List getContents() {
	if (this.pageValid == false) {
	    return new ArrayList();
	}
	return pageDefinition.getRootElement().getChild("contents").getChildren();
    }

    /**
     * Get the preload action list
     */
    public Element getInitActions() {
	Element headerElement = pageDefinition.getRootElement().getChild("header");
	if (headerElement != null) {
	    Element preloadElement = headerElement.getChild("init");
	    if (preloadElement != null) {
		return preloadElement;
	    }
	}
	return new Element("dummy");
    }

    /**
     * Get the title of this page
     */
    public String getTitle() {
	if (this.pageValid == false) {
	    return "No page defined";
	}
	else {
	    return pageDefinition.getRootElement().getAttributeValue("title","No title");
	}
    }

}
