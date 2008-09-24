package net.sf.taverna.dalec.exceptions;

/**
 * A subtype of BadWorkflowException, this exception is thrown specifically when Dalec is unable to find a recognised
 * processor name.
 *
 * @author Tony Burdett
 * @version 1.0
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
