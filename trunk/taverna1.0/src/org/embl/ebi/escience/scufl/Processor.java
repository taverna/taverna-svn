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
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.embl.ebi.escience.scufl.AlternateProcessor;
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
    protected ArrayList ports = new ArrayList();
    private ScuflModel model = null;
    protected int timeout = 0;
    protected int retries = 0;
    protected int retryDelay = 0;
    protected double backoff = 1.0;
    protected List alternates = new ArrayList();
    
    /**
     * Return the list of AlternateProcessor holders
     * for this primary processor implementation.
     */
    public AlternateProcessor[] getAlternatesArray() {
	return (AlternateProcessor[])alternates.toArray(new AlternateProcessor[0]);
    }

    /**
     * Return the alternates list object to allow
     * addition or reordering of alternate processors
     */
    public List getAlternatesList() {
	return this.alternates;
    }

    /**
     * Return the time in milliseconds after which
     * an instance of this processor should be regarded
     * as having failed with a timeout. If this value
     * is set to zero then no timeout applies.
     */
    public int getTimeout() {
	return this.timeout;
    }

    /**
     * Set the timeout parameter
     */
    public void setTimeout(int timeout) {
	this.timeout = timeout;
    }

    /**
     * Return the number of retries after the initial
     * invocation attempt. If set to zero then retry
     * behaviour is disabled.
     */
    public int getRetries() {
	return this.retries;
    }

    /**
     * Set the number of retries
     */
    public void setRetries(int retries) {
	this.retries = retries;
    }

    /**
     * Get the number of milliseconds to wait before
     * first retrying an invocation of the task
     * this processor represents.
     */
    public int getRetryDelay() {
	return this.retryDelay;
    }

    /**
     * Set the retry delay
     */
    public void setRetryDelay(int delay) {
	this.retryDelay = delay;
    }

    /**
     * Return the factor by which the timeout value
     * will be multiplied for each retry after the
     * first. This allows for exponential backoff
     * from failing service instances.
     */
    public double getBackoff() {
	return this.backoff;
    }

    /**
     * Set the backoff factor
     */
    public void setBackoff(double backoff) {
	this.backoff = backoff;
    }

    /**
     * The log level for this processor
     */
    int logLevel = -1;

    /**
     * Get the log level, this is the effective log level
     * of the processor taking into account possible inheritence
     * of the level from the ScuflModel instance.
     */
    public int getLogLevel() {
	if (this.getModel() == null || this.logLevel > -1) {
	    return this.logLevel;
	}
	else {
	    return this.getModel().getLogLevel();
	}
    }

    /**
     * Get the real log level set by this processor, this
     * can be -1 in which case the getLogLevel method will
     * return the log level of the ScuflModel that 'owns' 
     * this processor
     */
    public int getRealLogLevel() {
	return this.logLevel;
    }

    /**
     * Set the log level
     */
    public void setLogLevel(int level) {
	this.logLevel = level;
	fireModelEvent(new ScuflModelEvent(this, "Log level changed"));
    }

    /**
     * Return a properties object containing the processor specific
     * properties for this processor type instance. This is used by
     * the user interface code to display additional properties for
     * each processor and should be implemented by the subclasses to
     * display useful information
     */
    public abstract Properties getProperties();
    
    /**
     * Construct the processor with the given name and parent, complaining
     * if the name doesn't conform to [a-zA-Z_0-9]
     */
    public Processor(ScuflModel model, String name) 
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	// Check for nulls
	//
	//if (model == null) {
	//    throw new ProcessorCreationException("Cannot create a processor with the model as null");
	//}
	if (name == null) {
	    throw new ProcessorCreationException("Cannot create a processor with a null name");
	}
	if (name.equals("")) {
	    throw new ProcessorCreationException("Refusing to create a processor with name ''");
	}
	if (Pattern.matches("\\w++",name) == false) {
	    throw new ProcessorCreationException("Name contains an invalid character,\n"+
						 "names must match [a-zA-Z_0-9].");
	}
	// Check for duplicate names
	if (model!=null) {
	    Processor[] existing_processors = model.getProcessors();
	    for (int i = 0; i<existing_processors.length; i++) {
		Processor processor = existing_processors[i];
		if (processor.getName().equalsIgnoreCase(name)) {
		    throw new DuplicateProcessorNameException("Cannot create a processor with name '"+
							      name+"', because this name is already used in the model.");
		}
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
     * Remove a port from a processor (only really applicable 
     * to the workflow source and sink ports, so be careful
     * when you're using it)
     */
    public void removePort(Port the_port) {
	this.ports.remove(the_port);
	// Iterate over all the data constraints, removing any that have
	// this port as a source or a sink
	DataConstraint[] dc = model.getDataConstraints();
	for (int i = 0; i < dc.length; i++) {
	    if (dc[i].getSource() == the_port ||
		dc[i].getSink() == the_port) {
		if (model!=null) {
		    model.destroyDataConstraint(dc[i]);
		}
	    }
	}
	fireModelEvent(new ScuflModelEvent(this, "Removed a port"));
	
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
    protected void fireModelEvent(ScuflModelEvent event) {
	if (this.model!=null) {
	    this.model.fireModelEvent(event);
	}
    }

    /**
     * Return the processor's name in the toString()
     */
    public String toString() {
	return this.getName();
    }

}
