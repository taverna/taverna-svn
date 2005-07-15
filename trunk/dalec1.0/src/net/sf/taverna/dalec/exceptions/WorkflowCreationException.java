package net.sf.taverna.dalec.exceptions;

/**
 * Exception class to handle general failure of annotation, wrapping exceptions from within Taverna
 *
 * @version 1.0
 * @author Tony Burdett
 */
public class WorkflowCreationException extends Exception
{
    public WorkflowCreationException ()
    {
        super();
    }

    public WorkflowCreationException (Throwable cause)
    {
        super (cause);
    }

    public WorkflowCreationException (String message)
    {
        super (message);
    }

    public WorkflowCreationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
