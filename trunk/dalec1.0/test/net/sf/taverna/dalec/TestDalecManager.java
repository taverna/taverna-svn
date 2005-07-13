package net.sf.taverna.dalec;

import net.sf.taverna.dalec.exceptions.WorkflowCreationException;
import net.sf.taverna.dalec.exceptions.WaitWhileJobComputedException;
import net.sf.taverna.dalec.exceptions.UnableToAccessDatabaseException;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
import org.biojava.bio.seq.impl.SimpleSequence;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 24-Jun-2005
 */
public class TestDalecManager extends TestCase
{
    private DalecManager construct()
    {
        DalecManager davros = null;
        File xsFile = new File("C:\\home\\tony\\documents\\dalec1.0\\workflow.xml");
        try
        {
            davros = new DalecManager(xsFile, new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\db"));
        }
        catch (WorkflowCreationException e)
        {
            System.out.println("Encountered a WorkflowCreationException");
        }
        return davros;
    }

    public void testDavrosStartStop()
    {
        // Build a new dalec
        DalecManager dalec = construct();

        assertFalse(dalec == null);
        System.out.println("Dalec is active and running");

        assertFalse(dalec.getTerminatedStatus());

        synchronized (this)
        {
            try
            {
                wait(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        dalec.exterminate();
        System.gc();
        assertTrue(dalec.getTerminatedStatus());
    }

    public void testNewSequenceRequest()
    {
        // Build a new dalec
        DalecManager dalec = construct();

        assertFalse(dalec.getTerminatedStatus());

        synchronized (this)
        {
            try
            {
                wait(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        // Request new sequence
        try
        {
            dalec.requestSequence("embl:X13776");
        }
        catch (WaitWhileJobComputedException e)
        {
            System.out.println("This sequence is being calculated");
        }
        catch (UnableToAccessDatabaseException e)
        {
            System.out.println("Unable to acess database");
        }

        synchronized (this)
        {
            try
            {
                wait(5000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        dalec.exterminate();
        System.gc();
        assertTrue(dalec.getTerminatedStatus());
    }

    public void testOldSequenceRequest()
    {
        // Build a new dalec
        DalecManager dalec = construct();

        // Directly submit a dummy sequence to the Database
        TestDatabaseManager tdm = new TestDatabaseManager();
        tdm.testAddJob();
        // have now added testSequence to the database

        try
        {
            SimpleSequence seq = (SimpleSequence) dalec.requestSequence("testSequence");
            System.out.println("Sequence name is: " + seq.getName());
            System.out.println(seq.toString());
        }
        catch (WaitWhileJobComputedException e)
        {
            System.out.println("This sequence is being calculated");
        }
        catch (UnableToAccessDatabaseException e)
        {
            System.out.println("Unable to acess database");
        }

        dalec.exterminate();
        System.gc();
        assertTrue(dalec.getTerminatedStatus());
    }

    public void testManyNewSequences()
    {
        ArrayList ids = new ArrayList();
        ids.add("embl:X13776");
        ids.add("embl:XLA566764");
        ids.add("embl:XL43663");
        ids.add("embl:XL43664");
        ids.add("embl:XCCUR");
        ids.add("embl:XLAF1163");

        // Build a new dalec
        DalecManager dalec = construct();

        synchronized (this)
        {
            try
            {
                wait(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        // Request new sequences
        for (Iterator it = ids.iterator(); it.hasNext();)
        {
            try
            {
                dalec.requestSequence((String) it.next());
            }
            catch (WaitWhileJobComputedException e)
            {
                System.out.println("This sequence is being calculated");
            }
            catch (UnableToAccessDatabaseException e)
            {
                System.out.println("Unable to acess database");
            }
        }

        synchronized (this)
        {
            try
            {
                wait(20000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        dalec.exterminate();
        System.gc();
        assertTrue(dalec.getTerminatedStatus());
    }

    public void testErrorLogging()
    {
        DalecManager.logError(new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\db"), "testSequence", new UnableToAccessDatabaseException("testMessage", new UnableToAccessDatabaseException("test cause")));
        System.gc();
    }
}
