/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

// Utility Imports
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.UnknownPortException;
import java.lang.ClassCastException;
import java.lang.String;



/**
 * An abstract superclass of the various processor subtypes
 * @author Tom Oinn
 */
public abstract class Processor implements java.io.Serializable {
    
    private String name = "";
    private String description = "";
    private ArrayList ports = new ArrayList();
    private ScuflModel model = null;

    /**
     * Construct the processor with the given name and parent
     */
    public Processor(ScuflModel model, String name) 
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	// Check for nulls
	if (model == null) {
	    throw new ProcessorCreationException("Cannot create a processor with the model as null");
	}
	if (name == null) {
	    throw new ProcessorCreationException("Cannot create a processor with a null name");
	}
	if (name.equals("")) {
	    throw new ProcessorCreationException("Refusing to create a processor with name ''");
	}
	// Check for duplicate names
	Processor[] existing_processors = model.getProcessors();
	for (int i = 0; i<existing_processors.length; i++) {
	    Processor processor = existing_processors[i];
	    if (processor.getName().equalsIgnoreCase(name)) {
		throw new DuplicateProcessorNameException("Cannot create a processor with name '"+
							  name+"', because this name is already used in the model.");
	    }
	}
	this.model = model;
	this.name = name;
	fireModelEvent(new ScuflModelEvent(this, "New processor created '"+name+"'"));
    }

    /**
     * Get the name for this processor. There is
     * no corresponding set method because names
     * are immutable once created.
     */
    public String getName() {
	return this.name;
    }

    /**
     * Get a description of the processor.
     */
    public String getDescription() {
	return this.description;
    }

    /**
     * Set the description for the processor.
     */
    public void setDescription(String the_description) {
	if (the_description.equalsIgnoreCase(this.description)==false) {
	    fireModelEvent(new ScuflModelEvent(this,"Description changed"));
	}
	this.description = the_description;
    }

    /**
     * Get an array of the ports, input or output, defined
     * within this processor.
     */
    public Port[] getPorts() {
	return (Port[])(this.ports.toArray(new Port[0]));
    }

    /**
     * Get an array of the input ports that are bound
     * by data constraints defined within this processor
     */
    public InputPort[] getBoundInputPorts() {
	ArrayList temp = new ArrayList();
	HashSet boundPorts = new HashSet();
	// Iterate over all data constraints getting their
	// sink ports, if the input port is bound then
	// it'll be in the sink port of a constraint somewhere
	DataConstraint dc[] = model.getDataConstraints();
	for (int i = 0; i < dc.length; i++) {
	    DataConstraint d = dc[i];
	    boundPorts.add(d.getSink());
	}
	for (Iterator i = this.ports.iterator(); i.hasNext(); ) {
	    try {
		InputPort ip = (InputPort)i.next();
		if (boundPorts.contains(ip) || ip.isExternal()) {
		    temp.add(ip);
		}
	    }
	    catch (ClassCastException cce) {
		//
	    }
	}
	return (InputPort[])temp.toArray(new InputPort[0]);
    }

    /**
     * Get an array of all the output ports that are bound
     * by data constraints and defined within this processor
     */
    public OutputPort[] getBoundOutputPorts() {
	ArrayList temp = new ArrayList();
	HashSet boundPorts = new HashSet();
	// Iterate over all data constraints getting their
	// source ports, if the output port is bound then
	// it'll be in the source port of a constraint somewhere
	DataConstraint dc[] = model.getDataConstraints();
	for (int i = 0; i < dc.length; i++) {
	    DataConstraint d = dc[i];
	    boundPorts.add(d.getSource());
	}
	for (Iterator i = this.ports.iterator(); i.hasNext(); ) {
	    try {
		OutputPort op = (OutputPort)i.next();
		if (boundPorts.contains(op) || op.isExternal()) {
		    temp.add(op);
		}
	    }
	    catch (ClassCastException cce) {
		//
	    }
	}
	return (OutputPort[])temp.toArray(new OutputPort[0]);
    }


    /**
     * Find a particular named port
     */
    public Port locatePort(String port_name) 
	throws UnknownPortException {
	for (Iterator i = ports.iterator(); i.hasNext(); ) {
	    Port p = (Port)i.next();
	    if (p.getName().equalsIgnoreCase(port_name)) {
		return p;
	    }
	}
	throw new UnknownPortException("Unable to find the port with name '"+port_name+"' in '"+getName()+"'");
    }

    /**
     * Get an array containing only input ports
     */
    public InputPort[] getInputPorts() {
	ArrayList temp = new ArrayList();
	for (Iterator i = this.ports.iterator(); i.hasNext(); ) {
	    try {
		InputPort ip = (InputPort)i.next();
		temp.add(ip);
	    }
	    catch (ClassCastException cce) {
		//
	    }
	}
	return (InputPort[])(temp.toArray(new InputPort[0]));
    }
    
    /**
     * Get an array containing only output ports
     */
    public OutputPort[] getOutputPorts() {
	ArrayList temp = new ArrayList();
	for (Iterator i = this.ports.iterator(); i.hasNext(); ) {
	    try {
		OutputPort op = (OutputPort)i.next();
		temp.add(op);
	    }
	    catch (ClassCastException cce) {
		//
	    }
	}
	return (OutputPort[])(temp.toArray(new OutputPort[0]));
    }

    /**
     * Add a new port to this processor
     */
    public void addPort(Port the_port) {
	// Do not add duplicates
	if (this.ports.contains(the_port)) {
	    return;
	}
	// Do not add a port unless the port thinks we own it
	if (the_port.getProcessor()!=this) {
	    return;
	}
	// Add the port
	this.ports.add(the_port);
    }

    /**
     * Get the parent model
     */
    public ScuflModel getModel() {
	return this.model;
    }

    /**
     * Fire a change event back to the model
     */
    void fireModelEvent(ScuflModelEvent event) {
	this.model.fireModelEvent(event);
    }

    /**
     * Return the processor's name in the toString()
     */
    public String toString() {
	return this.getName();
    }

}
