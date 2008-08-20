package net.sf.taverna.t2.security.credentialmanager;

/**
 * Represents a (cryptographic or any other) exception thrown by Credential Manager.
 * 
 * @author Alexandra Nenadic
 */
public class CMException
    extends Exception
{
	
	private static final long serialVersionUID = 3885885604048806903L;

	/**
     * Creates a new CMException.
     */
    public CMException()
    {
        super();
    }

    /**
     * Creates a new CMException with the specified message.
     *
     * @param sMessage Exception message
     */
    public CMException(String sMessage)
    {
        super(sMessage);
    }

    /**
     * Creates a new CMException with the specified message and cause
     * throwable.
     *
     * @param causeThrowable The throwable that caused this exception to be
     * thrown
     * @param sMessage Exception message
     */
    public CMException(String sMessage, Throwable causeThrowable)
    {
        super(sMessage, causeThrowable);
    }

    /**
     * Creates a new CMException with the specified cause throwable.
     *
     * @param causeThrowable The throwable that caused this exception to be
     * thrown
     */
    public CMException(Throwable causeThrowable)
    {
        super(causeThrowable);
    }
}
