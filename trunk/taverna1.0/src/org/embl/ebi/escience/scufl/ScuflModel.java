/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

// Utility Imports
import java.util.ArrayList;
import java.util.Iterator;

import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import java.lang.String;



/**
 * Represents a single scufl workflow model
 * @author Tom Oinn
 */
public class ScuflModel implements java.io.Serializable {

    /**
     * The active model listeners for this model
     */
    private ArrayList listeners = new ArrayList();
    
    /** 
     * The processors defined by this workflow,
     * ArrayList of Processor subclasses.
     */
    private ArrayList processors = new ArrayList();
    
    /** 
     * The concurrency constraints defined by this workflow, 
     * ArrayList of ConcurrencyConstraint objects.
     */
    private ArrayList constraints = new ArrayList();
    
    /** 
     * The data flow constraints defined by this workflow,
     * ArrayList of DataConstraint objects.
     */
    private ArrayList dataconstraints = new ArrayList();
    
    /**
     * Return an array of the Processor objects
     * defined by this workflow model
     */
    public Processor[] getProcessors() {
	return (Processor[])(this.processors.toArray(new Processor[0]));
    }
    
    /**
     * Return an array of all Port subclasses that are
     * flagged as being exposed to the outside world. This
     * corresponds to the list of overall workflow inputs
     * and outputs.
     */
    public Port[] getExternalPorts() {
	ArrayList externalPortList = new ArrayList();
	// Iterate over processors
	for (Iterator i = this.processors.iterator(); i.hasNext(); ) {
	    Processor processor = (Processor)i.next();
	    // Iterate over ports within that processor
	    for (int j = 0; j < processor.getPorts().length; j++) {
		Port port = processor.getPorts()[j];
		if (port.isExternal()) {
		    // If the port is flagged as being external
		    // then add it to the list
		    externalPortList.add(port);
		}
	    }
	}
	return (Port[])(externalPortList.toArray(new Port[0]));
    }

    /**
     * Add a processor to the model
     */
    public void addProcessor(Processor the_processor) {
	this.processors.add(the_processor);
	fireModelEvent(new ScuflModelEvent(this, "Added processor '"+the_processor.getName()+"' to the model"));
    }

    /**
     * Add a data constraint to the model
     */
    public void addDataConstraint(DataConstraint the_constraint) {
	this.dataconstraints.add(the_constraint);
	fireModelEvent(new ScuflModelEvent(this, "Added data constraint '"+the_constraint.getName()+"' to the model"));
    }

    /**
     * Add a concurrency constraint to the model
     */
    public void addConcurrencyConstraint(ConcurrencyConstraint the_constraint) {
	this.constraints.add(the_constraint);
	fireModelEvent(new ScuflModelEvent(this, "Added concurrency constraint '"+the_constraint.getName()+"' to the model"));
    }

    /**
     * Return an array of the concurrency constraints
     * defined within this workflow model
     */
    public ConcurrencyConstraint[] getConcurrencyConstraints() {
	return (ConcurrencyConstraint[])(this.constraints.toArray(new ConcurrencyConstraint[0]));
    }

    /**
     * Return an array of data constraints defined
     * within this workflow model
     */
    public DataConstraint[] getDataConstraints() {
	return (DataConstraint[])(this.dataconstraints.toArray(new DataConstraint[0]));
    }
   
    /**
     * Add a new ScuflModelEventListener to the listener
     * list.
     */
    public void addListener(ScuflModelEventListener listener) {
	this.listeners.add(listener);
    }

    /**
     * Locate a given named port, the name is in the form
     * [PROCESSOR]:[PORT], and is not case sensitive.
     */
    public Port locatePort(String port_specifier)
	throws UnknownProcessorException,
	       UnknownPortException,
	       MalformedNameException {
	String[] parts = port_specifier.split(":");
	if (parts.length != 2) {
	    throw new MalformedNameException("You must supply a name of the form [PROCESSOR]:[PORT] to the locate operation");
	}
	String processor_name = parts[0];
	String port_name = parts[1];
	
	// Find the processor
	Processor processor = locateProcessor(processor_name);
	Port port = processor.locatePort(port_name);
	return port;
    }
    
    /**
     * Locate a named processor
     */
    public Processor locateProcessor(String processor_name) 
	throws UnknownProcessorException {
	for (Iterator i = processors.iterator(); i.hasNext(); ) {
	    Processor p = (Processor)i.next();
	    if (p.getName().equalsIgnoreCase(processor_name)) {
		return p;
	    }
	}
	throw new UnknownProcessorException("Unable to locate processor with name '"+processor_name+"'");
    }

    /**
     * Handle a ScuflModelEvent from one of our children
     */
    void fireModelEvent(ScuflModelEvent event) {
	// Should notify any listeners at this point
	for (Iterator i = this.listeners.iterator(); i.hasNext();) {
	    ScuflModelEventListener l = (ScuflModelEventListener)i.next();
	    l.receiveModelEvent(event);
	}
    }

}
    
