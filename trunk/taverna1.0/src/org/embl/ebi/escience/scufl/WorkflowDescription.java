/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import org.jdom.*;

/**
 * A container class for the metadata about a workflow
 * definition, including LSID if available, description
 * and author information
 * @author Tom Oinn
 */
public class WorkflowDescription {
    
    private String text = "";
    private String lsid = "";
    private String author = "";

    /**
     * Get the free text description for this workflow
     */
    public String getText() {
	return this.text;
    }
    
    /**
     * Set the free text description for this workflow
     */
    public void setText(String newText) {
	if (newText == null) {
	    throw new RuntimeException("Cannot set new textual description to null");
	}
	this.text = newText;
    }

    /**
     * Get the LSID for this workflow, if no LSID value has
     * been assigned this will return the empty string.
     */
    public String getLSID() {
	return this.lsid;
    }

    /**
     * Set the LSID value, to clear this value set it to the
     * empty string.
     */
    public void setLSID(String newLSID) {
	if (newLSID == null) {
	    throw new RuntimeException("Cannot set new LSID value to null");
	}
	this.lsid = newLSID;
    }

    /**
     * Get the author string for this workflow, at the moment
     * this is just treated as a single string, we may move to the
     * myGrid information model person type at some point
     */
    public String getAuthor() {
	return this.author;
    }

    /**
     * Set the author string for this workflow
     */
    public void setAuthor(String newAuthor) {
	this.author = newAuthor;
    }
    
    /**
     * Get the XML element corresponding to the
     * referenced WorkflowDescription object
     */
    public static Element getElement(WorkflowDescription theDescription) {
	Element descriptionElement = new Element("workflowdescription",XScufl.XScuflNS);
	descriptionElement.setText(theDescription.getText());
	descriptionElement.setAttribute("lsid",theDescription.getLSID());
	descriptionElement.setAttribute("author",theDescription.getAuthor());
	return descriptionElement;
    }

    /**
     * Construct a new WorkflowDescription object
     * from a JDom Element
     */
    public static WorkflowDescription build(Element theElement) {
	WorkflowDescription description = new WorkflowDescription();
	description.setText(theElement.getTextTrim());
	description.setAuthor(theElement.getAttributeValue("author",""));
	description.setLSID(theElement.getAttributeValue("lsid",""));
	return description;
    }
    
}
