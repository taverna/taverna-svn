package net.sf.taverna.dalec.exceptions;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 24-Jun-2005
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
