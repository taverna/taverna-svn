/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

// Utility Imports
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.jdom.Document;

/**
 * Represents a single scufl workflow model
 * 
 * @author Tom Oinn
 * @author Stian Soiland
 */
public class ScuflModel implements Serializable, LogAwareComponent {

	private static Logger logger = Logger.getLogger(ScuflModel.class);

	/**
	 * The processors defined by this workflow, ArrayList of Processor
	 * subclasses.
	 */
	private List<Processor> processors;

	/**
	 * An internal processor implementation to hold the overall workflow
	 * outputs, appearing as InputPort objects and acting as sinks for data from
	 * the externally visible processors.
	 */
	private InternalSinkPortHolder sinks;

	/**
	 * An internal processor implementation to hold the overall workflow source
	 * links; these appear as OutputPort objects in this processor.
	 */
	private InternalSourcePortHolder sources;

	/**
	 * The concurrency constraints defined by this workflow, ArrayList of
	 * ConcurrencyConstraint objects.
	 */
	private ArrayList<ConcurrencyConstraint> constraints;

	/**
	 * A workflow description object containing fields such as a free text
	 * description, author list etc
	 */
	private WorkflowDescription description;

	/**
	 * The data flow constraints defined by this workflow, ArrayList of
	 * DataConstraint objects.
	 */
	private ArrayList<DataConstraint> dataconstraints;

	/**
	 * The log level for the model overall
	 */
	int logLevel;

	/**
	 * Set to true if the workflow definition is being loaded in offline mode,
	 * i.e. no network activity
	 */
	boolean offline = false;

	/**
	 * The active model listeners for this model
	 */
	List<ScuflModelEventListener> listeners;

	/**
	 * Events pending to be processed by the notify thread
	 * 
	 */
	List<ScuflModelEvent> pendingEvents;

	/**
	 * Thread that processes events and pass them on to the listeners
	 */
	NotifyThread notifyThread;

	/**
	 * Whether the model should fire events to its listeners
	 */
	public boolean isFiringEvents = true;

	public ScuflModel() {
		// The event processing system (listeners/pendingEvents/notifyThread) 
		// lives throughout the lifetime of the ScuflModel. 		
		listeners = Collections
				.synchronizedList(new ArrayList<ScuflModelEventListener>());
		pendingEvents = new ArrayList<ScuflModelEvent>();
		initialize();
		notifyThread = new NotifyThread(pendingEvents, listeners);
	}

	protected void finalize() throws Throwable {
		// Stop our event thread
		if (notifyThread != null) {
			synchronized (notifyThread) {
				notifyThread.loop = false;
				notifyThread.interrupt();
			}
		}
	}

	public ScuflModel clone() throws CloneNotSupportedException {		
		Document xscufl = XScuflView.getDocument(this);		
		ScuflModel newModel = new ScuflModel();
		try {
			XScuflParser.populate(xscufl, newModel, null);
		} catch (ScuflException e) {
			logger.error("Model could not be cloned", e);
			throw new CloneNotSupportedException();
		}
		return newModel;
	}


	
	/**
	 * Initialize all members, used by constructor and clear()
	 * 
	 */
	void initialize() {
		setLogLevel(0);
		processors = new ArrayList<Processor>();
		try {
			sinks = new InternalSinkPortHolder(this);
			sources = new InternalSourcePortHolder(this);
		} catch (ScuflException e) {
			logger.error("Could not create internal sink/source holder", e);
		}
		dataconstraints = new ArrayList<DataConstraint>();
		constraints = new ArrayList<ConcurrencyConstraint>();
		description = new WorkflowDescription();		
	}

	@Override
	public String toString() {
		try {
			return "ScuflModel: " + getDescription().getTitle();
		} catch (RuntimeException ex) {
			logger.warn("Could not get title", ex);
			return super.toString();
		}
	}
	
	/**
	 * Clear the model, retaining any existing listeners but removing all model
	 * data. Restarts the notify thread.
	 */
	public void clear() {
		initialize();		
		fireModelEvent(new ScuflModelEvent(this,
				"Reset model to initial state."));		
	}

	/**
	 * Is the workflow in offline mode?
	 */
	public boolean isOffline() {
		return offline;
	}

	/**
	 * Set the online / offline status, true sets to offline, false to online
	 * (the initial value). In online mode, processors are allowed to use the
	 * network. Use offline mode to load a workflow with network problems.
	 */
	public synchronized void setOffline(boolean goOffline)
	throws SetOnlineException {
		if (goOffline == this.offline) {
			return;
		}				
		boolean originalEventStatus = this.isFiringEvents;
		try {
			this.offline = goOffline;
			if (!this.offline) {
				// We'll disable event statuses while we do lots of resets and stuff
				setEventStatus(false);			
				// Interesting case where the workflow was loaded offline
				// but is now in online mode again...							
				Document xscufl = XScuflView.getDocument(this);				
				// Now have the XML form which should be identical whether
				// loaded online or not. Can now reinstate the model
				// with the XML form, processor loaders will be aware of
				// the online status and fill in any details.
				clear();
				// Load the model in online mode with no prefix specified
				try {
					XScuflParser.populate(xscufl, this, null);
				} catch (ScuflException ex) {
					// Go back to offline-mode and reload the workflow
					clear();
					setOffline(true);
					try {
						XScuflParser.populate(xscufl, this, null);
					} catch (Exception e) {
						logger.fatal(e);
					}
					SetOnlineException soe = new SetOnlineException(
					"Unable to go online.");
					soe.initCause(ex);
					logger.error("Unable to go online");
					
					throw soe;
				}
				
			}
			// Iterate over all the processors and kick the appropriate method
			for (Processor processor : getProcessors()) {
				if (this.offline) {
					processor.setOffline();
				} else {
					processor.setOnline();
				}
			}
		} finally {			
			setEventStatus(originalEventStatus);
		}		
		// Throw a minor model event to give a hint to the UI that
		// the model online status has been changed.
		fireModelEvent(new ScuflModelEvent(this, "Offline status change"));
	}

	public int getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(int level) {
		logLevel = level;
	}

	public WorkflowDescription getDescription() {
		return description;
	}

	public void setDescription(WorkflowDescription description) {
		if (description == null) {
			throw new IllegalArgumentException("Cannot set description to null");
		}
		this.description = description;
	}

	/**
	 * Get the next valid name based on the specified arbitrary string that
	 * could be used to create a new processor. This method will strip non word
	 * characters, replacing with the '_' and append a numerical suffix if
	 * required to ensure that the name returned is both valid and not already
	 * in use
	 */
	public String getValidProcessorName(String originalName) {
		StringBuffer sb = new StringBuffer();
		String[] split = originalName.split("\\W");
		for (int i = 0; i < split.length; i++) {
			sb.append(split[i]);
			if (i < split.length - 1) {
				sb.append("_");
			}
		}
		String rootName = sb.toString();
		try {
			locateProcessor(rootName);
		} catch (UnknownProcessorException upe) {
			// Not found, so we can use this name
			return rootName;
		}
		// Otherwise will have to use a suffix
		int suffix = 0;
		while (true) {
			String name = rootName + (++suffix);
			try {
				locateProcessor(name);
			} catch (UnknownProcessorException upe) {
				return name;
			}
		}
	}

	/**
	 * Set the event reporting state, useful if you know you're going to be
	 * generating a large number of model events that actually reflect only a
	 * single change. Setting the event status to true will also fire a new
	 * model event, as this is normally only used where there have been events
	 * that were previously masked.
	 */
	public void setEventStatus(boolean reportEvents) {
		if (reportEvents == this.isFiringEvents) {
			return;
		}
		this.isFiringEvents = reportEvents;
		if (this.isFiringEvents) {
			fireModelEvent(new ScuflModelEvent(this,
					"Event reporting re-enabled, forcing update",
					ScuflModelEvent.LOAD));
		}
	}

	/**
	 * Handle a ScuflModelEvent from one of our children or self, only send an
	 * event notification if the isFiringEvents is set to true.
	 */
	public void fireModelEvent(ScuflModelEvent event) {		
		if (!isFiringEvents) {
			return;
		}		
		synchronized (pendingEvents) {
			pendingEvents.add(event);
		}
		// Poke the thread so it can process some
        synchronized (notifyThread) {            
            notifyThread.notify();
        }
	}

	/**
	 * Return all the ports that act as overal workflow inputs; in this case the
	 * workflow input ports are actually going to be instances of OutputPort,
	 * this is because they act as flow sources into the workflow. One
	 * possibility here, to make things a bit easier, would be to duplicate the
	 * ports on the input side as well, and return the corresponding InputPort
	 * instances, this presumably maps the interal processor implementation to
	 * the current way the enactor handles these workflows by creating a special
	 * input task.
	 */
	public Port[] getWorkflowSourcePorts() {
		return sources.getPorts();
	}

	/**
	 * as for the getWorkflowSourcePorts, but returns an array of ports that act
	 * as overal outputs from the workflow.
	 */
	public Port[] getWorkflowSinkPorts() {
		return sinks.getPorts();
	}

	/**
	 * Return the internal processor that represents the workflow sources.
	 */
	public Processor getWorkflowSourceProcessor() {
		return sources;
	}

	/**
	 * Return the internal processor that holds the overall workflow sink ports
	 */
	public Processor getWorkflowSinkProcessor() {
		return sinks;
	}

	/**
	 * Return an array of the Processor objects defined by this workflow model
	 */
	public Processor[] getProcessors() {
		synchronized (processors) {
			return processors.toArray(new Processor[0]);
		}
	}

	/**
	 * Returns an array of Processors that are an instance of the Class
	 * 
	 * @return
	 */
	public Processor[] getProcessorsOfType(Class type) {
		List<Processor> result = new ArrayList<Processor>();
		for (Processor p : getProcessors()) {
			if (type.isInstance(p)) {
				result.add(p);
			}
		}
		return result.toArray(new Processor[0]);
	}

	/**
	 * Crawl down the workflow locating all processors and expanding nested
	 * workflow processors recursively to get a complete list of all (non
	 * workflow) processors in the workflow. Consumes a Map to which the new
	 * processors are added as values, the key being the name of the processor
	 * in local scope with the prefix specified.
	 */
	public void collectAllProcessors(Map target, String prefix) {
		for (Processor p : getProcessors()) {
			if (p instanceof ScuflWorkflowProcessor) {
				String newPrefix = p.getName();
				if (prefix != null) {
					newPrefix = prefix + "." + newPrefix;
				}
				ScuflWorkflowProcessor wp = (ScuflWorkflowProcessor) p;
				wp.getInternalModel().collectAllProcessors(target, newPrefix);
			} else {
				if (prefix == null) {
					target.put(p.getName(), p);
				} else {
					target.put(prefix + "." + p.getName(), p);
				}
			}
		}
	}

	/**
	 * Add a processor to the model.
	 * 
	 * @throws NullPointerException
	 *             if the processor is null
	 */
	public void addProcessor(Processor processor) {
		if (processor == null) {
			throw new NullPointerException("Processor must not be null");
		}
		synchronized (processors) {
			processors.add(processor);
			fireModelEvent(new ScuflModelAddEvent(this, processor));
		}
	}

	/**
	 * Destroy a processor, this also removes any data constraints that have the
	 * processor as either a source or a sink.
	 */
	public void destroyProcessor(Processor processor) {
		synchronized (processors) {
			if (!processors.remove(processor)) {
				logger.warn("Could not destroy unknown processor: " + processor);
				return;
			}
		}
		// Iterate over all the data constraints, remove any that
		// refer to this processor.
		HashSet<Object> removed = new HashSet<Object>();
		for (DataConstraint dc : getDataConstraints()) {
			Processor source = dc.getSource().getProcessor();
			Processor sink = dc.getSink().getProcessor();
			if (source == processor || sink == processor) {
				removed.add(dc);
				dataconstraints.remove(dc);
			}
		}
		for (ConcurrencyConstraint cc : getConcurrencyConstraints()) {
			if (processor == cc.getTargetProcessor()
					|| processor == cc.getControllingProcessor()) {
				removed.add(cc);
				constraints.remove(cc);
			}
		}
		String message = "Removed " + ScuflModelEvent.getClassName(processor)
				+ " " + processor.getName() + ", and edges " + removed;
		removed.add(processor);

		fireModelEvent(new ScuflModelRemoveEvent(this, removed, message));
	}

	/**
	 * Add a data constraint to the model
	 */
	public void addDataConstraint(DataConstraint the_constraint) {
		dataconstraints.add(the_constraint);
		fireModelEvent(new ScuflModelAddEvent(this, the_constraint));
	}

	/**
	 * Remove a data constraint from the model
	 */
	public void destroyDataConstraint(DataConstraint the_constraint) {
		if (dataconstraints.remove(the_constraint)) {
			fireModelEvent(new ScuflModelRemoveEvent(this, the_constraint));
		}
	}

	/**
	 * Add a concurrency constraint to the model
	 */
	public void addConcurrencyConstraint(ConcurrencyConstraint the_constraint) {
		constraints.add(the_constraint);
		fireModelEvent(new ScuflModelAddEvent(this, the_constraint));
	}

	/**
	 * Remove a concurrency constraint from the model
	 */
	public void destroyConcurrencyConstraint(
			ConcurrencyConstraint the_constraint) {
		if (constraints.remove(the_constraint)) {
			fireModelEvent(new ScuflModelRemoveEvent(this, the_constraint));
		}
	}

	/**
	 * Return an array of the concurrency constraints defined within this
	 * workflow model
	 */
	public ConcurrencyConstraint[] getConcurrencyConstraints() {
		return constraints.toArray(new ConcurrencyConstraint[0]);
	}

	/**
	 * Return an array of data constraints defined within this workflow model
	 */
	public DataConstraint[] getDataConstraints() {
		DataConstraint[] result = dataconstraints
				.toArray(new DataConstraint[0]);
		Arrays.sort(result);
		return result;
	}

	/**
	 * Add a new ScuflModelEventListener to the listener list.
	 */
	public void addListener(ScuflModelEventListener listener) {
        if (listener == null) {
            throw new NullPointerException("Attempt to add null listener");
        }
		listeners.add(listener);
	}

	/**
	 * Remove a ScuflModelEventListener from the listener list.
	 */
	public void removeListener(ScuflModelEventListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Get an array of ScuflMovelEventListener implementors registered with this
	 * ScuflModel.
	 */
	public ScuflModelEventListener[] getListeners() {
		return listeners.toArray(new ScuflModelEventListener[0]);
	}

	/**
	 * Locate a given named port, the name is in the form [PROCESSOR]:[PORT],
	 * and is not case sensitive. If the processor part is missing, this method
	 * will attempt to locate a new external ports with the single supplied port
	 * name in either the internal sink or internal source processor.
	 */
	public Port locatePort(String port_specifier)
			throws UnknownProcessorException, UnknownPortException,
			MalformedNameException {
		String[] parts = port_specifier.split(":");
		if (parts.length < 1 || parts.length > 2) {
			throw new MalformedNameException(
					"You must supply a name of the form [PROCESSOR]:[PORT] to the locate operation");
		} else if (parts.length == 2) {
			// Should be a reference to an externally visible processor
			// port combination.
			String processor_name = parts[0];
			String port_name = parts[1];

			// Find the processor
			Processor processor = locateProcessor(processor_name);
			Port port = processor.locatePort(port_name);
			return port;
		} else if (parts.length == 1) {
			// Got a reference to an internal port
			String port_name = parts[0];
			try {
				// Look for a source port
				return sources.locatePort(port_name);
			} catch (UnknownPortException upe) {
				return sinks.locatePort(port_name);
			}
		}
		throw new MalformedNameException("Couldn't resolve port name '"
				+ port_specifier + "'.");
	}

	Port locatePortOrCreate(String port_specifier, boolean isInputPort)
			throws UnknownProcessorException, UnknownPortException,
			MalformedNameException {
		String[] parts = port_specifier.split(":");
		if (parts.length < 1 || parts.length > 2) {
			throw new MalformedNameException(
					"You must supply a name of the form [PROCESSOR]:[PORT] to the locate operation");
		} else if (parts.length == 2) {
			// Should be a reference to an externally visible processor
			// port combination.
			String processor_name = parts[0];
			String port_name = parts[1];

			// Find the processor
			Processor processor = locateProcessor(processor_name);
			Port port = processor.locatePortOrCreate(port_name, isInputPort);
			return port;
		} else if (parts.length == 1) {
			// Got a reference to an internal port
			String port_name = parts[0];
			if (isInputPort) {
				return sinks.locatePort(port_name);
			}
			return sources.locatePort(port_name);
		}
		throw new MalformedNameException("Couldn't resolve port name '"
				+ port_specifier + "'.");
	}

	/**
	 * Locate a named processor
	 */
	public Processor locateProcessor(String processor_name)
			throws UnknownProcessorException {
		for (Processor p : getProcessors()) {
			if (p.getName().equalsIgnoreCase(processor_name)) {
				return p;
			}
		}
		throw new UnknownProcessorException(
				"Unable to locate processor with name '" + processor_name + "'");
	}

	/**
	 * Create an internal model event to force an update of the model
	 */
	public void forceUpdate() {
		fireModelEvent(new ScuflModelEvent(this, "Forced update"));
	}
}

/**
 * A Processor subclass to hold ports for the overal workflow inputs. These
 * ports are therefore output ports, as they are used as data sources for links
 * into the workflow
 */
class InternalSourcePortHolder extends Processor {
	protected InternalSourcePortHolder(ScuflModel model)
			throws DuplicateProcessorNameException, ProcessorCreationException {
		super(model, "SCUFL_INTERNAL_SOURCEPORTS");
	}

	public Properties getProperties() {
		return null;
	}
}

/**
 * A Processor subclass to hold ports for the overall workflow outputs, these
 * ports are therefore held as input ports, acting as they do as data sinks.
 */
class InternalSinkPortHolder extends Processor {	

	protected InternalSinkPortHolder(ScuflModel model)
			throws DuplicateProcessorNameException, ProcessorCreationException {
		super(model, "SCUFL_INTERNAL_SINKPORTS");
	}

	public Properties getProperties() {
		return null;
	}
}

/**
 * A thread subclass to notify listeners of events. Starts itself on
 * construction, set loop=false and notify it to stop the thread.
 */
class NotifyThread extends Thread {
	private static Logger logger = Logger.getLogger(NotifyThread.class);

	// Set to false when the thread is to stop. Call .interrupt()
	// or wait max_sleep miliseconds
	public boolean loop;

	// Maximum sleep in milliseconds before looking at new events
	static int max_sleep = 10000;

	// events that should be passed to listeners
	List<ScuflModelEvent> pendingEvents;

	List<ScuflModelEventListener> listeners;

	protected NotifyThread(List<ScuflModelEvent> pendingEvents,
			List<ScuflModelEventListener> listeners) {
		super();
		// We'll keep references to the events and listeners, but not the
		// ScuflModel. That way, its destructor has a chance to stop us.
		this.pendingEvents = pendingEvents;
		this.listeners = listeners;
		setDaemon(true);
		loop = true;
		this.start();
	}

	public void run() {			
		while (loop) {
			// Are there any pending events?
			if (pendingEvents.isEmpty()) {
				try {
                    synchronized (this) {                        
                        this.wait(max_sleep);
                    }
				} catch (InterruptedException e) {
					// Awake again!
				}
				// Re-loop, might be loop==false
				continue;
			}
			List<ScuflModelEvent> events;
			synchronized (pendingEvents) {
				// Copy off the event list and clear it
				events = new ArrayList<ScuflModelEvent>(pendingEvents);
				pendingEvents.clear();
			}
			for (ScuflModelEvent event : events) {
				logger.debug("Processing event " + event);
				// Copy the listeners as well to avoid iterator failing
				for (ScuflModelEventListener l : new ArrayList<ScuflModelEventListener>(
						listeners)) {
					try {
						l.receiveModelEvent(event);
					} catch (Throwable ex) {
						logger.error("Could not notify " + l + " of event "
								+ event, ex);
					}
				}
			}
		}
	}
}
