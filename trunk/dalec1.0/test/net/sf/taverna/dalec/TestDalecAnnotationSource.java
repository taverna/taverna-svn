package net.sf.taverna.dalec;

import org.biojava.servlets.dazzle.datasource.DataSourceException;
import org.biojava.bio.seq.Sequence;

import java.io.File;

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

        dalec.setMapMaster("http://www.ebi.ac.uk/das-srv/uniprot/das/aristotle/");
        dalec.setXScuflFile(new File("C:\\home\\tony\\documents\\dalec1.0\\workflow.xml"));
        dalec.setSequenceDBLocation(new File("C:\\home\\tony\\documents\\dalec1.0\\outputTest\\"));

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

    public void testGetSequence() throws DataSourceException
    {
        Sequence seq = dalec.getSequence("O35502");
    }

    public void testGetAllTypes()
    {
        System.out.println(dalec.getAllTypes());
    }
}
