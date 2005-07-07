package net.sf.taverna.dalec;

import org.biojava.bio.program.gff.SimpleGFFRecord;
import org.biojava.bio.program.gff.GFFRecord;

import java.io.File;
import java.util.HashMap;

import net.sf.taverna.dalec.exceptions.UnableToAccessDatabaseException;
import junit.framework.TestCase;

/**
 * Test class for database manager
 *
 * @author Tony Burdett date: 24-Jun-2005
 */
public class TestDatabaseManager extends TestCase
{
    final File dbGenLoc = new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\");

    public void testDBCreation()
    {
        // New database with test directory
        new DatabaseManager(new File(dbGenLoc, "db"));

        assertTrue("Database doesn't exist", dbGenLoc.exists());

        System.gc();
    }

    public void testDbRun()
    {
        // New database with test directory
        DatabaseManager db = new DatabaseManager(new File(dbGenLoc, "db"));

        // Create thread and start it running
        Thread thr = new Thread(db);
        thr.start();

        // Simple wait block to give "thr" time to start
        synchronized (thr)
        {
            try
            {
                thr.wait(100);
            }
            catch (InterruptedException e)
            {
            }
        }

        assertTrue("Thread is not waiting for jobs - status is" + thr.getState().toString(), thr.getState() == Thread.State.WAITING);

        // set exterminate request
        db.exterminate();
        // Simple wait block to give "thr" time to start
        synchronized (thr)
        {
            try
            {
                thr.wait(100);
            }
            catch (InterruptedException e)
            {
            }
        }
        // now check thread has exited
        assertTrue(thr.getState().toString(), isStopped(thr));
        System.gc();
    }

    public void testDBStop()
    {
        DatabaseManager db = new DatabaseManager(new File(dbGenLoc, "db"));

        Thread thr = new Thread(db);
        thr.start();

        // Simple wait block to give "thr" time to start
        synchronized (thr)
        {
            try
            {
                thr.wait(100);
            }
            catch (InterruptedException e)
            {
            }
        }

        // set exterminate request
        db.exterminate();
        // Simple wait block to give "thr" time to start
        synchronized (thr)
        {
            try
            {
                thr.wait(100);
            }
            catch (InterruptedException e)
            {
            }
        }
        // now check thread has exited
        assertTrue(thr.getState().toString(), isStopped(thr));
        System.gc();
    }

    public void testAddJob()
    {
        boolean exceptions = false;

        // New database with test directory
        DatabaseManager db = new DatabaseManager(new File(dbGenLoc, "db"));

        // Create thread and start it running
        Thread thr = new Thread(db);
        thr.start();

        // Simple wait block to give "thr" time to start
        synchronized (thr)
        {
            try
            {
                thr.wait(100);
            }
            catch (InterruptedException e)
            {
            }
        }

        // If we add a dummy "null" GFFRecord it should get written
        SimpleGFFRecord record = new SimpleGFFRecord();
        record.setSeqName("testSequence");
        db.addNewResult(record); // Empty GFFRecord

        // Simple wait block to give "thr" time to start
        synchronized (thr)
        {
            try
            {
                thr.wait(100);
            }
            catch (InterruptedException e)
            {
            }
        }

        GFFRecord retrieved = null;
        try
        {
            retrieved = (GFFRecord) db.getGFFEntry(record.getSeqName()).lineIterator().next();
        }
        catch (UnableToAccessDatabaseException e)
        {
            exceptions = true;
        }

        // Check exception wasn't thrown
        assertFalse("An exception was thrown", exceptions);

        // Check file "testSequence.gff" exists and that it contains record equal to actual value.
        assertTrue("Sequence file doesn't exist", new File(dbGenLoc, "db\\testSequence.gff").exists());

        assertTrue("Sequence file doesn't match record", match(retrieved, record));

        // set exterminate request
        db.exterminate();
        // now check thread has exited
        // // Simple wait block to give "thr" time to start
        synchronized (thr)
        {
            try
            {
                thr.wait(100);
            }
            catch (InterruptedException e)
            {
            }
        }
        assertTrue(thr.getState().toString(), isStopped(thr));
        System.gc();
     }

    public void testAddMultipleJobs()
    {
        // Create a new DB
        DatabaseManager db = new DatabaseManager(new File(dbGenLoc, "db"));

        // Start database thread running
        Thread thr = new Thread(db);
        thr.start();

        // Simple wait block to give "thr" time to start
        synchronized (thr)
        {
            try
            {
                thr.wait(500);
            }
            catch (InterruptedException e)
            {
            }
        }

        // Add 10 jobs
        for (int i = 10; i < 110; i++)
        {
            // create a SimpleGFFRecord with null or 0 values excpet sequence name
            SimpleGFFRecord gffr = new SimpleGFFRecord();
            gffr.setSeqName("seq" + i);
            db.addNewResult(gffr);
        }

        // Simple wait block to give "thr" time to start
        synchronized (thr)
        {
            try
            {
                thr.wait(1000);
            }
            catch (InterruptedException e)
            {
            }
        }
        // set exterminate request
        db.exterminate();

        synchronized (thr)
        {
            try
            {
                thr.wait(100);
            }
            catch (InterruptedException e)
            {
            }
        }

        // Check they all exist
        for (int i = 10; i < 110; i++)
        {
            assertTrue("File: seq" + i + ".gff doesn't exist", new File(dbGenLoc, "db\\seq" + i + ".gff").exists());
        }
        // now check thread has exited
        assertTrue(thr.getState().toString(), isStopped(thr));
        System.gc();
    }

    public void testAddListener()
    {
        // Create a new DB to check listener activity
        DatabaseManager db = new DatabaseManager(new File(dbGenLoc, "db"));

        // register a new listener onto it, so we know when new entries are created
        db.addDatabaseListener(new DatabaseListener()
        {
            public void databaseEntryCreated(String entryName)
            {
                // do nothing
            }

            public void databaseEntryFailed(String entryName, Throwable e)
            {
                // do nothing
            }

            public void databaseEntryRemoved(String entryName)
            {
                // do nothing
            }
        });

        // Check one listener is added
        assertFalse("Listener was not registered successfully", db.getDatabaseListener() == null);

        System.gc();
    }

    public void testNotification()
    {
        // Create a new DB to check listener activity
        DatabaseManager db = new DatabaseManager(new File(dbGenLoc, "db"));

        // Create a hashmap to check notification events
        final HashMap notified = new HashMap();

        // register a new listener onto it, so we know when new entries are created
        db.addDatabaseListener(new DatabaseListener()
        {
            public void databaseEntryCreated(String entryName)
            {
                //System.out.println("Record created");
                notified.put(entryName, "created");
            }

            public void databaseEntryFailed(String entryName, Throwable e)
            {
                notified.put(entryName, "failed");
            }

            public void databaseEntryRemoved(String entryName)
            {
                notified.put(entryName, "removed");
            }
        });

        // Check one listener is added
        assertFalse("Listener was not added successfully", db.getDatabaseListener() == null);

        // Add 10 jobs
        for (int i = 0; i < 10; i++)
        {
            // create a SimpleGFFRecord with null or 0 values excpet sequence name
            SimpleGFFRecord gffr = new SimpleGFFRecord();
            gffr.setSeqName("seq" + i);
            db.addNewResult(gffr);
        }

        // Run database thread to add these jobs
        // Start database thread running
        Thread thr = new Thread(db);
        thr.start();

        // Wait a while to give DB time to add records
        synchronized (thr)
        {
            try
            {
                thr.wait(500);
            }
            catch (InterruptedException e)
            {
            }
        }

        // Check records exist
        for (int i = 0; i < 10; i++)
        {
            assertTrue((new File(dbGenLoc, "db\\seq" + i + ".gff")).exists());
        }

        // Check notified 10 times
        for (int i = 0; i < 10; i++)
        {
            assertTrue((notified.get("seq" + i)).equals("created"));
        }

        // set exterminate request
        db.exterminate();
        // Simple wait block to give "thr" time to start
        synchronized (thr)
        {
            try
            {
                thr.wait(100);
            }
            catch (InterruptedException e)
            {
            }
        }
        // now check thread has exited
        assertTrue(thr.getState().toString(), isStopped(thr));
        System.gc();
     }

    private boolean match(GFFRecord record1, GFFRecord record2)
    {
        // Do some matching - actually not totally comprehensive but will suffice for now
        boolean match = true;
        if (record1.getComment() == null && record2.getComment() == null)
        {//match can stay true
        }
        else if (!record1.getComment().matches(record2.getComment()))
        {
            match = false;
            System.out.println("Comment match failed");
            System.out.println("1: " + record1.getComment() + "; 2: " + record2.getComment());
        }
        if (record1.getEnd() != record2.getEnd())
        {
            match = false;
            System.out.println("End match failed");
            System.out.println("1: " + record1.getEnd() + "; 2: " + record2.getEnd());
        }
        if (record1.getFrame() != record2.getFrame())
        {
            match = false;
            System.out.println("Frame match failed");
            System.out.println("1: " + record1.getFrame() + "; 2: " + record2.getFrame());
        }
        if (record1.getScore() == 0 && record2.getScore() == 0)
        {//match can stay true
        }
        else if (record1.getScore() != (record2.getScore()))
        {
            match = false;
            System.out.println("Score match failed");
            System.out.println("1: " + record1.getScore() + "; 2: " + record2.getScore());
        }
        if (!record1.getSeqName().matches(record2.getSeqName()))
        {
            match = false;
            System.out.println("SeqName match failed");
            System.out.println("1: " + record1.getSeqName() + "; 2: " + record2.getSeqName());
        }

        if (record1.getStart() != record2.getStart())
        {
            match = false;
            System.out.println("Start match failed");
            System.out.println("1: " + record1.getStart() + "; 2: " + record2.getStart());
        }
        System.gc();
        return match;
    }

    private boolean isStopped (Thread thread)
    {
        return (thread.getState() == Thread.State.TERMINATED);
    }
}
