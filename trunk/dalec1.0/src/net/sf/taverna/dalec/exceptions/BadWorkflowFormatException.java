package net.sf.taverna.dalec.exceptions;

/**
 * A standard Exception class.  A BadWorkflowFormatException should be thrown whenever Dalec attempts to use a submitted
 * workflow which does not conform to certain required standards, such as input and output processor naming conventions.
 * See the user documentation for more details.
 *
 * @author Tony Burdett
 * @version 1.0
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
