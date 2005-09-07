package net.sf.taverna.dalec.exceptions;

/**
 * Exception class to handle general failure to create a compiled annotation workflow, wrapping exceptions from within
 * Taverna.
 *
 * @author Tony Burdett
 * @version 1.0
 */
public class WorkflowCreationException extends Exception
{
    public WorkflowCreationException()
    {
        super();
    }

    public WorkflowCreationException(Throwable cause)
    {
        super(cause);
    }

    public WorkflowCreationException(String message)
    {
        super(message);
    }

    public WorkflowCreationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
