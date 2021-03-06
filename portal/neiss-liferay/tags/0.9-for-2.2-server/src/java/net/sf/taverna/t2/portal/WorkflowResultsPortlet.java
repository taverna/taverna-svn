/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.t2.portal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * Workflow Results Portlet - enables user to view status of
 * workflow submission jobs submitted to the T2 Server and
 * fetch results of finished jobs.
 *
 * @author Alex Nenadic
 */
public class WorkflowResultsPortlet extends GenericPortlet {

    // Address of the T2 Server
    private String T2_SERVER_URL;
    // Directory where info for all submitted jobs for all users is persisted
    private static File JOBS_DIR;
    // URL of the file serving servlet
    private String FILE_SERVLET_URL;
    // Max size of data to be sent as preview (in KB)
    public static long MAX_PREVIEW_DATA_SIZE_IN_KB;
    public static long DEFAULT_MAX_PREVIEW_DATA_SIZE_IN_KB = 250;
    // Background thread for polling job statuses of unfinished jobs
    static Timer pollJobStatusesTimer;
    // Initial delay and period for background thread for
    // updating jobs statuses and fethcing results
    private static long JOB_UPDATE_PERIOD_IN_MS = 5 * 60 * 1000; // 5 min
    private static long JOB_UPDATE_DELAY_IN_MS = 5 * 60 * 1000; // 5 min
    // Locks for synschronising between the background thread for updating jobs
    // and fethcing results and the main thread
    static final Object statusLock = new Object();
    static final Object resultsLock = new Object();

    // Namespace of this portlet
    private static String PORTLET_NAMESPACE;
    
    /*
     * Do the init stuff once at portlet loading time.
     */
    @Override
    public void init() {

        // Get the URL of the T2 Server defined in web.xml as an
        // app-wide init parameter ( <context-param> element)
        T2_SERVER_URL = getPortletContext().getInitParameter(Constants.T2_SERVER_URL_PROPERTY);

        // Get the directory where info for submitted jobs for all users is persisted
        JOBS_DIR = new File(getPortletContext().getInitParameter(Constants.JOBS_DIRECTORY_PATH_PROPERTY));
        if (!JOBS_DIR.exists()) {
            try {
                JOBS_DIR.mkdirs(); // create all intermediate directories as well if neccessary
            } catch (Exception ex) {
                System.out.println("Workflow Results Portlet: Failed to create a directory " + JOBS_DIR.getAbsolutePath() + " where submitted jobs are to be persisted.");
                ex.printStackTrace();
            }
        }
        System.out.println("Workflow Results Portlet: Directory where jobs will be persisted set to " + JOBS_DIR.getAbsolutePath());

        // Start a background thread that will periodically poll the T2 Server
        // for the status of unfinished jobs and fetch results once they become available
        pollJobStatusesTimer = new Timer();
        pollJobStatusesTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                updateJobStatusesAndFetchResultsForAllUsers();
            }
        }, JOB_UPDATE_DELAY_IN_MS, JOB_UPDATE_PERIOD_IN_MS); // delay of 5 minutes for the first execution and runs every 5 minutes

        FILE_SERVLET_URL = getPortletContext().getInitParameter(Constants.FILE_SERVLET_URL_PROPERTY);

        try {
            MAX_PREVIEW_DATA_SIZE_IN_KB = Long.valueOf((String) getPortletContext().getInitParameter(Constants.MAX_PREVIEW_DATA_SIZE_IN_KB_PROPERTY));
        } catch (Exception ex) {
            MAX_PREVIEW_DATA_SIZE_IN_KB = DEFAULT_MAX_PREVIEW_DATA_SIZE_IN_KB;
        }
    }

    @Override
    public void destroy() {
        if (pollJobStatusesTimer != null) {
            pollJobStatusesTimer.cancel();
        }
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException {

        ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>) request.getPortletSession().
                getAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
                PortletSession.APPLICATION_SCOPE);

        // If there was a request to refresh the job statuses
        if (request.getParameter(Constants.REFRESH_WORKFLOW_JOBS) != null) {
            updateJobStatusesForUser(workflowSubmissionJobs, request);
        } // If there was a request to show results of a workflow run
        else if (request.getParameter(Constants.FETCH_RESULTS) != null) {

            // If workflowSubmissionJobs is null or does not contain this job id
            // this is just a page refresh after redeployment of the app/restart of
            // the sever/some form or refresh while the URL parameter FETCH_RESULTS
            // managed to linger in the URL in the browser from the previous
            // session so just ignore it.
            if (workflowSubmissionJobs != null) {
                String workflowResourceUUID = URLDecoder.decode(request.getParameterValues(Constants.FETCH_RESULTS)[0], "UTF-8");
                for (WorkflowSubmissionJob job : workflowSubmissionJobs) {
                    if (job.getUuid().equals(workflowResourceUUID)) {
                        System.out.println("Workflow Results Portlet: Fetching results for job id " + workflowResourceUUID);

                        // See if results are already downloaded from the T2 Server -
                        // if not download and save the Baclava file with outputs now.
                        Map<String, DataThing> resultDataThingMap = fetchJobResults(job, request);

                        // Also fetch the inputs from the disk
                        Map<String, DataThing> inputsDataThingMap = fetchJobInputs(job, request);

                        request.setAttribute(Constants.OUTPUTS_MAP_ATTRIBUTE, resultDataThingMap);
                        request.setAttribute(Constants.INPUTS_MAP_ATTRIBUTE, inputsDataThingMap);

                        request.setAttribute(Constants.WORKFLOW_SUBMISSION_JOB, job);
                        break;
                    } // else just ignore it if it is not in the job id list
                }
            }
        } // If there was a request to delete a workflow run
        else if (request.getParameter(Constants.DELETE_JOB) != null) {
            // If workflowSubmissionJobs is null or does not contain this job id
            // this is just a page refresh after redeployment of the app/restart of
            // the sever/some form or refresh while the URL parameter DELETE_JOB
            // managed to linger in the URL in the browser from the previous
            // session so just ignore it.
            if (workflowSubmissionJobs != null) {
                String workflowResourceUUID = URLDecoder.decode(request.getParameterValues(Constants.DELETE_JOB)[0], "UTF-8");

                Iterator<WorkflowSubmissionJob> iter = workflowSubmissionJobs.iterator();
                while (iter.hasNext()) {
                    if (iter.next().getUuid().equals(workflowResourceUUID)) {
                        System.out.println("Workflow Results Portlet: Deleting job " + workflowResourceUUID);
                        iter.remove();
                        deleteJobForUser(workflowResourceUUID, request);
                        //request.setAttribute(Constants.WORKFLOW_SUBMISSION_JOBS, workflowSubmissionJobs);
                        request.getPortletSession().setAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
                                workflowSubmissionJobs,
                                PortletSession.APPLICATION_SCOPE);
                        break;
                    }
                } // else just ignore it if it is not in the job id list
            }
        }

        // Pass all request parameters over to the doView() and other render stage methods
        response.setRenderParameters(request.getParameterMap());
    }

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        // Just print all the parameters we have received, for testing purposes
        Enumeration names = request.getParameterNames();
        while(names.hasMoreElements()){
            String parameterName = (String) names.nextElement();
            System.out.println("\nWorkflow Results Portlet (doView): parameter name: " + parameterName);
            System.out.println("Workflow Results Portlet (doView): parameter value: " + request.getParameter(parameterName));
            System.out.println();
        }

        if (PORTLET_NAMESPACE == null){
            PORTLET_NAMESPACE = response.getNamespace();
        }

        response.setContentType("text/html");

        // Print out a message to the user, if any
        if (request.getAttribute(Constants.ERROR_MESSAGE) != null){
            response.getWriter().println("<span class=\"portlet-msg-error\"><b>"+ request.getAttribute(Constants.ERROR_MESSAGE)+ "</b></span>\n");
            response.getWriter().println("<hr>");
        }
        if (request.getAttribute(Constants.INFO_MESSAGE) != null){
            response.getWriter().println("<span class=\"portlet-msg-info\"><b>"+ request.getAttribute(Constants.INFO_MESSAGE)+ "</b></span>\n");
            response.getWriter().println("<hr>");
        }

        // Get currently logged in user
        String user = (String) request.getPortletSession().
                getAttribute(Constants.USER,
                PortletSession.APPLICATION_SCOPE);
        if (user == null) {
            if (request.getUserPrincipal() == null) {
                user = Constants.USER_ANONYMOUS;
            } else {
                user = request.getUserPrincipal().getName();
            }
            System.out.println("Workflow Results Portlet: Session started for user " + user + ".");
            request.getPortletSession().setAttribute(Constants.USER,
                    user,
                    PortletSession.APPLICATION_SCOPE);
        }

        // Get all jobs for the current user that have been persisted on a disk
        ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>) request.getPortletSession().
                getAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
                PortletSession.APPLICATION_SCOPE);
        if (workflowSubmissionJobs == null) { // load user's jobs from disk
            workflowSubmissionJobs = loadWorkflowSubmissionJobsForUser(JOBS_DIR, user);
            request.getPortletSession().setAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
                    workflowSubmissionJobs,
                    PortletSession.APPLICATION_SCOPE);
        }

        // Refresh job statuses if there is a job that has not finished yet
        updateJobStatusesForUser(workflowSubmissionJobs, request);

        PortletRequestDispatcher dispatcher =
                getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_view.jsp");
        dispatcher.include(request, response);

        // If there was a request to show results of a workflow run
        if (request.getParameter(Constants.FETCH_RESULTS) != null) {

            String workflowResourceUUID = URLDecoder.decode(request.getParameterValues(Constants.FETCH_RESULTS)[0], "UTF-8");
            WorkflowSubmissionJob job = (WorkflowSubmissionJob) request.getAttribute(Constants.WORKFLOW_SUBMISSION_JOB);

            response.getWriter().println("<br>\n");
            response.getWriter().println("<a name=\""+Constants.RESULTS_ANCHOR+"\"<hr></a>\n");

            // The close button to clear the results view
            //dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/CloseResultsViewForm.jsp");
            //dispatcher.include(request, response);

            response.getWriter().println("<form name=\""+PORTLET_NAMESPACE+Constants.CLEAR+"\" action=\"" + response.createRenderURL()+"\" >\n");
            response.getWriter().println("<p>\n");
            response.getWriter().println("<input type=\"image\" src=\""+request.getContextPath()+"/images/close.gif\" style=\"border:0;\" >\n");
            response.getWriter().println("</p>\n");
            response.getWriter().println("</form>\n");


            // Parse the result values from the Baclava file
            response.getWriter().println("<b>Workflow run id: " + job.getUuid() + "</b><br>\n");

            String workflowName;
            if (job.getWorkflow().getFileName() != null) { // this is a local workflow
                workflowName = job.getWorkflow().getFileName();
            } else { // this is a workflow from myExperiment
                workflowName = job.getWorkflow().getMyExperimentWorkflowResource();
            }
            response.getWriter().println("<b>Workflow: " + workflowName + "</b><br><br>\n");

            // But if workflowSubmissionJobs is null or does not contain this job id
            // this is just a page refresh after redeployment of the app/restart of
            // the sever/some form or refresh while the URL parameter FETCH_RESULTS
            // managed to linger in the URL in the browser from the previous
            // session so just ignore it. In this case the map of result DataThings
            // will be null so just ignore this request. Also if something went wrong
            // with fetching the outputs - the map will be null so do not show anything -
            // an error message will be displayed to the user.
            Map<String, DataThing> resultDataThingMap =
                    (Map<String, DataThing>) request.getAttribute(Constants.OUTPUTS_MAP_ATTRIBUTE); // just populated in the processAction method
            if (resultDataThingMap != null) {

                String baclavaOutputsFilePath = JOBS_DIR + Constants.FILE_SEPARATOR
                        + user + Constants.FILE_SEPARATOR
                        + workflowResourceUUID + Constants.FILE_SEPARATOR
                        + Constants.OUTPUTS_BACLAVA_FILE;
                String baclavaOutputsFileURL = request.getContextPath() + FILE_SERVLET_URL + "?" + Constants.DATA_FILE_PATH + "=" + URLEncoder.encode(baclavaOutputsFilePath, "UTF-8")
                        + "&" + Constants.MIME_TYPE + "=" + URLEncoder.encode(Constants.CONTENT_TYPE_APPLICATION_XML, "UTF-8");

                String outputsTableHTML = createHTMLTableFromResultsDataThingMap(baclavaOutputsFileURL, resultDataThingMap, user, workflowResourceUUID, request);

                response.getWriter().println(outputsTableHTML);

                // Add some space before we print the inputs data table
                response.getWriter().println("<br>\n");
                response.getWriter().println("<br>\n");
            }
            // Do the similar thing for the inputs - show them in a table
            // unless they are null.
            Map<String, DataThing> inputsDataThingMap =
                    (Map<String, DataThing>) request.getAttribute(Constants.INPUTS_MAP_ATTRIBUTE); // just populated in the processAction method
            if (inputsDataThingMap != null) { // if is null - error message will be displayed so do not do anything here

                // Parse the input values from the Baclava file
                String baclavaInputsFilePath = JOBS_DIR + Constants.FILE_SEPARATOR
                        + user + Constants.FILE_SEPARATOR
                        + workflowResourceUUID + Constants.FILE_SEPARATOR
                        + Constants.INPUTS_BACLAVA_FILE;
                String baclavaInputsFileURL = request.getContextPath() + FILE_SERVLET_URL + "?" + Constants.DATA_FILE_PATH + "=" + URLEncoder.encode(baclavaInputsFilePath, "UTF-8")
                        + "&" + Constants.MIME_TYPE + "=" + URLEncoder.encode(Constants.CONTENT_TYPE_APPLICATION_XML, "UTF-8");

                String inputsTableHTML = createHTMLTableFromInputsDataThingMap(baclavaInputsFileURL, inputsDataThingMap, user, workflowResourceUUID, request);

                response.getWriter().println(inputsTableHTML);
                response.getWriter().println("<br>\n");
                response.getWriter().println("<br>\n");
            }
        }
    }

    @Override
    public void doEdit(RenderRequest request, RenderResponse response) throws PortletException, IOException {
        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
                getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_edit.jsp");
        dispatcher.include(request, response);
    }

    @Override
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException, IOException {

        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
                getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_help.jsp");
        dispatcher.include(request, response);
    }

    /*
     * Fetches job statuses from a T2 Server for all the jobs that
     * have not already finished.
     */
    private void updateJobStatusesForUser(ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs, PortletRequest request) {

        synchronized (statusLock) {
            // Get the current user
            String user = (String) request.getPortletSession().
                    getAttribute(Constants.USER,
                    PortletSession.APPLICATION_SCOPE);
            for (int i = workflowSubmissionJobs.size() - 1; i >= 0; i--) {
                WorkflowSubmissionJob job = workflowSubmissionJobs.get(i);
                if (job.getStatus().equals(Constants.JOB_STATUS_OPERATING)) {
                    // Read status from the disk first as it may have been
                    // updated by the background thread in the meantime
                    String statusOnDisk = null;
                    File jobDir = new File(JOBS_DIR + Constants.FILE_SEPARATOR + user + Constants.FILE_SEPARATOR + job.getUuid());
                    File[] statusFiles = jobDir.listFiles(statusFileFilter);
                    if (statusFiles.length != 0) {
                        statusOnDisk = statusFiles[0].getName().substring(0, statusFiles[0].getName().indexOf(Constants.STATUS_FILE_EXT));
                    } else {// this is not good (the status file should be there) - skip this job
                        continue;
                    }

                    if (statusOnDisk != null && !statusOnDisk.equals(job.getStatus())) { // status changed
                        job.setStatus(statusOnDisk);
                        if (statusOnDisk.equals(Constants.JOB_STATUS_FINISHED)) {
                            // Read the end date from .enddate file name and update the job object
                            String[] enddateFileNames = jobDir.list(enddateFileFilter);
                            if (enddateFileNames.length != 0) {
                                String enddateFileName = enddateFileNames[0]; // should be only 1 element or else we are in trouble
                                String enddate = enddateFileName.substring(0, enddateFileName.indexOf(Constants.ENDDATE_FILE_EXT));
                                job.setEndDate(new Date(Long.parseLong(enddate)));
                            } else {// this is not good (the status file should be there) - skip this job
                                continue;
                            }
                        }
                    } else {
                        // Get the updated job's status from the T2 Server
                        String statusOnServer = getWorkflowSubmissionJobStatusFromServer(job.getUuid());
                        if (!statusOnServer.equals(job.getStatus())) {
                            // If the job is not available on the Server any more - set its status to "Expired"
                            if (statusOnServer.equals(Constants.UNKNOWN_RUN_UUID)) {
                                job.setStatus(Constants.JOB_STATUS_EXPIRED);
                                // Persist the new status for this job on a disk
                                updateJobStatusOnDiskForUser(job.getUuid(), Constants.JOB_STATUS_EXPIRED, user, null);
                            } else {
                                job.setStatus(statusOnServer);
                                // If the job has not expired and the status changed - then the job has finished.
                                // Check it here nevertheless.
                                Date endDate = null;
                                if (statusOnServer.equals(Constants.JOB_STATUS_FINISHED)) {
                                    // Record this as the time when the workflow run has finished -
                                    // this is the best we can do regarding actual finish time
                                    endDate = new Date();
                                    job.setEndDate(endDate);
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

                                    String workflowName;
                                    if (job.getWorkflow().getFileName() != null) { // this is a local workflow
                                        workflowName = job.getWorkflow().getFileName();
                                    } else { // this is a workflow from myExperiment
                                        workflowName = job.getWorkflow().getMyExperimentWorkflowResource();
                                    }
                                    System.out.println("Workflow Submission Portlet: Execution of workflow " + workflowName + " finished on the Server. Results fetched at " + dateFormat.format(endDate) + " with job id: " + job.getUuid() + ".");
                                }
                                // Persist the new status for this job on a disk
                                updateJobStatusOnDiskForUser(job.getUuid(), statusOnServer, user, endDate);
                            }
                        }
                    }
                }
            }

            request.getPortletSession().
                    setAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
                    workflowSubmissionJobs,
                    PortletSession.APPLICATION_SCOPE);
        }
    }

    /*
     * Fetch status of a submitted job from the T2 Server.
     */
    private String getWorkflowSubmissionJobStatusFromServer(String workflowSubmissionJobId) {

        HttpClient httpClient = new DefaultHttpClient();
        String statusURL = T2_SERVER_URL + Constants.RUNS_URL + "/" + workflowSubmissionJobId + Constants.STATUS_URL;

        HttpGet httpGet = new HttpGet(statusURL);

        HttpResponse httpResponse = null;
        try {
            // Execute the request
            HttpContext localContext = new BasicHttpContext();
            httpResponse = httpClient.execute(httpGet, localContext);

            // Release resource
            httpClient.getConnectionManager().shutdown();

            if (httpResponse.getStatusLine().getStatusCode() == 403) { // HTTP/1.1 403 Forbidden
                System.out.println("Workflow Results Portlet: Job " + workflowSubmissionJobId
                        + " does not exist on the Server any more. The Server responded with: " + httpResponse.getStatusLine() + ".");
                return Constants.UNKNOWN_RUN_UUID;
            } else if (httpResponse.getStatusLine().getStatusCode() == 200) { // HTTP/1.1 200 OK
                HttpEntity httpEntity = httpResponse.getEntity();

                String value = null;
                String contentType = httpEntity.getContentType().getValue().toLowerCase();

                try {
                    if (contentType.startsWith("text")) {
                        // Read as text
                        value = readResponseBodyAsString(httpEntity).trim();
                        System.out.println("Workflow Results Portlet: Status of job " + workflowSubmissionJobId + " on the Server is '" + value + "'.");
                    } else {
                        System.out.println("Workflow Results Portlet: Server's response not text/plain for status of job " + workflowSubmissionJobId);
                    }
                } catch (Exception ex) {
                    System.out.println("Workflow Results Portlet: Failed to get the content of the job status respose from the Server.");
                    ex.printStackTrace();
                    return "Failed to get the content of the job status respose from the Server";
                }
                return value;
            } else {
                System.out.println("Workflow Results Portlet: Failed to get the status for job " + workflowSubmissionJobId + ". The Server responded with: " + httpResponse.getStatusLine() + ".");
                return "Failed to get the status for job. The Server responnded with: " + httpResponse.getStatusLine() + ".";
            }
        } catch (Exception ex) {
            System.out.println("Workflow Results Portlet: An error occured while trying to get the status for job " + workflowSubmissionJobId + ".");
            ex.printStackTrace();
            return "An error occured while trying to get the status for job.";
        }
    }

    /*
     * Update the status of the user's job status on a local disk.
     * Job's status is saved in a file with name <JOB_STATUS>.status in
     * a directory for that job (named after the job's UUID) and the owning user.
     *
     * If new status is 'Finished' then an end date is provided as well.
     */
    private void updateJobStatusOnDiskForUser(String workflowSubmissionJobId, String newStatus, String user, Date endDate) {
        synchronized (statusLock) {
            File userDir = new File(JOBS_DIR, user);
            File[] userJobsDir = userDir.listFiles(dirFilter);

            String oldStatus = null;
            for (File jobDir : userJobsDir) {
                if (jobDir.getName().equals(workflowSubmissionJobId)) {
                    String statusFileName = jobDir.list(statusFileFilter)[0]; // should be only 1 element or else we are in trouble
                    oldStatus = statusFileName.substring(0, statusFileName.indexOf(Constants.STATUS_FILE_EXT));
                    File statusFile = new File(jobDir, statusFileName);
                    statusFile.renameTo(new File(jobDir, newStatus + Constants.STATUS_FILE_EXT));
                    System.out.println("Workflow Results Portlet: Updated status for job " + jobDir.getAbsolutePath() + " from '" + oldStatus + "' to '" + newStatus + "'.");
                    // If job has finished - save the job's end date by creating
                    // an empty .enddate file named after the date
                    if (newStatus.equals(Constants.JOB_STATUS_FINISHED)) {
                        File enddateFile = new File(jobDir, endDate.getTime() + Constants.ENDDATE_FILE_EXT);
                        try {
                            FileUtils.touch(enddateFile);
                        } catch (Exception ex) {
                            System.out.println("Workflow Submission Portlet: Failed to create the job's end date file " + enddateFile.getAbsolutePath());
                            ex.printStackTrace();
                        }
                        System.out.println("Workflow Submission Portlet: Job's end date set at " + enddateFile.getAbsolutePath());
                    }
                }
            }
        }
    }

    /*
     * Create a result tree in JavaScript for a result data item.
     */
    private String createResultTree(Object dataObject, int maxDepth, int currentDepth, String parentIndex, String dataFileParentPath, String mimeType, PortletRequest request) {

        StringBuffer resultTreeHTML = new StringBuffer();

        if (maxDepth == 0) { // Result data is a single item only
            try {
                String dataFilePath = dataFileParentPath + Constants.FILE_SEPARATOR + "Value";
                long dataSizeInKB = Math.round(new File(dataFilePath).length() / 1000d); // size in kilobytes (divided by 1000 not 1024!!!)
                String dataFileURL = request.getContextPath() + FILE_SERVLET_URL
                        + "?" + Constants.DATA_FILE_PATH + "=" + URLEncoder.encode(dataFilePath, "UTF-8")
                        + "&" + Constants.MIME_TYPE + "=" + URLEncoder.encode(mimeType, "UTF-8")
                        + "&" + Constants.DATA_SIZE_IN_KB + "=" + URLEncoder.encode(Long.toString(dataSizeInKB), "UTF-8");
                resultTreeHTML.append("addNode2(\"result_data\", \"result_data_preview_textarea\", \"Value\", \"" + dataFileURL + "\", \"results_data_preview\");\n");
            } catch (Exception ex) {
                resultTreeHTML.append("addNode2(\"result_data\", \"result_data_preview_textarea\", \"Value\", \"\", \"results_data_preview\");\n");
            }
        } else {
            if (currentDepth == 0) { // A leaf in the tree
                try {
                    String dataFilePath = dataFileParentPath + Constants.FILE_SEPARATOR + "Value" + parentIndex;
                    long dataSizeInKB = Math.round(new File(dataFilePath).length() / 1000d); // size in kilobytes (divided by 1000 not 1024!!!)
                    String dataFileURL = request.getContextPath() + FILE_SERVLET_URL
                            + "?" + Constants.DATA_FILE_PATH + "=" + URLEncoder.encode(dataFilePath, "UTF-8")
                            + "&" + Constants.MIME_TYPE + "=" + URLEncoder.encode(mimeType, "UTF-8")
                            + "&" + Constants.DATA_SIZE_IN_KB + "=" + URLEncoder.encode(Long.toString(dataSizeInKB), "UTF-8");
                    resultTreeHTML.append("addNode2(\"result_data\", \"result_data_preview_textarea\", \"Value" + parentIndex + "\", \"" + dataFileURL + "\", \"results_data_preview\");\n");
                } catch (Exception ex) {
                    resultTreeHTML.append("addNode2(\"result_data\", \"result_data_preview_textarea\", \"Value" + parentIndex + "\", \"\", \"results_data_preview\");\n");
                }
            } else { // Result data is a list of (lists of ... ) items
                resultTreeHTML.append("startParentNode(\"result_data\", \"List" + parentIndex + "\");\n");
                for (int i = 0; i < ((Collection) dataObject).size(); i++) {
                    String newParentIndex = parentIndex.equals("") ? (new Integer(i + 1)).toString() : (parentIndex + "." + (i + 1));
                    resultTreeHTML.append(createResultTree(((ArrayList) dataObject).get(i),
                            maxDepth,
                            currentDepth - 1,
                            newParentIndex,
                            dataFileParentPath + Constants.FILE_SEPARATOR + "List" + parentIndex,
                            mimeType,
                            request));
                }
                resultTreeHTML.append("endParentNode();\n");
            }
        }
        return resultTreeHTML.toString();
    }

    /*
     * Create a inputs tree in JavaScript for an input data item.
     */
    private String createInputsTree(Object dataObject, int maxDepth, int currentDepth, String parentIndex, String dataFileParentPath, String mimeType, PortletRequest request) {

        StringBuffer inputsTreeHTML = new StringBuffer();

        if (maxDepth == 0) { // Input data is a single item only
            try {
                String dataFilePath = dataFileParentPath + Constants.FILE_SEPARATOR + "Value";
                long dataSizeInKB = Math.round(new File(dataFilePath).length() / 1000d); // size in kilobytes (divided by 1000 not 1024!!!)
                String dataFileURL = request.getContextPath() + FILE_SERVLET_URL
                        + "?" + Constants.DATA_FILE_PATH + "=" + URLEncoder.encode(dataFilePath, "UTF-8")
                        + "&" + Constants.MIME_TYPE + "=" + URLEncoder.encode(mimeType, "UTF-8")
                        + "&" + Constants.DATA_SIZE_IN_KB + "=" + URLEncoder.encode(Long.toString(dataSizeInKB), "UTF-8");
                inputsTreeHTML.append("addNode2(\"input_data\", \"input_data_preview_textarea\", \"Value\", \"" + dataFileURL + "\", \"inputs_data_preview\");\n");
            } catch (Exception ex) {
                inputsTreeHTML.append("addNode2(\"input_data\", \"input_data_preview_textarea\", \"Value\", \"\", \"inputs_data_preview\");\n");
            }
        } else {
            if (currentDepth == 0) { // A leaf in the tree
                try {
                    String dataFilePath = dataFileParentPath + Constants.FILE_SEPARATOR + "Value" + parentIndex;
                    long dataSizeInKB = Math.round(new File(dataFilePath).length() / 1000d); // size in kilobytes (divided by 1000 not 1024!!!)
                    String dataFileURL = request.getContextPath() + FILE_SERVLET_URL
                            + "?" + Constants.DATA_FILE_PATH + "=" + URLEncoder.encode(dataFilePath, "UTF-8")
                            + "&" + Constants.MIME_TYPE + "=" + URLEncoder.encode(mimeType, "UTF-8")
                            + "&" + Constants.DATA_SIZE_IN_KB + "=" + URLEncoder.encode(Long.toString(dataSizeInKB), "UTF-8");
                    inputsTreeHTML.append("addNode2(\"input_data\", \"input_data_preview_textarea\", \"Value" + parentIndex + "\", \"" + dataFileURL + "\", \"inputs_data_preview\");\n");
                } catch (Exception ex) {
                    inputsTreeHTML.append("addNode2(\"input_data\", \"input_data_preview_textarea\", \"Value" + parentIndex + "\", \"\", \"inputs_data_preview\");\n");
                }
            } else { // Result data is a list of (lists of ... ) items
                inputsTreeHTML.append("startParentNode(\"input_data\", \"List" + parentIndex + "\");\n");
                for (int i = 0; i < ((Collection) dataObject).size(); i++) {
                    String newParentIndex = parentIndex.equals("") ? (new Integer(i + 1)).toString() : (parentIndex + "." + (i + 1));
                    inputsTreeHTML.append(createInputsTree(((ArrayList) dataObject).get(i),
                            maxDepth,
                            currentDepth - 1,
                            newParentIndex,
                            dataFileParentPath + Constants.FILE_SEPARATOR + "List" + parentIndex,
                            mimeType,
                            request));
                }
                inputsTreeHTML.append("endParentNode();\n");
            }
        }
        return inputsTreeHTML.toString();
    }

    /*
     * Loads and returns all saved jobs for a user from a disk.
     * Synchronize on the status lock object so that the background thread for updating
     * the job statuses (and results) does not interfere while we load.
     */
    private ArrayList<WorkflowSubmissionJob> loadWorkflowSubmissionJobsForUser(File jobsDir, String user) {
        synchronized (statusLock) {

            ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = new ArrayList<WorkflowSubmissionJob>();
            File userDir = new File(jobsDir, user);
            if (!userDir.exists()) {
                try {
                    userDir.mkdir();
                } catch (Exception ex) {
                    System.out.println("Workflow Results Portlet: Failed to create a directory " + userDir.getAbsolutePath() + " where jobs submitted by the user " + user + " are to be persisted.");
                    ex.printStackTrace();
                    return workflowSubmissionJobs;
                }
            }

            File[] jobDirsForUser = userDir.listFiles(dirFilter);
            for (File jobDir : jobDirsForUser) {
                String uuid = jobDir.getName();
                try {
                    File workflowPropertiesFile = new File(jobDir, Constants.WORKFLOW_PROPERTIES_FILE); // properties file containing local wf file name or myExperiment resource details
                    Properties props = new Properties();
                    String workflowFileName = null;
                    String myExperimentResource = null;
                    String myExterimentVersion = null;
                    props.load(new FileInputStream(workflowPropertiesFile));
                    workflowFileName = props.getProperty(Constants.WORKFLOW_FILE_NAME);
                    myExperimentResource = props.getProperty(Constants.MYEXPERIMENT_WORKFLOW_RESOURCE);
                    myExterimentVersion = props.getProperty(Constants.MYEXPERIMENT_WORKFLOW_VERSION);

                    String statusFileName = jobDir.list(statusFileFilter)[0]; // should be only 1 element or else we are in trouble
                    String status = statusFileName.substring(0, statusFileName.indexOf(Constants.STATUS_FILE_EXT));

                    // Added workflow run description file later on so some jobs may not have it saved - check for it
                    File workflowRunDescriptionFile = new File(jobDir, Constants.WORKFLOW_RUN_DESCRIPTION_FILE);
                    String workflowRunDescription = "";
                    if (workflowRunDescriptionFile.exists()) {
                        workflowRunDescription = FileUtils.readFileToString(workflowRunDescriptionFile, "UTF-8");
                    }

                    Workflow workflow = new Workflow();
                    if (workflowFileName != null) { // this is a local workflow
                        workflow.setFileName(workflowFileName);
                        workflow.setIsMyExperimentWorkflow(false);
                    } else { // this is a workflow from myExperiment
                        workflow.setMyExperimentWorkflowResource(myExperimentResource);
                        workflow.setMyExperimentWorkflowVersion(Integer.parseInt(myExterimentVersion));
                        workflow.setIsMyExperimentWorkflow(true);
                    }
                    
                    WorkflowSubmissionJob workflowSubmissionJob = new WorkflowSubmissionJob(uuid, workflow, status, workflowRunDescription);

                    String startdateFileName = jobDir.list(startdateFileFilter)[0]; // should be only 1 element or else we are in trouble
                    String startdate = startdateFileName.substring(0, startdateFileName.indexOf(Constants.STARTDATE_FILE_EXT));
                    workflowSubmissionJob.setStartDate(new Date(Long.parseLong(startdate)));

                    if (status.equals(Constants.JOB_STATUS_FINISHED)) {
                        String[] enddateFileNames = jobDir.list(enddateFileFilter); // should be only 1 element or else we are in trouble
                        if (enddateFileNames.length != 0) {
                            String enddateFileName = enddateFileNames[0];
                            String enddate = enddateFileName.substring(0, enddateFileName.indexOf(Constants.ENDDATE_FILE_EXT));
                            workflowSubmissionJob.setEndDate(new Date(Long.parseLong(enddate)));
                        }
                    }

                    workflowSubmissionJobs.add(workflowSubmissionJob);

                    System.out.println("Workflow Results Portlet: Found job: " + uuid + "; workflow: " + workflowFileName!=null? workflowFileName : workflow.getMyExperimentWorkflowResource() + "; status: " + status + "\n");
                } catch (Exception ex) { // something went wrong with getting the files for this job - just skip it
                    System.out.println("Workflow Results Portlet: Failed to load info for a previously submitted job from " + jobsDir.getAbsolutePath());
                    ex.printStackTrace();
                }
            }


            // Sort the jobs according to their start date - freshest first.
            Comparator comp = new Comparator() {

                public int compare(Object job1, Object job2) {

                    Date date1 = ((WorkflowSubmissionJob) job1).getStartDate();
                    Date date2 = ((WorkflowSubmissionJob) job2).getStartDate();
                    if (date1.after(date2)) {
                        return -1;
                    } else if (date1.before(date2)) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            };
            Collections.sort(workflowSubmissionJobs, comp);
            return workflowSubmissionJobs;
        }
    }
    // This file filter only returns directories
    public static FileFilter dirFilter = new FileFilter() {

        public boolean accept(File file) {
            return file.isDirectory();
        }
    };
    // This file filter only returns files with .status extension
    public static FilenameFilter statusFileFilter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.STATUS_FILE_EXT);
        }
    };
    // This file filter only returns files with .t2flow extension
    public static FilenameFilter t2flowFileFilter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.T2FLOW_FILE_EXT);
        }
    };
    // This file filter only returns files with .properties extension
    public static FilenameFilter workflowPropertiesFileFilter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.PROPERTIES_FILE_EXT);
        }
    };
    // This file filter only returns files with .startdate extension
    public static FilenameFilter startdateFileFilter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.STARTDATE_FILE_EXT);
        }
    };
    // This file filter only returns files with .enddate extension
    public static FilenameFilter enddateFileFilter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.ENDDATE_FILE_EXT);
        }
    };
    // This file filter only returns files named 'inputs.baclava'
    public static FilenameFilter inputsBaclavaFileFilter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.equals(Constants.INPUTS_BACLAVA_FILE);
        }
    };
    // This file filter only returns files named 'outputs.baclava'
    public static FilenameFilter outputsBaclavaFileFilter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.equals(Constants.OUTPUTS_BACLAVA_FILE);
        }
    };

    /*
     * Fetch a Baclava file with outputs for the job locally from a disk or
     * by downloading from the T2 Server, parse it and return the map of
     * data objects.
     */
    private Map<String, DataThing> fetchJobResults(WorkflowSubmissionJob job, PortletRequest request) {

        synchronized (resultsLock) {
            String workflowResultsBaclavaFileURL = T2_SERVER_URL + Constants.RUNS_URL + "/" + job.getUuid() + Constants.WD_URL + "/" + Constants.BACLAVA_OUTPUT_FILE_NAME;
            //request.setAttribute(Constants.WORKFLOW_RESULTS_BACLAVA_FILE_URL, workflowResultsBaclavaFileURL);

            // First try to get the Baclava file from the local disk.
            // Get the current user first.
            String user = (String) request.getPortletSession().
                    getAttribute(Constants.USER,
                    PortletSession.APPLICATION_SCOPE);

            File userDir = new File(JOBS_DIR, user);
            File[] userJobsDir = userDir.listFiles(dirFilter);
            Map<String, DataThing> resultDataThingMap = null;
            for (File jobDir : userJobsDir) {
                if (jobDir.getName().equals(job.getUuid())) {
                    String[] outputsBaclavaFiles = jobDir.list(outputsBaclavaFileFilter);
                    if (outputsBaclavaFiles.length == 0) { // no such file on local disk - download the file from T2 Server
                        try {
                            InputStream inputStream;
                            System.out.println("Workflow Results Portlet: Downloading the XML Baclava results to local disk for from T2 Server at " + workflowResultsBaclavaFileURL);
                            URL url = new URL(workflowResultsBaclavaFileURL);
                            inputStream = url.openStream();
                            // Parse the result values from the downloaded Baclava file
                            // and save the file locally
                            resultDataThingMap = getDataThingMapWithResultsFromBaclava(inputStream, true, jobDir, request);
                        } catch (Exception ex) {
                            System.out.println("Workflow Results Portlet: An error occured while trying to download the XML Baclava file with outputs for job " + job.getUuid() + " from the Server.");
                            ex.printStackTrace();
                            request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to download the results for job " + job.getUuid() + " from the Server.<br>" + ex.getMessage());
                            return null;
                        }
                    } else {
                        InputStream inputStream;
                        try { // Read from the previously saved output Baclava file
                            File workflowResultsBaclavaFile = new File(jobDir, Constants.OUTPUTS_BACLAVA_FILE);
                            System.out.println("Workflow Results Portlet: Fetching results for from local disk " + workflowResultsBaclavaFile.getAbsolutePath());
                            inputStream = new FileInputStream(workflowResultsBaclavaFile);
                        } catch (Exception ex) {
                            System.out.println("Workflow Results Portlet: An error occured while trying to open the XML Baclava file with outputs for job " + job.getUuid() + ".");
                            ex.printStackTrace();
                            request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to open file with results for job " + job.getUuid() + ".<br>" + ex.getMessage());
                            return null;
                        }
                        // Parse the result values from the Baclava file
                        resultDataThingMap = getDataThingMapWithResultsFromBaclava(inputStream, false, jobDir, request);
                    }

                }
            }

            return resultDataThingMap;
        }
    }

    /*
     * Fetch a Baclava file with workflow input values saved locally on a disk.
     */
    private Map<String, DataThing> fetchJobInputs(WorkflowSubmissionJob job, PortletRequest request) {

        // Try to get the Baclava file from the local disk.
        // Get the current user first.
        String user = (String) request.getPortletSession().
                getAttribute(Constants.USER,
                PortletSession.APPLICATION_SCOPE);

        File userDir = new File(JOBS_DIR, user);
        File[] userJobsDir = userDir.listFiles(dirFilter);
        Map<String, DataThing> inputsDataThingMap = null;
        for (File jobDir : userJobsDir) {
            if (jobDir.getName().equals(job.getUuid())) {
                String[] inputsBaclavaFiles = jobDir.list(inputsBaclavaFileFilter);
                if (inputsBaclavaFiles.length == 0) { // no such file on local disk - send an error message back to the user
                    System.out.println("Workflow Results Portlet: No XML Baclava file with inputs for job " + job.getUuid() + " found on the local disk at " + jobDir.getAbsolutePath());
                    request.setAttribute(Constants.ERROR_MESSAGE, "No file with inputs for job " + job.getUuid() + " found.");
                    return null;
                } else {
                    InputStream inputStream;
                    try { // Read from the Baclava file
                        File workflowInputsBaclavaFile = new File(jobDir, Constants.INPUTS_BACLAVA_FILE);
                        System.out.println("Workflow Results Portlet: Fetching inputs from local disk " + workflowInputsBaclavaFile.getAbsolutePath());
                        inputStream = new FileInputStream(workflowInputsBaclavaFile);
                    } catch (Exception ex) {
                        System.out.println("Workflow Results Portlet: An error occured while trying to open the XML Baclava file with inputs for job " + job.getUuid() + ".");
                        ex.printStackTrace();
                        request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to open file with inputs for job " + job.getUuid() + ".<br>" + ex.getMessage());
                        return null;
                    }
                    // Parse the input values from the Baclava file
                    inputsDataThingMap = getDataThingMapWithInputsFromBaclava(inputStream, jobDir, request);
                }
            }
        }

        return inputsDataThingMap;
    }


    /* Polls statuses of all unfinished jobs for all users and
     * downloads their results.
     */
    private void updateJobStatusesAndFetchResultsForAllUsers() {
        System.out.println("Workflow Results Portlet (update job status thread): Starting background job update.");

        // Get job dirs for all users
        File[] userDirs = JOBS_DIR.listFiles(dirFilter);
        for (File userDir : userDirs) { // for each user, chech statuses of each of their jobs
            File[] userJobDirs = userDir.listFiles(dirFilter);
            for (File jobDir : userJobDirs) {
                File[] statusFiles = jobDir.listFiles(statusFileFilter);
                if (statusFiles.length == 0) {// this is not good (the status file should be there) - skip this job
                    continue;
                }
                String statusOnDisk = statusFiles[0].getName().substring(0, statusFiles[0].getName().indexOf(Constants.STATUS_FILE_EXT));
                if (statusOnDisk.equals(Constants.JOB_STATUS_FINISHED)) {
                    // If job has finished - just make sure there is the output baclava file as well
                    String[] outputsBaclavaFiles = jobDir.list(outputsBaclavaFileFilter);
                    if (outputsBaclavaFiles.length == 0) {
                        //Job finished, but the results not downloaded yet - download them now.
                        try {
                            InputStream inputStream;
                            String workflowResultsBaclavaFileURL = T2_SERVER_URL + Constants.RUNS_URL + "/" + jobDir.getName() + Constants.WD_URL + "/" + Constants.BACLAVA_OUTPUT_FILE_NAME;
                            URL url = new URL(workflowResultsBaclavaFileURL);

                            inputStream = url.openStream();
                            // Parse the result values from the downloaded Baclava file
                            // and save the file locally
                            getDataThingMapWithResultsFromBaclava(inputStream, true, jobDir, null);
                        } catch (Exception ex) {
                            System.out.println("Workflow Results Portlet (update job status thread): An error occured while trying to download the XML Baclava file with outputs for job " + jobDir.getName() + " from the Server.");
                            ex.printStackTrace();
                        }
                    }
                } else {
                    // Get the (possibly updated) job's status from the T2 Server
                    String statusOnServer = getWorkflowSubmissionJobStatusFromServer(jobDir.getName());
                    if (!statusOnServer.equals(statusOnDisk)) {
                        if (statusOnServer.equals(Constants.UNKNOWN_RUN_UUID)) {
                            updateJobStatusOnDiskForUser(jobDir.getName(), Constants.JOB_STATUS_EXPIRED, userDir.getName(), null);
                            //System.out.println("Workflow Results Portlet (update job status thread): Updating status of job "+ jobDir.getName()+" from "+statusOnDisk+" to " + Constants.JOB_STATUS_EXPIRED);
                        } else if (statusOnServer.equals(Constants.JOB_STATUS_FINISHED)) {
                            Date endDate = new Date();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                            System.out.println("Workflow Results Portlet (update job status thread): Execution of job " + jobDir.getName() + " finished on the Server at " + dateFormat.format(endDate) + ".");
                            updateJobStatusOnDiskForUser(jobDir.getName(), statusOnServer, userDir.getName(), endDate);
                            //System.out.println("Workflow Results Portlet (update job status thread): Updating status of job "+ jobDir.getName()+" from "+statusOnDisk+" to " + statusOnServer);

                            // Download the results as well
                            try {
                                InputStream inputStream;
                                String workflowResultsBaclavaFileURL = T2_SERVER_URL + Constants.RUNS_URL + "/" + jobDir.getName() + Constants.WD_URL + "/" + Constants.BACLAVA_OUTPUT_FILE_NAME;
                                URL url = new URL(workflowResultsBaclavaFileURL);

                                inputStream = url.openStream();
                                // Parse the result values from the downloaded Baclava file
                                // and save the file locally
                                getDataThingMapWithResultsFromBaclava(inputStream, true, jobDir, null);
                            } catch (Exception ex) {
                                System.out.println("Workflow Results Portlet (update job status thread): An error occured while trying to download the XML Baclava file with outputs for job " + jobDir.getName() + " from the Server.");
                                ex.printStackTrace();
                            }
                        }
                    }
                }

            }
        }
        System.out.println("Workflow Results Portlet (update job status thread): Finished background job update.");
    }

    /*
     * Parse the input stream with workflow run results
     * (read from a Baclava XML file) into a DataThing map
     * and optionally save it to disk.
     */
    private Map<String, DataThing> getDataThingMapWithResultsFromBaclava(InputStream inputStream, boolean saveLocally, File jobDir, PortletRequest request) {

        synchronized (resultsLock) {
            Map<String, DataThing> resultDataThingMap = null;
            try {
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(inputStream);
                resultDataThingMap = DataThingXMLFactory.parseDataDocument(doc);
                if (saveLocally) { //Are we also saving the Baclava document locally?
                    try {
                        File outputsFile = new File(jobDir, Constants.OUTPUTS_BACLAVA_FILE);
                        FileUtils.writeStringToFile(outputsFile, new XMLOutputter().outputString(doc));
                        System.out.println("Workflow Results Portlet: Saved the XML Baclava file with outputs of job " + jobDir.getName() + " to " + outputsFile.getAbsolutePath());

                        // Also save the individual output data values in
                        // <job_directory>/outputs directory where first level
                        // sub-dirs are port names that inside contain the data value for an output port
                        File outputsDir = new File(jobDir, Constants.OUTPUTS_DIRECTORY_NAME);
                        try {
                            if (!outputsDir.exists()) { // should not exist but hey
                                outputsDir.mkdir();
                            }
                            Utils.saveDataThingMapToDisk(resultDataThingMap, outputsDir);
                        } catch (Exception ex) {
                            System.out.println("Workflow Results Portlet: Failed to create directory " + outputsDir.getAbsolutePath() + " where individual values for all output ports are to be saved.");
                            ex.printStackTrace();
                        }
                    } catch (Exception ex) { // not fatal, so return the result map rather than null
                        System.out.println("Workflow Results Portlet: An error occured while trying to save the Baclava file with outputs to " + jobDir.getAbsolutePath() + Constants.FILE_SEPARATOR + Constants.OUTPUTS_BACLAVA_FILE);
                        ex.printStackTrace();
                        return resultDataThingMap;
                    }
                }
            } catch (Exception ex) {
                System.out.println("Workflow Results Portlet: An error occured while trying to parse the XML Baclava file with outputs for job " + jobDir.getName() + ".");
                ex.printStackTrace();
                if (request != null) {
                    request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to parse the results for job " + jobDir.getName() + ".");
                }
                return null;
            } finally {
                try {
                    inputStream.close();
                } catch (Exception ex2) {
                    // Do nothing
                }
            }
            return resultDataThingMap;
        }
    }

    /*
     * Parse the input stream with workflow inputs (read from a Baclava XML file)
     * into a DataThing map.
     */
    private Map<String, DataThing> getDataThingMapWithInputsFromBaclava(InputStream inputStream, File jobDir, PortletRequest request) {

        Map<String, DataThing> inputsDataThingMap = null;
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(inputStream);
            inputsDataThingMap = DataThingXMLFactory.parseDataDocument(doc);
        } catch (Exception ex) {
            System.out.println("Workflow Results Portlet: An error occured while trying to parse the XML Baclava file with inputs for job " + jobDir.getName() + ".");
            ex.printStackTrace();
            if (request != null) {
                request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to parse the inputs for job " + jobDir.getName() + ".");
            }
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (Exception ex2) {
                // Do nothing
            }
        }
        return inputsDataThingMap;
    }

    /**
     * Creates a HTML table that contains a table with data structure contained
     * in a DataThing map (port->data) that is linked to a data preview table where
     * actual data values can be viewed once user clicks on the link.
     *
     * The DataThing map contains workflow results.
     */
    private String createHTMLTableFromResultsDataThingMap(String baclavaFileURL, Map<String, DataThing> dataThingMap, String user, String workflowResourceUUID, PortletRequest request) {

        // Parse the data values from the Baclava file
        StringBuffer dataTableHTML = new StringBuffer();

        if (dataThingMap == null || dataThingMap.keySet().isEmpty()) { // should not be null
            dataTableHTML.append("<p><b>Results:</b></p><p>No results available for this workflow run.</p>");
            return dataTableHTML.toString();
        }

        dataTableHTML.append("<table width=\"100%\" style=\"margin-bottom:3px;\">\n");
        dataTableHTML.append("<tr>\n");
        dataTableHTML.append("<td valign=\"bottom\"><div class=\"nohover_nounderline\"><b>Results:</b></div></td>\n");
        dataTableHTML.append("<td align=\"right\">Download the results as a <a target=\"_blank\" href=\""
                + baclavaFileURL
                + "\">single Baclava XML file</a>.<br>"
                + "You can view the file with Taverna's DataViewer tool.</td>\n");
        dataTableHTML.append("</tr>\n");
        dataTableHTML.append("</table>\n");

        dataTableHTML.append("<table width=\"100%\">\n");// table that contains the data links table and data preview table
        dataTableHTML.append("<tr><td style=\"vertical-align:top;\">\n");
        dataTableHTML.append("<table class=\"results\">\n");
        dataTableHTML.append("<tr>\n");
        dataTableHTML.append("<th width=\"20%\">Output port</th>\n");
        dataTableHTML.append("<th width=\"15%\">Data</th>\n");
        dataTableHTML.append("</tr>\n");
        int rowCount = 1;
        // Get all the ports and data associated with them
        for (Iterator i = dataThingMap.keySet().iterator(); i.hasNext();) {
            String portName = (String) i.next();
            DataThing dataThing = dataThingMap.get(portName);

            // Calculate the depth of the data for the port
            Object dataObject = dataThing.getDataObject();
            int dataDepth = calculateDataDepth(dataObject);
            if (rowCount % 2 != 0) {
                dataTableHTML.append("<tr>\n");
            } else {
                dataTableHTML.append("<tr style=\"background-color: #F0FFF0;\">\n");
            }
            String dataTypeBasedOnDepth;
            if (dataDepth == 0) {
                dataTypeBasedOnDepth = "single value";
            } else {
                dataTypeBasedOnDepth = "list of depth " + dataDepth;
            }
            // Get data's MIME type as given by the Baclava file
            String mimeType = dataThing.getMostInterestingMIMETypeForObject(dataObject);
            dataTableHTML.append("<td width=\"20%\" style=\"vertical-align:top;\">\n");
            dataTableHTML.append("<div class=\"output_name\">" + portName + "<span class=\"output_depth\"> - " + dataTypeBasedOnDepth + "</span></div>\n");
            dataTableHTML.append("<div class=\"output_mime_type\">" + mimeType + "</div>\n");
            dataTableHTML.append("</td>");

            // Create the data tree (with links to actual data vales)
            String dataFileParentPath = null;

            dataFileParentPath = JOBS_DIR + Constants.FILE_SEPARATOR
                    + user + Constants.FILE_SEPARATOR
                    + workflowResourceUUID + Constants.FILE_SEPARATOR
                    + Constants.OUTPUTS_DIRECTORY_NAME + Constants.FILE_SEPARATOR
                    + portName;

            dataTableHTML.append("<td width=\"15%\" style=\"vertical-align:top;\"><script language=\"javascript\">" + createResultTree(dataObject, dataDepth, dataDepth, "", dataFileParentPath, mimeType, request) + "</script></td>\n");
            rowCount++;
            dataTableHTML.append("</tr>\n");
        }
        dataTableHTML.append("</table>\n");
        dataTableHTML.append("</td>\n");
        dataTableHTML.append("<td style=\"vertical-align:top;\">\n");
        dataTableHTML.append("<table class=\"results_data_preview\"><tr><th>Data preview</th></tr><tr><td><div style=\"vertical-align:top;\" id=\"results_data_preview\">When you select a data item - a preview of its value will appear here.</div></td></tr></table>\n");
        dataTableHTML.append("</td>\n");
        dataTableHTML.append("</tr>\n");
        dataTableHTML.append("</table>\n");
        dataTableHTML.append("</br>\n");

        dataTableHTML.append("Download the results as a <a target=\"_blank\" href=\""
                + baclavaFileURL
                + "\">single Baclava XML file</a>.<br>"
                + "You can view the file with Taverna's DataViewer tool.");

        return dataTableHTML.toString();
    }

    /**
     * Creates a HTML table that contains a table with data structure contained
     * in a DataThing map (port->data) that is linked to a data preview table where
     * actual data values can be viewed once user clicks on the link.
     *
     * The DataThing map contains workflow inputs.
     */
    private String createHTMLTableFromInputsDataThingMap(String baclavaFileURL, Map<String, DataThing> dataThingMap, String user, String workflowResourceUUID, PortletRequest request) {

        // Parse the data values from the Baclava file
        StringBuffer dataTableHTML = new StringBuffer();

        if (dataThingMap == null || dataThingMap.keySet().isEmpty()) { // should not be nulls
            dataTableHTML.append("<p><b>Inputs:</b></p><p>This workflow has no inputs.</p>");
            return dataTableHTML.toString();
        }

        dataTableHTML.append("<table width=\"100%\" style=\"margin-bottom:3px;\">\n");
        dataTableHTML.append("<tr>\n");
        dataTableHTML.append("<td valign=\"bottom\"><div class=\"nohover_nounderline\"><b>Inputs:</b></div></td>\n");
        dataTableHTML.append("<td align=\"right\">Download the inputs as a <a target=\"_blank\" href=\""
                + baclavaFileURL
                + "\">single Baclava XML file</a>.<br>"
                + "You can view the file with Taverna's DataViewer tool.</td>\n");
        dataTableHTML.append("</tr>\n");
        dataTableHTML.append("</table>\n");

        dataTableHTML.append("<table width=\"100%\">\n");// table that contains the data links table and data preview table
        dataTableHTML.append("<tr><td style=\"vertical-align:top;\">\n");
        dataTableHTML.append("<table class=\"inputs\">\n");
        dataTableHTML.append("<tr>\n");
        dataTableHTML.append("<th width=\"20%\">Input port</th>\n");
        dataTableHTML.append("<th width=\"15%\">Data</th>\n");
        dataTableHTML.append("</tr>\n");
        int rowCount = 1;
        // Get all the ports and data associated with them
        for (Iterator i = dataThingMap.keySet().iterator(); i.hasNext();) {
            String portName = (String) i.next();
            DataThing dataThing = dataThingMap.get(portName);

            // Calculate the depth of the data for the port
            Object dataObject = dataThing.getDataObject();
            int dataDepth = calculateDataDepth(dataObject);
            if (rowCount % 2 != 0) {
                dataTableHTML.append("<tr>\n");
            } else {
                dataTableHTML.append("<tr style=\"background-color: #F0FFF0;\">\n");
            }
            String dataTypeBasedOnDepth;
            if (dataDepth == 0) {
                dataTypeBasedOnDepth = "single value";
            } else {
                dataTypeBasedOnDepth = "list of depth " + dataDepth;
            }
            // Get data's MIME type as given by the Baclava file
            String mimeType = dataThing.getMostInterestingMIMETypeForObject(dataObject);
            dataTableHTML.append("<td width=\"20%\" style=\"vertical-align:top;\">\n");
            dataTableHTML.append("<div class=\"input_name\">" + portName + "<span class=\"input_depth\"> - " + dataTypeBasedOnDepth + "</span></div>\n");
            dataTableHTML.append("<div class=\"input_mime_type\">" + mimeType + "</div>\n");
            dataTableHTML.append("</td>");

            // Create the data tree (with links to actual data vales)
            String dataFileParentPath = null;

            dataFileParentPath = JOBS_DIR + Constants.FILE_SEPARATOR
                    + user + Constants.FILE_SEPARATOR
                    + workflowResourceUUID + Constants.FILE_SEPARATOR
                    + Constants.INPUTS_DIRECTORY_NAME + Constants.FILE_SEPARATOR
                    + portName;

            dataTableHTML.append("<td width=\"15%\" style=\"vertical-align:top;\"><script language=\"javascript\">" + createInputsTree(dataObject, dataDepth, dataDepth, "", dataFileParentPath, mimeType, request) + "</script></td>\n");
            rowCount++;
            dataTableHTML.append("</tr>\n");
        }
        dataTableHTML.append("</table>\n");
        dataTableHTML.append("</td>\n");
        dataTableHTML.append("<td style=\"vertical-align:top;\">\n");
        dataTableHTML.append("<table class=\"inputs_data_preview\"><tr><th>Data preview</th></tr><tr><td><div style=\"vertical-align:top;\" id=\"inputs_data_preview\">When you select a data item - a preview of its value will appear here.</div></td></tr></table>\n");
        dataTableHTML.append("</td>\n");
        dataTableHTML.append("</tr>\n");
        dataTableHTML.append("</table>\n");
        dataTableHTML.append("</br>\n");

        dataTableHTML.append("Download the inputs as a <a target=\"_blank\" href=\""
                + baclavaFileURL
                + "\">single Baclava XML file</a>.<br>"
                + "You can view the file with Taverna's DataViewer tool.");

        return dataTableHTML.toString();
    }

    private void deleteJobForUser(String jobId, PortletRequest request) {
        // Get currently logged in user
        String user = (String) request.getPortletSession().
                getAttribute(Constants.USER,
                PortletSession.APPLICATION_SCOPE);
        File dirToDelete = new File(JOBS_DIR, user + Constants.FILE_SEPARATOR + jobId);
        try {
            FileUtils.deleteDirectory(dirToDelete);
        } catch (Exception ex) {
            System.out.println("Workflow Results Portlet: An error occured while trying to delete job " + jobId + ".");
            ex.printStackTrace();
            request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to delete job " + jobId + ".");
        }
    }

    /*
     * Return the body of the HTTP response as a String.
     * From Sergejs Aleksejevs' Taverna REST plugin.
     */
    private static String readResponseBodyAsString(HttpEntity entity) throws IOException {
        // Get charset name. Use UTF-8 if not defined.
        String charset = "UTF-8";
        String contentType = entity.getContentType().getValue().toLowerCase();

        String[] contentTypeParts = contentType.split(";");
        for (String contentTypePart : contentTypeParts) {
            contentTypePart = contentTypePart.trim();
            if (contentTypePart.startsWith("charset=")) {
                charset = contentTypePart.substring("charset=".length());
            }
        }

        // read the data line by line
        StringBuilder responseBodyString = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), charset));

        String str;
        while ((str = reader.readLine()) != null) {
            responseBodyString.append(str + "\n");
        }

        return (responseBodyString.toString());
    }

    /*
     * Calculate depth of a result data item.
     */
    private static int calculateDataDepth(Object dataObject) {

        if (dataObject instanceof Collection<?>) {
            if (((Collection<?>) dataObject).isEmpty()) {
                return 1;
            } else {
                // Calculate the depth of the first element in collection + 1
                return calculateDataDepth(((Collection<?>) dataObject).iterator().next()) + 1;
            }
        } else {
            return 0;
        }
    }
}
