/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.provenance.process;

/**
 * Event corresponding to a single error in a service
 * invocation. This may result in retry, alternate
 * scheduling or service failure if neither retries
 * nor alternates are available.
 * @author Tom Oinn
 */
public class ServiceError extends ProcessEvent {
 
    private Exception underlyingException;
    
    public String getMessage() {
	if (underlyingException!=null) {
	    return underlyingException.getMessage();
	}
	else {
	    return "Unknown exception";
	}
    }
   
    /**
     * Construct a service error event from the given
     * exception.
     */
    public ServiceError(Exception ex) {
	super();
	this.underlyingException = ex;
    }

}
