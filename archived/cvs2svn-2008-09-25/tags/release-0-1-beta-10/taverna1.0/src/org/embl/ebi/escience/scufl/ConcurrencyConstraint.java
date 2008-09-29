/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

/**
 * Represents a concurrency constraint between two processors
 * @author Tom Oinn
 */
public class ConcurrencyConstraint implements java.io.Serializable {
    
    /** Signifies that the processor has been created but is not running */
    public static final int SCHEDULED = 0;
    /** Signifies that the processor has an active thread in the 'invoke' method */
    public static final int RUNNING = 1;
    /** Signifies that the processor has completed */
    public static final int COMPLETED = 2;
    /** Signifies that the processor has failed in some fashion */
    public static final int ABORTED = 3;
  
    private static final int MAXIMUM_STATUS_VALUE = 3;
    private Processor controller, target;
    private int targetStateFrom, targetStateTo, controllerStateGuard;
    private String name;
    private ScuflModel model;
    
    /**
     * Return the name of the constraint
     */
    public String getName() {
	return this.name;
    }
    
    /**
     * Create a new concurrency constraint. The arguments mean
     * that the constraint should prevent the target processor
     * moving from targetStateFrom to targetStateTo unless the
     * controller processer is in state controllerStateGuard
     */
    public ConcurrencyConstraint(ScuflModel model, String name, Processor controller, Processor target, int targetStateFrom, int targetStateTo, int controllerStateGuard) 
	throws DuplicateConcurrencyConstraintNameException, ConcurrencyConstraintCreationException {
	
	this.controller = controller; 
	this.target = target;
	this.targetStateFrom = targetStateFrom;
	this.targetStateTo = targetStateTo;
	this.controllerStateGuard = controllerStateGuard;
	this.name = name;
	this.model = model;

	if (this.model == null) {
	    throw new ConcurrencyConstraintCreationException("Scufl model parameter cannot be null!");
	}
	if (this.target == null) {
	    throw new ConcurrencyConstraintCreationException("Target processor cannot be null!");
	}
	if (this.controller == null) {
	    throw new ConcurrencyConstraintCreationException("Controller processor cannot be null!");
	}
	if (this.targetStateFrom < 0 || this.targetStateFrom > ConcurrencyConstraint.MAXIMUM_STATUS_VALUE) {
	    throw new ConcurrencyConstraintCreationException("Invalid value for targetStateFrom in constructor!");
	}
	if (this.targetStateTo < 0 || this.targetStateTo > ConcurrencyConstraint.MAXIMUM_STATUS_VALUE) {
	    throw new ConcurrencyConstraintCreationException("Invalid value for targetStateTo in constructor!");
	}
	if (this.controllerStateGuard < 0 || this.controllerStateGuard > ConcurrencyConstraint.MAXIMUM_STATUS_VALUE) {
	    throw new ConcurrencyConstraintCreationException("Invalid value for controllerStateGuard in constructor!");
	}

	// Check for duplicate names
	ConcurrencyConstraint[] existingConstraints = model.getConcurrencyConstraints();
	for (int i = 0; i<existingConstraints.length; i++) {
	    if (existingConstraints[i].getName().equalsIgnoreCase(name)) {
		throw new DuplicateConcurrencyConstraintNameException("Cannot create a new concurrency constraint "+
								      "with name '"+name+"' as this name is already "+ 
								      "bound within the model.");
	    }
	}
	// Notify the model that we've been created
	model.fireModelEvent(new ScuflModelEvent(this,"Created new concurrency constraint"));
    }
    
    /**
     * Return the processor that this constraint is regulating
     */
    public Processor getTargetProcessor() {
	return this.target;
    }

    /**
     * Return the processor whos state is controlling the transition
     * of the target processor
     */
    public Processor getControllingProcessor() {
	return this.controller;
    }

    /**
     * Return the state from which the target processor will move
     * in the event of the control state being satisfied
     */
    public int getTargetStateFrom() {
	return this.targetStateFrom;
    }

    /**
     * Return the state to which the target processor will be allowed
     * to change if the control state is satisfied
     */
    public int getTargetStateTo() {
	return this.targetStateTo;
    }

    /**
     * Return the state the controller processor must be in in order
     * to permit the specified transition of the target processor
     */
    public int getControllerStateGuard() {
	return this.controllerStateGuard;
    }

    /**
     * Convert a state int to a String representation of it
     */
    public static String statusCodeToString(int status) {
	if (status == 0) {
	    return "Scheduled";
	}
	else if (status == 1) {
	    return "Running";
	}
	else if (status == 2) {
	    return "Completed";
	}
	else if (status == 3) {
	    return "Aborted";
	}
	// Failure case
	return "Unknown State!";
    }

    /**
     * Convert a string into an int status code, the string
     * must be one of scheduled, running, completed, aborted
     * and is not case sensitive.
     */
    public static int statusStringToInt(String status) {
	if (status.equalsIgnoreCase("scheduled")) {
	    return ConcurrencyConstraint.SCHEDULED;
	}
	else if (status.equalsIgnoreCase("running")) {
	    return ConcurrencyConstraint.RUNNING;
	}
	else if (status.equalsIgnoreCase("completed")) {
	    return ConcurrencyConstraint.COMPLETED;
	}
	else if (status.equalsIgnoreCase("aborted")) {
	    return ConcurrencyConstraint.ABORTED;
	}
	// Failure case
	return -1;
    }

    /**
     * Override toString method to display the two processors,
     * controller first, that this constraint applies to.
     */
    public String toString() {
	return this.getControllingProcessor().getName()+"::"+this.getTargetProcessor().getName();
    }

}
