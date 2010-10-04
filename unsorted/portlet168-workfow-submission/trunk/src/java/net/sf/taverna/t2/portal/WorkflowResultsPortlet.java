/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.portal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
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

/**
 * Workflow Results Portlet - enables user to view status of
 * workflow submission jobs submitted to the T2 Server and
 * fetch results of finished jobs.
 *
 * @author Alex Nenadic
 */
public class WorkflowResultsPortlet extends GenericPortlet{

    // Address of the T2 Server
    String t2ServerURL;

    // Directory where info for all submitted jobs for all users is persisted
    private File jobsDir;

    /*
     * Do the init stuff one at portlet loading time.
     */
    @Override
    public void init(){

        // Get the URL of the T2 Server defined in web.xml as an
        // app-wide init parameter ( <context-param> element)
        //t2ServerURL = getPortletConfig().getInitParameter(Constants.T2_SERVER_URL_PARAMETER); // portlet specific, defined in portlet.xml
        t2ServerURL = getPortletContext().getInitParameter(Constants.T2_SERVER_URL_PARAMETER);

        // Get the directory where info for submitted jobs for all users is persisted
        jobsDir = new File(getPortletContext().getInitParameter(Constants.JOBS_DIRECTORY_PATH),
                Constants.JOBS_DIRECTORY_NAME);
        if (!jobsDir.exists()){
            try{
                jobsDir.mkdir();
            }
            catch(Exception ex){
                System.out.println("Workflow Results Portlet: Failed to create a directory "+jobsDir.getAbsolutePath()+" where submitted jobs are to be persisted.");
                ex.printStackTrace();
            }
        }
        System.out.println("Workflow Results Portlet: Directory where jobs will be persisted set to " + jobsDir.getAbsolutePath());
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {

        ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)request.getPortletSession().
        getAttribute(Constants.WORKFLOW_JOBS_ATTRIBUTE,
        PortletSession.APPLICATION_SCOPE);

        // If there was a request to refresh the job statuses
        if (request.getParameter(Constants.REFRESH_WORKFLOW_JOBS) != null){
            refreshJobStatuses(request, workflowSubmissionJobs);
        }
        // If there was a request to show results of a workflow run
        else if (request.getParameter(Constants.FETCH_RESULTS) != null){

            // If workflowSubmissionJobs is null or does not contain this job ID
            // this is just a page refresh after redeployment of the app/restart of
            // the sever/some form or refresh while the URL parameter FETCH_RESULTS
            // managed to linger in the URL in the browser from the previous
            // session so just ignore it.
            if (workflowSubmissionJobs != null){
                String workflowResourceUUID = URLDecoder.decode(request.getParameterValues(Constants.FETCH_RESULTS)[0], "UTF-8");
                for (WorkflowSubmissionJob job : workflowSubmissionJobs){
                    if (job.getUuid().equals(workflowResourceUUID)){
                        System.out.println("Workflow Results Portlet: Fetching results for job ID " + workflowResourceUUID);

                        // See of results are already downloaded from the T2 Server -
                        // if not download and save the Baclava file with outputs now.
                        Map<String, DataThing> resultDataThingMap = fetchOutputBaclavaFile(job, request);
                        request.setAttribute(Constants.OUTPUTS_MAP_ATTRIBUTE, resultDataThingMap);
                        request.setAttribute(Constants.WORKFLOW_SUBMISSION_JOB_ATTRIBUTE, job);
                        break; 
                    } // else just ignore it if it is not in the job ID list
                }
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
            response.getWriter().println("<br/>");
            response.getWriter().println("<hr/>");
            response.getWriter().println("<br/>");
        }
        if (request.getAttribute(Constants.INFO_MESSAGE) != null){
            response.getWriter().println("<p><b>"+ request.getAttribute(Constants.INFO_MESSAGE)+ "</b></p>\n");
            response.getWriter().println("<br/>");
            response.getWriter().println("<hr/>");
            response.getWriter().println("<br/>");
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
                                            getAttribute(Constants.WORKFLOW_JOBS_ATTRIBUTE,
                                            PortletSession.APPLICATION_SCOPE);
        if (workflowSubmissionJobs == null){ // load user's jobs from disk
            workflowSubmissionJobs = loadWorkflowSubmissionJobs(jobsDir, user);
            request.getPortletSession().setAttribute(Constants.WORKFLOW_JOBS_ATTRIBUTE,
                    workflowSubmissionJobs,
                    PortletSession.APPLICATION_SCOPE);
        }
        
        // Refresh job statuses if there is a job that has not finished yet
        refreshJobStatuses(request, workflowSubmissionJobs);
                
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_view.jsp");
        dispatcher.include(request, response);

        // If there was a request to show results of a workflow run
        if (request.getParameter(Constants.FETCH_RESULTS) != null){
            
            // But if workflowSubmissionJobs is null or does not contain this job ID
            // this is just a page refresh after redeployment of the app/restart of
            // the sever/some form or refresh while the URL parameter FETCH_RESULTS
            // managed to linger in the URL in the browser from the previous
            // session so just ignore it. In this case the map of result DataThings
            // will be null so just ignore this request. Also if something went wrong
            // with fetching the outputs - the map will be null so do not show anything -
            // an error message will be displayed to the user.
            Map<String, DataThing> resultDataThingMap = (Map<String, DataThing>)request.getAttribute(Constants.OUTPUTS_MAP_ATTRIBUTE);
            if (resultDataThingMap != null){
                String workflowResourceUUID = URLDecoder.decode(request.getParameterValues(Constants.FETCH_RESULTS)[0], "UTF-8");
                WorkflowSubmissionJob job = (WorkflowSubmissionJob)request.getAttribute(Constants.WORKFLOW_SUBMISSION_JOB_ATTRIBUTE);

                response.getWriter().println("<br/>\n");
                response.getWriter().println("<hr/>\n");
                response.getWriter().println("<br/>\n");

                // Parse the result values from the Baclava file
                StringBuffer outputsTableHTML = new StringBuffer();
                response.getWriter().println("<b>Job ID: " + job.getUuid() + "</b><br/>\n");
                response.getWriter().println("<b>Workflow: " + job.getWorkflowFileName() + "</b><br/><br/>\n");

                outputsTableHTML.append("<b>Results:</b><br/>\n");
                outputsTableHTML.append("<table class=\"results\">\n");
                outputsTableHTML.append("<tr>\n");
                outputsTableHTML.append("<th width=\"25%\">Output port</th>\n");
                outputsTableHTML.append("<th>Data</th>\n");
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
                        rowCount++;
                        String dataTypeBasedOnDepth;
                        if (dataDepth==0){
                            dataTypeBasedOnDepth = "single value";
                        }
                        else{
                            dataTypeBasedOnDepth = "list of depth " + dataDepth;
                        }
                        // Get data's MIME type as given by the Baclava file
                        String mimeType = resultDataThing.getMostInterestingMIMETypeForObject(dataObject);
                        outputsTableHTML.append("<td width=\"25%\">\n");
                        outputsTableHTML.append("<div class=\"output_name\">" + outputPortName + "<span class=\"output_depth\"> - " + dataTypeBasedOnDepth + "</span></div>\n");
                        outputsTableHTML.append("<div class=\"output_mime_type\">" + mimeType + "</div>\n");
                        outputsTableHTML.append("</td>");
                        // Create result tree
                        outputsTableHTML.append("<td><script language=\"javascript\">" + createResultTree(dataObject, dataDepth, dataDepth, -1, "") + "</script></td>\n");
                        outputsTableHTML.append("</tr>\n");
                }
                outputsTableHTML.append("</table>\n");
                outputsTableHTML.append("</br>\n");
                response.getWriter().println(outputsTableHTML.toString());

                response.getWriter().println("Download the results as a <a target=\"_blank\" href=\"" + workflowResourceUUID + "\">single XML file</a>. " +
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
     * Fetch status of a submitted job from a T2 Server.
     */
    private String getWorkflowSubmissionJobStatus(WorkflowSubmissionJob workflowSubmissionJob){

        HttpClient httpClient = new DefaultHttpClient();
        String statusURL = t2ServerURL + Constants.RUNS_URL + "/" + workflowSubmissionJob.getUuid() + Constants.STATUS_URL;

        HttpGet httpGet = new HttpGet(statusURL);

        HttpResponse httpResponse = null;
        try{
            // Execute the request
            HttpContext localContext = new BasicHttpContext();
            httpResponse = httpClient.execute(httpGet, localContext);

            // Release resource
            httpClient.getConnectionManager().shutdown();

            if (httpResponse.getStatusLine().getStatusCode() == 403){ // HTTP/1.1 403 Forbidden
                System.out.println("Workflow Results Portlet: Job " +workflowSubmissionJob.getUuid()+
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
                         System.out.println("Workflow Results Portlet: Status of job " +workflowSubmissionJob.getUuid() + " is '" + value + "'.");
                    }
                    else{
                        System.out.println("Workflow Results Portlet: Server's response not text/plain for status of job " +workflowSubmissionJob.getUuid());
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
               System.out.println("Workflow Results Portlet: Failed to get the status for job " + workflowSubmissionJob.getUuid() + ". The Server responded with: " + httpResponse.getStatusLine()+".");
               return "Failed to get the status for job. The Server responnded with: " + httpResponse.getStatusLine() + ".";
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Results Portlet: An error occured while trying to get the status for job " + workflowSubmissionJob.getUuid() + ".");
            ex.printStackTrace();
            return "An error occured while trying to get the status for job.";
        }
    }

    /*
     * Return the body of the HTTP response as a String.
     * From Sergejs Aleksejevs Taverna REST plugin.
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
    private int calculateDataDepth(Object dataObject) {

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

    /*
     * Create a result tree in JavaScript for a result data item.
     */
    private String createResultTree(Object dataObject, int maxDepth, int currentDepth, int index, String parentIndex){

        StringBuffer resultTreeHTML = new StringBuffer();

        if (maxDepth == 0){ // Result data is a single item only
            resultTreeHTML.append("addNode(\"Value\", \"\", \"_blank\");\n");
        }
        else{
            if (currentDepth == 0){ // A leaf in the tree
                resultTreeHTML.append("addNode(\"Value " + parentIndex + "\", \"\", \"_blank\");\n");
            }
            else{ // Result data is a list of (lists of ... ) items
                resultTreeHTML.append("startParentNode(\"List " + parentIndex +"\");\n");
                for (int i=0; i < ((Collection)dataObject).size(); i++){
                    String newParentIndex = parentIndex.equals("") ? (new Integer(i+1)).toString() : (parentIndex +"."+(i+1));
                    resultTreeHTML.append(createResultTree(((ArrayList)dataObject).get(i), maxDepth, currentDepth - 1, i, newParentIndex));
                }
                resultTreeHTML.append("endParentNode();\n");
            }
        }
        return resultTreeHTML.toString();
    }

    /*
     * Loads and returns all saved jobs for a user from a disk.
     */
    private ArrayList<WorkflowSubmissionJob> loadWorkflowSubmissionJobs(File jobsDir, String user){

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


    // This file filter only returns directories
    FileFilter dirFilter = new FileFilter() {
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
     * Fetches job statuses from a T2 Server for all jobs that
     * have not already finished.
     */
    private void refreshJobStatuses(PortletRequest request, ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs){

        for (int i = workflowSubmissionJobs.size()-1; i>=0; i--){

            WorkflowSubmissionJob job = workflowSubmissionJobs.get(i);

            if (!job.getStatus().equals(Constants.JOB_STATUS_FINISHED)){
                // Get the updated the job's status from the T2 Server
                String status = getWorkflowSubmissionJobStatus(job);
                // If the job is not available on the Server any more - set its status to "Expired"
                if (status.equals(Constants.UNKNOWN_RUN_UUID)){
                    job.setStatus(Constants.JOB_STATUS_EXPIRED);
                }
                else{
                    if (status.equals(Constants.JOB_STATUS_FINISHED)){
                        job.setStatus(status);
                        // persist the new status for this job on a disk
                        updateJobStatusOnDisk(job, status, request);
                    }
                }
            }
        }

        request.getPortletSession().
                setAttribute(Constants.WORKFLOW_JOBS_ATTRIBUTE,
                workflowSubmissionJobs,
                PortletSession.APPLICATION_SCOPE);
    }

    /*
     * Update job's status as persisted on a local disk.
     * Job's status is saved in a file with name <JOB_STATUS>.status in
     * a directory for that job (named after the job's UUID) and the owning user.
     */
    private void updateJobStatusOnDisk(WorkflowSubmissionJob job, String newStatus, PortletRequest request){

        // Get the current user
        String user = (String)request.getPortletSession().
                                    getAttribute(Constants.USER,
                                    PortletSession.APPLICATION_SCOPE);

        File userDir = new File (jobsDir, user);
        File[] userJobsDir = userDir.listFiles(dirFilter);

        String oldStatus = null;
        for (File jobDir : userJobsDir){
            if (jobDir.getName().equals(job.getUuid())){
                String statusFileName = jobDir.list(statusFileFilter)[0]; // should be only 1 element or else we are in trouble
                oldStatus = statusFileName.substring(0, statusFileName.indexOf(Constants.STATUS_FILE_EXT));
                File statusFile = new File(jobDir, statusFileName);
                statusFile.renameTo(new File(jobDir, newStatus + Constants.STATUS_FILE_EXT));
                System.out.println("Workflow Results Portlet: Updated status for job " + jobDir.getAbsolutePath() + " from '" + oldStatus + "' to '" + newStatus + "'.");
            }
        }
    }

    /*
     * 
     */
    private Map<String, DataThing> fetchOutputBaclavaFile(WorkflowSubmissionJob job, PortletRequest request){

        String workflowResultsBaclavaFileURL = t2ServerURL + Constants.RUNS_URL + "/"+ job.getUuid() + Constants.WD_URL + "/" + Constants.BACLAVA_OUTPUT_FILE_NAME;
        request.setAttribute(Constants.WORKFLOW_RESULTS_BACLAVA_FILE_URL_ATTRIBUTE, workflowResultsBaclavaFileURL);

        // First try to get the Baclava file from the local disk
        // Get the current user
        String user = (String)request.getPortletSession().
                                    getAttribute(Constants.USER,
                                    PortletSession.APPLICATION_SCOPE);

        InputStream is = null;
        File userDir = new File (jobsDir, user);
        File[] userJobsDir = userDir.listFiles(dirFilter);
        for (File jobDir : userJobsDir){
            if (jobDir.getName().equals(job.getUuid())){
                String[] outputsBaclavaFiles = jobDir.list(outputsBaclavaFileFilter);
                if (outputsBaclavaFiles.length == 0){ // no such file on local disk - download the file from T2 Server
                    try{
                        System.out.println("Workflow Results Portlet: Downloading results to local disk for from T2 Server at " + workflowResultsBaclavaFileURL);
                        URL url = new URL(workflowResultsBaclavaFileURL);
                        InputStream is2 = url.openStream();
                        // Save the file locally
                        File workflowResultsBaclavaFile  = new File (jobDir , Constants.OUTPUTS_BACLAVA_FILE);
                        OutputStream os = new FileOutputStream(workflowResultsBaclavaFile);
                        byte[] theBytes;
                        try{
                            theBytes = new byte[is2.available()];
                            is2.read(theBytes);
                            os.write(theBytes);
                        }
                        catch(Exception ex){
                            System.out.println("Workflow Results Portlet: Failed to save the downloaded Baclava outputs file for job " + job.getUuid() + " to " + workflowResultsBaclavaFile.getAbsolutePath());
                            ex.printStackTrace();
                            // Continue - even though we could not save the file we still have it
                        }
                        finally{
                            try{
                                is2.close();
                            }
                            catch(Exception ex){
                                // Do nothing
                            }
                            try{
                                os.close();
                            }
                            catch(Exception ex){
                                // Do nothing
                            }
                        }
                    }
                    catch(Exception ex){
                        System.out.println("Workflow Results Portlet: An error occured while trying to download the XML Baclava file with outputs for job " + job.getUuid() + " from the Server.");
                        ex.printStackTrace();
                        request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to download the results for job " + job.getUuid() + " from the Server.<br/>" + ex.getMessage());
                        return null;
                    }
                }
                try{ // We should have the file by now - if it wasn't there already we should have just downloaded it
                    File workflowResultsBaclavaFile  = new File (jobDir, Constants.OUTPUTS_BACLAVA_FILE);
                    System.out.println("Workflow Results Portlet: Fetching results for from local disk " + workflowResultsBaclavaFile.getAbsolutePath());
                    is = new FileInputStream(workflowResultsBaclavaFile);
                }
                catch(Exception ex){
                    System.out.println("Workflow Results Portlet: An error occured while trying to open the XML Baclava file with outputs for job " + job.getUuid() + ".");
                    ex.printStackTrace();
                    request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to open file with results for job " + job.getUuid() + ".<br/>" + ex.getMessage());
                    return null;
                }
 
            }
        }
        Map<String, DataThing> resultDataThingMap = null;
        // Parse the result values from the Baclava file
        try{
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(is);
            resultDataThingMap = DataThingXMLFactory.parseDataDocument(doc);
        }
        catch(Exception ex){
            System.out.println("Workflow Results Portlet: An error occured while trying to parse the XML Baclava file with outputs for job " + job.getUuid() + ".");
            ex.printStackTrace();
            request.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to parse the results for job " + job.getUuid() + ".");
            return null;
        }
        finally{
            try{
                is.close();
            }
            catch(Exception ex2){
                // Do nothing
            }
        }
        return resultDataThingMap;
    }
}
