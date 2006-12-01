/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.LSIDProvider;
import org.jdom.Element;

/**
 * A container class for the metadata about a workflow definition, including
 * LSID if available, description and author information
 * 
 * @author Tom Oinn
 */
public class WorkflowDescription {

	// Just for numbering the untitled workflows, really
	private static int instanceCounter = 0;

	public static String DEFAULT_TITLE = "Untitled workflow";
	
	private String text = "";

	private String lsid = "";

	private String author = "";

	private String title = DEFAULT_TITLE + " #" + ++instanceCounter;

	/**
	 * Override the default constructor to set an LSID by default from the
	 * assigning service if one has been defined in the global configuration
	 */
	public WorkflowDescription() {
		// Assign a new LSID by default from any configured assigning service
		LSIDProvider p = DataThing.SYSTEM_DEFAULT_LSID_PROVIDER;
		if (p != null) {
			this.lsid = p.getID(LSIDProvider.WFDEFINITION);
		}
	}

	/**
	 * Get a short descriptive name, not guaranteed to be unique, for this
	 * workflow.
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Set the title for this workflow
	 */
	public void setTitle(String theTitle) {
		if (theTitle == null) {
			throw new IllegalArgumentException("Cannot set title to null");
		}
		String trimmedTitle = theTitle.trim();
		if (!trimmedTitle.equals("")) {
			this.title = trimmedTitle;
		}
	}

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
			throw new RuntimeException(
					"Cannot set new textual description to null");
		}
		this.text = newText;
	}

	/**
	 * Get the LSID for this workflow, if no LSID value has been assigned this
	 * will return the empty string.
	 */
	public String getLSID() {
		return this.lsid;
	}

	/**
	 * Set the LSID value, to clear this value set it to the empty string.
	 */
	public void setLSID(String newLSID) {
		if (newLSID == null) {
			throw new RuntimeException("Cannot set new LSID value to null");
		}
		this.lsid = newLSID;
	}

	/**
	 * Get the author string for this workflow, at the moment this is just
	 * treated as a single string, we may move to the myGrid information model
	 * person type at some point
	 */
	public String getAuthor() {
		return this.author;
	}

	/**
	 * Set the author string for this workflow
	 */
	public void setAuthor(String newAuthor) {
		if (newAuthor == null) {
			throw new IllegalArgumentException("Cannot set author to null");
		}
		this.author = newAuthor;
	}

	/**
	 * Get the XML element corresponding to the referenced WorkflowDescription
	 * object
	 */
	public static Element getElement(WorkflowDescription theDescription) {
		Element descriptionElement = new Element("workflowdescription",
				XScufl.XScuflNS);
		descriptionElement.setText(theDescription.getText());
		descriptionElement.setAttribute("lsid", theDescription.getLSID());
		descriptionElement.setAttribute("author", theDescription.getAuthor());
		descriptionElement.setAttribute("title", theDescription.getTitle());
		return descriptionElement;
	}

	/**
	 * Construct a new WorkflowDescription object from a JDom Element
	 */
	public static WorkflowDescription build(Element theElement) {
		try {
			Class.forName("org.embl.ebi.escience.baclava.DataThing");
		} catch (Exception e) {
			//
		}
		WorkflowDescription description = new WorkflowDescription();
		description.setText(theElement.getTextTrim());
		description.setAuthor(theElement.getAttributeValue("author", ""));
		description.setLSID(theElement.getAttributeValue("lsid", ""));
		description.setTitle(theElement.getAttributeValue("title", ""));
		if (description.lsid.equals("")) {
			// Assign a new LSID by default from any configured assigning
			// service
			LSIDProvider p = DataThing.SYSTEM_DEFAULT_LSID_PROVIDER;
			if (p != null) {
				description.lsid = p.getID(LSIDProvider.WFDEFINITION);
			}
		}
		return description;
	}

}
