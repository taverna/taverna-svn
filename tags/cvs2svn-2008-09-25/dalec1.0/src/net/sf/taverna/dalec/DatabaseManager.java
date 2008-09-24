package net.sf.taverna.dalec;

import org.biojava.bio.program.gff.GFFRecord;
import org.biojava.bio.program.gff.GFFEntrySet;
import org.biojava.bio.program.gff.GFFTools;

import java.io.*;
import java.util.*;

import net.sf.taverna.dalec.exceptions.UnableToAccessDatabaseException;
import net.sf.taverna.dalec.exceptions.BadWorkflowFormatException;

/**
 * Files will be stored in the file-store database as individual flat files, referenced by Filename in the format
 * {sequenceID}.gff. Records are stored as GFF format files. Requests to the database get back the record as a Biojava
 * <code>GFFRecord</code> object. DatabaseManager is an implementation of runnable, so an instance of
 * <code>DatabaseManager</code> handles the submitting and retrieving of the data to the database, and also can be
 * <code>run</code> to perform these actions automatically within a separate thread.  One file per sequence is created
 * within the database.
 * <p/>
 * A client therefore can: <ul> <li>call <code>getPendingResults()</code> to retrieve a list of all results yet to be
 * written,</li> <li>call <code>writeToFile()</code> to write all pending results to the Database.</li> </ul> Each
 * method should be invoked in turn.
 * <p/>
 * Alternatively, the method <code>DatabaseManager.run()</code> is called - it is intended that this is used by creating
 * a new thread and supplying <code>DatabaseManager</code> as it's <code>Runnable</code>.  This automatically enters
 * sequences into the database as they are generated. This is the recommended method.
 * <p/>
 * However, if a client is to use the "manual" method of writing results to the database, it should be considered that
 * every workflow instance submits its results to <code>DatabaseManager</code> as they are generated, and that these
 * results are retained in memory until written.  Hence any extending class which wishes to manually control when
 * results are written to the database should include some method for checking the pending results, as there is no
 * restriction on the amount of annotation data that can be retained in memory by Dalec.
 *
 * @author Tony Burdett
 * @version 1.0
 */
public class DatabaseManager implements Runnable
{
    private File dbLoc;
    private List resultsToWrite = new ArrayList();
    private List dbListeners = new ArrayList();
    private boolean terminated = false;

    /**
     * Constructor for DatabaseManager.  Simply creates the database root directory if it doesn't already exist.
     *
     * @param databaseLocation A <code>File</code> representing the path to the root directory of the database
     */
    public DatabaseManager(File databaseLocation)
    {
        this.dbLoc = databaseLocation;
        if (!dbLoc.exists()) dbLoc.mkdirs();
    }

    /**
     * This method 'activates' the DatabaseManager within a new thread.  As <code>DatabaseManager</code> is an
     * implementation of <code>Runnable</code>, this <code>run()</code> method is used whenever
     * <code>DatabaseManager</code> is used in the construction of a new Thread, "<code>database_thread</code>" for
     * example. While there are pending results set on the DatabaseManager, a <code>database_thread</code> will
     * continually write these results to disk, creating a new GFF format file for every sequence.  If there are no
     * results currently pending, the <code>database_thread</code> will sit idle, waiting for new results to be added.
     * Then when new results are added, the thread is woken up. Once the <code>run()</code> method is called, any
     * instance of <code>DatabaseManager</code> will remain active until the <code>exterminate()</code> method is called
     * or the thread is somehow interrupted.
     */
    public void run()
    {
        try
        {
            // continually run as long as thread isn't interrupted
            while (!terminated)
            {
                // wait if there are no results waiting to be written
                while (resultsToWrite.isEmpty() && !terminated)
                {
                    synchronized (resultsToWrite)
                    {
                        resultsToWrite.wait();
                    }
                }
                if (terminated) break;

                // Otherwise, grab the pending results and write them to a file
                writeToFile(getPendingResults());
            }
        }
        catch (InterruptedException e)
        {
            // Thread will normally run indefinitely unless interrupted
            DalecManager.logError(dbLoc, "database activity", e);
        }
    }

    /**
     * Method for terminating any active DatabaseManager thread, calling this method allows the current job to complete
     * and then allows the run method to exit.  If this method is called, any results which have not yet been written
     * will be lost and the annotation will need to be redone.
     */
    public void exterminate()
    {
        // set terminated to true to prevent new write job starting
        terminated = true;
        // this won't wake a waiting thread though, so notify any threads waiting on resultsToWrite
        synchronized (resultsToWrite)
        {
            resultsToWrite.notifyAll();
        }
    }

    /**
     * This method is used to append a new result to the end of the pending queue.  Results will be written to the
     * database in the order in which they are generated, and stored within an in-memory cache until they can be
     * written.
     *
     * @param result The Map object representing the result as generated by the implicit Taverna workflow.
     */
    public void addNewResult(GFFEntrySet result)
    {
        // Add GFFEntrySet to resultsToWrite List
        synchronized (resultsToWrite)
        {
            resultsToWrite.add(result);
            resultsToWrite.notifyAll();
        }
    }

    /**
     * Returns <code>true</code> if the file already exists within the database, <code>false</code> otherwise.
     *
     * @param seqID the sequence ID for the required sequence, corresponding to the name of the file in the database.
     * @return boolean true if file exists, false otherwise
     */
    public synchronized boolean fileExists(String seqID)
    {
        synchronized (dbLoc)
        {
            // This assumes files will be stored in a database by using the format "seqID.gff" - should be fine
            File seqFile = new File(dbLoc, seqID + ".gff");
            if (seqFile.exists())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * This method returns the Biojava object <code>GFFEntrySet</code> represented by this GFF file within the database.
     * Calling classes should handle the extraction of sequence or annotation data from the supplied GFFEntrySet.
     *
     * @param seqID the sequence ID for the required sequence, corresponding to the name of the file in the database.
     * @return the data held within the GFF file, as a <code>GFFEntrySet</code> Biojava object
     * @throws UnableToAccessDatabaseException
     *          if there is some problem with access to the Database
     */
    public synchronized GFFEntrySet getGFFEntry(String seqID) throws UnableToAccessDatabaseException
    {
        // Read the file corresponding to the specified seqID - should be $seqID.gff in database
        try
        {
            return GFFTools.readGFF(new File(dbLoc, seqID + ".gff"));
        }
        catch (Exception e)
        {
            throw new UnableToAccessDatabaseException("Unable to read GFF file from database", e);
        }
    }

    /**
     * Add a <code>DatabaseListener</code> to this database.  There is no limit on the number of listeners which can be
     * registered to one database.
     *
     * @param listener the <code>DatabaseListener</code> to register
     */
    public void addDatabaseListener(DatabaseListener listener)
    {
        dbListeners.add(listener);
    }

    /**
     * Returns a list of <code>DatabaseListeners</code> registered to this DatabaseManager
     *
     * @return a <code>List</code> of registered DatabaseListeners
     */
    public List getDatabaseListener()
    {
        return dbListeners;
    }

    /**
     * Returns a <code>List</code> representing all data which has yet to be written to the database - they are held in
     * an in-memory cache until they are written to disk.  All elements within this <code>List</code> should be
     * instances of <code>GFFEntrySet</code>.
     *
     * @return a <code>List</code> of pending results - those which have not yet been written to disk.
     */
    public List getPendingResults()
    {
        synchronized (resultsToWrite)
        {
            List results = new ArrayList(resultsToWrite);
            resultsToWrite.clear();
            return results;
        }
    }

    /**
     * Returns a file representing the path to the home directory of the database
     *
     * @return the <code>File</code> representing the path to the database root directory
     */
    public File getDatabaseLocation()
    {
        return dbLoc;
    }

    /**
     * This method writes all pending results (ie. those which are returned by calling <code>getPendingResults</code>)
     * to disk.  This would normally be called automatically by the <code>run()</code> method of an instance of this
     * class, provided it has been <code>Thread.start()</code> has been called.
     * <p/>
     * If an implementation of Dalec is built where DatabaseManager is NOT to be used in a dynamic form - ie. no databse
     * thread has been started, it is possible to call this method manually, to write all results to file.  However, it
     * should be considered that this method performs NO checks to determine whether a file alreayd exists.
     * <p/>
     * It is assumed that any results which are pending a write-to-file operation do not exist within the database, as a
     * check is performed to see if a file exists, is currently being annotated, or is awaiting a write-to-file
     * operation, before the sequence is annotated by the workflow.
     *
     * @param results A list of results which should be written to disk
     */
    public synchronized void writeToFile(List results)
    {
        Iterator it = results.iterator();

        // all elements in results must be GFFEntrySet instances
        while (it.hasNext())
        {
            // get next GFFEntrySet from the list, ready to be written to file
            GFFEntrySet entry = (GFFEntrySet) it.next();

            // To set a file name, we need the sequence name - for any entry set generated by a single input workflow,
            // all lines should be from ONE sequence and therefore have same name. So check first GFFRecord for name
            String seqName = null;
            for (Iterator jt = entry.lineIterator(); jt.hasNext();)
            {
                seqName = ((GFFRecord) jt.next()).getSeqName();
                break;
            }

            // write the entry set to a file
            synchronized (dbLoc)
            {
                Iterator kt = dbListeners.iterator();
                try
                {
                    // write the file and notify all listeners
                    if (seqName != null)
                    {
                        GFFTools.writeGFF(new File(dbLoc, seqName + ".gff"), entry);
                        while (kt.hasNext())
                        {
                            ((DatabaseListener) kt.next()).databaseEntryCreated(seqName);
                        }
                    }
                    else
                    {
                        // if seqName is not found, throw BadWorkflowFormatException
                        throw new BadWorkflowFormatException("No sequence name found");
                    }
                }
                catch (Exception e)
                {
                    // notify listener that an entry failed as IOException occured, then continue
                    while (kt.hasNext())
                    {
                        DalecManager.logError(dbLoc, "GFF entry write-to-file", e);
                        ((DatabaseListener) kt.next()).databaseEntryFailed(seqName, e);
                    }
                }
            }
        }
    }
}