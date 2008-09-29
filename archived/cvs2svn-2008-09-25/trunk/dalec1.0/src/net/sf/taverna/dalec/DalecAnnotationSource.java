package net.sf.taverna.dalec;

import org.biojava.servlets.dazzle.datasource.AbstractDataSource;
import org.biojava.servlets.dazzle.datasource.DataSourceException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.DummySymbolList;
import org.biojava.bio.BioException;
import org.biojava.bio.Annotation;
import org.biojava.bio.program.gff.SimpleGFFRecord;
import org.biojava.bio.program.gff.GFFEntrySet;
import org.biojava.utils.ChangeVetoException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import java.io.*;
import java.util.*;

import net.sf.taverna.dalec.exceptions.*;
import net.sf.taverna.dalec.io.WorkflowInput;
import net.sf.taverna.dalec.io.SequenceWorkflowInput;
import net.sf.taverna.dalec.io.SequenceIDWorkflowInput;

import javax.servlet.ServletContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

/**
 * This class serves as a Dazzle datasource plugin for the Dalec annotation package.  In keeping with Dazzle datasource
 * protocols, properties can be set within the <code>dazzlecfg.xml</code> file.  Dazzle can be configured to serve up
 * multiple datasources, so it is possible to have multiple Dalec datasources (multiple Dalecs) per Dazzle
 * implementation.  Dalecs can be configured using the Dalec configurator tool (see user docs).
 * <p/>
 * <h3>Dalec Property Requirements</h3>
 * <p/>
 * Importantly for Dalec, the properties specified in <code>dazzlecfg.xml</code> should include: <ul> <li>[name]: the
 * name of the Dalec, which should be unique </li> <li>[description]: a description of this Dalec </li>
 * <li>[xscuflFile]: the location of the <code>.XScufl</code> format file used to construct the annotation
 * workflow,</li> <li>[MapMaster]: the path to the DAS reference server from which sequences will be annotated.</li>
 * <li>[dbLocation]: the full path to the root directory of the databse filestore, in which the Dalec database should
 * store its results</li> </ul>
 * <p/>
 * Note that, if the specified directory for the Dalec database does not exist, it will be created by Dalec.  Care
 * should be taken when setting this property, as large quantities of data could potentially be written to disk - a
 * single GFF format file record is stored for every sequence query submitted to Dalec.  If you intend to permanently
 * deploy Dalec, the databse filestore should be monitored regularly, adequate disk space should be available and that
 * write permissions are sufficient.
 * <p/>
 * Once all properties are set and the <code>init()</code> method is called, an instance of DalecManager will be created
 * ready to annotate submitted requests.
 *
 * @author Tony Burdett
 * @version 1.0
 */
public class DalecAnnotationSource extends AbstractDataSource
{
    private String mapMaster;
    private File xscuflFile;
    private File seqDB;

    private DalecManager davros;

    /**
     * Implementation of the <code>init()</code> method specified by <code>DazzleDataSource</code>.  In this case, the
     * main purpose of this method is to create a new <code>DalecManager</code>, so all annotation and database
     * retrieval tasks can be delegated, and handled "on-the-fly".
     *
     * @param ctx The servlet context
     * @throws DataSourceException if this DataSource cannot enter service.  This is most likely to be due to a
     *                             worokflow comilation problem in Dalec.
     */
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

    /**
     * Return the fully annotated sequence.  Unlike most Dazzle datasources, when this method is called the information
     * may or may not be held within the database backing this datasource.  The difference is that most datasources
     * would throw a <code>NoSuchElementException</code> if we requested a non-existent sequence, but with Dalec such
     * sequences are submitted to the workflow to be annotated "on-the-fly". In this scenario, a "dummy" sequence is
     * returned, which contains a single annotation over the length of the sequence, labelled with a description
     * "Features are being evaluated".  This allows a client to return this dummy sequence, and a request shouldbe
     * resubmitted after allowing time for the sequence to be fully annotated by the workflow.
     *
     * @param ref The DAS reference server identifier for the sequence requested
     * @return The fully annotated Sequence
     * @throws DataSourceException    If there is a problem acquiring the requested Sequence
     * @throws NoSuchElementException If the sequence requested does not exist on the Reference Server
     */
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
            try
            {
                davros.submitJob(createWorkflowInput(ref));
                return makeDummySequence();
            }
            catch (IncorrectlyNamedInputException e1)
            {
                throw new DataSourceException("Workflow contains no 'seqID' or 'sequence' input processor");
            }
        }
        catch (UnableToAccessDatabaseException e)
        {
            throw new DataSourceException(e, "A problem occurred whilst trying to access the database for the Dalec Annotation Source");
        }
        catch (IllegalSymbolException e)
        {
            throw new DataSourceException(e, "Non-protein sequence or unknown symbol found");
        }
        catch (BioException e)
        {
            throw new DataSourceException(e, "Illegal alphabet or other failure while annotating sequence with the GFF entry retrieved");
        }
        catch (ChangeVetoException e)
        {
            throw new DataSourceException(e, "Sequence won't allow annotation or annotation vetoed");
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
     * @param xscuflFilename String representing the path to the location of the desired workflow <code>.XScufl</code>
     */
    public void setXscuflFile(String xscuflFilename)
    {
        this.xscuflFile = new File(xscuflFilename);
    }

    /**
     * Javabeans style method for setting the Database location used to store the output data for this instance of
     * Dalec.
     *
     * @param sequenceDBLocation String representing the path to the URL of the database storage location.
     */
    public void setSequenceDBLocation(String sequenceDBLocation)
    {
        // TODO - refactor DB access to use URL rather than File locations - across all classes
        this.seqDB = new File(sequenceDBLocation);
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

    /**
     * Make a dummy sequence, which isn't a real sequence but contains some notes explaining that this sequence has been
     * submitted to Dalec and is being calculated.
     */
    protected Sequence makeDummySequence()
    {
        // Make a dummy GFFEntrySet with one record with a human readable explanation to wait
        SimpleGFFRecord dummyRecord = new SimpleGFFRecord();
        dummyRecord.setSeqName("Dalec Unannotated Sequence");
        dummyRecord.setFeature("***ANNOTATIONS BEING EVALUATED BY DALEC*** Please wait while the annotations for this sequence are being calculated.  Resubmit your request shortly.");
        dummyRecord.setComment("features are currently being evaluated");
        dummyRecord.setSource("Dalec1.0");
        GFFEntrySet dummyEntry = new GFFEntrySet();
        dummyEntry.add(dummyRecord);

        // Make a sequence containing this dummy record as the only feature
        Sequence dummySeq = new SimpleSequence(new DummySymbolList(ProteinTools.getTAlphabet(), 0), dummyRecord.getSeqName(), dummyRecord.getSeqName(), Annotation.EMPTY_ANNOTATION);
        try
        {
            dummyEntry.getAnnotator().annotate(dummySeq);
        }
        catch (Exception e)
        {
            // shouldn't ever occur as sequence and annotations are correctly built, above.
        }

        // Return this dummy sequence - only purpose is to show human readable info saying that we're working it out!
        return dummySeq;
    }

    /**
     * Create a <code>WorkflowInput</code> object which wraps all the data needed to be submitted to a workflow, along
     * with correctly named processor names.  Dalec currently recognises two types of workflows.  The first type has a
     * single input processor named "seqID" which accepts a sequence identifier which the workflow then looks up itself.
     * The second type is a workflow with two inputs, one being seqID (which in this case need not necessarily be a
     * unique identifier) and the other being "sequence", accepting the raw sequence data to be annotated.
     * <p/>
     * Additional types of workflow inputs can easily be added by implementing the <code>WorkflowInput</code> interface
     * to describe an object wrapping all the data needed for a specific workflow, and then overriding this method to
     * provide the means to create a WorkflowInput of the new type if the workflow supplied requires it.
     *
     * @param ref the sequenceID
     * @return A new WorkflowInput object of appropriate type
     * @throws IncorrectlyNamedInputException If Dalec does not recognise the pattern of input processors in the
     *                                        supplied workflow
     * @throws DataSourceException            If there is a problem acquiring the requested Sequence
     */
    protected WorkflowInput createWorkflowInput(String ref) throws IncorrectlyNamedInputException, DataSourceException
    {
        // create WorkflowInput objects for any known set of input processors
        WorkflowInput input;

        List inputNames = davros.getInputs();
        int numProcs = inputNames.size();

        if (numProcs == 2 && inputNames.contains("sequence") && inputNames.contains("seqID"))
        {
            // This is a legal workflow type - 2 processors, contains sequence and seqID input processors
            SequenceWorkflowInput thisInput = new SequenceWorkflowInput();
            thisInput.setJobID(ref);
            thisInput.setSequence(fetchQuerySequence(ref));
            input = thisInput;
        }
        else if (numProcs == 1 && inputNames.contains("seqID"))
        {
            // Legal workflow type - 1 processor, seqID processor only
            SequenceIDWorkflowInput thisInput = new SequenceIDWorkflowInput();
            thisInput.setJobID(ref);
            input = thisInput;
        }
        else
        {
            throw new IncorrectlyNamedInputException("No recognised input processor set found in this workflow");
        }

        return input;
    }

    /**
     * Private method to retrieve a query sequence, if the ID was supplied
     *
     * @param seqID
     * @return
     * @throws DataSourceException
     * @throws NoSuchElementException
     */
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
            return seq.toString().trim();
        }
        else
        {
            throw new DataSourceException("More than one 'SEQUENCE' tag present in this XML document");
        }
    }
}
