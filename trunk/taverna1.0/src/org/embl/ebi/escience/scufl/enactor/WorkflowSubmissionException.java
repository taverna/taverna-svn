/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor;

/**
 * Thrown when an error occurs during workflow submission, wraps
 * the real exception using the standard exception chaining mechanism
 * @author Tom Oinn
 */
public class WorkflowSubmissionException extends Exception {
    public WorkflowSubmissionException() {
	super();
    }
    public WorkflowSubmissionException(String message) {
	super(message);
    }
    public WorkflowSubmissionException(Throwable cause) {
	super(cause);
    }
    public WorkflowSubmissionException(String message, Throwable cause) {
	super(message, cause);
    }
}
