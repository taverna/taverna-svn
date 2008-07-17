package net.sf.taverna.t2.security.profiles;
/**
 * Represents an exception thrown when someone tries to access a security property that is not defined.
 * 
 * @author Alexandra Nenadic
 */
public class NoSuchSecurityPropertyException 
	extends Exception
{
	private static final long serialVersionUID = -4620133394046592596L;

	/**
     * Creates a new NoSuchSecurityPropertyException.
     */
    public NoSuchSecurityPropertyException()
    {
        super("Unknown security property of a service");
    }

    /**
     * Creates a new NoSuchSecurityPropertyException with the specified message.
     *
     * @param sMessage Exception message
     */
    public NoSuchSecurityPropertyException(String sMessage)
    {
        super(sMessage);
    }

    /**
     * Creates a new NoSuchSecurityPropertyException with the specified message and cause
     * throwable.
     *
     * @param causeThrowable The throwable that caused this exception to be
     * thrown
     * @param sMessage Exception message
     */
    public NoSuchSecurityPropertyException(String sMessage, Throwable causeThrowable)
    {
        super(sMessage, causeThrowable);
    }

    /**
     * Creates a new NoSuchSecurityPropertyException with the specified cause throwable.
     *
     * @param causeThrowable The throwable that caused this exception to be
     * thrown
     */
    public NoSuchSecurityPropertyException(Throwable causeThrowable)
    {
        super(causeThrowable);
    }
}
