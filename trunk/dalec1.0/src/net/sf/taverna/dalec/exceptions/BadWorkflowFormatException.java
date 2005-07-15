package net.sf.taverna.dalec.exceptions;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 15-Jul-2005
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
