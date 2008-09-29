package net.sf.taverna.dalec.exceptions;

/**
 * A NewJobSubmissionException is thrown by an instance of <code>DalecManager</code> when an annotation is requested
 * which does not yet exist in the database.  This exception is handled in such a way that the query sequence is then
 * submitted to the workflow enactor for annotation.
 *
 * @author Tony Burdett
 * @version 1.0
 */
public class NewJobSubmissionException extends Exception
{
    public NewJobSubmissionException()
    {
        super();
    }

    public NewJobSubmissionException(String errorMsg)
    {
        super(errorMsg);
    }

    public NewJobSubmissionException(String errorMsg, Exception e)
    {
        super(errorMsg, e);
    }
}
