package net.sf.taverna.dalec;

import org.biojava.servlets.dazzle.datasource.AbstractDataSource;
import org.biojava.servlets.dazzle.datasource.DataSourceException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.HashSet;

import net.sf.taverna.dalec.exceptions.*;
import net.sf.taverna.dalec.io.SequenceIDWorkflowInput;
import net.sf.taverna.dalec.io.SequenceWorkflowInput;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

/**
 * This class serves as a Dazzle datasource plugin for the Dalec annotation package.  In keeping with Dazzle datasource
 * protocols, properties can be set within the <code>dazzlecfg.xml</code> file.
 * <p/>
 * <h3>Dalec Property Requirements</h3>
 * <p/>
 * Importantly for Dalec, the properties specified in <code>dazzlecfg.xml</code> should include: <ul><li>[XScuflFile]:
 * the location of the <code>.XScufl</code> format file used to construct the annotation workflow,</li>
 * <li>[SequenceDBLocation]: the filepath of the location in which the Dalec database should store its results,</li>
 * <li>[MapMaster]: the path to the DAS reference server from which sequences will be annotated.</ul>
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
 * @version 1.0
 * @author Tony Burdett
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
            throw new DataSourceException(e, "Unable to create workflow - see the error log file (" + seqDB.getAbsolutePath() + "\\log\\<DATE>.err) for more information.");
        }

        // Now use normal init method
        super.init(ctx);
    }

    /**
     * Terminate all current activity for this datasource.
     * <p/>
     * Dalec is intended to run indefinitely, automatically spawning new threads to carry out workflow calculations and
     * database entries.  These threads remain live, waiting for the submission of new jobs, until the
     * <code>destroy()</code> method is called.  When called, this method will remove all pending jobs, and allow any
     * active threads to die once their current job has been completed. A <code>DalecAnnotationSource</code> can then be
     * taken out of service. Note that this method will remove any jobs which have been submitted but not yet annotasted
     * by the workflow.  Also, any jobs which <i>have</i> been annotated but not yet written to disk will be lost when
     * this method is called.  Any jobs which have not been added to Dalec's database must be resubmitted when Dalec is
     * restarted.
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
            // Get annotations and build raw sequence
            Sequence seq = ProteinTools.createProteinSequence(fetchQuerySequence(ref), ref);

            // Now annotate this sequence with annotations from davros
            davros.requestAnnotations(ref).getAnnotator().annotate(seq);
            return seq;
        }
        catch (NewJobSubmissionException e)
        {
            // Newly submitted job - so make a new WorkflowInput and submit it to DalecManager
            try
            {
                if (davros.getInputName().matches("sequence"))
                {
                    SequenceWorkflowInput input = new SequenceWorkflowInput();
                    input.setProcessorName(davros.getInputName());
                    input.setJobID(ref);
                    input.setSequenceData(fetchQuerySequence(ref));
                    davros.submitJob(input);
                }
                else if (davros.getInputName().matches("seqID"))
                {
                    SequenceIDWorkflowInput input = new SequenceIDWorkflowInput();
                    input.setProcessorName(davros.getInputName());
                    input.setJobID(ref);
                    davros.submitJob(input);
                }
                else
                {
                    // Should be anything else, IncorrectlyNamedInputExcpetion would be thrown from getInputName()
                }
            }
            catch (IncorrectlyNamedInputException e1)
            {
                throw new DataSourceException(e1);
            }

            // TODO - clever bit needed (!), how to return the "waiting" message to the client?
            return new SimpleSequence(null, null, "Hang on, I'm working this out!", null); //test
        }
        catch (UnableToAccessDatabaseException e)
        {
            throw new DataSourceException(e, "A problem occurred whilst trying to access the database for the Dalec Annotation Source");
        }
        catch (IllegalSymbolException e)
        {
            throw new DataSourceException(e, "Non-protein sequence or unknown symbol found");
        }
        catch (Exception e)
        {
            throw new DataSourceException(e, "A problem was encountered whilst trying to build the Sequence: " + ref);
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
     * Unlike many datasource plugins, Dalec does not have an 'inherent' reference server - rather, Dalec should be
     * pointed at some DAS reference server to annotate the sequences held therein on request.  This method should be
     * used to set that server.
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
     * instance of Dalec. A copy of the workflow .xscufl file should be placed on the server.
     *
     * @param xscuflFile File representing the location of the desried workflow <code>.XScufl</code> file.
     */
    public void setXScuflFile(File xscuflFile)
    {
        // TODO - File or URI? File would mean saving workflow on server, which is probably best anyway
        this.xscuflFile = xscuflFile;
    }

    /**
     * Javabeans style method for setting the Database location used to store the output data for this instance of
     * Dalec.
     *
     * @param sequenceDBLocation File representing the root directory of the database storage location.
     */
    public void setSequenceDBLocation(File sequenceDBLocation)
    {
        // TODO - File or URI? URI is probably better...?
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
        NodeList children = doc.getElementsByTagName("SEQUENCE");
        if (children.getLength() != 1)
        {
            // some error = more than one sequence tag for one sequence ID???
        }
        else
        {
            // pull out version info
            Node child = children.item(0);
            version = ((Element) child).getAttribute("version");
        }
        return version;
    }

    public Set getAllTypes()
    {
        Set types = new HashSet();

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

        // Get elements representing "TYPE" data and add into HashSet
        NodeList nodes = doc.getElementsByTagName("TYPE");
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node typeNode = nodes.item(i);
            types.add(((Element) typeNode).getAttribute("id"));
        }
        return types;
    }

    private String fetchQuerySequence(String seqID) throws DataSourceException, NoSuchElementException
    {
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
            String querySeq = seq.toString().trim();
            System.out.println(querySeq);
            return querySeq;
        }
        else
        {
            throw new DataSourceException("More than one 'SEQUENCE' tag present in this XML document");
        }
    }
}
