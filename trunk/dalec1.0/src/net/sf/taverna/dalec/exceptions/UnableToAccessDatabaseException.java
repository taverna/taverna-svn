package net.sf.taverna.dalec.exceptions;

/**
 * Javadocs go here.
 *
 * @version 1.0
 * @author Tony Burdett
 */
public class UnableToAccessDatabaseException extends Exception
{
    public UnableToAccessDatabaseException()
    {
        super();
    }

    public UnableToAccessDatabaseException(String message)
    {
        super(message);
    }

    public UnableToAccessDatabaseException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public UnableToAccessDatabaseException(Throwable cause)
    {
        super(cause);
    }
}
