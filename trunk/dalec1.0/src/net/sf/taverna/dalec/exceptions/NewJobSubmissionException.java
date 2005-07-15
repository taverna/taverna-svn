package net.sf.taverna.dalec.exceptions;

/**
 * Javadocs go here.
 *
 * @version 1.0
 * @author Tony Burdett
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
