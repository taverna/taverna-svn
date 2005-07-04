package net.sf.taverna.dalec;

import net.sf.taverna.dalec.exceptions.WorkflowCreationException;
import net.sf.taverna.dalec.exceptions.WaitWhileJobComputedException;
import net.sf.taverna.dalec.exceptions.UnableToAccessDatabaseException;

import java.io.File;

import junit.framework.TestCase;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 24-Jun-2005
 */
public class TestDalecManager extends TestCase
{
    public DalecManager davrosCreate() throws WorkflowCreationException
    {
        File xsFile = new File("C:\\home\\tony\\documents\\dalec1.0\\workflow.xml");

        // Test constructor for Dalec Manager by creating a test workflow
        DalecManager davros = null;
        davros = new DalecManager(xsFile, new File("C:\\home\\tony\\documents\\dalec1.0\\db"));
        return davros;
    }

    public void testDavrosCreation()
    {
        DalecManager davros = null;
        try
        {
            davros = davrosCreate();
        }
        catch (WorkflowCreationException e)
        {
            // A workflow exception
        }
        assertFalse(davros == null);
    }

    public void testSequenceRequest()
    {
        DalecManager davros = null;
        try
        {
            davros = davrosCreate();
        }
        catch (WorkflowCreationException e)
        {
            // A workflow exception
        }
        // Request existing sequence
        try
        {
            davros.requestSequence("embl:X13776\t");
        }
        catch (WaitWhileJobComputedException e)
        {
            System.out.println ("This sequence is already being calculated");
        }
        catch (UnableToAccessDatabaseException e)
        {
            System.out.println ("Unable to acess database");
        }
    }

    public void testErrorLogging ()
    {
        DalecManager.logError(new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\db"), "testSequence", new UnableToAccessDatabaseException());
    }
}
