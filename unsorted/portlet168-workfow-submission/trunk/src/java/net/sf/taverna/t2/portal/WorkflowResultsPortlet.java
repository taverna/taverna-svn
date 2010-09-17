/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.portal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
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

    /*
     * Do the init stuff one at portlet loading time.
     */
    @Override
    public void init(){

        // Get the URL of the T2 Server defined in web.xml as an
        // app-wide init parameter ( <context-param> element)
        //t2ServerURL = getPortletConfig().getInitParameter(Constants.T2_SERVER_URL_PARAMETER); // portlet specific, defined in portlet.xml
        t2ServerURL = getPortletContext().getInitParameter(Constants.T2_SERVER_URL_PARAMETER);
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {

        // If there was a request to refresh the job ID status table
        if (request.getParameter(Constants.REFRESH_WORKFLOW_JOB_UUIDS) != null){
            ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)request.getPortletSession().
                    getAttribute(Constants.WORKFLOW_JOB_UUIDS_PORTLET_ATTRIBUTE, PortletSession.APPLICATION_SCOPE);

            if (workflowSubmissionJobs != null){
                for (int i = workflowSubmissionJobs.size()-1; i>=0; i--){

                    WorkflowSubmissionJob job = workflowSubmissionJobs.get(i);

                    // Get the updated the job's status from the T2 Server
                    String status = getWorkflowSubmissionJobStatus(job);

                    // If the job is not available on the Server any more - remove it
                    if (status.equals(Constants.UNKNOWN_RUN_UUID)){
                        workflowSubmissionJobs.remove(i);
                    }
                    else{
                        job.setStatus(status);
                    }
                }
            }
            request.getPortletSession().
                    setAttribute(Constants.WORKFLOW_JOB_UUIDS_PORTLET_ATTRIBUTE, workflowSubmissionJobs, PortletSession.APPLICATION_SCOPE);
        }
        // If there was a request to show results of a workflow run
        else if (request.getParameter(Constants.FETCH_RESULTS) != null){

            // But if workflowSubmissionJobs is null or does not contain this job ID
            // this is just a page refresh after redeployment of the app/restart of
            // the sever/some form or refresh while the URL parameter FETCH_RESULTS
            // managed to linger in the URL in the browser from the previous
            // session so just ignore it.
            ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)request.getPortletSession().
                    getAttribute(Constants.WORKFLOW_JOB_UUIDS_PORTLET_ATTRIBUTE, PortletSession.APPLICATION_SCOPE);

            if (workflowSubmissionJobs != null){
                String workflowResourceUUID = URLDecoder.decode(request.getParameterValues(Constants.FETCH_RESULTS)[0], "UTF-8");
                for (WorkflowSubmissionJob job : workflowSubmissionJobs){
                    if (job.getUuid().equals(workflowResourceUUID)){
                        System.out.println("Workflow Submission Portlet: Fetching results for job ID " + workflowResourceUUID);
                        break; 
                    } // else just ignore it if it is not in the job ID list
                }
            }
        }

        // Pass all request parameters over to the doView() and other render stage methods
        response.setRenderParameters(request.getParameterMap());

    }

    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {
        
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
            ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)request.getPortletSession().
                    getAttribute(Constants.WORKFLOW_JOB_UUIDS_PORTLET_ATTRIBUTE, PortletSession.APPLICATION_SCOPE);

            if (workflowSubmissionJobs != null){
                String workflowResourceUUID = URLDecoder.decode(request.getParameterValues(Constants.FETCH_RESULTS)[0], "UTF-8");
                for (WorkflowSubmissionJob job : workflowSubmissionJobs){
                    if (job.getUuid().equals(workflowResourceUUID)){
                        String workflowBaclavaOutputURL = t2ServerURL + Constants.RUNS_URL + "/"+ workflowResourceUUID + Constants.WD_URL + "/" + Constants.BACLAVA_OUTPUT_FILE_NAME;

                        response.getWriter().println("<br />");
                        response.getWriter().println("<hr />");
                        response.getWriter().println("<br />");

                        request.setAttribute(Constants.WORKFLOW_BACLAVA_OUTPUT_URL_ATTRIBUTE, workflowBaclavaOutputURL);
                        request.setAttribute(Constants.WORKFLOW_SUBMISSION_JOB_ATTRIBUTE, job);

                        dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/IndividualWorkflowResults.jsp");
                        dispatcher.include(request, response);

                        break;
                    } // else just ignore it if it is not in the job ID list
                }
            }
        }
    }

    public void doEdit(RenderRequest request,RenderResponse response) throws PortletException,IOException {
            response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_edit.jsp");
        dispatcher.include(request, response);
    }

    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_help.jsp");
        dispatcher.include(request, response);
    }

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
                System.out.println("Workflow Submission Portlet: Job " +workflowSubmissionJob.getUuid()+
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
                         System.out.println("Workflow Submission Portlet: Status of job " +workflowSubmissionJob.getUuid() + " " + value);
                    }
                    else{
                        System.out.println("Workflow Submission Portlet: Server's response not text/plain for status of job " +workflowSubmissionJob.getUuid());
                    }
                }
                catch(Exception ex){
                    System.out.println("Workflow Submission Portlet: Failed to get the content of the job status respose from the Server.");
                    ex.printStackTrace();
                    return "Failed to get the content of the job status respose from the Server";
                }
                return value;
            }
            else {
               System.out.println("Workflow Submission Portlet: Failed to get the status for job " + workflowSubmissionJob.getUuid() + ". The Server responded with: " + httpResponse.getStatusLine()+".");
               return "Failed to get the status for job. The Server responnded with: " + httpResponse.getStatusLine() + ".";
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: An error occured while trying to get the status for job " + workflowSubmissionJob.getUuid() + ".");
            ex.printStackTrace();
            return "An error occured while trying to get the status for job.";
        }
    }

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

}
