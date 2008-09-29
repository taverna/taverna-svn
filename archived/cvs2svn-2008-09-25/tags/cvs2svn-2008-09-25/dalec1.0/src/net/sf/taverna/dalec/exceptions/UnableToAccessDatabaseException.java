package net.sf.taverna.dalec.exceptions;

/**
 * An UnableToAccessDatabaseException is thrown whenever the designated database cannot be accessed, for any reason.
 * The underlying cause (most likely an IOException) will be wrapped within this exception.
 *
 * @author Tony Burdett
 * @version 1.0
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
