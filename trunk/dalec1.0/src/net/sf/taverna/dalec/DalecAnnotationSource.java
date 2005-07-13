package net.sf.taverna.dalec;

import org.biojava.servlets.dazzle.datasource.AbstractDataSource;
import org.biojava.servlets.dazzle.datasource.DataSourceException;
import org.biojava.bio.seq.Sequence;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Set;

import net.sf.taverna.dalec.exceptions.WorkflowCreationException;
import net.sf.taverna.dalec.exceptions.WaitWhileJobComputedException;
import net.sf.taverna.dalec.exceptions.UnableToAccessDatabaseException;
import net.sf.taverna.dalec.exceptions.IncorrectlyNamedProcessorException;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

/**
 * This class serves as a Dazzle datasource plugin for the Dalec annotation package.  In keeping with Dazzle datasource
 * protocols, properties can be set within the <code>dazzlecfg.xml</code> file.
 * <p/>
 * <h3>Dalec Property Requirements</h3>
 * <p/>
 * Importantly for Dalec, the properties specified in <code>dazzlecfg.xml</code>  should include: <ul><li>The location
 * of the <code>.XScufl</code> format file used to construct the annotation workflow, and</li> <li>a location in which
 * the Dalec database should store its results.</li></ul>
 * <p/>
 * Note that, if the specified directory for the Dalec database does not exist, it will be created by Dalec.  Care
 * should be taken when setting this property, as large quantities of data could potentially be written to disk - a
 * single GFF format file record is stored for every sequence query submitted to Dalec.  If you intend to permanently
 * deploy Dalec within a DAS server, ensure adequate disk space will be available and that write permissions are
 * sufficient.
 * <p/>
 * Once all properties are set and the <code>init()</code> method is called, an instance of DalecManager will be created
 * ready to annotate submitted requests.
 *
 * @author Tony Burdett Date: 15-Jun-2005 Time: 10:58:08
 */
public class DalecAnnotationSource extends AbstractDataSource
{
    private String mapMaster;
    private File xscuflFile;
    private File seqDB;

    private DalecManager davros;

    public void init(ServletContext ctx) throws DataSourceException
    {
        try
        {
            davros = new DalecManager(xscuflFile, seqDB);
        }
        catch (WorkflowCreationException e)
        {
            DalecManager.logError(seqDB, "workflow creation", e);
            throw new DataSourceException(e, "Unable to create workflow - see the error log file (" + seqDB.getAbsolutePath() + "log\\<DATE>.err) for more information.");
        }

        // Now use normal init method
        super.init(ctx);
    }

    /**
     * Specific destroy method for this Datasource.  The <code>destroy()</code> method specified in the abstract class
     * <code>AbstractDataSource</code> by default does nothing.  However, Dalec is intended to run indefinitely,
     * automatically spawning new threads to carry out workflow calculations and database entries.  These threads then
     * remain active until interrupted.  Hence, if a DalecAnnotationSource is to be taken out of service, these threads
     * must be terminated using this destroy method.
     */
    public void destroy()
    {
        // Call DalecManager.exterminate() which ceases all active threads
        davros.exterminate();

        // Now set all initialised values to null
        mapMaster = null;
        xscuflFile = null;
        seqDB = null;
        davros = null;
    }

    public Sequence getSequence(String ref) throws DataSourceException, NoSuchElementException
    {
        try
        {
            if (davros.inputIsSequence())
            {
                String seq = fetchQuerySequence(ref);
                return davros.requestSequence(seq);
            }
            else
            {
                return davros.requestSequence(ref);
            }
        }
        catch (WaitWhileJobComputedException e)
        {
            // TODO - clever bit needed (!), how to return the "waiting" message to the client?
            return null; //or something else?
        }
        catch (UnableToAccessDatabaseException e)
        {
            throw new DataSourceException(e, "A problem occurred whilst trying to access the database for the Dalec Annotation Source");
        }
        catch (IncorrectlyNamedProcessorException e)
        {
            throw new DataSourceException(e, "This workflow does not conform to required Dalec protocols");
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

    /**
     * Javabeans style method for setting the "MapMaster" URL for the reference server for this DataSource plugin.
     *
     * @param s A String representing the MapMaster URL.
     */
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
     * Javabeans style method for setting the Database location used to store the output data for this instance of
     * Dalec.
     *
     * @param sequenceDBLocation File representing the root direcotry of the database storage location.
     */
    public void setSequenceDBLocation(File sequenceDBLocation)
    {
        this.seqDB = sequenceDBLocation;
    }

    public String getLandmarkVersion(String ref) throws DataSourceException, NoSuchElementException
    {
        String version = null;

        // formulate DAS query URL
        String queryURL = getMapMaster() + "sequence?segment=" + ref;

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
            // if an exception is thrown, determine whether it is due to access error or if this sequence doesn't exist
            if (doc.getXmlVersion() != null)
            {
                // There is a valid XML doc for this sequence
                throw new DataSourceException(e.getCause(), "Exception occurred whilst parsing document: " + queryURL);
            }
            else
            {
                throw new NoSuchElementException("The DAS query for " + queryURL + " returned no results");
            }
        }

        // get relevant "SEQUENCE" element
        Element currentElement = doc.getDocumentElement();
        NodeList children = currentElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if (child instanceof Element)
            {
                if (child.getNodeName() == "SEQUENCE")
                {
                    // get version info
                    version = ((Element) child).getAttribute("version");
                }
            }
        }
        return version;
    }

    public Set getAllTypes()
    {
        Set types = null;

        // formulate DAS query URL
        String queryURL = getMapMaster() + "types";

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
            // if any exception is caught here, we can just return an empty set
        }

        Element currentElement = doc.getDocumentElement();
        NodeList children = currentElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if (child instanceof Element)
            {
                if (child.getNodeName() == "TYPE")
                {
                    // get version and types info
                    types.add(((Element) child).getAttribute("id"));
                }
            }
        }
        return types;
    }

    private String fetchQuerySequence(String seqID) throws DataSourceException, NoSuchElementException
    {
        // Set up empty string for query sequence
        String querySeq = null;

        // formulate DAS query URL
        String queryURL = getMapMaster() + "sequence?segment=" + seqID;

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
            // if an exception is thrown, determine whether it is due to access error or if this sequence doesn't exist
            if (doc.getXmlVersion() != null)
            {
                // There is a valid XML doc for this sequence
                throw new DataSourceException(e.getCause(), "Exception occurred whilst parsing document: " + queryURL);
            }
            else
            {
                throw new NoSuchElementException("The DAS query for " + queryURL + " returned no results");
            }
        }

        // get relevant "SEQUENCE" element
        Element currentElement = doc.getDocumentElement();
        NodeList children = currentElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if (child instanceof Element)
            {
                if (child.getNodeName() == "SEQUENCE")
                {
                    // parse content to sequence string
                    querySeq = child.getTextContent();
                }
            }
        }
        return querySeq;
    }
}
