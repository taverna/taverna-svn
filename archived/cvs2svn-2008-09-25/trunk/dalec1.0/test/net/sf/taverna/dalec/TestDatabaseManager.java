package net.sf.taverna.dalec;

import org.biojava.bio.program.gff.SimpleGFFRecord;
import org.biojava.bio.program.gff.GFFRecord;
import org.biojava.bio.program.gff.GFFEntrySet;

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
    DatabaseManager dm;
    Thread dbThread;

    protected void setUp()
    {
        dm = new DatabaseManager(new File("outputTest\\database"));
        dbThread = new Thread(dm);
        synchronized (dbThread)
        {
            dbThread.start();
        }
    }

    protected void writeDelay()
    {
        synchronized (dbThread)
        {
            try
            {
                dbThread.wait(1000); // give this thread an arbitrary amount of time to start up
            }
            catch (InterruptedException e)
            {
                // Don't really care about this
            }
        }
    }

    protected void tearDown()
    {
        try
        {
            dm.exterminate();
            // need to wait till supplied jobs have finished
            while (dbThread.getState() != Thread.State.TERMINATED)
            {
                // do nothing
            }
        }
        finally
        {
            dbThread.interrupt();
            dbThread = null;
            System.gc();
        }
    }

    public void testDBLocation ()
    {
        assertTrue (dm.getDatabaseLocation().equals(new File("outputTest\\database")));
    }

    public void testDbRun()
    {
        synchronized (dbThread)
        {
            assertTrue("Thread is not waiting for jobs - status is " + dbThread.getState().toString(), dbThread.getState() == Thread.State.WAITING);
        }
    }

    public void testAddJob()
    {
        boolean exceptions = false;

        // If we add a dummy "null" GFFRecord to an new GFFEntrySet it should get written
        GFFEntrySet gffe = new GFFEntrySet();
        SimpleGFFRecord record = new SimpleGFFRecord();
        record.setSeqName("testSequence");
        gffe.add(record);
        dm.addNewResult(gffe); // GFFEntrySet of one empty GFFRecord

        writeDelay();

        GFFRecord retrieved = null;
        try
        {
            retrieved = (GFFRecord) dm.getGFFEntry(record.getSeqName()).lineIterator().next();
        }
        catch (UnableToAccessDatabaseException e)
        {
            exceptions = true;
        }

        // Check exception wasn't thrown
        assertFalse("An exception was thrown", exceptions);

        // Check file "testSequence.gff" exists and that it contains record equal to actual value.
        assertTrue("Sequence file doesn't exist", (new File(dm.getDatabaseLocation(), "testSequence.gff")).exists());

        assertTrue("Sequence file doesn't match record", match(retrieved, record));
    }

    public void testAddMultipleJobs()
    {
        // Add 10 jobs
        for (int i = 10; i < 110; i++)
        {
            // create SimpleGFFRecords with null or 0 values excpet sequence name
            GFFEntrySet gffe = new GFFEntrySet();
            SimpleGFFRecord record = new SimpleGFFRecord();
            record.setSeqName("seq" + i);
            gffe.add(record);

            dm.addNewResult(gffe);
        }

        writeDelay();

        // Check they all exist
        for (int i = 10; i < 110; i++)
        {
            assertTrue("File: seq" + i + ".gff doesn't exist", (new File(dm.getDatabaseLocation(), "seq" + i + ".gff")).exists());
        }
    }

    public void testAddListener()
    {
        // register a new listener onto it, so we know when new entries are created
        dm.addDatabaseListener(new DatabaseListener()
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
        assertNotNull("Listener was not registered successfully", dm.getDatabaseListener());

        System.gc();
    }

    public void testNotification()
    {
        // Create a hashmap to check notification events
        final HashMap notified = new HashMap();

        // register a new listener onto it, so we know when new entries are created
        dm.addDatabaseListener(new DatabaseListener()
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
        assertNotNull("Listener was not added successfully", dm.getDatabaseListener());

        // Add 10 jobs
        for (int i = 0; i < 10; i++)
        {
            // create SimpleGFFRecords with null or 0 values excpet sequence name
            GFFEntrySet gffe = new GFFEntrySet();
            SimpleGFFRecord record = new SimpleGFFRecord();
            record.setSeqName("ListenerTestSeq" + i);
            gffe.add(record);

            dm.addNewResult(gffe);
        }

        writeDelay();

        // Check records exist
        for (int i = 0; i < 10; i++)
        {
            assertTrue("File: ListenerTestSeq" + i + ".gff doesn't exist", (new File(dm.getDatabaseLocation(), "ListenerTestSeq" + i + ".gff")).exists());
        }

        // Check notified 10 times
        for (int i = 0; i < 10; i++)
        {
            assertTrue((notified.get("ListenerTestSeq" + i)).equals("created"));
        }
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
}
