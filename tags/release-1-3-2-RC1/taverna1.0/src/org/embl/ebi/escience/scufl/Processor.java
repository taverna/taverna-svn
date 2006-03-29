/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

// Utility Imports
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;



/**
 * An abstract superclass of the various processor subtypes
 * @author Tom Oinn
 */
public abstract class Processor implements Serializable, IProcessor {

    private String name = "";
    private String description = "";
    protected ArrayList ports = new ArrayList();
    private ScuflModel model = null;
    protected int timeout = 0;
    protected int retries = 0;
    protected int retryDelay = 0;
    protected double backoff = 1.0;
    /** If a processor is critical its execution failure will fail the workflow */
    protected boolean critical = false;
    protected List alternates = new ArrayList();
    //final public static DataFlavor FLAVOR =
    //	new DataFlavor(Processor.class, "Procesor");
    //static DataFlavor[] flavors = { FLAVOR };
    Processor parentProcessor = null;
    protected List templates = new ArrayList();
    protected IterationStrategy iterationStrategy = null;
    public boolean firingEvents = false;
    private boolean breakpoint=false;
    protected boolean boring = false;
    
    /**
     * Is this processor boring? If so it shouldn't show up
     * by default in the status display
     */
    public boolean isBoring() {
	return this.boring;
    }

    /**
     * Set whether this processor is boring
     */
    public void setBoring(boolean boring) {
	if (boring != this.boring) {
	    this.boring = boring;
	    fireModelEvent(new MinorScuflModelEvent(this, "Boringness changed!"));
	}
    }

    /**
     * Allow subclasses to intercept requests to go offline and
     * online, allows e.g. the workflow processor to delegate
     * this to the child workflow
     */
    public void setOnline() {
	//
    }
    public void setOffline() {
	//
    }


    /**
     * Check for a breakpoint
     */
    public boolean hasBreakpoint(){
	return breakpoint;
    }


    /**
     * Add a breakpoint to the processor
     */
    public void addBreakpoint(){
	breakpoint=true;
    }


    /**
     * Remove a breakpoint to the processor
     */
    public void rmvBreakpoint(){
	breakpoint=false;
    }


    /**
     * If this is an alternate processor then fetch the AlternateProcessor
     * object which represents it in the parent
     */
    private AlternateProcessor getAlternateDescription() {
	if (this.model == null && this.parentProcessor != null) {
	    List alternateList = parentProcessor.getAlternatesList();
	    for (Iterator i = alternateList.iterator(); i.hasNext();) {
		AlternateProcessor ap = (AlternateProcessor)i.next();
		if (ap.getProcessor() == this) {
		    return ap;
		}
	    }
	    throw new RuntimeException("No alternates found within parent processor!");
	}
	else {
	    throw new RuntimeException("Cannot fetch the alternate description for a primary processor "+getName());
	}	
    }


    /**
     * If meaningful, obtain the host that the resource is based
     * in. Of course, not all processor implementations are service
     * based so this may return the string Processor.ENACTOR instead
     * to represent this, and this is the default.
     */
    public String getResourceHost() {
	return Processor.ENACTOR;
    }


    /**
     * Return the maximum number of task implementations that should
     * be launched for this processor. This is ignored unless the
     * processor is being iterated over in which case a number of
     * threads up to the value specified are created to process the
     * information. You must be particularly careful that the task
     * implementation is thread safe before changing this from the
     * default specified here, at the current time the web service
     * processor is not thread safe!
     */
    public int getMaximumWorkers() {
	return 1;
    }


    /**
     * Return the default number of workers for an instance of this
     * processor type
     */
    public int getDefaultWorkers() {
	return 1;
    }


    /**
     * The number of threads that this processor will use when
     * running on an implicit iteration run
     */
    private int workerThreads = getDefaultWorkers();


    /**
     * Return the number of workers for this instance
     */
    public final int getWorkers() {
	return this.workerThreads;
    }


    /**
     * Set the number of workers
     */
    public final void setWorkers(int workers) {
	if (workers < 1) {
	    workers = 1;
	}
	if (workers > getMaximumWorkers()) {
	    this.workerThreads = getMaximumWorkers();
	}
	else {
	    this.workerThreads = workers;
	}
    }

    /**
     * Set the name, providing that names doesn't exist within the
     * current workflow that this processor is bound to. If it does
     * then do nothing
     */
    public void setName(String newName) {
	if (this.model == null) {
	    return;
	}
	try {
	    model.locateProcessor(newName);
	    return;
	}
	catch (UnknownProcessorException upe) {
	    // No existing processor with this name
	    if (name.equals("")) {
		return;
	    }
	    if (Pattern.matches("\\w++",newName) == false) {
		return;
	    }
	    String oldName = name;
	    name = newName;
	    fireModelEvent(new ScuflModelRenameEvent(this, oldName));
	}
    }

    /**
     * Get the iteration strategy for this processor, or null
     * if the default should be used
     */
    public IterationStrategy getIterationStrategy() {
	return this.iterationStrategy;
    }

    /**
     * Set the iteration strategy
     */
    public void setIterationStrategy(IterationStrategy i) {
	this.iterationStrategy = i;
    }

    /**
     * Return an array containing all annotation templates
     * for this processor
     */
    public AnnotationTemplate[] getAnnotationTemplates() {
	return (AnnotationTemplate[])templates.toArray(new AnnotationTemplate[0]);
    }

    /**
     * Add an annotation template to this processor
     */
    public void addAnnotationTemplate(AnnotationTemplate theTemplate) {
	if (this.templates.contains(theTemplate)==false) {
	    this.templates.add(theTemplate);
	    fireModelEvent(new MinorScuflModelEvent(this, "Template added"));
	}
    }

    /**
     * Remove an annotation template from this processor
     */
    public void removeAnnotationTemplate(AnnotationTemplate theTemplate) {
	if (this.templates.contains(theTemplate)) {
	    this.templates.remove(theTemplate);
	    fireModelEvent(new MinorScuflModelEvent(this, "Template removed"));
	}
    }

    /**
     * Create a standard annotation template for each pair of
     * bound input / output ports, mostly just to test the
     * metadata store and browser functionality. Removes any existing
     * template definitions!
     */
    public AnnotationTemplate[] defaultAnnotationTemplates() {
	List dtemplates = new ArrayList();
	Port[] boundInputs, boundOutputs;
	if (model != null) {
	    boundInputs = getBoundInputPorts();
	    boundOutputs = getBoundOutputPorts();
	}
	else if (model == null && parentProcessor != null) {
	    boundInputs = parentProcessor.getBoundInputPorts();
	    boundOutputs = parentProcessor.getBoundOutputPorts();
	}
	else {
	    boundInputs = new Port[0];
	    boundOutputs = new Port[0];
	}
	for (int i = 0; i < boundInputs.length; i++) {
	    for (int j = 0; j < boundOutputs.length; j++) {
		Port input = boundInputs[i];
		Port output = boundOutputs[j];
		dtemplates.add(AnnotationTemplate.standardTemplate(output,"tavp:createdFrom",input));
	    }
	}
	return (AnnotationTemplate[])dtemplates.toArray(new AnnotationTemplate[0]);
    }

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
     * Add an alternate processor to this processor definition
     */
    public void addAlternate(AlternateProcessor ap) {
	this.alternates.add(ap);
	ap.setOriginalProcessor(this);
	fireModelEvent(new ScuflModelEvent(this, "Alternate added"));
    }

    /**
     * Delete an alternate processor from this processor definition
     */
    public void removeAlternate(AlternateProcessor ap) {
	this.alternates.remove(ap);
	fireModelEvent(new ScuflModelEvent(this, "Alternate removed"));
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
	if (timeout != this.timeout) {
	    this.timeout = timeout;
	    fireModelEvent(new MinorScuflModelEvent(this, "Timeout changed"));
	}
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
	if (retries != this.retries) {
	    this.retries = retries;
	    fireModelEvent(new MinorScuflModelEvent(this, "Retry count changed"));
	}
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
	if (delay != this.retryDelay) {
	    this.retryDelay = delay;
	    fireModelEvent(new MinorScuflModelEvent(this, "Retry delay changed"));
	}
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
	if (backoff != this.backoff) {
	    this.backoff = backoff;
	    fireModelEvent(new MinorScuflModelEvent(this, "Backoff changed"));
	}
    }

    /**
     * Get whether the Processor is critical. This is true
     * if its execution failure will cause its containing
     * workflow to fail.
     */
    public boolean getCritical() {
        return critical;
    }

    /**
     * Set whether the Processor is critical. When a critical Processor's
     * execution fails, its containing workflow is halted and fails. In contrast,
     * the containing workflow will ignore failures of non-critical Processors
     * and will continue to run.
     */
    public void setCritical(boolean critical) {
        this.critical = critical;
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
     * display useful information.
     *
     * @return a Properties instance containing all processor-specific
     *         properties, or optinally null if there are none
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
	//fireModelEvent(new ScuflModelEvent(this, "New processor created '"+name+"'"));
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
	    //fireModelEvent(new ScuflModelEvent(this,"Description changed"));
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
	if (model != null) {
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
		    if (boundPorts.contains(ip)) {
			temp.add(ip);
		    }
		}
		catch (ClassCastException cce) {
		    //
		}
	    }
	    return (InputPort[])temp.toArray(new InputPort[0]);
	}
	else {
	    if (parentProcessor == null) {
		// Shouldn't really happen, but behave sensibly
		return new InputPort[0];
	    }
	    else {
		AlternateProcessor ap = getAlternateDescription();
		Map inputMapping = ap.getInputMapping();		
		// This is an alternate processor
		InputPort[] parentInputs = parentProcessor.getBoundInputPorts();
		List resultList = new ArrayList();
		for (int i = 0; i < parentInputs.length; i++) {
		    // For each parent port which has been bound add the
		    // corresponding child port to the list if the mapping
		    // exists for that port name
		    String parentPortName = parentInputs[i].getName();
		    String childPortName = (String)inputMapping.get(parentPortName);
		    if (childPortName != null) {
			try {
			    resultList.add(locatePort(childPortName));
			}
			catch (UnknownPortException ex) {
			    // Ignore the potential exception here, it should
			    // never happen and it's existance indicates that
			    // the port wasn't bound (it didn't exist!)
			}
		    }
		}
		return (InputPort[])resultList.toArray(new InputPort[0]);
	    }
	}
    }

    /**
     * Get an array of all the output ports that are bound
     * by data constraints and defined within this processor
     */
    public OutputPort[] getBoundOutputPorts() {
	if (this.model != null) {
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
		    if (boundPorts.contains(op)) {
			temp.add(op);
		    }
		}
		catch (ClassCastException cce) {
		    //
		}
	    }
	    return (OutputPort[])temp.toArray(new OutputPort[0]);
	}
	else {
	    if (parentProcessor == null) {
		// Shouldn't really happen, but behave sensibly
		return new OutputPort[0];
	    }
	    else {
		// This is an alternate processor		
		AlternateProcessor ap = getAlternateDescription();		
		Map outputMapping = ap.getOutputMapping();
		OutputPort[] parentOutputs = parentProcessor.getBoundOutputPorts();
		List resultList = new ArrayList();
		for (int i = 0; i < parentOutputs.length; i++) {
		    // For each parent port which has been bound add the
		    // corresponding child port to the list if the mapping
		    // exists for that port name
		    String parentPortName = parentOutputs[i].getName();
		    String childPortName = (String)outputMapping.get(parentPortName);
		    if (childPortName != null) {
			try {
			    resultList.add(locatePort(childPortName));
			}
			catch (UnknownPortException ex) {
			    // Ignore the potential exception here, it should
			    // never happen and it's existance indicates that
			    // the port wasn't bound (it didn't exist!)
			}
		    }
		}
		return (OutputPort[])resultList.toArray(new OutputPort[0]);
	    }
	}
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
     * Find a particular named port, input port if boolean flag is true
     */
    public Port locatePort(String port_name, boolean isInputPort) 
	throws UnknownPortException {
	for (Iterator i = ports.iterator(); i.hasNext();) {
	    Port p = (Port)i.next();
	    if (p.getName().equalsIgnoreCase(port_name)) {
		if ((isInputPort && p instanceof InputPort) ||
		    (!isInputPort && p instanceof OutputPort))
		    return p;
	    }
	}
	throw new UnknownPortException("Unable to find the port with name '"+port_name+"' in '"+getName()+"'");
    }
    
    /**
     * Find a particular named port with a given type,
     * creating the appropriate port if and only if the
     * port is not found and the workflow is in offline
     * mode
     */
    synchronized Port locatePortOrCreate(String port_name, boolean isInputPort) 
	throws UnknownPortException {
	try {
	    return locatePort(port_name, isInputPort);
	}
	catch (UnknownPortException upe) {
	    if (this.model != null && this.model.isOffline()) {
		// Create a new port
		try {
		    Port result;
		    if (isInputPort) {
			result = new InputPort(this, port_name);
		    }
		    else {
			result = new OutputPort(this, port_name);
		    }
		    addPort(result);
		    return result;
		}
		catch (PortCreationException pce) {
		    pce.printStackTrace();
		    throw upe;
		}
		catch (DuplicatePortNameException dpne) {
		    dpne.printStackTrace();
		    throw upe;
		}
	    }
	    else {
		throw upe;
	    }
	}
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
	fireModelEvent(new ScuflModelRemoveEvent(this, the_port));

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
    public void fireModelEvent(ScuflModelEvent event) {
	if (firingEvents) {
	    if (this.model!=null) {
		this.model.fireModelEvent(event);
	    }
	    else {
		// Fire event back to the parent processor if this
		// is an alternate
		if (this.parentProcessor != null) {
		    event.source = this.parentProcessor;
		    this.parentProcessor.fireModelEvent(event);
		}
	    }
	}
    }

    /**
     * Return the processor's name in the toString()
     */
    public String toString() {
	return this.getName();
    }

    public boolean isOffline() {
	if (this.model != null && this.model.isOffline()) {
	    return true;
	}
	else if (this.model == null && this.parentProcessor != null) {
	    return this.parentProcessor.isOffline();
	}
	else {
	    return false;
	}
    }

}
