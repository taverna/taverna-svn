/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * A port that exposes output data on behalf of a processor
 * @author Tom Oinn
 */
public class OutputPort extends Port implements java.io.Serializable {
    
    public OutputPort(Processor processor, String name) 
	throws DuplicatePortNameException,
	       PortCreationException {
	super(processor, name);
    }

}
