/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import java.util.*;

/**
 * A container class for the semantic markup and mime 
 * type data for overall workflow input and output
 * ports, although presumably it could be attached
 * to other entities as well. The data contained are a
 * free text description, an array of zero or more 
 * mime types and a chunk of text containing semantic
 * metadata. Currently this last is unstructured, but
 * this class will be improved by the addition of a more
 * structured version based possibly on Jena at some
 * point.
 * @author Tom Oinn
 */
public class SemanticMarkup {

    private String description  = "";
    private String semanticType = "";
    private List mimeTypeList = new ArrayList();
    private Object subject = null;
    
    /**
     * Create a new item of semantic markup for the
     * Object specified. This should be interpreted
     * as 'this markup object applies to the supplied
     * Object'.
     */
    public SemanticMarkup(Object subject) {
	super();
	this.subject = subject;
    }

    /**
     * Get the Object that is the subject of this
     * markup, if that's not too confusing a way
     * of expressing it... This cannot be altered
     * once the object is created, I'm not entirely
     * sure this is actually a requirement but I
     * can't think of a good reason to do otherwise.
     */
    public Object getSubject() {
	return this.subject;
    }

    /**
     * Get the array of strings containing MIME
     * types for the item this markup object 
     * applies to.
     */
    public String[] getMIMETypes() {
	synchronized(this.mimeTypeList) {
	    return (String[])mimeTypeList.toArray(new String[0]);
	}
    }

    /**
     * Get the first MIME type in the list, or
     * return the empty string if no MIME types
     * have been defined.
     */
    public String getFirstMIMEType() {
	synchronized(this.mimeTypeList) {
	    if (mimeTypeList.isEmpty()) {
		return "";
	    }
	    else {
		return (String)mimeTypeList.get(0);
	    }
	}
    }
    
    /**
     * Clear the array of MIME types
     */
    public void clearMIMETypes() {
	synchronized(this.mimeTypeList) {
	    this.mimeTypeList.clear();
	}
    }

    /**
     * Add a MIME type
     */
    public void addMIMEType(String mimeType) {
	synchronized(this.mimeTypeList) {
	    if (mimeType != null) {
		for (Iterator i = this.mimeTypeList.iterator(); i.hasNext(); ) {
		    if (((String)i.next()).equals(mimeType)) {
			// Bail if we already have one
			return;
		    }
		}
		this.mimeTypeList.add(mimeType);
	    }
	}
    }

    /**
     * Get the string of semantic markup text
     * FIXME - this is currently unstructured
     * free text, which it almost certainly
     * shouldn't be.
     */
    public String getSemanticType() {
	return this.semanticType;
    }

    /**
     * Set the semantic markup as a string,
     * not the best way to do things but will
     * have to do for now
     */
    public void setSemanticType(String newSemanticType) {
	if (newSemanticType != null) {
	    this.semanticType = newSemanticType;
	}
    }

}
