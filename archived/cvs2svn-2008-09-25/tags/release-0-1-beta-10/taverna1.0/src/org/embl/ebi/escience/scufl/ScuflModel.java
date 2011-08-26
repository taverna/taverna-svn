/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

// Utility Imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.io.*;
import java.awt.datatransfer.*;

import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.LogAwareComponent;
import org.embl.ebi.escience.scufl.MalformedNameException;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.UnknownPortException;
import org.embl.ebi.escience.scufl.UnknownProcessorException;
import java.lang.String;
import java.lang.Thread;



/**
 * Represents a single scufl workflow model
 * @author Tom Oinn
 */
public class ScuflModel
    implements Serializable,
	       LogAwareComponent {

    /**
     * The log level for the model overall
     */
    int logLevel = 0;

    /**
     * Get the log level
     */
    public int getLogLevel() {
	return this.logLevel;
    }

    /**
     * Set the log level
     */
    public void setLogLevel(int level) {
	this.logLevel = level;
    }

    /**
     * Whether the model should fire events to its listeners
     */
    public boolean isFiringEvents = true;

    /**
     * The active model listeners for this model
     */
    List listeners = Collections.synchronizedList(new ArrayList());

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
     * Get the next valid name based on the specified arbitrary string
     * that could be used to create a new processor. This method will
     * strip non word characters, replacing with the '_' and append
     * a numerical suffix if required to ensure that the name returned
     * is both valid and not already in use
     */
    public String getValidProcessorName(String originalName) {
	int suffix = 0;
	StringBuffer sb = new StringBuffer();
	String[] split = originalName.split("\\W");
	for (int i = 0; i < split.length; i++) {
	    sb.append(split[i]);
	    if (i < split.length-1) {
		sb.append("_");
	    }
	}
	String rootName = sb.toString();
	try {
	    Processor testExists = locateProcessor(rootName);
	}
	catch (UnknownProcessorException upe) {
	    // Not found, so we can use this name
	    return rootName;
	}
	// Otherwise will have to use a suffix
	while (true) {
	    try {
		Processor testExists = locateProcessor(rootName+(++suffix));
	    }
	    catch (UnknownProcessorException upe) {
		return rootName+suffix;
	    }
	}
    }

    /**
     * Set the event reporting state, useful if you know you're going
     * to be generating a large number of model events that actually
     * reflect only a single change. Setting the event status to true
     * will also fire a new model event, as this is normally only used
     * where there have been events that were previously masked.
     */
    public void setEventStatus(boolean reportEvents) {
	if (reportEvents == this.isFiringEvents) {
	    return;
	}
	else {
	    this.isFiringEvents = reportEvents;
	    if (this.isFiringEvents) {
		fireModelEvent(new ScuflModelEvent(this,"Event reporting re-enabled, forcing update", ScuflModelEvent.LOAD));
	    }
	}
    }

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
     * Clear the model, retaining any existing listeners but
     * removing all model data.
     */
    public void clear() {
	try {
	    this.sinks = new InternalSinkPortHolder(this);
	    this.sources = new InternalSourcePortHolder(this);
	    this.dataconstraints = new ArrayList();
	    this.constraints = new ArrayList();
	    this.processors = new ArrayList();
	    this.setLogLevel(0);
	    fireModelEvent(new ScuflModelEvent(this, "Reset model to initial state."));
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
     * Add a processor to the model.
     *
     * @throws NullPointerException if the processor is null
     */
    public void addProcessor(Processor processor) {
      if(processor == null) {
        throw new NullPointerException("Processor must not be null");
      }
      
	synchronized(this.processors) {
	    this.processors.add(processor);
	    processor.firingEvents = true;
	    fireModelEvent(new ScuflModelEvent(processor, "Added processor '"+processor.getName()+"' to the model"));
	}
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
	ConcurrencyConstraint[] cc = getConcurrencyConstraints();
	for (int i = 0; i < cc.length; i++) {
	    if (the_processor == cc[i].getTargetProcessor() ||
		the_processor == cc[i].getControllingProcessor()) {
		destroyConcurrencyConstraint(cc[i]);
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
     * Remove a concurrency constraint from the model
     */
    public void destroyConcurrencyConstraint(ConcurrencyConstraint the_constraint) {
	this.constraints.remove(the_constraint);
	fireModelEvent(new ScuflModelEvent(this, "Removed concurrency constraint '"+the_constraint.getName()+"' to the model"));
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
     * Remove a ScuflModelEventListener from the listener list.
     */
    public void removeListener(ScuflModelEventListener listener) {
	this.listeners.remove(listener);
    }

    /**
     * Get an array of ScuflMovelEventListener implementors registered
     * with this ScuflModel.
     */
    public ScuflModelEventListener[] getListeners() {
	return (ScuflModelEventListener[])(this.listeners.toArray(new ScuflModelEventListener[0]));
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
     * Handle a ScuflModelEvent from one of our children or self, only
     * send an event notification if the isFiringEvents is set to true.
     */
    void fireModelEvent(ScuflModelEvent event) {
	if (this.isFiringEvents) {
	    synchronized(pendingEventList) {
		pendingEventList.add(event);
	    }
	    eventThread.interrupt();
	}
    }

    /**
     * Create an internal model event to force an update of the model
     */
    public void forceUpdate() {
	fireModelEvent(new ScuflModelEvent(this, "Forced update"));
    }	

    Thread eventThread = new NotifyThread(this);
    List pendingEventList = new ArrayList();

    /**
     * A thread subclass to notify listeners of an event
     */
    class NotifyThread extends Thread {
	private ScuflModelEvent event;
	private List listeners;
	protected NotifyThread(ScuflModel model) {
	    super();
	    this.listeners = model.listeners;
	    this.start();
	}
	public void run() {
	    while (true) {
		// Are there any pending events?
		if (pendingEventList == null ||
		    pendingEventList.isEmpty()) {
		    try {
			Thread.sleep(10000);
		    }
		    catch (InterruptedException ie) {
			//
		    }
		}
		else {
		    ScuflModelEvent[] events;
		    synchronized(pendingEventList) {
			// Copy the event list across to an array of events and
			// clear it
			events = (ScuflModelEvent[])pendingEventList.toArray(new ScuflModelEvent[0]);
			pendingEventList.clear();
		    }
		    for (int i = 0; i < events.length; i++) {
			System.out.println(events[i].toString());
			for (Iterator j = listeners.iterator(); j.hasNext();) {
			    ScuflModelEventListener l = (ScuflModelEventListener)j.next();
			    try {
				l.receiveModelEvent(events[i]);
			    }
			    catch (Exception ex) {
				ex.printStackTrace();
			    }
			}
		    }
		}
	    }
	}
    }

}
/**
 * A thread subclass to notify listeners of an event
 */
class NotifyThread extends Thread {
    private ScuflModelEvent event;
    private List listeners;
    protected NotifyThread(ScuflModelEvent event, ScuflModel model) {
	super();
	this.event = event;
	this.listeners = model.listeners;
	this.start();
    }
    public void run() {
	for (Iterator i = listeners.iterator(); i.hasNext();) {
	    ScuflModelEventListener l = (ScuflModelEventListener)i.next();
	    //System.out.println("Firing event to "+l.toString());
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
	firingEvents = true;
    }
    public Properties getProperties() {
	return null;
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
	firingEvents = true;
    }
    public Properties getProperties() {
	return null;
    }
}
