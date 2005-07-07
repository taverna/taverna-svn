package net.sf.taverna.dalec;

import net.sf.taverna.dalec.exceptions.WorkflowCreationException;
import net.sf.taverna.dalec.exceptions.WaitWhileJobComputedException;
import net.sf.taverna.dalec.exceptions.UnableToAccessDatabaseException;

import java.io.File;

import junit.framework.TestCase;
import org.biojava.bio.seq.impl.SimpleSequence;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 24-Jun-2005
 */
public class TestDalecManager extends TestCase
{
    DalecManager davros;

    public void construct()
    {
        File xsFile = new File("C:\\home\\tony\\documents\\dalec1.0\\workflow.xml");
        try
        {
            davros = new DalecManager(xsFile, new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\db"));
        }
        catch (WorkflowCreationException e)
        {
            System.out.println("Encountered a WorkflowCreationException");
        }
    }

    public void testDavrosStartup()
    {
        System.out.println("Number of threads at start: " + Thread.activeCount());

        // Build a new davros
        if (davros == null) construct();

        assertFalse(davros == null);
        System.out.println("Dalec is active and running");

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

        System.out.println("Number of threads at end: " + Thread.activeCount());
        davros.exterminate();
        davros = null;
        System.gc();
    }

    public void testNewSequenceRequest()
    {
        if (davros == null) construct();

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

        // Request existing sequence
        try
        {
            davros.requestSequence("embl:X13776\t");
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
                wait(15000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        davros.exterminate();
        davros = null;
        System.gc();
    }

    public void testOldSequenceRequest()
    {
        if (davros == null) construct();

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
        try
        {
            SimpleSequence seq = (SimpleSequence)davros.requestSequence("mySeq");
            System.out.println ("Sequence name is: " + seq.getName());
            System.out.println (seq.toString());
        }
        catch (WaitWhileJobComputedException e)
        {
            System.out.println("This sequence is being calculated");
        }
        catch (UnableToAccessDatabaseException e)
        {
            System.out.println("Unable to acess database");
        }

        davros.exterminate();
        davros = null;
        System.gc();
    }

    public void testErrorLogging()
    {
        DalecManager.logError(new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\db"), "testSequence", new UnableToAccessDatabaseException("testMessage", new UnableToAccessDatabaseException("test cause")));
        System.gc();
    }
}
