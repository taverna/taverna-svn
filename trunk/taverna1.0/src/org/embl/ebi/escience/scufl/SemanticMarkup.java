/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import java.util.*;
import org.jdom.*;

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
     * Get hold of the List used to hold the MIME types,
     * useful for UI components.
     */
    public List getMIMETypeList() {
	return this.mimeTypeList;
    }

    /**
     * Set the free text description
     */
    public void setDescription(String theDescription) {
	this.description = theDescription;
	fireModelEvent();
    }

    /**
     * Get the free text description
     */
    public String getDescription() {
	return this.description;
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
	    fireModelEvent();
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
		fireModelEvent();
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
	fireModelEvent();
    }

    /**
     * Configure this markup object from the supplied
     * XML element. This is assuming that the element
     * passed in is the 'metadata' element in the XScufl
     * namespace.
     */
    public void configureFromElement(Element theElement) {
	// Do mime types
	Element mimeTypeListElement = theElement.getChild("mimeTypes",XScufl.XScuflNS);
	if (mimeTypeListElement != null) {
	    for (Iterator i = mimeTypeListElement.getChildren("mimeType",XScufl.XScuflNS).iterator(); i.hasNext(); ) {
		Element typeElement = (Element)i.next();
		addMIMEType(typeElement.getTextTrim());
	    }
	}
	// Do description
	Element descriptionElement = theElement.getChild("description",XScufl.XScuflNS);
	if (descriptionElement != null) {
	    this.description = descriptionElement.getTextTrim();
	}
	// Do semantic type
	Element semanticTypeElement = theElement.getChild("semanticType",XScufl.XScuflNS);
	if (semanticTypeElement != null) {
	    this.semanticType = semanticTypeElement.getTextTrim();
	}
	fireModelEvent();
    }

    /**
     * Emit an element that would be used to configure
     * this object in the method above
     */
    public Element getConfigurationElement() {
	Element topElement = new Element("metadata",XScufl.XScuflNS);
	// Store MIME types
	Element mimeTypeList = new Element("mimeTypes",XScufl.XScuflNS);
	topElement.addContent(mimeTypeList);
	synchronized(this.mimeTypeList) {
	    for (Iterator i = this.mimeTypeList.iterator(); i.hasNext(); ) {
		Element typeElement = new Element("mimeType",XScufl.XScuflNS);
		typeElement.setText((String)i.next());
		mimeTypeList.addContent(typeElement);
	    }
	}
	// Store free text description
	Element descriptionElement = new Element("description",XScufl.XScuflNS);
	topElement.addContent(descriptionElement);
	descriptionElement.setText(this.description);
	// Store semantic type, still as text for now
	Element semanticTypeElement = new Element("semanticType",XScufl.XScuflNS);
	topElement.addContent(semanticTypeElement);
	semanticTypeElement.setText(this.semanticType);
	
	return topElement;
    }

    /**
     * If the subject of this metadata is a port then fire events off
     * when things are changed, if not then tough, we don't know how
     * to.
     */
    void fireModelEvent() {
	if (this.subject instanceof Port) {
	    ((Port)this.subject).fireModelEvent(new ScuflModelEvent(this, "Metadata change"));
	}
    }

}
