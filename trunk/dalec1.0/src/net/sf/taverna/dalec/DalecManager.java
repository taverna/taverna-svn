package net.sf.taverna.dalec;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.impl.SimpleSequence;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.Annotation;
import org.biojava.bio.program.gff.SimpleGFFRecord;
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
import net.sf.taverna.dalec.exceptions.IncorrectlyNamedProcessorException;
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
    /**
     * Indicates whether this DalecManager is running or not (ie. whether <code>init()</code> has been called).  Returns
     * true if running, false otherwise.
     */
    private DatabaseManager dbMan;
    private ScuflModel model;

    private List jobList = new ArrayList();
    private List computeList = new ArrayList();

    private Thread[] workflowThreadPool = new Thread[4];
    private Thread dbThread;
    private boolean terminated = false;

    /**
     * Constructor for class <code>DalecManager</code>.  When a new <code>DalecManager</code> is created, the database
     * it will use is created using the <code>File</code> parameter passed to it.  A <code>DatabaseListener</code> is
     * registered to the DatabaseManager, and a dedicated database thread is created and started to handle database
     * entries as the results are generated from the workflow.
     * <p/>
     * The workflow file passed is used to populate a workflow model, which will then be used to compile workflows to do
     * annotations on sequences passed to Dalec by the client.  A <code>WorkflowCreationException</code> is thrown if
     * problems are encountered in populating the model.
     * <p/>
     * Once the model has been populated, several concurrent threads are started, containing copies of the wokflow
     * model.  Each thread then compiles a working instance of the workflow.  Each thread then dynamically annotates
     * submitted sequences as jobs are sent to DalecManager, sending the generated results to the instance of
     * <code>DatabaseManager</code> to be entered into the database.
     *
     * @param xscuflFile
     * @param sequenceDBLocation
     * @throws WorkflowCreationException
     */
    public DalecManager(File xscuflFile, File sequenceDBLocation) throws WorkflowCreationException
    {
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
        dbThread = new Thread(dbMan, "Database_Thread");
        dbThread.start();

        // grab Xscufl file and use it to populate the scufl model
        model = new ScuflModel();
        try
        {
            try
            {
                synchronized (model)
                {
                    XScuflParser.populate(new FileInputStream(xscuflFile), model, null);
                }
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

                while (retry < 3 && !success)
                {
                    // wait a while
                    wait(1000);
                    //retry populating model
                    try
                    {
                        XScuflParser.populate(new FileInputStream(xscuflFile), model, null);
                        // successful if ProcessorCreationException isn't thrown again - so set succes to true and exit
                        success = true;
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

        // create concurrent Threads so several copies of the workflow can be executed simultaneously
        // each thread compiles its own copy of workflow from 'model'
        for (int i = 0; i < workflowThreadPool.length; i++)
        {
            workflowThreadPool[i] = new Thread("Workflow_Thread_" + i)
            {
                public void run()
                {
                    try
                    {
                        // Run indefinitely until terminated
                        while (!terminated)
                        {
                            String jobID = null;
                            DataThing job;
                            Map inputs = new HashMap();

                            // Continually request new jobs
                            synchronized (jobList)
                            {
                                // Check to make sure there are pending jobs - otherwise just wait
                                while (jobList.isEmpty() && !terminated)
                                {
                                    jobList.wait();
                                }
                                if (terminated) break;

                                // Gotten past wait and break operations so must have new job
                                jobID = (String) jobList.get(0);
                                job = new DataThing(jobID);

                                // So a new job is allocated - remove from the jobList and place into computeList
                                jobList.remove(jobID);
                                synchronized (computeList)
                                {
                                    computeList.add(jobID);
                                }
                            }
                            inputs.put("seqID", job); // now have our job set as an input

                            // compile our workflow
                            try
                            {
                                final WorkflowInstance workflow = (new FreefluoEnactorProxy()).compileWorkflow(model, null);

                                // Add WorkFlowStateListener so we can be notified of results
                                ((WorkflowInstanceImpl) workflow).addWorkflowStateListener(new WorkflowStateListener()
                                {
                                    public void workflowStateChanged(WorkflowStateChangedEvent event)
                                    {
                                        WorkflowState state = event.getWorkflowState();
                                        // If workflow has finished current job, (ie. state.isFinal()) then send results to DB
                                        if (state.isFinal())
                                        {
                                            // TODO - handling workflow outputs! Writing data to/from the database, need to parse correctly!
                                            Map output = workflow.getOutput();
                                            DataThing out = (DataThing) output.get("GFFRecord");

                                            SimpleGFFRecord gffOut = new SimpleGFFRecord();
                                            gffOut.setSeqName(gffOut.getSeqName());
                                            gffOut.setSource(out.toString());

                                            System.out.print("*result DONE*");
                                            dbMan.addNewResult(gffOut);
                                            // notify this thread that workflow has finished
                                            synchronized (workflow)
                                            {
                                                workflow.notify();
                                            }
                                        }
                                    }
                                });

                                // we have next job and a newly compiled workflow so set inputs and run this job
                                System.out.println("Job: " + jobID + " being done by " + this.getName());

                                workflow.setInputs(inputs);
                                workflow.run();
                                synchronized (workflow)
                                {
                                    // wait until this thread is notified that workflow has finished
                                    workflow.wait();
                                }
                            }
                            catch (WorkflowSubmissionException e)
                            {
                                // Couldn't submit workflow this time - log this problem
                                logError(dbMan.getDatabaseLocation(), "Compiling workflow", e);

                                // Now remove from computeList and put back into jobList -
                                // this allows this job to be retried at a later time
                                synchronized (computeList)
                                {
                                    computeList.remove(jobID);
                                    synchronized (jobList)
                                    {
                                        jobList.add(jobID);
                                    }
                                }
                            }
                            catch (InvalidInputException e)
                            {
                                // log the invalid input exception, but can still continue
                                logError(dbMan.getDatabaseLocation(), "Starting workflow for input: " + jobID, e);
                            }
                        }
                    }
                    catch (InterruptedException e)
                    {
                        // Thread has been externally interrupted whilst waiting - must close this thread
                        if (this.isInterrupted())
                        {
                            logError(dbMan.getDatabaseLocation(), "Running workflow", e);
                        }
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
     * Destroy-type method, used for halting all currently active threads controlled by <code>DalecManager</code>.  This
     * method should be called when a DalecAnnotationSource is being taken out of service, as workflow threads and
     * database threads wil continue to run otherwise.
     */
    public synchronized void exterminate()
    {
        // setting terminated prevents new jobs starting
        terminated = true;
        // this won't wake waiting threads though, so notify all threads which are waiting on jobList
        synchronized (jobList)
        {
            jobList.notifyAll();
        }
        // call dbMan.exterminate which halts database thread
        dbMan.exterminate();

        System.out.print("Dalec shutdown request: waiting for active threads to complete");
        for (int i = 0; i < workflowThreadPool.length; i++)
        {
            do
            {
                System.out.print(".");
                try
                {
                    this.wait(100);
                }
                catch (InterruptedException e)
                {
                    // do nothing
                }
            } while (workflowThreadPool[i].getState() != Thread.State.TERMINATED);
        }
        do
        {
            System.out.print(".");
        } while (dbThread.getState() != Thread.State.TERMINATED);
        // Set all initialised threads to null
        for (int i = 0; i < workflowThreadPool.length; i++)
        {
            workflowThreadPool[i] = null;
        }
        dbThread = null;
        // Run garbage collector
        System.gc();
        System.out.println("done");
    }

    /**
     * Request a Biojava <code>sequence</code> object containing annotation information, as evaluated by Dalec using the
     * specified <code>.XScufl</code> workflow.
     * <p/>
     * The sequence will be returned from the database if the annotation is completed.  If not, an exception is thrown
     * indicating that this sequence needs to be computed - the client should then try resubmitting the request.
     *
     * @param ref - String representing the ID of the sequence requested
     * @return a biojava sequence containing annotation information
     * @throws WaitWhileJobComputedException - If this sequence has been previously submitted and is to be calculated
     *                                       shortly.
     * @throws UnableToAccessDatabaseException
     *                                       - If there is a problem accessing the data held within the database.
     */
    public Sequence requestSequence(String ref) throws WaitWhileJobComputedException, UnableToAccessDatabaseException
    {
        if (jobExists(ref))
        {
            // Use an exception here to notify DataSource that we are "waiting"
            throw new WaitWhileJobComputedException();
        }
        else
        {
            // job is not in jobList at the moment - this means it has:

            // EITHER been done before and stored - so return the data
            //TODO - this is where data is retrieved from database - convert GFF data to biojava sequence?
            if (dbMan.fileExists(ref))
            {
                try
                {
                    dbMan.getGFFEntry(ref);
                }
                catch (UnableToAccessDatabaseException e)
                {
                    // Problem acessing database - log this in error file and throw the error
                    logError(dbMan.getDatabaseLocation(), "Attempting to access database", e);
                    throw e;
                }

                // this is a test, need to parse data correctly
                return new SimpleSequence(new SimpleSymbolList(Alphabet.EMPTY_ALPHABET), ref, ref, Annotation.EMPTY_ANNOTATION);
            }

            // OR it has never been done - so add to the job list and return a "waiting" message
            else
            {
                submitJob(ref);
                throw new WaitWhileJobComputedException();
            }
        }
    }

    /**
     * Static method which can be called to add an entry to the error log for this database.  Simple text dump for any
     * exceptions which have occurred unusually, this is mainly IO errors which occurred when attempting to read or
     * write to the database, thread interruptions or other anticipated problems.
     *
     * @param sequenceDBLocation The location of the Database - error log files are added to "$DB_LOC$/log/$ERR_FILE$"
     * @param jobName            A string describing the job being performed when the problem occurred
     * @param e                  The throwable cause of the problem making an error log entry necessary.
     */
    public synchronized static void logError(File sequenceDBLocation, String jobName, Throwable e)
    {
        // Name files by todays date
        Calendar cal = new GregorianCalendar();
        String date = cal.get(Calendar.DAY_OF_MONTH) + "-" + (cal.get(Calendar.MONTH) + 1) + "-" + cal.get(Calendar.YEAR);
        String time = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);

        // Files get created as <DB_LOCATION>/log/date.err so eg. /home/usr/myDB/log/01-01-2005.err
        File errFile = new File(sequenceDBLocation, "\\log\\" + date + ".err");
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
            errLog.println("Error occurred at " + time + ", whilst doing job: " + jobName);
            errLog.println(e.getMessage());
            errLog.println(e.getCause());
            errLog.println("---------------------------------------------------------------");
            errLog.println();
            errLog.close();
        }
        catch (IOException e1)
        {
            System.out.println("Unable to write entry to error log file due to an IOException");
            e1.printStackTrace();
        }
    }

    /**
     * Returns <code>true</code> if <code>DalecManager</code> is terminated, <code>false</code> otherwise.
     *
     * @return boolean terminated status
     */
    public boolean getTerminatedStatus()
    {
        return terminated;
    }

    /**
     * Returns true if the input for this workflow is "raw" sequence, false if the input is in the form of a sequence
     * ID
     *
     * @return
     */
    public boolean inputIsSequence() throws IncorrectlyNamedProcessorException
    {
        Port[] p = model.getWorkflowSourcePorts();
        int i = 0;
        while (i < p.length)
        {
            if (p[i].isSource() && p[i].getName() == "sequence")
            {
                return true;
            }
            else if (p[i].isSource() && p[i].getProcessor().getName() == "seqID")
            {
                return false;
            }
            i++;
        }
        throw new IncorrectlyNamedProcessorException("Unable to locate an input processor named \"sequence\" or \"seq_ID\" in this model");
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
}