package net.sf.taverna.dalec;

import net.sf.taverna.dalec.exceptions.WorkflowCreationException;
import net.sf.taverna.dalec.exceptions.NewJobSubmissionException;
import net.sf.taverna.dalec.exceptions.UnableToAccessDatabaseException;
import net.sf.taverna.dalec.exceptions.IncorrectlyNamedInputException;
import net.sf.taverna.dalec.workflow.io.SequenceIDWorkflowInput;

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
            System.out.println("Dalec is active");
        }
        catch (WorkflowCreationException e)
        {
            System.out.println("Encountered a WorkflowCreationException");
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

    public void testNewSequenceRequest()
    {
        System.out.println("Dalec is active and running");

        assertFalse(dalec.getTerminatedStatus());

        // Request new sequence
        try
        {
            dalec.requestAnnotations("embl:X13776");
        }
        catch (NewJobSubmissionException e)
        {
            try
            {
                System.out.println("This sequence is being calculated");
                SequenceIDWorkflowInput input = new SequenceIDWorkflowInput();
                input.setProcessorName(dalec.getInputName());
                input.setJobID("embl:X13776");
                dalec.submitJob(input);
            }
            catch (IncorrectlyNamedInputException e1)
            {
                e1.printStackTrace();
            }
        }
        catch (UnableToAccessDatabaseException e)
        {
            System.out.println("Unable to acess database");
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
                System.out.println("Sequence name: " + record.getSeqName());
                System.out.println("Source:" + record.getSource());
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

    public void testManyNewSequences()
    {
        ArrayList ids = new ArrayList();
        ids.add("embl:X13776");
        ids.add("embl:XLA566764");
        ids.add("embl:XL43663");
        ids.add("embl:XL43664");
        ids.add("embl:XCCUR");
        ids.add("embl:XLAF1163");

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
                try
                {
                    System.out.println("This sequence is being calculated");
                    SequenceIDWorkflowInput input = new SequenceIDWorkflowInput();
                    input.setProcessorName(dalec.getInputName());
                    input.setJobID(nextSeq);
                    dalec.submitJob(input);
                }
                catch (IncorrectlyNamedInputException e1)
                {
                    e1.printStackTrace();
                }
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
    }

    public void testErrorLogging()
    {
        DalecManager.logError(new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\db"), "testSequence", new UnableToAccessDatabaseException("testMessage", new UnableToAccessDatabaseException("test cause")));
        System.gc();
    }
}
