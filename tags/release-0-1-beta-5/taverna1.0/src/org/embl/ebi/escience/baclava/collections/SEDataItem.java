/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.collections;

/**
 * A simple wrapper around Object that has placeholders
 * for MIME type and a chunk of arbitrary metadata, probably
 * in RDF format, and held as a String.
 * @author Tom Oinn
 */
public class SEDataItem {
    
    private Object userObject = null;
    private String mimeType = "";
    private String metaData = "";
    
    /**
     * Create a new data wrapper around the supplied user
     * object, assigning no metadata to it.
     */
    public SEDataItem(Object userObject) {
	this.userObject = userObject;
    }

    /**
     * Create a new data wrapper around the supplied user
     * object, attaching the specified mimeType and
     * metaData fields.
     */
    public SEDataItem(Object userObject,
		      String mimeType,
		      String metaData) {
	this.userObject = userObject;
	this.mimeType = mimeType;
	this.metaData = metaData;
    }
    
    /**
     * Return the user object that this wrapper contains
     */
    public Object getUserObject() {
	return this.userObject;
    }

    /**
     * Return the mime type of this wrapper's data, if
     * not assigned then this returns the empty string.
     */
    public String getMimeType() {
	return this.mimeType;
    }

    /**
     * Return the metadata about this wrapper's data object,
     * if none has been supplied then return the empty string
     */
    public String getMetaData() {
	return this.metaData;
    }

}
