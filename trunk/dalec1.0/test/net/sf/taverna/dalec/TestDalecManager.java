package net.sf.taverna.dalec;

import net.sf.taverna.dalec.exceptions.WorkflowCreationException;
import net.sf.taverna.dalec.exceptions.WaitWhileJobComputedException;

import java.io.File;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 24-Jun-2005
 */
public class TestDalecManager
{
    public static void main(String[] args)
    {
        File xsFile = new File ("C:\\home\\tony\\documents\\dalec1.0\\workflow.xml");

        // Test constructor for Dalec Manager by creating a test workflow
        DalecManager davros = null;
        try
        {
            davros = new DalecManager(xsFile, new File ("C:\\home\\tony\\documents\\dalec1.0\\db"));
        }
        catch (WorkflowCreationException e)
        {
            System.out.println (e.getMessage());
            System.out.println ("Caused by: " + e.getCause().toString());
            e.getCause().printStackTrace();
        }

        // Request a sequence - three scenarios are existing sequence, brand new sequence or resubmission of a sequence

        // Request existing sequence
        try
        {
            davros.requestSequence("embl:X13776\t");
        }
        catch (WaitWhileJobComputedException e)
        {
            System.out.println ("This sequence is already being calculated");
        }
    }
}
