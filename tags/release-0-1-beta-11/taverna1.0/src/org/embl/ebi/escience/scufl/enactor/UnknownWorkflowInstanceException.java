/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor;

/**
 * Thrown when a client attempts to reconnect to a workflow
 * instance that is either unknown or that has security constraints
 * which result in the client not having sufficient access to 
 * connect.
 * @author Tom Oinn
 */
public class UnknownWorkflowInstanceException extends Exception {
    public UnknownWorkflowInstanceException() {
	super();
    }
    public UnknownWorkflowInstanceException(String message) {
	super(message);
    }
    public UnknownWorkflowInstanceException(Throwable cause) {
	super(cause);
    }
    public UnknownWorkflowInstanceException(String message, Throwable cause) {
	super(message, cause);
    }
}
