/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.portal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    public static final String REFRESH_WORKFLOW_JOB_UUIDS = "refresh_workflow_job_uuids";

    // Address of the T2 Server
    String t2ServerURL;

    /*
     * Do the init stuff one at portlet loading time.
     */
    @Override
    public void init(){

        // Get the URL of the T2 Server
        t2ServerURL = getPortletConfig().getInitParameter(Constants.T2_SERVER_URL_PARAMETER);
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {

        if (request.getParameter(REFRESH_WORKFLOW_JOB_UUIDS) != null){
            ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)request.getPortletSession().
                    getAttribute(Constants.WORKFLOW_JOB_UUIDS_PORTLET_ATTRIBUTE, PortletSession.APPLICATION_SCOPE);

            if (workflowSubmissionJobs != null){
                for (WorkflowSubmissionJob job : workflowSubmissionJobs){
                    // Get the updated the job's status from the T2 Server
                    String status = getWorkflowSubmissionJobStatus(job);
                    job.setStatus(status);
                }
            }

            request.getPortletSession().
                    setAttribute(Constants.WORKFLOW_JOB_UUIDS_PORTLET_ATTRIBUTE, workflowSubmissionJobs, PortletSession.APPLICATION_SCOPE);

        }

        // Pass all request parameters over to the doView() and other render stage methods
        response.setRenderParameters(request.getParameterMap());

    }

    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowResults_view.jsp");
        dispatcher.include(request, response);
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

            if (httpResponse.getStatusLine().getStatusCode() != 200){ // HTTP/1.1 200 OK
               System.out.println("Workflow Submission Portlet: Failed to get the status for job " + workflowSubmissionJob.getUuid() + ". The Server responded with: " + httpResponse.getStatusLine()+".");
               return "Failed to get the job's status. The Server responnded with: " + httpResponse.getStatusLine() + ".";
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: An error occured while trying to get the status for job " + workflowSubmissionJob.getUuid() + ".");
            ex.printStackTrace();
            return "An error occured while trying to get the status for job.";
        }

        HttpEntity httpEntity = httpResponse.getEntity();

        String value = null;
        String contentType = httpEntity.getContentType().getValue().toLowerCase();

        try{
            if (contentType.startsWith("text")) {
                // read as text
                value = readResponseBodyAsString(httpEntity);
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to get the content of the job status respose from the Server.");
            ex.printStackTrace();
            return "Failed to get the content of the job status respose from the Server";
        }

        return value;
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
