/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

// Utility Imports
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.*;
import java.awt.datatransfer.*;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.InternalSinkPortHolder;
import org.embl.ebi.escience.scufl.InternalSourcePortHolder;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.SemanticMarkup;
import java.lang.RuntimeException;
import java.lang.String;



/**
 * An abstract superclass of all processor ports
 * @author Tom Oinn
 */
public abstract class Port implements Serializable {
    
    private String name = "";
    private Processor processor = null;
    private boolean isExternal = false;
    private String syntacticType = "";
    private String semanticType = "";
    private List aliases = null;
    //private String description = "";
    private SemanticMarkup metadata = null;
    
    /**
     * Create a new port (obviously you can't actually construct this 
     * because it's abstract. Names must match [a-zA-Z_0-9].
     * Lies lies lies, I removed that bit because it was breaking EMBOSS.
     * tmo
     */
    public Port(Processor processor, String name)
	throws DuplicatePortNameException,
	       PortCreationException {
	// Create a new metadata holder
	metadata = new SemanticMarkup(this);
	// Check we have no nulls
	if (processor == null) {
	    throw new PortCreationException("Invalid call to create a port, the processor was null!");
	}
	if (name == null) {
	    throw new PortCreationException("Invalid call to create a port, the name was null!");
	}
	if (name.equals("")) {
	    throw new PortCreationException("Refusing to create a port with name ''");
	}
	// Commented out - was failing with certain soaplab EMBOSS services which had names
	// like foo-2
	// tmo 30th May 2003
	/**if (Pattern.matches("\\w++",name) == false) {
	   throw new PortCreationException("Name contains an invalid character,\n"+
	   "names must match [a-zA-Z_0-9].");
	   }*/
	
	// Scan through the list of ports defined within 
	// the parent processor and check that the name
	// isn't a duplicate.
	Port[] the_ports = processor.getPorts();
	for (int i = 0; i<the_ports.length; i++) {
	    String existing_port_name = the_ports[i].getName();
	    if (existing_port_name.equalsIgnoreCase(name)) {
		throw new DuplicatePortNameException("Cannot create duplicate port name, was attempting to create '"+name+
						     "', but it already exists in processor '"+processor.getName()+"'.");
	    }
	}
	// Assign internal private members
	this.processor = processor;
	//this.name = name.toLowerCase(); //this causes problems with wsdl invocations
	this.name = name;
	fireModelEvent(new ScuflModelEvent(this, "New port created '"+name+"' in processor '"+processor.getName()+"'"));
    }
    
    /**
     * Get a reference to the SemanticMarkup container
     * associated with this port
     */
    public SemanticMarkup getMetadata() {
	return this.metadata;
    }

    /**
     * Set the free text description of the port
     */
    /**public void setDescription(String theDescription) {
	if (theDescription != null) {
	    this.description = theDescription;
	    fireModelEvent(new ScuflModelEvent(this, "Description set"));
	}
	}*/

    /**
     * Get the description for this port
     */
    /**
       public String getDescription() {
       return this.description;
       }
    */
    
    /**
     * Set the syntactic type of the port, only visible
     * within this package by default.
     */
    public void setSyntacticType(String new_type) {
	if (new_type.equals(this.syntacticType)==false) {
	    this.syntacticType = new_type;
	    fireModelEvent(new ScuflModelEvent(this, "Syntactic type changed to '"+new_type+"'"));
	}
    }

    /**
     * Get the syntactic type of the port
     */
    public String getSyntacticType() {
	return this.syntacticType;
    }
    
    /**
     * Set the semantic type of the port, only visible
     * within this package, currently unused by this
     * version of the software.
     */
    void setSemanticType(String new_type) {
	throw new RuntimeException("Semantic types are not implemented at the moment.");
    }

    /**
     * Get the semantic type of the port, not implemented
     * in this version of the spec or software.
     */
    public String getSemanticType() {
	throw new RuntimeException("Semantic types are not implemented at the moment.");
    }

    /**
     * Set the visibility of this port outside the scope of the workflow
     * @deprecated
     */
    public void setExternal(boolean external_value) {
	if (external_value != this.isExternal) {
	    fireModelEvent(new ScuflModelEvent(this,"Visibility of port changed to "+external_value));
	}
	this.isExternal = external_value;
    }

    /**
     * Is this port visible outside the workflow?
     * @deprecated
     */
    public boolean isExternal() {
	return this.isExternal;
    }

    /**
     * Is this port a workflow source?
     */
    public boolean isSource() {
	return (this.getProcessor() instanceof InternalSourcePortHolder);
    }

    /**
     * Is this port a workflow sink?
     */
    public boolean isSink() {
	return (this.getProcessor() instanceof InternalSinkPortHolder);
    }

    /**
     * Get the processor that this port belongs
     * to.
     */
    public Processor getProcessor() {
	return this.processor;
    }

    /**
     * Get the name for this port. There is no set method,
     * ports are named at creation time and the names are
     * immutable from that point onwards.
     */
    public String getName() {
	return this.name;
    }

    /**
     * Return the name as the toString() implementation
     */
    public String toString() {
	return this.getName();
    }

    /**
     * Add a name to the list of aliases used to reference 
     * this port.
     */
    public void addAlias(String name) {
	if(aliases==null)
	    aliases = new ArrayList();
	aliases.add(name);
	//Note at present no way to remove name, can't see
	//any need for it at moment - would introduce 
	//synchronization issues anyway. - djm 29/04/2003.
    }
    
    /**
     * Checks to see if supplied name is an alias for the port
     */
    public boolean isAlias(String name) {
	if(aliases!=null){
	    Iterator i = aliases.iterator();
	    while(i.hasNext()) {
		String n = (String) i.next();
		if(n.equals(name))
		    return true;
	    }
	}
	return false;
    }	
    
    /**
     * Handle model events
     */
    void fireModelEvent(ScuflModelEvent event) {
	this.processor.fireModelEvent(event);
    }

}
