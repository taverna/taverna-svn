package net.sf.taverna.dalec;

import net.sf.taverna.dalec.exceptions.WorkflowCreationException;
import net.sf.taverna.dalec.exceptions.NewJobSubmissionException;
import net.sf.taverna.dalec.exceptions.UnableToAccessDatabaseException;
import net.sf.taverna.dalec.io.WorkflowInput;
import net.sf.taverna.dalec.io.SequenceWorkflowInput;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import junit.framework.TestCase;
import org.biojava.bio.program.gff.GFFEntrySet;
import org.biojava.bio.program.gff.GFFRecord;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
        File xsFile = new File("dalecTestWorkflow.xml");
        try
        {
            dalec = new DalecManager(xsFile, new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\database"));
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
                wait(5000);
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
            dalec = new DalecManager(xsFile, new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\database"));
            fail();
        }
        catch (WorkflowCreationException e)
        {
            System.out.println ("Test passed - a WorkflowCreationException was correctly thrown");
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
                System.out.println("TEST: GFF record named '" + record.getSeqName() + "' found");
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
            WorkflowInput input = createSequenceWorkflowInput("O35502");
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
        ids.add("Q12345");
        ids.add("Q07732");
        ids.add("Q12386");
        ids.add("Q38316");
        ids.add("Q03818");
        ids.add("Q12380");

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
                    WorkflowInput input = createSequenceWorkflowInput(nextSeq);
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
        DalecManager.logError(new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\database"), "testSequence", new UnableToAccessDatabaseException("testMessage", new UnableToAccessDatabaseException("test cause")));
        System.gc();
    }

    private WorkflowInput createSequenceWorkflowInput (String ref)
    {
        String querySeq = "";
        String queryURL = "http://www.ebi.ac.uk/das-srv/uniprot/das/aristotle/sequence?segment=" + ref;

        // Build and parse XML doc
        DocumentBuilder db = null;
        Document doc = null;
        try
        {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = db.parse(queryURL);
        }
        catch (Exception e)
        {
            fail();
        }

        // get relevant "SEQUENCE" element
        NodeList children = doc.getElementsByTagName("SEQUENCE");
        if (children.getLength() == 1)
        {
            Node child = children.item(0);
            String rawSeq = child.getTextContent();

            // Now we have querySeq - only problem is, contains newline chars!
            char[] charSeq = rawSeq.toCharArray();
            StringBuffer seq = new StringBuffer();
            for (int i = 0; i < charSeq.length; i++)
            {
                if (charSeq[i] != '\n')
                {
                    seq.append(charSeq[i]);
                }
                else
                {
                    // ignore the newline char
                }
            }
            querySeq = seq.toString().trim();
        }

        SequenceWorkflowInput input = new SequenceWorkflowInput();
        input.setJobID(ref);
        input.setSequence(querySeq);
        return input;
    }
}
