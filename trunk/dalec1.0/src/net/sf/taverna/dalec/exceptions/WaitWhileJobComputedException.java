package net.sf.taverna.dalec.exceptions;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 17-Jun-2005
 */
public class WaitWhileJobComputedException extends Exception
{
    public WaitWhileJobComputedException ()
    {
        super();
    }

    public WaitWhileJobComputedException (String errorMsg)
    {
        super (errorMsg);
    }

    public WaitWhileJobComputedException (String errorMsg, Exception e)
    {
        super (errorMsg, e);
    }
}
