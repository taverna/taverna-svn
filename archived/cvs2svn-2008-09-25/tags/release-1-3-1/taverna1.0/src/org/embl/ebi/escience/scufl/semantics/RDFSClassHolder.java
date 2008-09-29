/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.semantics;

/**
 * Provides a trivial string holder for a Class
 * in the RDFS syntax. We need it so that tree
 * handlers can correctly distinguish between classes
 * and text and handle selection events appropriately.
 * @author Tom Oinn
 */
public class RDFSClassHolder {
 
    private String className = "";
   
    public RDFSClassHolder(String className) {
	this.className = className;
    }
    
    public String getClassName() {
	return this.className;
    }

    public String toString() {
	String[] parts = getClassName().split("#");
	if (parts.length == 2) {
	    return parts[1];
	}
	else {
	    return getClassName();
	}
    }

}
