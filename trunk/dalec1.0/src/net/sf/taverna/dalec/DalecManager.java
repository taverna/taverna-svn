package net.sf.taverna.dalec;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.Annotation;
import org.biojava.bio.program.gff.GFFRecord;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.enactor.implementation.FreefluoEnactorProxy;
import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowInstanceImpl;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.baclava.DataThing;

import java.util.*;
import java.io.*;

import net.sf.taverna.dalec.exceptions.WorkflowCreationException;
import net.sf.taverna.dalec.exceptions.WaitWhileJobComputedException;
import net.sf.taverna.dalec.exceptions.UnableToAccessDatabaseException;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateListener;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.main.WorkflowState;
import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

/**
 * This class co-ordinates all the "hard work" for Dalec, including retaining the cache of jobs to waiting to be done,
 * checking and handling concurrency, invoking the workflow, collecting and returning results.  DalecManager also
 * creates a new instance of DatabaseManager, allowing results to be compiled into a flat file database as they are
 * generated.
 * <p/>
 * Normally, an instance of DalecManager would be created when the <code>init()</code> method is called on
 * DalecAnnotationSource.  When this happens, DalecManager runs through its normal start-up procedure, which involves
 * creating several threads for individual copies of the workflow to run in, allowing the handling of concurrent
 * queries, and creation of a new database (and a new instance of DatabaseManager) to output the results.
 * <p/>
 * When a new request is received from the DAS client, the job can be added to the DalecManager by calling the
 * requestSequence() method. DalecManager then hadnles this request appropriately, whether this sequence is already
 * within the database or needs to be annotated 'on the fly'.
 * <p/>
 * Author: Tony Burdett Date: 15-Jun-2005 Time: 13:49:29
 */
public class DalecManager
{
    private List jobList = new ArrayList();
    private List computeList = new ArrayList();
    private Thread[] workflowThreadPool = new Thread[4];
    private DatabaseManager dbMan;

    /**
     * Constructor for class <code>DalecManager</code>.  This is a failry intensive step, as it involves the creation of
     * several threads and compiles copies of the workflow, using the supplied <code>.Xscufl</code> file into each
     * thread.  Each thread runs indefinitely, allowing jobs to be continually submitted to DalecManager and generating
     * annotations as described by the supplied workflow.
     *
     * @param xscuflFile
     * @param sequenceDBLocation
     * @throws WorkflowCreationException
     */
    public DalecManager(File xscuflFile, File sequenceDBLocation) throws WorkflowCreationException
    {
        // grab Xscufl file and use it to populate the scufl model
        final ScuflModel model = new ScuflModel();
        try
        {
            try
            {
                XScuflParser.populate(new FileInputStream(xscuflFile), model, null);
                System.out.println("Model populated ok");
            }
            catch (XScuflFormatException e)
            {
                throw new FileNotFoundException("Valid XScufl format File not found");
            }
            catch (ProcessorCreationException e)
            {
                // If this occurs, may just be a temp problem with the service
                // Try 3 times before giving up!
                int retry = 0;
                boolean success = false;

                while (retry < 3)
                {
                    // wait a while
                    wait(1000);
                    //retry to populate model
                    try
                    {
                        XScuflParser.populate(new FileInputStream(xscuflFile), model, null);
                        // successful if ProcessorCreationException isn't thrown again - so set succes to true and exit
                        success = true;
                        break;
                    }
                    catch (ProcessorCreationException f)
                    {
                        // Exception thrown again - increment retry and loop
                        retry++;
                    }
                }
                if (!success)
                {
                    // success is false, no processor created after 3 attempts so give up
                    throw new WorkflowCreationException("Unable to create a processor", e);
                }
            }
        }
        catch (FileNotFoundException e)
        {
            throw new WorkflowCreationException("File not found", e);
        }
        catch (Exception e)
        {
            throw new WorkflowCreationException("A problem occurred whilst creating the workflow", e);
        }

        // Create a new DatabaseManager to handle the data outputs - DatabaseManager is Runnable
        dbMan = new DatabaseManager(sequenceDBLocation);

        // register a new listener onto it, so we know when new entries are created
        dbMan.addDatabaseListener(new DatabaseListener()
        {
            public void databaseEntryCreated(String entryName)
            {
                // when the File is entered into the database, we can remove it from the computeList
                synchronized (computeList)
                {
                    computeList.remove(entryName);
                }
            }

            public void databaseEntryFailed(String entryName, Throwable cause)
            {
                // failure should be loggedin error log
                logError(new File(dbMan.getDatabaseLocation(), entryName), "Database entry failure", cause);
                // remove this job from the compute list as it failed
                synchronized (computeList)
                {
                    computeList.remove(entryName);
                }
            }

            public void databaseEntryRemoved(String entryName)
            {
                // Not concerned with this
            }
        });
        // Now start a new thread for the databaseManager, so entries are created dynamically
        (new Thread(dbMan)).start();

        // create concurrent Threads so several copies of the workflow can be executed simultaneously
        // each thread compiles its own copy of workflow from 'model'
        for (int i = 0; i < workflowThreadPool.length; i++)
        {
            workflowThreadPool[i] = new Thread()
            {
                public void run()
                {
                    final WorkflowInstance workflow;
                    // Compile the workflow
                    try
                    {
                        workflow = (new FreefluoEnactorProxy()).compileWorkflow(model, null);

                        // Add WorkFlowStateListener so we can be notified of results
                        ((WorkflowInstanceImpl) workflow).addWorkflowStateListener(new WorkflowStateListener()
                        {
                            public void workflowStateChanged(WorkflowStateChangedEvent event)
                            {
                                WorkflowState state = event.getWorkflowState();
                                // If workflow has finished current job, (ie. state.isFinal()) then send results to DB
                                if (state.isFinal())
                                {
                                    Map output = workflow.getOutput();
                                    GFFRecord gffOut = (GFFRecord) output.get("GFFRecord");
                                    dbMan.addNewResult(gffOut);
                                    // TODO - handle outputs as GFFRecords? or is GFFEntrySet better?
                                    // as this stands, unless a GFF record is the output from the workflow
                                    // and has the taverna output key value "GFFRecord", this will fall over!
                                }
                            }
                        });

                        // Now run forever indefinitely
                        while (true)
                        {
                            try
                            {
                                String jobID;
                                DataThing job;
                                Map inputs = new HashMap();

                                // Continually request new jobs
                                synchronized (jobList)
                                {
                                    // Check to make sure there are pending jobs - otherwise just wait
                                    while (jobList.isEmpty())
                                    {
                                        jobList.wait();
                                    }

                                    jobID = (String) jobList.get(0);
                                    job = (DataThing) jobList.get(0);

                                    // So a new job is allocated - remove from the jobList and place into computeList
                                    jobList.remove(jobID);
                                    synchronized (computeList)
                                    {
                                        computeList.add(jobID);
                                    }
                                }
                                inputs.put(job, job);

                                // do current job
                                workflow.setInputs(inputs);
                                workflow.run();
                            }
                            catch (InvalidInputException e)
                            {
                                // Log the fact that an InvalidInput was received - but then continue
                            }
                        }
                        // TODO - recycling of workflows needed?
                        // Check this is ok; may not be possible to recycle workflows, in which case will need to
                        // recompile every time - and presumably add new listeners?
                    }

                            // Critical Exceptions
                            // If can't submit a workflow, log the problem and interrupt this thread so it can be retried
                    catch (WorkflowSubmissionException e)
                    {

                        Thread.currentThread().interrupt();
                    }
                    catch (InterruptedException e)
                    {

                    }
                }
            };
        }

        // So now all threads are created - so start them running
        for (int i = 0; i < workflowThreadPool.length; i++)
        {
            workflowThreadPool[i].start();
        }
    }

    /**
     * Request a Biojava <code>sequence</code> object containing annotation information, as evaluated by Dalec using the
     * specified <code>.XScufl</code> workflow.
     * <p/>
     * The sequence will be returned from the database if the annotation is completed.  If not, an exception is thrown
     * indicating that this sequence needs to be computed - the client should then try resubmitting the request.
     *
     * @param seqID - String representing the ID of the sequence requested
     * @return a biojava sequence containing annotation information
     * @throws WaitWhileJobComputedException - If this sequence has been previously submitted and is to be calculated
     *                                       shortly.
     * @throws UnableToAccessDatabaseException
     *                                       - If there is a problem accessing the data held within the database.
     */
    public Sequence requestSequence(String seqID) throws WaitWhileJobComputedException, UnableToAccessDatabaseException
    {
        if (jobExists(seqID))
        {
            // Use an exception here to notify DataSource that we are "waiting"
            throw new WaitWhileJobComputedException();
        }
        else
        {
            // job is not in jobList at the moment - this means it has:

            // EITHER been done before and stored - so return the data
            if (dbMan.fileExists(seqID))
            {
                try
                {
                    dbMan.getGFFEntry(seqID);
                }
                catch (UnableToAccessDatabaseException e)
                {
                    // Problem acessing database - log this in error file and throw the error
                    logError(dbMan.getDatabaseLocation(), "Attempting to access database", e);
                    throw e;
                }
                // TODO - parse data from GFF file and compile a valid sequence object - this ISN'T a valid sequence object!!!
                return new SimpleSequence(new SimpleSymbolList(Alphabet.EMPTY_ALPHABET), seqID, seqID, Annotation.EMPTY_ANNOTATION);
            }

            // OR it has never been done - so add to the job list and return a "waiting" message
            else
            {
                submitJob(seqID);
                throw new WaitWhileJobComputedException();
            }
        }
    }

    private void submitJob(String inputID)
    {
        // Synchronized around jobList, prevents concurrent modification
        synchronized (jobList)
        {
            if (jobList.isEmpty())
            {
                // When new job submitted, notifies all threads which are waiting when no outstanding jobs
                jobList.add(inputID);
                jobList.notifyAll();
            }
            else
            {
                // Otherwise just adds next jobs if not empty
                jobList.add(inputID);
            }
        }
    }

    private synchronized boolean jobExists(String jobID)
    {
        // acquire locks on the jobList and computeList
        synchronized (jobList)
        {
            synchronized (computeList)
            {
                // If jobID is present in neither list we return false - otherwise true
                return (jobList.contains(jobID) || computeList.contains(jobID));
            }
        }
    }

    /**
     * Static method which can be called to add an entry to the error log for this database.  Simple text dump for any
     * exceptions which have occurred unusually, this is mainly IO errors which occurred when attempting to read or
     * write to the database, thread interruptions or other anticipated problems.
     *
     * @param sequenceDBLocation The location of the Database - error log files are added to "$DB_LOC$/log/$ERR_FILE$"
     * @param jobName A string describing the job being performed when the problem occurred
     * @param e The throwable cause of the problem making an error log entry necessary.
     */
    public synchronized static void logError(File sequenceDBLocation, String jobName, Throwable e)
    {
        // Name files by todays date
        Calendar cal = new GregorianCalendar();
        String date = cal.get(Calendar.DAY_OF_MONTH) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.YEAR);
        String time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);

        // Files get created as <DB_LOCATION>/log/date.err so eg. /home/usr/myDB/log/01-01-2005.err
        File errFile = new File(sequenceDBLocation, "\\log\\" + date + ".err");

        System.out.println("Path: " + errFile.getPath());

        try
        {
            PrintWriter errLog = null;
            if (errFile.exists())
            {
                // append new error details to the existing file for todays date
                errLog = new PrintWriter(new BufferedWriter(new FileWriter(errFile, true)));
            }
            else
            {
                // create a new error file
                (new File(sequenceDBLocation, "\\log\\")).mkdirs();
                errLog = new PrintWriter(new BufferedWriter(new FileWriter(errFile)));
            }
            errLog.println("**********Start of new log entry**********");
            errLog.println("Error occurred at " + time + ", whilst processing " + jobName);
            errLog.println(e.getMessage());
            errLog.println(e.getCause());
            errLog.println("**********End of log entry**********");
            errLog.println();
            errLog.close();
        }
        catch (IOException e1)
        {
            System.out.println("Unable to write entry to error log file");
        }

    }
}