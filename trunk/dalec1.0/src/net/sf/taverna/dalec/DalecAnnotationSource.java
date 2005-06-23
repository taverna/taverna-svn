package net.sf.taverna.dalec;

import org.biojava.servlets.dazzle.datasource.AbstractDataSource;
import org.biojava.servlets.dazzle.datasource.DataSourceException;
import org.biojava.bio.seq.Sequence;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sf.taverna.dalec.exceptions.WorkflowCreationException;
import net.sf.taverna.dalec.exceptions.WaitWhileJobComputedException;

import javax.servlet.ServletContext;

/**
 * This class serves as a Dazzle datasource plugin for the Dalec annotation package.  In keeping with Dazzle datasource
 * protocols, properties can be set within the <code>dazzlecfg.xml</code> file.
 * <p/>
 * <h3>Dalec Property Requirements</h3>
 * <p/>
 * Importantly for Dalec, the properties  specified in <code>dazzlecfg.xml</code>  should include: <li>The location of
 * the <code>.XScufl</code> format file used to construct the annotation workflow, and</li> <li>a location in which the
 * Dalec database should store its results.</li>
 * <p/>
 * Note that, if the specified directory for the Dalec database does not exist, it will be created by Dalec.  Care
 * should be taken when setting this property, as large quantities of data could potentially be written to disk - a
 * single GFF format file record is stored for every query submitted to Dalec.  If you intend to permanently deploy
 * Dalec within a DAS server, adequate disk space will be needed to store the acquired results over time.
 * <p/>
 *
 * @author Tony Burdett Date: 15-Jun-2005 Time: 10:58:08
 */
public class DalecAnnotationSource extends AbstractDataSource
{
    private String mapMaster;
    private File xscuflFile;
    private File seqDB;

    DalecManager davros;

    public void init(ServletContext servletContext) throws DataSourceException
    {
        super.init(servletContext);

        try
        {
            davros = new DalecManager(xscuflFile, seqDB);
        }
        catch (WorkflowCreationException e)
        {
            throw new DataSourceException(e, "Unable to create workflow");

            // TODO - some sensible response as to why this failed should go here
        }
    }

    public Sequence getSequence(String seqID) throws DataSourceException, NoSuchElementException
    {
        try
        {
            return davros.requestSequence(seqID);
        }
        catch (WaitWhileJobComputedException e)
        {
            // TODO - clever bit needed (!), how to return the "waiting" message to the client?
            return null; //or something else?
        }
    }

    public String getDataSourceType()
    {
        return "Dalec";
    }

    public String getDataSourceVersion()
    {
        return "1.0";
    }

    public void setMapMaster(String s)
    {
        this.mapMaster = s;
    }

    public String getMapMaster()
    {
        return mapMaster;
    }

    /**
     * Javabeans style method for setting the <code>.XScufl</code> file location used to construct the workflow for this
     * instance of Dalec.
     *
     * @param xscuflFile File representing the location of the desried workflow <code>.XScufl</code> file.
     */
    public void setXScuflFile(File xscuflFile)
    {
        this.xscuflFile = xscuflFile;
    }

    /**
     * Javabeans style method for setting the Database location used to store the output data for this
     * instance of Dalec.
     *
     * @param sequenceDBLocation    File representing the root direcotry of the database storage location.
     */
    public void setSequenceDBLocation(File sequenceDBLocation)
    {
        this.seqDB = sequenceDBLocation;
    }

    public String getLandmarkVersion(String s) throws DataSourceException, NoSuchElementException
    {
        // TODO - getLandmarkVersion()... what is expected here?
        return null;
    }

    public Set getAllTypes()
    {
        // TODO - getAllTypes()... what is expected here?
        return null;
    }
}
