package net.sf.taverna.dalec.exceptions;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 12-Jul-2005
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
