/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import java.util.ArrayList;
import java.util.Iterator;

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
    
    /** The concurrency constraints defined by this workflow */
    private ConcurrencyConstraint[] constraints;
    
    /** The data flow constraints defined by this workflow */
    private DataConstraint[] dataconstraints;
    
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
    public addProcessor(Processor the_processor) {
	this.processors.add(the_processor);
	fireModelEvent(this, "Added processor '"+the_processor.getName()+"' to the model")
    }

    /**
     * Return an array of the concurrency constraints
     * defined within this workflow model
     */
    public ConcurrencyConstraint[] getConcurrencyConstraints() {
	return this.constraints;
    }

    /**
     * Return an array of data constraints defined
     * within this workflow model
     */
    public DataConstraint[] getDataConstraints() {
	return this.dataconstraints;
    }
   
    /**
     * Add a new ScuflModelEventListener to the listener
     * list.
     */
    public void addListener(ScuflModelEventListener listener) {
	this.listeners.add(listener);
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
    
