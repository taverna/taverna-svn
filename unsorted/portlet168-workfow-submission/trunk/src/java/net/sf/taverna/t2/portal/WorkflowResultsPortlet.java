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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
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
public class WorkflowResultsPortlet extends GenericPortlet{

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
    private static long JOB_UPDATE_PERIOD_IN_MS = 5*60*1000; // 5 min
    private static long JOB_UPDATE_DELAY_IN_MS = 5*60*1000; // 5 min

    // Locks for synschronising between the background thread for updating jobs
    // and fethcing results and the main thread
    static final Object statusLock = new Object();
    static final Object resultsLock = new Object();

    /*
     * Do the init stuff one at portlet loading time.
     */
    @Override
    public void init(){

        // Get the URL of the T2 Server defined in web.xml as an
        // app-wide init parameter ( <context-param> element)
        T2_SERVER_URL = getPortletContext().getInitParameter(Constants.T2_SERVER_URL);

        // Get the directory where info for submitted jobs for all users is persisted
        JOBS_DIR = new File(getPortletContext().getInitParameter(Constants.JOBS_DIRECTORY_PATH),
                Constants.JOBS_DIRECTORY_NAME);
        if (!JOBS_DIR.exists()){
            try{
                JOBS_DIR.mkdir();
            }
            catch(Exception ex){
                System.out.println("Workflow Results Portlet: Failed to create a directory "+JOBS_DIR.getAbsolutePath()+" where submitted jobs are to be persisted.");
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

        FILE_SERVLET_URL = getPortletContext().getInitParameter(Constants.FILE_SERVLET_URL);

        try{
            MAX_PREVIEW_DATA_SIZE_IN_KB = Long.valueOf((String)getPortletContext().getInitParameter(Constants.MAX_PREVIEW_DATA_SIZE_IN_KB));
        }
        catch(Exception ex){
            MAX_PREVIEW_DATA_SIZE_IN_KB = DEFAULT_MAX_PREVIEW_DATA_SIZE_IN_KB;
        }
    }

    @Override
    public void destroy(){
        if (pollJobStatusesTimer != null){
            pollJobStatusesTimer.cancel();
        }
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {

        ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)request.getPortletSession().
        getAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
        PortletSession.APPLICATION_SCOPE);

        // If there was a request to refresh the job statuses
        if (request.getParameter(Constants.REFRESH_WORKFLOW_JOBS) != null){
            updateJobStatusesForUser(workflowSubmissionJobs, request);
        }
        // If there was a request to show results of a workflow run
        else if (request.getParameter(Constants.FETCH_RESULTS) != null){

            // If workflowSubmissionJobs is null or does not contain this job id
            // this is just a page refresh after redeployment of the app/restart of
            // the sever/some form or refresh while the URL parameter FETCH_RESULTS
            // managed to linger in the URL in the browser from the previous
            // session so just ignore it.
            if (workflowSubmissionJobs != null){
                String workflowResourceUUID = URLDecoder.decode(request.getParameterValues(Constants.FETCH_RESULTS)[0], "UTF-8");
                for (WorkflowSubmissionJob job : workflowSubmissionJobs){
                    if (job.getUuid().equals(workflowResourceUUID)){
                        System.out.println("Workflow Results Portlet: Fetching results for job id " + workflowResourceUUID);

                        // See if results are already downloaded from the T2 Server -
                        // if not download and save the Baclava file with outputs now.
                        Map<String, DataThing> resultDataThingMap = fetchJobResultsForUser(job, request);
                        request.setAttribute(Constants.OUTPUTS_MAP_ATTRIBUTE, resultDataThingMap);
                        request.setAttribute(Constants.WORKFLOW_SUBMISSION_JOB, job);
                        break; 
                    } // else just ignore it if it is not in the job id list
                }
            }
        }
        // If there was a request to delete a workflow run
        else if (request.getParameter(Constants.DELETE_JOB) != null){
            // If workflowSubmissionJobs is null or does not contain this job id
            // this is just a page refresh after redeployment of the app/restart of
            // the sever/some form or refresh while the URL parameter DELETE_JOB
            // managed to linger in the URL in the browser from the previous
            // session so just ignore it.
            if (workflowSubmissionJobs != null){
                String workflowResourceUUID = URLDecoder.decode(request.getParameterValues(Constants.DELETE_JOB)[0], "UTF-8");
                
                Iterator<WorkflowSubmissionJob> iter = workflowSubmissionJobs.iterator();
                while (iter.hasNext()) {
                    if (iter.next().getUuid().equals(workflowResourceUUID)){
                        System.out.println("Workflow Results Portlet: Deleting job " + workflowResourceUUID);
                        iter.remove();
                        deleteJobForUser(workflowResourceUUID, request);
                        request.setAttribute(Constants.WORKFLOW_SUBMISSION_JOBS, workflowSubmissionJobs);
                        break;
                    }
                } // else just ignore it if it is not in the job id list
            }
        }

        // Pass all request parameters over to the doView() and other render stage methods
        response.setRenderParameters(request.getParameterMap());

    }

    @Override
    public void doView(RenderRequest request, RenderResponse response) throws PortletException,IOException {
        response.setContentType("text/html");

        // Print out a message to the user, if any
        if (request.getAttribute(Constants.ERROR_MESSAGE) != null){
            response.getWriter().println("<p style=\"color:red;\"><b>"+ request.getAttribute(Constants.ERROR_MESSAGE)+ "</b></p>\n");
            response.getWriter().println("<br>");
            response.getWriter().println("<hr/>");
            response.getWriter().println("<br>");
        }
        if (request.getAttribute(Constants.INFO_MESSAGE) != null){
            response.getWriter().println("<p><b>"+ request.getAttribute(Constants.INFO_MESSAGE)+ "</b></p>\n");
            response.getWriter().println("<br>");
            response.getWriter().println("<hr/>");
            response.getWriter().println("<br>");
        }

        // Get currently logged in user
        String user = (String)request.getPortletSession().
                                            getAttribute(Constants.USER,
                                            PortletSession.APPLICATION_SCOPE);
        if (user == null){
            if (request.getUserPrincipal() == null){
                user = Constants.USER_ANONYMOUS;
            }
            else{
                user = request.getUserPrincipal().getName();
            }
            System.out.println("Workflow Results Portlet: Session started for user " + user + "." );
            request.getPortletSession().setAttribute(Constants.USER,
                    user,
                    PortletSession.APPLICATION_SCOPE);
        }

        // Get all jobs for the current user that have been persisted on a disk
        ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)request.getPortletSession().
                                            getAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
                                            PortletSession.APPLICATION_SCOPE);
        if (workflowSubmissionJobs == null){ // load user's jobs from disk
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
        if (request.getParameter(Constants.FETCH_RESULTS) != null){
            
            // But if workflowSubmissionJobs is null or does not contain this job id
            // this is just a page refresh after redeployment of the app/restart of
            // the sever/some form or refresh while the URL parameter FETCH_RESULTS
            // managed to linger in the URL in the browser from the previous
            // session so just ignore it. In this case the map of result DataThings
            // will be null so just ignore this request. Also if something went wrong
            // with fetching the outputs - the map will be null so do not show anything -
            // an error message will be displayed to the user.
            Map<String, DataThing> resultDataThingMap = 
                    (Map<String, DataThing>)request.
                    getAttribute(Constants.OUTPUTS_MAP_ATTRIBUTE); // just populated in the processAction method
            if (resultDataThingMap != null){
                String workflowResourceUUID = URLDecoder.decode(request.getParameterValues(Constants.FETCH_RESULTS)[0], "UTF-8");
                WorkflowSubmissionJob job = (WorkflowSubmissionJob)request.getAttribute(Constants.WORKFLOW_SUBMISSION_JOB);

                response.getWriter().println("<br>\n");
                response.getWriter().println("<hr/>\n");
                response.getWriter().println("<br>\n");

                // Parse the result values from the Baclava file
                StringBuffer outputsTableHTML = new StringBuffer();
                response.getWriter().println("<b>Job Id: " + job.getUuid() + "</b><br>\n");
                response.getWriter().println("<b>Workflow: " + job.getWorkflowFileName() + "</b><br><br>\n");

                String baclavaOutputsFilePath = JOBS_DIR + Constants.FILE_SEPARATOR +
                        user + Constants.FILE_SEPARATOR +
                        workflowResourceUUID + Constants.FILE_SEPARATOR +
                        Constants.OUTPUTS_BACLAVA_FILE;
                String baclavaOutputsFileURL = request.getContextPath() + FILE_SERVLET_URL + "?"+ Constants.DATA_FILE_PATH +"=" + URLEncoder.encode(baclavaOutputsFilePath, "UTF-8") +
                        "&" + Constants.MIME_TYPE + "=" + URLEncoder.encode(Constants.CONTENT_TYPE_APPLICATION_XML, "UTF-8");

                outputsTableHTML.append("<table width=\"100%\" style=\"margin-bottom:3px;\">\n");
                outputsTableHTML.append("<tr>\n");
                outputsTableHTML.append("<td valign=\"bottom\"><b>Results:</b></td>\n");
                outputsTableHTML.append("<td align=\"right\">Download the results as a <a target=\"_blank\" href=\"" +
                        baclavaOutputsFileURL +
                        "\">single XML file</a>.<br>" +
                        "You can view the file with Taverna's DataViewer tool.</td>\n");
                outputsTableHTML.append("</tr>\n");
                outputsTableHTML.append("</table>\n");

                outputsTableHTML.append("<table class=\"results\">\n");
                outputsTableHTML.append("<tr>\n");
                outputsTableHTML.append("<th width=\"20%\">Output port</th>\n");
                outputsTableHTML.append("<th width=\"15%\">Data</th>\n");
                outputsTableHTML.append("<th>Data preview</th>\n");
                outputsTableHTML.append("</tr>\n");
                int rowCount = 1;
                // Get all output ports and data associated with them
                for (Iterator i = resultDataThingMap.keySet().iterator(); i.hasNext();) {
                        String outputPortName = (String) i.next();
                        DataThing resultDataThing = resultDataThingMap.get(outputPortName);

                        // Calculate the depth of the result data for the port
                        Object dataObject = resultDataThing.getDataObject();
                        int dataDepth = calculateDataDepth(dataObject);
                        if (rowCount % 2 != 0){
                            outputsTableHTML.append("<tr>\n");
                        }
                        else{
                            outputsTableHTML.append("<tr style=\"background-color: #F0FFF0;\">\n");
                        }
                        String dataTypeBasedOnDepth;
                        if (dataDepth==0){
                            dataTypeBasedOnDepth = "single value";
                        }
                        else{
                            dataTypeBasedOnDepth = "list of depth " + dataDepth;
                        }
                        // Get data's MIME type as given by the Baclava file
                        String mimeType = resultDataThing.getMostInterestingMIMETypeForObject(dataObject);
                        outputsTableHTML.append("<td width=\"20%\">\n");
                        outputsTableHTML.append("<div class=\"output_name\">" + outputPortName + "<span class=\"output_depth\"> - " + dataTypeBasedOnDepth + "</span></div>\n");
                        outputsTableHTML.append("<div class=\"output_mime_type\">" + mimeType + "</div>\n");
                        outputsTableHTML.append("</td>");
                        // Create result tree
                        String dataFileParentPath = JOBS_DIR + Constants.FILE_SEPARATOR + 
                                user + Constants.FILE_SEPARATOR +
                                workflowResourceUUID + Constants.FILE_SEPARATOR +
                                Constants.OUTPUTS_DIRECTORY_NAME + Constants.FILE_SEPARATOR +
                                outputPortName;
                        outputsTableHTML.append("<td width=\"15%\"><script language=\"javascript\">" + createResultTree(dataObject, dataDepth, dataDepth, "", dataFileParentPath, mimeType, request) + "</script></td>\n");
                        if (rowCount == 1){ // Add the data preview cell but only in the first row as it spans across the table height
                            outputsTableHTML.append("<td style=\"border:none;vertical-align:top;\" colspan=\""+resultDataThingMap.keySet().size()+"\"><div style=\"vertical-align:top;\" id=\"data_preview\"></div></td>\n");
                        }
                        rowCount++;
                        outputsTableHTML.append("</tr>\n");
                }
                outputsTableHTML.append("</table>\n");
                outputsTableHTML.append("</br>\n");
                response.getWriter().println(outputsTableHTML.toString());

                response.getWriter().println("Download the results as a <a target=\"_blank\" href=\"" + 
                        baclavaOutputsFileURL+
                        "\">single XML file</a>. " +
                        "You can view the file with Taverna's DataViewer tool.");
            }
        }
    }

    @Override
    public void doEdit(RenderRequest request,RenderResponse response) throws PortletException,IOException {
            response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_edit.jsp");
        dispatcher.include(request, response);
    }

    @Override
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_help.jsp");
        dispatcher.include(request, response);
    }

    /*
     * Fetches job statuses from a T2 Server for all the jobs that
     * have not already finished.
     */
    private void updateJobStatusesForUser(ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs, PortletRequest request){

        synchronized(statusLock){
            // Get the current user
            String user = (String)request.getPortletSession().
                                        getAttribute(Constants.USER,
                                        PortletSession.APPLICATION_SCOPE);
            for (int i = workflowSubmissionJobs.size()-1; i>=0; i--){
                WorkflowSubmissionJob job = workflowSubmissionJobs.get(i);
                if (job.getStatus().equals(Constants.JOB_STATUS_OPERATING)){
                    // Read status from the disk first as it may have been
                    // updated by the background thread in the meantime
                    String statusOnDisk = null;
                    File jobDir = new File(JOBS_DIR + Constants.FILE_SEPARATOR + user + Constants.FILE_SEPARATOR + job.getUuid());
                    File[] statusFiles = jobDir.listFiles(statusFileFilter);
                    if (statusFiles.length != 0){
                        statusOnDisk = statusFiles[0].getName().substring(0, statusFiles[0].getName().indexOf(Constants.STATUS_FILE_EXT));
                    }
                    else{// this is not good (the status file should be there) - skip this job
                        continue;
                    }

                    if (statusOnDisk != null && !statusOnDisk.equals(job.getStatus())){ // status changed
                        job.setStatus(statusOnDisk);
                    }
                    else{
                        // Get the updated job's status from the T2 Server
                        String statusOnServer = getWorkflowSubmissionJobStatusFromServer(job.getUuid());
                        if (!statusOnServer.equals(job.getStatus())){
                            // If the job is not available on the Server any more - set its status to "Expired"
                            if (statusOnServer.equals(Constants.UNKNOWN_RUN_UUID)){
                                job.setStatus(Constants.JOB_STATUS_EXPIRED);
                                // Persist the new status for this job on a disk
                                updateJobStatusOnDiskForUser(job.getUuid(), Constants.JOB_STATUS_EXPIRED, user);
                            }
                            else{
                                job.setStatus(statusOnServer);
                                // Persist the new status for this job on a disk
                                updateJobStatusOnDiskForUser(job.getUuid(), statusOnServer, user);
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
    private String getWorkflowSubmissionJobStatusFromServer(String workflowSubmissionJobId){

        HttpClient httpClient = new DefaultHttpClient();
        String statusURL = T2_SERVER_URL + Constants.RUNS_URL + "/" + workflowSubmissionJobId + Constants.STATUS_URL;

        HttpGet httpGet = new HttpGet(statusURL);

        HttpResponse httpResponse = null;
        try{
            // Execute the request
            HttpContext localContext = new BasicHttpContext();
            httpResponse = httpClient.execute(httpGet, localContext);

            // Release resource
            httpClient.getConnectionManager().shutdown();

            if (httpResponse.getStatusLine().getStatusCode() == 403){ // HTTP/1.1 403 Forbidden
                System.out.println("Workflow Results Portlet: Job " + workflowSubmissionJobId +
                        " does not exist on the Server any more. The Server responded with: " + httpResponse.getStatusLine()+".");
                return Constants.UNKNOWN_RUN_UUID;
            }
            else if (httpResponse.getStatusLine().getStatusCode() == 200){ // HTTP/1.1 200 OK
                HttpEntity httpEntity = httpResponse.getEntity();

                String value = null;
                String contentType = httpEntity.getContentType().getValue().toLowerCase();

                try{
                    if (contentType.startsWith("text")) {
                        // Read as text
                        value = readResponseBodyAsString(httpEntity).trim();
                         System.out.println("Workflow Results Portlet: Status of job " + workflowSubmissionJobId + " on the Server is '" + value + "'.");
                    }
                    else{
                        System.out.println("Workflow Results Portlet: Server's response not text/plain for status of job " + workflowSubmissionJobId);
                    }
                }
                catch(Exception ex){
                    System.out.println("Workflow Results Portlet: Failed to get the content of the job status respose from the Server.");
                    ex.printStackTrace();
                    return "Failed to get the content of the job status respose from the Server";
                }
                return value;
            }
            else {
               System.out.println("Workflow Results Portlet: Failed to get the status for job " + workflowSubmissionJobId + ". The Server responded with: " + httpResponse.getStatusLine()+".");
               return "Failed to get the status for job. The Server responnded with: " + httpResponse.getStatusLine() + ".";
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Results Portlet: An error occured while trying to get the status for job " + workflowSubmissionJobId + ".");
            ex.printStackTrace();
            return "An error occured while trying to get the status for job.";
        }
    }

    /*
     * Update the status of the user's job status on a local disk.
     * Job's status is saved in a file with name <JOB_STATUS>.status in
     * a directory for that job (named after the job's UUID) and the owning user.
     */
    private void updateJobStatusOnDiskForUser(String workflowSubmissionJobId, String newStatus, String user){
        synchronized (statusLock){
            File userDir = new File (JOBS_DIR, user);
            File[] userJobsDir = userDir.listFiles(dirFilter);

            String oldStatus = null;
            for (File jobDir : userJobsDir){
                if (jobDir.getName().equals(workflowSubmissionJobId)){
                    String statusFileName = jobDir.list(statusFileFilter)[0]; // should be only 1 element or else we are in trouble
                    oldStatus = statusFileName.substring(0, statusFileName.indexOf(Constants.STATUS_FILE_EXT));
                    File statusFile = new File(jobDir, statusFileName);
                    statusFile.renameTo(new File(jobDir, newStatus + Constants.STATUS_FILE_EXT));
                    System.out.println("Workflow Results Portlet: Updated status for job " + jobDir.getAbsolutePath() + " from '" + oldStatus + "' to '" + newStatus + "'.");
                }
            }
        }
    }

    /*
     * Create a result tree in JavaScript for a result data item.
     */
    private String createResultTree(Object dataObject, int maxDepth, int currentDepth, String parentIndex, String dataFileParentPath, String mimeType, PortletRequest request){

        StringBuffer resultTreeHTML = new StringBuffer();

        if (maxDepth == 0){ // Result data is a single item only
            try{
                String dataFilePath = dataFileParentPath + Constants.FILE_SEPARATOR + "Value";
                long dataSizeInKB = Math.round(new File(dataFilePath).length()/1000d); // size in kilobytes (divided by 1000 not 1024!!!)
                String dataFileURL = request.getContextPath() + FILE_SERVLET_URL +
                        "?"+ Constants.DATA_FILE_PATH +"=" + URLEncoder.encode(dataFilePath, "UTF-8") +
                        "&" + Constants.MIME_TYPE + "=" + URLEncoder.encode(mimeType, "UTF-8") +
                        "&" + Constants.DATA_SIZE_IN_KB + "=" + URLEncoder.encode(Long.toString(dataSizeInKB), "UTF-8");
                resultTreeHTML.append("addNode2(\"Value\", \""+dataFileURL+"\", \"data_preview\");\n");
            }
            catch(Exception ex){
                resultTreeHTML.append("addNode2(\"Value\", \"\", \"data_preview\");\n");
            }
        }
        else{
            if (currentDepth == 0){ // A leaf in the tree
                try{
                    String dataFilePath = dataFileParentPath + Constants.FILE_SEPARATOR + "Value" + parentIndex;
                    long dataSizeInKB = Math.round(new File(dataFilePath).length()/1000d); // size in kilobytes (divided by 1000 not 1024!!!)
                    String dataFileURL = request.getContextPath() + FILE_SERVLET_URL +
                        "?"+ Constants.DATA_FILE_PATH +"=" + URLEncoder.encode(dataFilePath, "UTF-8") +
                        "&" + Constants.MIME_TYPE + "=" + URLEncoder.encode(mimeType, "UTF-8") +
                        "&" + Constants.DATA_SIZE_IN_KB + "=" + URLEncoder.encode(Long.toString(dataSizeInKB), "UTF-8");
                    resultTreeHTML.append("addNode2(\"Value" + parentIndex + "\", \""+dataFileURL+"\", \"data_preview\");\n");
                }
                catch(Exception ex){
                    resultTreeHTML.append("addNode2(\"Value" + parentIndex + "\", \"\", \"data_preview\");\n");
                }
            }
            else{ // Result data is a list of (lists of ... ) items
                resultTreeHTML.append("startParentNode(\"List" + parentIndex +"\");\n");
                for (int i=0; i < ((Collection)dataObject).size(); i++){
                    String newParentIndex = parentIndex.equals("") ? (new Integer(i+1)).toString() : (parentIndex +"."+(i+1));
                    resultTreeHTML.append(createResultTree(((ArrayList)dataObject).get(i),
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
     * Loads and returns all saved jobs for a user from a disk.
     * Synchronize on the status lock object so that the background thread for updating
     * the job statuses (and results) does not interfere while we load.
     */
    private ArrayList<WorkflowSubmissionJob> loadWorkflowSubmissionJobsForUser(File jobsDir, String user){
        synchronized (statusLock){
    
            ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = new ArrayList<WorkflowSubmissionJob>();
            File userDir = new File (jobsDir, user);
            if (!userDir.exists()){
                try{
                    userDir.mkdir();
                }
                catch(Exception ex){
                    System.out.println("Workflow Results Portlet: Failed to create a directory "+userDir.getAbsolutePath()+" where jobs submitted by the user " + user + " are to be persisted.");
                    ex.printStackTrace();
                    return workflowSubmissionJobs;
                }
            }

            File[] jobDirsForUser = userDir.listFiles(dirFilter);
            for (File jobDir : jobDirsForUser){
                String uuid = jobDir.getName();
                try{
                    String workflowFileNameWithExtension = jobDir.list(t2flowFileFilter)[0]; // should be only 1 element or else we are in trouble
                    String workflowFileName = workflowFileNameWithExtension.substring(0, workflowFileNameWithExtension.indexOf(Constants.T2_FLOW_FILE_EXT));

                    String statusFileName = jobDir.list(statusFileFilter)[0]; // should be only 1 element or else we are in trouble
                    String status = statusFileName.substring(0, statusFileName.indexOf(Constants.STATUS_FILE_EXT));

                    WorkflowSubmissionJob workflowSubmissionJob =  new WorkflowSubmissionJob(uuid, workflowFileName, status);

                    String startdateFileName = jobDir.list(startdateFileFilter)[0]; // should be only 1 element or else we are in trouble
                    String startdate = startdateFileName.substring(0, startdateFileName.indexOf(Constants.STARTDATE_FILE_EXT));
                    workflowSubmissionJob.setStartDate(new Date(Long.parseLong(startdate)));

                    workflowSubmissionJobs.add(workflowSubmissionJob);

                    System.out.println("Workflow Results Portlet: Found job: " + uuid + "; workflow: " + workflowFileName + "; status: " + status + "\n");
                }
                catch(Exception ex){ // something went wrong with getting the files for this job - just skip it
                    System.out.println("Workflow Results Portlet: Failed to load info for a previously submitted job from " + jobsDir.getAbsolutePath());
                    ex.printStackTrace();
                }
            }


            // Sort the jobs according to their start date - freshest first.
            Comparator comp = new Comparator(){
                public int compare(Object job1, Object job2){

                    Date date1 = ((WorkflowSubmissionJob)job1).getStartDate();
                    Date date2 = ((WorkflowSubmissionJob)job2).getStartDate();
                    if (date1.after(date2)){
                        return -1;
                    }
                    else if(date1.before(date2)){
                        return 1;
                    }
                    else{
                        return 0;
                    }
                }
            };
            Collections.sort(workflowSubmissionJobs,comp);
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
            return name.endsWith(Constants.T2_FLOW_FILE_EXT);
        }
    };
    // This file filter only returns files with .startdate extension
    public static FilenameFilter startdateFileFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.STARTDATE_FILE_EXT);
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
    private Map<String, DataThing> fetchJobResultsForUser(WorkflowSubmissionJob job, PortletRequest request){

        synchronized(resultsLock){
            String workflowResultsBaclavaFileURL = T2_SERVER_URL + Constants.RUNS_URL + "/"+ job.getUuid() + Constants.WD_URL + "/" + Constants.BACLAVA_OUTPUT_FILE_NAME;
            request.setAttribute(Constants.WORKFLOW_RESULTS_BACLAVA_FILE_URL, workflowResultsBaclavaFileURL);

            // First try to get the Baclava file from the local disk.
            // Get the current user first.
            String user = (String)request.getPortletSession().
                                        getAttribute(Constants.USER,
                                        PortletSession.APPLICATION_SCOPE);

            File userDir = new File (JOBS_DIR, user);
            File[] userJobsDir = userDir.listFiles(dirFilter);
            Map<String, DataThing> resultDataThingMap = null;
            for (File jobDir : userJobsDir){
                if (jobDir.getName().equals(job.getUuid())){
                    String[] outputsBaclavaFiles = jobDir.list(outputsBaclavaFileFilter);
                    if (outputsBaclavaFiles.length == 0){ // no such file on local disk - download the file from T2 Server
                        try{
                            InputStream inputStream;
                            System.out.println("Workflow Results Portlet: Downloading the XML Baclava results to local disk for from T2 Server at " + workflowResultsBaclavaFileURL);
                            URL url = new URL(workflowResultsBaclavaFileURL);
                            inputStream = url.openStream();
                            // Parse the result values from the downloaded Baclava file
                            // and save the file locally
                            resultDataThingMap = parseBaclavaFile(inputStream, true, jobDir, request);
                        }
                        catch(Exception ex){
                            System.out.println("Workflow Results Portlet: An error occured while trying to download the XML Baclava file with outputs for job " + job.getUuid() + " from the Server.");
                            ex.printStackTrace();
                            request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to download the results for job " + job.getUuid() + " from the Server.<br>" + ex.getMessage());
                            return null;
                        }
                    }
                    else{
                        InputStream inputStream;
                        try{ // Read from the previously saved output Baclava file
                            File workflowResultsBaclavaFile  = new File (jobDir, Constants.OUTPUTS_BACLAVA_FILE);
                            System.out.println("Workflow Results Portlet: Fetching results for from local disk " + workflowResultsBaclavaFile.getAbsolutePath());
                            inputStream = new FileInputStream(workflowResultsBaclavaFile);
                        }
                        catch(Exception ex){
                            System.out.println("Workflow Results Portlet: An error occured while trying to open the XML Baclava file with outputs for job " + job.getUuid() + ".");
                            ex.printStackTrace();
                            request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to open file with results for job " + job.getUuid() + ".<br>" + ex.getMessage());
                            return null;
                        }
                        // Parse the result values from the Baclava file
                        resultDataThingMap = parseBaclavaFile(inputStream, false, null, request);
                    }

                }
            }

            return resultDataThingMap;
        }
    }

    /* Polls statuses of all unfinished jobs for all users and
     * downloads their results.
     */
    private void updateJobStatusesAndFetchResultsForAllUsers(){
        System.out.println("Workflow Results Portlet (update job status thread): Starting background job update.");

        // Get job dirs for all users
        File[] userDirs = JOBS_DIR.listFiles(dirFilter);
        for (File userDir : userDirs){ // for each user, chech statuses of each of their jobs
            File[] userJobDirs = userDir.listFiles(dirFilter);
            for (File jobDir : userJobDirs){
                File[] statusFiles = jobDir.listFiles(statusFileFilter);
                if (statusFiles.length == 0){// this is not good (the status file should be there) - skip this job
                    continue;
                }
                String statusOnDisk = statusFiles[0].getName().substring(0, statusFiles[0].getName().indexOf(Constants.STATUS_FILE_EXT));
                if (statusOnDisk.equals(Constants.JOB_STATUS_FINISHED)){
                    // If job has finished - just make sure there is the output baclava file as well
                    String[] outputsBaclavaFiles = jobDir.list(outputsBaclavaFileFilter);
                    if (outputsBaclavaFiles.length == 0){
                        //Job finished, but the results not downloaded yet - download them now.
                        try{
                            InputStream inputStream;
                            String workflowResultsBaclavaFileURL = T2_SERVER_URL + Constants.RUNS_URL + "/"+ jobDir.getName() + Constants.WD_URL + "/" + Constants.BACLAVA_OUTPUT_FILE_NAME;
                            URL url = new URL(workflowResultsBaclavaFileURL);

                            inputStream = url.openStream();
                            // Parse the result values from the downloaded Baclava file
                            // and save the file locally
                            parseBaclavaFile(inputStream, true, jobDir, null);
                        }
                        catch(Exception ex){
                            System.out.println("Workflow Results Portlet (update job status thread): An error occured while trying to download the XML Baclava file with outputs for job " + jobDir.getName() + " from the Server.");
                            ex.printStackTrace();
                        }
                    }
                }
                else{
                    // Update the job's status from the T2 Server
                    String statusOnServer = getWorkflowSubmissionJobStatusFromServer(jobDir.getName());
                    if (!statusOnServer.equals(statusOnDisk)){
                        if (statusOnServer.equals(Constants.UNKNOWN_RUN_UUID)){
                            updateJobStatusOnDiskForUser(jobDir.getName(), Constants.JOB_STATUS_EXPIRED, userDir.getName());
                            //System.out.println("Workflow Results Portlet (update job status thread): Updating status of job "+ jobDir.getName()+" from "+statusOnDisk+" to " + Constants.JOB_STATUS_EXPIRED);
                        }
                        else if (statusOnServer.equals(Constants.JOB_STATUS_FINISHED)){
                            updateJobStatusOnDiskForUser(jobDir.getName(), statusOnServer, userDir.getName());
                            //System.out.println("Workflow Results Portlet (update job status thread): Updating status of job "+ jobDir.getName()+" from "+statusOnDisk+" to " + statusOnServer);
                            // Download the results as well
                            try{
                                InputStream inputStream;
                                String workflowResultsBaclavaFileURL = T2_SERVER_URL + Constants.RUNS_URL + "/"+ jobDir.getName() + Constants.WD_URL + "/" + Constants.BACLAVA_OUTPUT_FILE_NAME;
                                URL url = new URL(workflowResultsBaclavaFileURL);

                                inputStream = url.openStream();
                                // Parse the result values from the downloaded Baclava file
                                // and save the file locally
                                parseBaclavaFile(inputStream, true, jobDir, null);
                            }
                            catch(Exception ex){
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
     * Saves a map of data objects for all workflow output ports
     * to individual files. Each port gets its own directory where
     * its data gets saved and all of these are contained in the
     * <job_directory>/outputs directory.
     */
    private void saveDataThingMapToDisk(Map<String, DataThing> resultDataThingMap, File jobDir){
        File outputsDir = new File(jobDir, Constants.OUTPUTS_DIRECTORY_NAME);
        try{
            if (!outputsDir.exists()){ // should not exist but hey
                outputsDir.mkdir();
            }
        }
        catch(Exception ex){// not fatal, so return the result map rather than null
            System.out.println("Workflow Results Portlet: Failed to create a directory "+outputsDir.getAbsolutePath()+" where individual values for all output ports are to be saved.");
            ex.printStackTrace();
        }

        for (String outputPortName : resultDataThingMap.keySet()){
            File outputPortDir = new File (outputsDir, outputPortName);
            try{
                if (!outputPortDir.exists()){// should not exist but hey
                    outputPortDir.mkdir();
                }
                int dataDepth = calculateDataDepth(resultDataThingMap.get(outputPortName).getDataObject());
                if (!saveResultsForPort(resultDataThingMap.get(outputPortName).getDataObject(), outputPortDir, dataDepth, dataDepth, "")){
                    System.out.println("Workflow Results Portlet: Failed to save individual output data item for output port "+outputPortName + " to " + outputPortDir.getAbsolutePath());
                }
            }
            catch(Exception ex){
                System.out.println("Workflow Results Portlet: Failed to create a directory "+outputPortDir.getAbsolutePath()+" where result value for output port "+outputPortName+" are to be saved.");
                ex.printStackTrace();
            }
        }
    }

    private boolean saveResultsForPort(Object dataObject, File parentDirectory, int maxDepth, int currentDepth, String parentIndex){

        boolean success = true;

        if (maxDepth == 0){ // Result data is a single item only
            return saveDataObjectToFile(new File(parentDirectory, "Value"), dataObject);
        }
        else{
            if (currentDepth == 0){ // A leaf in the tree
                return saveDataObjectToFile(new File(parentDirectory, "Value" + parentIndex), dataObject);
            }
            else{ // Result data is a list of (lists of ... ) items
                File currentDirectory;
                if (parentIndex.equals("")){
                    currentDirectory = new File(parentDirectory, "List");
                    try{
                        currentDirectory.mkdir();
                    }
                    catch(Exception ex){
                        System.out.println("Workflow Results Portlet: Failed to create a directory "+currentDirectory.getAbsolutePath());
                        ex.printStackTrace();
                        return false;
                    }
                }
                else{
                    currentDirectory = new File(parentDirectory, "List" + parentIndex);
                    try{
                        currentDirectory.mkdir();
                    }
                    catch(Exception ex){
                        System.out.println("Workflow Results Portlet: Failed to create a directory "+currentDirectory.getAbsolutePath());
                        ex.printStackTrace();
                        return false;
                    }
                }
                for (int i=0; i < ((Collection)dataObject).size(); i++){
                    String newParentIndex = parentIndex.equals("") ? (new Integer(i+1)).toString() : (parentIndex +"."+(i+1));
                    success = success && saveResultsForPort(((ArrayList)dataObject).get(i), currentDirectory, maxDepth, currentDepth - 1, newParentIndex);
                }
            }
        }
        return success;
    }

    private boolean saveDataObjectToFile(File file, Object dataObject){
        if (dataObject instanceof String){
            try{
                FileUtils.writeStringToFile(file, (String)dataObject, "UTF-8");
                return true;
            }
            catch(Exception ex){
                System.out.println("Workflow Results Portlet: Failed to save data object to " + file);
                ex.printStackTrace();
                return false;
            }
        }
        else if (dataObject instanceof byte[]){
            try{
                FileUtils.writeByteArrayToFile(file, (byte[])dataObject);
                return true;
            }
            catch(Exception ex){
                System.out.println("Workflow Results Portlet: Failed to save data object to " + file);
                ex.printStackTrace();
                return false;
            }
        }
        else{ // unrecognised data type
            return false;
        }
    }

    /*
     * Parse the input stream into a Baclava document and optionally save the
     * document to a disk.
     */
    private Map<String, DataThing> parseBaclavaFile(InputStream inputStream, boolean saveLocally, File jobDir, PortletRequest request){
 
        synchronized(resultsLock){
            Map<String, DataThing> resultDataThingMap = null;
            try{
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(inputStream);
                resultDataThingMap = DataThingXMLFactory.parseDataDocument(doc);
                if (saveLocally){ //Are we also saving the Baclava document locally?
                    try{
                        File outputsFile = new File (jobDir, Constants.OUTPUTS_BACLAVA_FILE);
                        FileUtils.writeStringToFile(outputsFile, new XMLOutputter().outputString(doc));
                        System.out.println("Workflow Results Portlet: Saved the XML Baclava file with outputs of job " + jobDir.getName() + " to " + outputsFile.getAbsolutePath());
                        // Also save the individual data objects to files in <job_directory>/outputs
                        saveDataThingMapToDisk(resultDataThingMap, jobDir);
                    }
                    catch(Exception ex){ // not fatal, so return the result map rather than null
                        System.out.println("Workflow Results Portlet: An error occured while trying to save the Baclava file with outputs to " + jobDir.getAbsolutePath() + Constants.FILE_SEPARATOR + Constants.OUTPUTS_BACLAVA_FILE);
                        ex.printStackTrace();
                        return resultDataThingMap;
                    }
                }
            }
            catch(Exception ex){
                System.out.println("Workflow Results Portlet: An error occured while trying to parse the XML Baclava file with outputs for job " + jobDir.getName() + ".");
                ex.printStackTrace();
                if (request != null){
                    request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to parse the results for job " + jobDir.getName() + ".");
                }
                return null;
            }
            finally{
                try{
                    inputStream.close();
                }
                catch(Exception ex2){
                    // Do nothing
                }
            }
            return resultDataThingMap;
        }
    }

    private void deleteJobForUser(String jobId, PortletRequest request){
        // Get currently logged in user
        String user = (String)request.getPortletSession().
                                            getAttribute(Constants.USER,
                                            PortletSession.APPLICATION_SCOPE);
        File dirToDelete = new File(JOBS_DIR, user + Constants.FILE_SEPARATOR + jobId);
        try{
            FileUtils.deleteDirectory(dirToDelete);
        }
        catch(Exception ex){
            System.out.println("Workflow Results Portlet: An error occured while trying to delete job " + jobId + ".");
            ex.printStackTrace();
            request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to delete job " + jobId + ".");
        }
    }

    /*
     * Return the body of the HTTP response as a String.
     * From Sergejs Aleksejevs' Taverna REST plugin.
     */
    private static String readResponseBodyAsString(HttpEntity entity) throws IOException
    {
        // Get charset name. Use UTF-8 if not defined.
        String charset = "UTF-8";
        String contentType = entity.getContentType().getValue().toLowerCase();

        String[] contentTypeParts = contentType.split(";");
        for (String contentTypePart : contentTypeParts)
        {
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

        if (dataObject instanceof Collection<?>){
            if (((Collection<?>)dataObject).isEmpty()){
                    return 1;
            }
            else{
                    // Calculate the depth of the first element in collection + 1
                    return calculateDataDepth(((Collection<?>)dataObject).iterator().next()) + 1;
            }
        }
        else{
            return 0;
        }
    }

}
