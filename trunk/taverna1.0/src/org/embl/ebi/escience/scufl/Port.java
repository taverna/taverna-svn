/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * An abstract superclass of all processor ports
 * @author Tom Oinn
 */
public abstract class Port implements java.io.Serializable {

    private String name = "";
    private Processor processor = null;
    private boolean isExternal = false;
    private String syntacticType = "";
    private String semanticType = "";

    /**
     * Create a new port (obviously you can't actually construct this 
     * because it's abstract... All names are converted to lower case!
     */
    public Port(Processor processor, String name)
	throws DuplicatePortNameException,
	       PortCreationException {
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
	this.name = name.toLowerCase();
	fireModelEvent(new ScuflModelEvent(this, "New port created '"+name+"' in processor '"+processor.getName()+"'"));
    }
    
    /**
     * Set the syntactic type of the port, only visible
     * within this package by default.
     */
    void setSyntacticType(String new_type) {
	if (new_type.equals(this.syntacticType)==false) {
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
     */
    public void setExternal(boolean external_value) {
	if (external_value != this.isExternal) {
	    fireModelEvent(new ScuflModelEvent(this,"Visibility of port changed to "+external_value));
	}
	this.isExternal = external_value;
    }

    /**
     * Is this port visible outside the workflow?
     */
    public boolean isExternal() {
	return this.isExternal;
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
     * Handle model events
     */
    void fireModelEvent(ScuflModelEvent event) {
	this.processor.fireModelEvent(event);
    }

}
