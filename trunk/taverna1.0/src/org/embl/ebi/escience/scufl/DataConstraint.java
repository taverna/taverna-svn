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

	model.fireModelEvent(new ScuflModelEvent(this, "New data constraint created, '"+getName()+"'"));

    }
    
    /**
     * Return a string representation of the data link
     */
    public String getName() {
	return this.source.getProcessor().getName()+":"+this.source.getName()+"'->'"+this.sink.getProcessor().getName()+":"+this.sink.getName();
    }

}
