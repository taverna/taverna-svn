package net.sf.taverna.dalec;

/**
 * Interface describing a DatabaseListener.  All implementing subclasses should describe methods for database entry
 * creation, failure and removal events.
 *
 * @author Tony Burdett date: 20-Jun-2005
 */
public interface DatabaseListener
{
    /**
     * Occurs when an entry has been made into the database
     */
    public void databaseEntryCreated(String entryName);

    /**
     * Occurs when an entry is submitted but fails
     */
    public void databaseEntryFailed(String entryName, Throwable cause);

    /**
     * Occurs when an entry is removed from the database
     */
    public void databaseEntryRemoved(String entryName);
}
