/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Represents a data dependancy between two processors
 * @author Tom Oinn
 */
public class DataConstraint implements java.io.Serializable {

    private ScuflModel model;
    private InputPort sink;
    private OutputPort source;

    /**
     * Create a new data constraint from two name strings of the form
     * [PROCESSOR]:[PORT] for source and sink. These must exist within
     * the model specified.
     * @exception MalformedNameException if the name supplied is not in the correct format
     * @exception DataConstraintCreationException if the ports are found but are incorrectly typed
     * @exception UnknownPortException if the processor is found but doesn't contain the named port
     * @exception UnknownProcessorException if the model doesn't contain the particular processor name
     */
    public DataConstraint(ScuflModel model, String source_name, String sink_name)
	throws DataConstraintCreationException,
	       UnknownPortException,
	       UnknownProcessorException,
	       MalformedNameException {
	this(model, model.locatePortOrCreate(source_name, false), model.locatePortOrCreate(sink_name, true));
    }

    /**
     * Return the source Port
     */
    public Port getSource() {
	return this.source;
    }

    /**
     * Return the sink Port
     */
    public Port getSink() {
	return this.sink;
    }

    /**
     * Create a new data constraint from the source to the sink port. 
     * The ports must be instances of OutputPort and InputPort respectively
     * @exception DataConstraintCreationException if the ports are found but are incorrectly typed
     */
    public DataConstraint(ScuflModel model, Port source, Port sink)
	throws DataConstraintCreationException {
	
	// Check model parameter
	if (model == null) {
	    throw new DataConstraintCreationException("Must specify a non null model for data constraint creation.");
	}
	this.model = model;
	
	// Check that the sink is an input
	try {
	    this.sink = (InputPort)sink;
	}
	catch (ClassCastException cce) {
	    throw new DataConstraintCreationException("Sink port must be an instance of InputPort");
	}

	// Check that the source is an output
	try {
	    this.source = (OutputPort)source;
	}
	catch (ClassCastException cce) {
	    throw new DataConstraintCreationException("Source port must be an instance of OutputPort");
	}

	// If the source port is an external input, set the type of it to the same type as the sink
	if (this.source.getProcessor() == model.getWorkflowSourceProcessor()) {
	    this.source.setSyntacticType(this.sink.getSyntacticType());
	}
	// If the sink port is an external input, set the type of it to the same as the source
	else if (this.sink.getProcessor() == model.getWorkflowSinkProcessor()) {
	    this.sink.setSyntacticType(this.source.getSyntacticType());
	}

	model.fireModelEvent(new ScuflModelEvent(this, "New data constraint created, '"+getName()+"'"));

    }
    
    /**
     * Return a string representation of the data link
     */
    public String getName() {
	String from = "";
	String to = "";
	if (this.source.getProcessor() == model.getWorkflowSourceProcessor()) {
	    from = this.source.getName();
	}
	else {
	    from = this.source.getProcessor().getName()+":"+this.source.getName();
	}
	if (this.sink.getProcessor() == model.getWorkflowSinkProcessor()) {
	    to = this.sink.getName();
	}
	else {
	    to = this.sink.getProcessor().getName()+":"+this.sink.getName();
	}
	return from+"->"+to;
    }

    /**
     * Return the name as the toString() method
     */
    public String toString() {
	return this.getName();
    }

}
