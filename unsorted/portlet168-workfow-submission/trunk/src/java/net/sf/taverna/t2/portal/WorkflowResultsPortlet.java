/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.portal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
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
                
        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_view.jsp");
        dispatcher.include(request, response);

        // If there was a request to show results of a workflow run
        if (request.getParameter(Constants.FETCH_RESULTS) != null){
            
            // But if workflowSubmissionJobs is null or does not contain this job ID
            // this is just a page refresh after redeployment of the app/restart of
            // the sever/some form or refresh while the URL parameter FETCH_RESULTS
            // managed to linger in the URL in the browser from the previous
            // session so just ignore it.         
            if (workflowSubmissionJobs != null){
                String workflowResourceUUID = URLDecoder.decode(request.getParameterValues(Constants.FETCH_RESULTS)[0], "UTF-8");
                for (WorkflowSubmissionJob job : workflowSubmissionJobs){
                    if (job.getUuid().equals(workflowResourceUUID)){
                        String workflowResultsBaclavaFileURL = t2ServerURL + Constants.RUNS_URL + "/"+ workflowResourceUUID + Constants.WD_URL + "/" + Constants.BACLAVA_OUTPUT_FILE_NAME;

                        response.getWriter().println("<br />");
                        response.getWriter().println("<hr />");
                        response.getWriter().println("<br />");

                        request.setAttribute(Constants.WORKFLOW_RESULTS_BACLAVA_FILE_URL_ATTRIBUTE, workflowResultsBaclavaFileURL);
                        request.setAttribute(Constants.WORKFLOW_SUBMISSION_JOB_ATTRIBUTE, job);

                        //dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_Baclava.jsp");
                        //dispatcher.include(request, response);

                        // Parse the result values from the Baclava file
                        StringBuffer outputsTableHTML = new StringBuffer();
                        response.getWriter().println("<b>Workflow: " + job.getWorkflowFileName() + "</b><br/>");
                        response.getWriter().println("<b>Job ID: " + job.getUuid() + "</b><br/><br/>");
                        try{
                            URL url = new URL(workflowResultsBaclavaFileURL);
                            InputStream is = url.openStream();
                            SAXBuilder builder = new SAXBuilder();
                            Document doc = builder.build(is);
                            Map<String, DataThing> resultDataThingMap = DataThingXMLFactory.parseDataDocument(doc);

                            outputsTableHTML.append("<b>Results:</b><br/>");
                            outputsTableHTML.append("<table class=\"jobs\">");
                            outputsTableHTML.append("<tr>");
                            outputsTableHTML.append("<th width=\"25%\">Output port</th>");
                            outputsTableHTML.append("<th>Data</th>");
                            outputsTableHTML.append("</tr>");
                            // Get all output ports and data associated with them
                            for (Iterator i = resultDataThingMap.keySet().iterator(); i.hasNext();) {
                                    String outputPortName = (String) i.next();
                                    DataThing resultDataThing = resultDataThingMap.get(outputPortName);

                                    // Calculate the depth of the result data for the port
                                    Object dataObject = resultDataThing.getDataObject();
                                    int dataDepth = calculateDataDepth(dataObject);
                                    outputsTableHTML.append("<tr");
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
                            }
                            outputsTableHTML.append("</table>\n");
                            outputsTableHTML.append("</br>\n");
                            response.getWriter().println(outputsTableHTML.toString());
                        }
                        catch(Exception ex){
                            System.out.println("Failed to fetch/parse Baclava file from " + workflowResultsBaclavaFileURL);
                            ex.printStackTrace();
                            response.getWriter().println("<p style=\"color:red;\"><b>There was an error with parsing results.</b></p><br/><br/>");
                        }
                        finally{
                            response.getWriter().println("Download the results as a <a target=\"_blank\" href=\"" + workflowResultsBaclavaFileURL + "\">single XML file</a>. " +
                                    "You can view the file with Taverna's DataViewer tool.");
                        }

                        break;
                    } // else just ignore it if it is not in the job ID list
                }
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
            resultTreeHTML.append("addNode(\"Value\", \"\", \"_blank\")\n");
        }
        else{
            if (currentDepth == 0){ // A leaf in the tree
                resultTreeHTML.append("addNode(\"Value " + parentIndex + "\", \"\", \"_blank\")\n");
            }
            else{ // Result data is a list of (lists of ... ) items
                resultTreeHTML.append("startParentNode(\"List " + parentIndex +"\")\n");
                for (int i=0; i < ((Collection)dataObject).size(); i++){
                    String newParentIndex = parentIndex.equals("") ? (new Integer(i+1)).toString() : (parentIndex +"."+(i+1));
                    resultTreeHTML.append(createResultTree(((ArrayList)dataObject).get(i), maxDepth, currentDepth - 1, i, newParentIndex));
                }
                resultTreeHTML.append("endParentNode()\n");
            }
        }
        return resultTreeHTML.toString();
    }

    /*
     * Loads and returns all saved jobs for a user from a disk.
     */
    private ArrayList<WorkflowSubmissionJob> loadWorkflowSubmissionJobs(File jobsDir, String user){

        ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = new ArrayList<WorkflowSubmissionJob>();
        try{
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

                String workflowFileNameWithExtension = jobDir.list(t2flowFileFilter)[0]; // should be only 1 element or else we are in trouble
                String workflowFileName = workflowFileNameWithExtension.substring(0, workflowFileNameWithExtension.indexOf(Constants.T2_FLOW_FILE_EXT));

                String statusFileName = jobDir.list(statusFileFilter)[0]; // should be only 1 element or else we are in trouble
                String status = statusFileName.substring(0, statusFileName.indexOf(Constants.STATUS_FILE_EXT));

                WorkflowSubmissionJob workflowSubmissionJob =  new WorkflowSubmissionJob(uuid, workflowFileName, status);
                workflowSubmissionJobs.add(workflowSubmissionJob);

                System.out.println("Workflow Results Portlet: Found job: " + uuid + " " + workflowFileName + " " + status);
            }
        }
        catch(Exception ex){
           System.out.println("Workflow Results Portlet: Failed to load previously submitted jobs from " + jobsDir.getAbsolutePath());
           ex.printStackTrace();
        }

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
}
