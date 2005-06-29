package net.sf.taverna.dalec;

import org.biojava.bio.program.gff.SimpleGFFRecord;
import org.biojava.bio.program.gff.GFFRecord;
import org.biojava.bio.program.gff.GFFEntrySet;

import java.io.File;

import net.sf.taverna.dalec.exceptions.UnableToAccessDatabaseException;

/**
 * Test class for database manager
 *
 * @author Tony Burdett date: 24-Jun-2005
 */
public class TestDatabaseManager
{
    public static void main(String[] args)
    {
        File dbGenLoc = new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\");
        // New database with test directory
        DatabaseManager db1 = new DatabaseManager(new File(dbGenLoc, "db1"));

        // Create thread and start it running
        Thread thr1 = new Thread(db1);
        thr1.start();

        // no jobs yet so thread should be waiting
        try
        {
            assert(thr1.getState() == Thread.State.WAITING);
        }
        catch (AssertionError e)
        {
            System.out.println("Thread not waiting for jobs");
        }

        // If we add a dummy "null" GFFRecord it should get written
        System.out.println("Thread waiting - lets give it a job to do...");
        GFFRecord record = new SimpleGFFRecord();
        db1.addNewResult(record); // Empty GFFRecord

        // Check this result has been created
        try
        {
            // Check file exists in DB
            assert(new File(dbGenLoc, "db1\\null.gff").exists());
            System.out.println("File exist - is it correct?");

            // Check record in DB is same as created example record
            GFFEntrySet dbRecord = db1.getGFFEntry(record.getSeqName());
            assert ((GFFRecord) (dbRecord.lineIterator().next())).equals(record);

            // assertion passed, record is correct
            System.out.println("Retrieved DB record is correct");
        }
        catch (AssertionError e)
        {
            System.out.println("Test file 'null.gff' does not exist or is incorrect!");
        }
        catch (UnableToAccessDatabaseException e)
        {
            System.out.println("Unable to access DB");
        }

        // Stop the current database manager thread.
        thr1.interrupt();
        try
        {
            thr1.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        // Create a new DB to check listener activity
        DatabaseManager db2 = new DatabaseManager(new File(dbGenLoc, "db2"));

        // register a new listener onto it, so we know when new entries are created
        db2.addDatabaseListener(new DatabaseListener()
        {
            public void databaseEntryCreated(String entryName)
            {
                System.out.println("Listener registers EntryCreated - Entryname: " + entryName);
            }

            public void databaseEntryFailed(String entryName)
            {
                System.out.println("Listener registers EntryFailed - Entryname: " + entryName);
            }

            public void databaseEntryRemoved(String entryName)
            {
                System.out.println("Listener registers EntryRemoved - Entryname: " + entryName);
            }
        });

        // Start db2 running
        Thread thr2 = new Thread(db2);
        thr2.start();

        // add a set of 10 jobs
        for (int i = 0; i < 10; i++)
        {
            // create a SimpleGFFRecord with null or 0 values excpet sequence name
            SimpleGFFRecord gffr = new SimpleGFFRecord();
            gffr.setSeqName("seq" + i);
            db2.addNewResult(gffr);
        }

        for (int i = 0; i < 10; i++)
        {
            try
            {
                // check they all exist
                assert(new File(dbGenLoc, "db2\\seq" + i).exists());
                System.out.println("File seq" + i + " exists");
            }
            catch (AssertionError e)
            {
                System.out.println("A file which should exist isn't there");

            }
        }

        // Now stop thread 2
        thr2.interrupt();
        try
        {
            thr2.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

    }
}
