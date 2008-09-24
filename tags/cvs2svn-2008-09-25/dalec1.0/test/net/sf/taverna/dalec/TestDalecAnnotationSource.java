package net.sf.taverna.dalec;

import org.biojava.servlets.dazzle.datasource.DataSourceException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.Feature;

import java.util.Iterator;

import junit.framework.TestCase;

/**
 * Javadocs go here.
 *
 * @author Tony Burdett date: 28-Jun-2005
 */
public class TestDalecAnnotationSource extends TestCase
{
    DalecAnnotationSource dalec;

    protected void setUp() throws DataSourceException
    {
        dalec = new DalecAnnotationSource();

        dalec.setName("Dalec_test");
        dalec.setDescription("test annotation source for dalec");
        dalec.setMapMaster("http://www.ebi.ac.uk/das-srv/uniprot/das/aristotle/");
        dalec.setXscuflFile("dalecTestWorkflow.xml");
        dalec.setSequenceDBLocation("outputTest\\database");

        // Use null ServletContext, don't need it
        dalec.init(null);
    }

    protected void tearDown()
    {
        try
        {
            dalec.destroy();
        }
        finally
        {
            dalec = null;
            System.gc();
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

    public void testGetSequence() throws DataSourceException
    {
        Sequence seq = dalec.getSequence("O35502");
        System.out.println("Sequence acquired is: " + seq.getName());
        System.out.println("It contains the following feature(s)...");
        int count=1;
        for (Iterator it=seq.features(); it.hasNext();)
        {
            System.out.println("Feature " + count + ":");
            Feature f = (Feature)it.next();
            System.out.println("\tLocation: " + f.getLocation().getMin() + "..." + f.getLocation().getMax());
            System.out.println("\tType: " + f.getType());
            System.out.println("\tSource: " + f.getSource());
        }

        calcDelay();
    }

    public void testGetAllTypes()
    {
        System.out.println(dalec.getAllTypes());
    }
}
