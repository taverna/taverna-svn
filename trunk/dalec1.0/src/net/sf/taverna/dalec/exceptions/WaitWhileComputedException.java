package net.sf.taverna.dalec.exceptions;

import org.biojava.servlets.dazzle.datasource.DataSourceException;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett
 * @version 1.0 date: 27-Jul-2005
 */
public class WaitWhileComputedException extends DataSourceException
{
    public WaitWhileComputedException(String s)
    {
        super(s);
    }

    public WaitWhileComputedException(Throwable throwable, String s)
    {
        super(throwable, s);
    }

    public WaitWhileComputedException(Throwable throwable)
    {
        super(throwable);
    }
}
