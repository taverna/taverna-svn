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
     * An internal processor implementation to hold the overall
     * workflow source links; these appear as OutputPort objects
     * in this processor.
     */
    private InternalSourcePortHolder sources = null;
    
    /** 
     * An internal processor implementation to hold the overall 
     * workflow outputs, appearing as InputPort objects and acting
     * as sinks for data from the externally visible processors.
     */
    private InternalSinkPortHolder sinks = null;

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
     * Default constructor, creates internal port holders
     */
    public ScuflModel() {
	try {
	    this.sinks = new InternalSinkPortHolder(this);
	    this.sources = new InternalSourcePortHolder(this);
	}
	catch (ProcessorCreationException pce) {
	    //
	}
	catch (DuplicateProcessorNameException dpne) {
	    //
	}
    }

    /**
     * Return all the ports that act as overal workflow inputs; 
     * in this case the workflow input ports are actually going
     * to be instances of OutputPort, this is because they act
     * as flow sources into the workflow.
     * One possibility here, to make things a bit easier, would
     * be to duplicate the ports on the input side as well, and
     * return the corresponding InputPort instances, this presumably
     * maps the interal processor implementation to the current
     * way the enactor handles these workflows by creating a special
     * input task.
     */
    public Port[] getWorkflowSourcePorts() {
	return this.sources.getPorts();
    }

    /**
     * as for the getWorkflowSourcePorts, but returns
     * an array of ports that act as overal outputs from
     * the workflow.
     */
    public Port[] getWorkflowSinkPorts() {
	return this.sinks.getPorts();
    }

    /**
     * Return the internal processor that represents the
     * workflow sources.
     */
    public Processor getWorkflowSourceProcessor() {
	return this.sources;
    }

    /**
     * Return the internal processor that holds the overall
     * workflow sink ports
     */
    public Processor getWorkflowSinkProcessor() {
	return this.sinks;
    }
	
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
     * Destroy a processor, this also removes any data constraints
     * that have the processor as either a source or a sink.
     */
    public void destroyProcessor(Processor the_processor) {
	this.processors.remove(the_processor);
	// Iterate over all the data constraints, remove any that
	// refer to this processor.
	DataConstraint[] constraints = getDataConstraints();
	for (int i = 0; i < constraints.length; i++) {
	    Processor source = constraints[i].getSource().getProcessor();
	    Processor sink = constraints[i].getSink().getProcessor();
	    if (source == the_processor || sink == the_processor) {
		destroyDataConstraint(constraints[i]);
	    }
	}
	fireModelEvent(new ScuflModelEvent(this, "Destroyed processor '"+the_processor.getName()+"'"));
	
    }

    /**
     * Add a data constraint to the model
     */
    public void addDataConstraint(DataConstraint the_constraint) {
	this.dataconstraints.add(the_constraint);
	fireModelEvent(new ScuflModelEvent(this, "Added data constraint '"+the_constraint.getName()+"' to the model"));
    }

    /**
     * Remove a data constraint from the model
     */
    public void destroyDataConstraint(DataConstraint the_constraint) {
	this.dataconstraints.remove(the_constraint);
	fireModelEvent(new ScuflModelEvent(this, "Removed data constraint '"+the_constraint.getName()+"'"));
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
     * If the processor part is missing, this method will
     * attempt to locate a new external ports with the single
     * supplied port name in either the internal sink or 
     * internal source processor.
     */
    public Port locatePort(String port_specifier)
	throws UnknownProcessorException,
	       UnknownPortException,
	       MalformedNameException {
	String[] parts = port_specifier.split(":");
	if (parts.length < 1 || parts.length > 2) {
	    throw new MalformedNameException("You must supply a name of the form [PROCESSOR]:[PORT] to the locate operation");
	}
	else if (parts.length == 2) {
	    // Should be a reference to an externally visible processor
	    // port combination.
	    String processor_name = parts[0];
	    String port_name = parts[1];
	    
	    // Find the processor
	    Processor processor = locateProcessor(processor_name);
	    Port port = processor.locatePort(port_name);
	    return port;
	}
	else if (parts.length == 1) {
	    // Got a reference to an internal port
	    Port port = null;
	    String port_name = parts[0];
	    try {
		// Look for a source port
		return this.sources.locatePort(port_name);
	    }
	    catch (UnknownPortException upe) {
		return this.sinks.locatePort(port_name);
	    }
	}
	throw new MalformedNameException("Couldn't resolver port name '"+port_specifier+"'.");
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
/**
 * A Processor subclass to hold ports for the overal workflow inputs. These
 * ports are therefore output ports, as they are used as data sources for
 * links into the workflow
 */
class InternalSourcePortHolder extends Processor implements java.io.Serializable {
    protected InternalSourcePortHolder(ScuflModel model) 
	throws DuplicateProcessorNameException,
	       ProcessorCreationException {
	super(model,"SCUFL_INTERNAL_SOURCEPORTS");
    }
}
/**
 * A Processor subclass to hold ports for the overall workflow outputs, these
 * ports are therefore held as input ports, acting as they do as data sinks.
 */
class InternalSinkPortHolder extends Processor implements java.io.Serializable {
    protected InternalSinkPortHolder(ScuflModel model) 
	throws DuplicateProcessorNameException,
	       ProcessorCreationException {
	super(model,"SCUFL_INTERNAL_SINKPORTS");
    }
}
