package net.sf.taverna.dalec.exceptions;

/**
 * Javadocs go here.
 *
 * @version 1.0
 * @author Tony Burdett
 */
public class IncorrectlyNamedInputException extends BadWorkflowFormatException
{
    public IncorrectlyNamedInputException()
    {
        super();
    }

    public IncorrectlyNamedInputException(String message)
    {
        super(message);
    }

    public IncorrectlyNamedInputException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public IncorrectlyNamedInputException(Throwable cause)
    {
        super(cause);
    }
}
