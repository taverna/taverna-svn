package net.sf.taverna.dalec;

import org.biojava.bio.program.gff.GFFEntrySet;
import org.biojava.bio.program.gff.GFFDocumentHandler;
import org.biojava.bio.program.gff.GFFParser;
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

import net.sf.taverna.dalec.exceptions.*;
import net.sf.taverna.dalec.io.WorkflowInput;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateListener;
import uk.ac.soton.itinnovation.freefluo.event.WorkflowStateChangedEvent;
import uk.ac.soton.itinnovation.freefluo.main.WorkflowState;
import uk.ac.soton.itinnovation.freefluo.main.InvalidInputException;

/**
 * This class co-ordinates all the "hard work" for Dalec, including retaining the cache of jobs waiting to be done,
 * handling data submission and data access, invoking the workflow, and returning annotation results.
 * <p/>
 * Normally, an instance of <code>DalecManager</code> would be created when the <code>init()</code> method is called on
 * <code>DalecAnnotationSource</code>.  When this happens, <code>DalecManager</code> runs through its normal start-up
 * procedure, which involves creating several threads for individual copies of the workflow (allowing several queries to
 * be handled concurrently) and creation of a new database (and a new instance of <code>DatabaseManager</code>) to
 * permanently retain generated results.
 * <p/>
 * When a new request is received from the DAS client, this request can be passed onto DalecManager by calling the
 * <code>requestAnnotations()</code> method. If the sequence requested has already been annotated by this server, the
 * results are returned. If no results exist, a <code>NewJobSubmissionException</code> is thrown.  Any calling class
 * should handle this exception by then submitting the sequence as a new job, calling the <code>submitJob()</code>
 * method.
 * <p/>
 *
 * @author Tony Burdett
 * @version 1.0
 */
public class DalecManager
{
    private DatabaseManager dbMan;
    private ScuflModel model;

    private List jobList = new ArrayList();
    private List computeList = new ArrayList();

    private Thread[] workflowThreadPool = new Thread[4];
    private Thread dbThread;
    private boolean terminated = false;

    /**
     * Constructor for class <code>DalecManager</code>.  When a new <code>DalecManager</code> is created, the database
     * it will use is created using the <code>File</code> parameter passed to it.  A new <code>DatabaseManager</code> is
     * created, a <code>DatabaseListener</code> is registered to it, and a dedicated database thread is started to
     * handle database entries as the results are generated from the workflow.
     * <p/>
     * The workflow file passed is used to populate a workflow model, which will then be used to compile workflows to do
     * annotations on sequences passed to Dalec by the client.  A <code>WorkflowCreationException</code> is thrown if
     * problems are encountered in populating the model.
     * <p/>
     * Once the model has been populated, several concurrent threads are started, containing copies of the wokflow
     * model.  Each thread then compiles a working instance of the workflow.  Each thread then dynamically annotates
     * submitted sequences as jobs are sent to DalecManager, sending the generated results to the
     * <code>DatabaseManager</code> to be entered into the database.
     *
     * @param xscuflFile         The XML file which represents the workflow to use for this DalecManager
     * @param sequenceDBLocation The path to the root directory of the sequence database to be used
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
                System.out.println("New entry created");
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
            synchronized (model)
            {
                try
                {
                    XScuflParser.populate(new FileInputStream(xscuflFile), model, null);
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
                        model.wait(1000);
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
        }
        catch (FileNotFoundException e)
        {
            throw new WorkflowCreationException("File not found", e);
        }
        catch (Exception e)
        {
            throw new WorkflowCreationException("A problem occurred whilst creating the workflow", e);
        }

        // Each thread compiles its own copy of workflow from 'model'
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
                            WorkflowInput job;

                            // Continually request new jobs
                            synchronized (jobList)
                            {
                                // Check to make sure there are pending jobs - otherwise just wait
                                while (jobList.isEmpty() && !terminated)
                                {
                                    jobList.wait();
                                }
                                if (terminated) break;

                                // Must be jobs in jobList here
                                job = (WorkflowInput) jobList.get(0);
                                jobID = job.getJobID();

                                // So a new job is allocated - remove from the jobList and place into computeList
                                jobList.remove(0);
                                synchronized (computeList)
                                {
                                    computeList.add(job);
                                }
                            }

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
                                            if (workflow.getOutput().containsKey("annotations"))
                                            {
                                                DataThing output = (DataThing) workflow.getOutput().get("annotations");

                                                // output is a DataThing representing XML GFF data
                                                String data = (String) output.getDataObject(); // should retrieve the string of XML data

                                                // Set up the stuff for parsing GFF data
                                                BufferedReader bReader = new BufferedReader(new StringReader(data));
                                                GFFEntrySet gff = new GFFEntrySet();
                                                GFFDocumentHandler handler = gff.getAddHandler();
                                                GFFParser parser = new GFFParser();

                                                // Now parse the output data to GFFEntrySet, and submit to DB
                                                try
                                                {
                                                    parser.parse(bReader, handler);
                                                    dbMan.addNewResult(gff);
                                                }
                                                catch (Exception e)
                                                {
                                                    // Unable to parse GFF Data from workflow output
                                                    logError(dbMan.getDatabaseLocation(), "Parsing GFF output from workflow", e);
                                                    System.out.println("A sequence annotation failed - unable to parse output from workflow");
                                                }

                                                // notify this thread that workflow has finished
                                                synchronized (workflow)
                                                {
                                                    workflow.notify();
                                                }
                                            }
                                            else
                                            {
                                                // can't find correctly named output - log this error
                                                logError(dbMan.getDatabaseLocation(), "Retrieving output from workflow", new Exception("No output processor named 'annotations'", new BadWorkflowFormatException()));
                                                // and notify thread that we're done with this workflow
                                                synchronized (workflow)
                                                {
                                                    workflow.notify();
                                                }
                                            }
                                        }
                                    }
                                });

                                // we have next job and a newly compiled workflow so set inputs and run this job
                                workflow.setInputs(job.getInputs());
                                workflow.run();
                                synchronized (workflow)
                                {
                                    // wait until this thread is notified that workflow has finished
                                    workflow.wait(100000);
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
                                    computeList.remove(job);
                                    synchronized (jobList)
                                    {
                                        jobList.add(job);
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
     * Destroy-type method, used for halting all currently active threads controlled by <code>DalecManager</code>. This
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
        terminated = true;
    }

    /**
     * Request a Biojava <code>GFFEntrySet</code> object containing annotation information, as evaluated by Dalec using
     * the specified <code>.XScufl</code> workflow.
     * <p/>
     * The GFFEntrySet will be returned from the database, as long as the specified annotation is completed.  If not, an
     * exception is thrown indicating that this sequence needs to be computed - the client should then call
     * <code>submitJob()</code>, to request an annotation for this sequence.
     *
     * @param ref String representing either the ID of the sequence requested
     * @return a biojava sequence containing annotation information
     * @throws NewJobSubmissionException if this sequence has been previously submitted and is to be calculated
     *                                   shortly.
     * @throws UnableToAccessDatabaseException
     *                                   if there is a problem accessing the data held within the database.
     */
    public GFFEntrySet requestAnnotations(String ref) throws NewJobSubmissionException, UnableToAccessDatabaseException
    {
        if (jobExists(ref))
        {
            // Use an exception here to notify DataSource that we are "waiting"
            throw new NewJobSubmissionException();
        }
        else
        {
            // job is not in jobList at the moment - this means it has:

            // EITHER been done before and stored - so return the data
            if (dbMan.fileExists(ref))
            {
                try
                {
                    return dbMan.getGFFEntry(ref);
                }
                catch (UnableToAccessDatabaseException e)
                {
                    // Problem acessing database - log this in error file and throw the error
                    logError(dbMan.getDatabaseLocation(), "Attempting to access database", e);
                    throw e;
                }
            }

            // OR it has never been done - so add to the job list and return a "waiting" message
            else
            {
                throw new NewJobSubmissionException();
            }
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
     * Returns the list of source processor names for the ScuflModel held by this DalecManager.  Dalec needs to know the
     * source processors for the workflow being used in order to pass the correct <code>WorkflowInput</code> to the
     * workflow.  Source processors should be named appropriately - for more info on processor naming see
     * <code>DalecAnnotationSource</code>.
     *
     * @return The list of input processors in thie workflow contained by this DalecManager
     */
    public List getInputs()
    {
        ArrayList inputs = new ArrayList();
        Port [] p = model.getWorkflowSourcePorts();
        for (int i = 0; i < p.length; i++)
        {
            inputs.add(p[i].getName());
        }
        return inputs;
    }
//        // Old method for checking inputs - naming is not handled here anymore
//        String inputName = "";
//        Port[] p = model.getWorkflowSourcePorts();
//        for (int i = 0; i < p.length; i++)
//        {
//
//            if ((p[i].getName().matches("sequence") && inputName.matches("seqID")) || (p[i].getName().matches("seqID") && inputName.matches("sequence")))
//            {
//                // 2 named inputs, seqID AND sequence present - this is not valid
//                throw new IncorrectlyNamedInputException("Found source processors named 'sequence' AND 'seqID'in this model - not valid!");
//            }
//            else if (p[i].getName().matches("sequence"))
//            {
//                inputName = p[i].getName();
//            }
//            else if (p[i].getName().matches("seqID"))
//            {
//                inputName = p[i].getName();
//            }
//        }
//        if (inputName == null)
//        {
//            throw new IncorrectlyNamedInputException("Unable to locate a source processor named 'sequence' or 'seqID' in this model");
//        }
//        else
//        {
//            return inputName;
//        }
//    }

    /**
     * Submit a job to be annotated.  This method should be called once it has been determined that a sequence has not
     * previously been submitted for annotation.  No checks are performed for this here, so it is your responsiblity to
     * do so - the easiest way to do this is to attempt a <code>requestSequence()</code> call, catch the
     * <code>NewJobSubmissionException</code> and handle it by calling this method. The "job" which should be submitted
     * is a <code>WorkflowInput</code> Object.  This Object wraps all the required information for submission to the
     * workflow - the processor name, the ID of the sequence to be submitted (the ID being the form recognised by the
     * reference server this sequence was acquired from) and the actual data to be fed into the workflow.
     *
     * @param input A <code>WorkflowInput</code> object containing the data to be submitted to the workflow.
     */
    public void submitJob(WorkflowInput input)
    {
        synchronized (jobList)
        {
            if (jobList.isEmpty())
            {
                // When new job submitted, notifies all threads which are waiting when no outstanding jobs
                jobList.add(input);
                jobList.notifyAll();
            }
            else
            {
                // Otherwise just adds next jobs if not empty
                jobList.add(input);
            }
        }
    }

    /**
     * Static method which can be called to add an entry to the error log for this database.  Simple text dump for any
     * exceptions which have occurred unusually, this is mainly io errors which occurred when attempting to read or
     * write to the database, thread interruptions or other anticipated problems.
     *
     * @param sequenceDBLocation The location of the Database - error log files are added here under ../log/{Date}.err
     * @param jobName            A string describing the job being performed when the problem occurred
     * @param exception          The throwable cause of the problem making an error log entry necessary.
     */
    public synchronized static void logError(File sequenceDBLocation, String jobName, Throwable exception)
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
            errLog.println(exception.getMessage());
            if (exception.getCause() != null)
            {
                errLog.println(exception.getCause());
            }
            else
            {
                errLog.println(exception.getStackTrace()[0]);
            }
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
     * Private method uses to determine whether a given job exists (i.e. it is present within the jobList, or is
     * currently being computed)
     *
     * @param jobID
     * @return true if the job exists, false otherwise
     */
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