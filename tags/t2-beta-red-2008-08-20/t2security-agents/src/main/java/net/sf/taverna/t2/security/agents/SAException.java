package net.sf.taverna.t2.security.agents;

/**
 * Represents a (cryptographic or any other) exception thrown by Securty Agent.
 * 
 * @author Alexandra Nenadic
 */

public class SAException  extends Exception 
{

	private static final long serialVersionUID = 6033026734988422030L;

	/**
     * Creates a new SAException.
     */
    public SAException()
    {
        super();
    }

    /**
     * Creates a new SAException with the specified message.
     *
     * @param sMessage Exception message
     */
    public SAException(String sMessage)
    {
        super(sMessage);
    }

    /**
     * Creates a new SAException with the specified message and cause
     * throwable.
     *
     * @param causeThrowable The throwable that caused this exception to be
     * thrown
     * @param sMessage Exception message
     */
    public SAException(String sMessage, Throwable causeThrowable)
    {
        super(sMessage, causeThrowable);
    }

    /**
     * Creates a new SAException with the specified cause throwable.
     *
     * @param causeThrowable The throwable that caused this exception to be
     * thrown
     */
    public SAException(Throwable causeThrowable)
    {
        super(causeThrowable);
    }
}


