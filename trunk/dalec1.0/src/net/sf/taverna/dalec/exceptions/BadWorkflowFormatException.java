package net.sf.taverna.dalec.exceptions;

/**
 * Javadocs go here.
 *
 * @version 1.0
 * @author Tony Burdett
 */
public class BadWorkflowFormatException extends Exception
{
    public BadWorkflowFormatException()
    {
        super();
    }

    public BadWorkflowFormatException(String message)
    {
        super(message);
    }

    public BadWorkflowFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BadWorkflowFormatException(Throwable cause)
    {
        super(cause);
    }
}
