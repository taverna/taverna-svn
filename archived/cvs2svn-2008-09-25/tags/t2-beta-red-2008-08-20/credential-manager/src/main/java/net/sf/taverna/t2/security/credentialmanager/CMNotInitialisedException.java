package net.sf.taverna.t2.security.credentialmanager;

/**
 * Represents an exception thrown by Credential Manager 
 * if an application tries to invoke any methods on it 
 * before it has been initialised.
 * 
 * @author Alexandra Nenadic
 */
public class CMNotInitialisedException
    extends Exception
{

	private static final long serialVersionUID = 6041577726294822985L;
	
	/**
     * Creates a new CMNotInitialisedException.
     */
    public CMNotInitialisedException()
    {
        super();
    }

    /**
     * Creates a new CMNotInitialisedException with the specified message.
     *
     * @param sMessage Exception message
     */
    public CMNotInitialisedException(String sMessage)
    {
        super(sMessage);
    }
}

