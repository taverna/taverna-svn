package net.sf.taverna.dalec;

import net.sf.taverna.dalec.exceptions.WorkflowCreationException;
import net.sf.taverna.dalec.exceptions.NewJobSubmissionException;
import net.sf.taverna.dalec.exceptions.UnableToAccessDatabaseException;
import net.sf.taverna.dalec.io.SequenceIDWorkflowInput;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
import org.biojava.bio.program.gff.GFFEntrySet;
import org.biojava.bio.program.gff.GFFRecord;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 24-Jun-2005
 */
public class TestDalecManager extends TestCase
{
    DalecManager dalec;

    protected void setUp()
    {
        File xsFile = new File("C:\\home\\tony\\documents\\dalec1.0\\workflow.xml");
        try
        {
            dalec = new DalecManager(xsFile, new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\db"));
            System.out.println("TEST: Dalec is active");
        }
        catch (WorkflowCreationException e)
        {
            fail();
        }
    }

    protected void calcDelay()
    {
        // allow time for jobs to complete before destroying
        synchronized (this)
        {
            try
            {
                wait(20000);
            }
            catch (InterruptedException e)
            {
                fail();
            }
        }
    }

    protected void tearDown()
    {
        try
        {
            dalec.exterminate();
        }
        finally
        {
            dalec = null;
            System.gc();
        }
    }

    public void testWrongWorkflow()
    {
        File xsFile = new File("C:\\home\\tony\\documents\\dalec1.0\\wrongfile.xml"); // doesn't exist

        try
        {
            dalec = new DalecManager(xsFile, new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\db"));
            fail();
        }
        catch (WorkflowCreationException e)
        {
            fail();
        }
        finally
        {
            assertFalse(dalec.getTerminatedStatus());
        }
    }

    public void testNewNotOld()
    {
        assertFalse(dalec.getTerminatedStatus());

        try
        {
            dalec.requestAnnotations("Non-existent sequence");
            fail();
        }
        catch (UnableToAccessDatabaseException e)
        {
            fail();
        }
        catch (NewJobSubmissionException e)
        {
            System.out.println("Test passed - this sequence should be a new submission");
        }
    }

    public void testOldSequenceRequest()
    {
        // Directly submit a dummy sequence to the Database
        TestDatabaseManager tdm = new TestDatabaseManager();
        tdm.setUp();
        tdm.testAddJob();
        tdm.tearDown();
        // have now added testSequence to the database
        try
        {
            GFFEntrySet gffe = dalec.requestAnnotations("testSequence");
            for (Iterator it = gffe.lineIterator(); it.hasNext();)
            {
                GFFRecord record = (GFFRecord) it.next();
                System.out.println("TEST: GFF record " + record.toString());
            }
        }
        catch (NewJobSubmissionException e)
        {
            System.out.println("This sequence is being calculated");
        }
        catch (UnableToAccessDatabaseException e)
        {
            System.out.println("Unable to acess database");
        }
    }

    public void testNewSequenceRequest()
    {
        assertFalse(dalec.getTerminatedStatus());

        // Request new sequence
        try
        {
            dalec.requestAnnotations("embl:X13776");
        }
        catch (NewJobSubmissionException e)
        {
            System.out.println("TEST: This sequence is being calculated");
            SequenceIDWorkflowInput input = new SequenceIDWorkflowInput();
            input.setJobID("embl:X13776\t");
            dalec.submitJob(input);
        }
        catch (UnableToAccessDatabaseException e)
        {
            System.out.println("Unable to acess database");
        }

        calcDelay();
    }

    public void testManyNewSequences()
    {
        ArrayList ids = new ArrayList();
        ids.add("embl:X13776\t");
        ids.add("embl:XLA566764\t");
        ids.add("embl:XL43663\t");
        ids.add("embl:XL43664\t");
        ids.add("embl:XCCUR\t");
        ids.add("embl:XLAF1163\t");

        // Request new sequences
        for (Iterator it = ids.iterator(); it.hasNext();)
        {
            String nextSeq = (String) it.next();
            try
            {
                dalec.requestAnnotations(nextSeq);
            }
            catch (NewJobSubmissionException e)
            {
                    System.out.println("TEST: This sequence is being calculated");
                    SequenceIDWorkflowInput input = new SequenceIDWorkflowInput();
                    input.setJobID(nextSeq);
                    dalec.submitJob(input);
            }
            catch (UnableToAccessDatabaseException e)
            {
                fail();
            }
        }

        calcDelay();
    }

    public void testErrorLogging()
    {
        DalecManager.logError(new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\db"), "testSequence", new UnableToAccessDatabaseException("testMessage", new UnableToAccessDatabaseException("test cause")));
        System.gc();
    }
}
