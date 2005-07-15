package net.sf.taverna.dalec.exceptions;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 17-Jun-2005
 */
public class NewJobSubmissionException extends Exception
{
    public NewJobSubmissionException ()
    {
        super();
    }

    public NewJobSubmissionException (String errorMsg)
    {
        super (errorMsg);
    }

    public NewJobSubmissionException (String errorMsg, Exception e)
    {
        super (errorMsg, e);
    }
}
