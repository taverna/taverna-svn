<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp --%>
<%@ page import="javax.portlet.*"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ page import="net.sf.taverna.t2.portal.WorkflowSubmissionJob" %>
<%@ page import="net.sf.taverna.t2.portal.Constants" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%-- Include the styling CSS for the inputs, jobs and results tables --%>
<%@ include file="ResultsCSS.jsp" %>
<%@ include file="InputsCSS.jsp" %>

<%-- Include the JavaScript for creating the results data table as a tree --%>
<%@ include file="ResultsTreeJavaScript.jsp" %>
<%-- Include the JavaScript for confirming job deletion --%>
<%@ include file="ConfirmJobDeletionJavaScript.jsp" %>
<%-- Include the JavaScript for dynamic loading of data files in the result preview --%>
<%@ include file="AjaxDataPreviewJavaScript.jsp" %>

<%
// List of UUIDs of workflows submitted to the T2 Server.
ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)renderRequest.
        getPortletSession().
        getAttribute(Constants.WORKFLOW_SUBMISSION_JOBS, PortletSession.APPLICATION_SCOPE);

SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

%>

<%-- Print out a message to the user, if any --%>

<%-- Form for selecting the workflow job to show results for --%>
<% if (workflowSubmissionJobs != null && !workflowSubmissionJobs.isEmpty()){ %>
<p>
    <b>
        To view results of a workflow run, click on the workflow run id once the workflow run status becomes "Finished".<br>
        To refresh workflow run statuses, click on the "Refresh" button.
    </b>
</p>
<table width="100%" style="margin-bottom:3px;">
    <tr>
        <td valign="bottom">
            <b>Workflow runs:</b>
        </td>
        <td align="right">
        <form action="<portlet:actionURL/>" method="post">
        <input type="submit" name="<portlet:namespace/><%= Constants.REFRESH_WORKFLOW_JOBS %>" value="Refresh">
        </form>
        </td>
    </tr>
</table>
<%--<div style="float:right; padding:0px 0px 5px 5px;">
    <form action="<portlet:actionURL/>" method="post">
        <input type="submit" name="<portlet:namespace/><%= Constants.REFRESH_WORKFLOW_JOBS %>" value="Refresh">
    </form>
</div>
<div style="float:left; margin-bottom:3px;"><b>Workflow submission jobs:</b></div>--%>
<table class="jobs">
    <tr>
        <th>Workflow run id</th>
        <th>Workflow run description</th>
        <th>Workflow name</th>
        <th>Status</th>
        <th>Start time</th>
        <th>End time</th>
        <th>Delete run</th>
    </tr>
    <%
    for (int i=0; i< workflowSubmissionJobs.size(); i++ ) {
        String confirmDeletionText = "Are you sure you want to delete the run "+ workflowSubmissionJobs.get(i).getUuid()+ "?";

        if (i % 2 == 0){%>
            <tr>
        <%}
        else{%>
            <tr style="background-color: #EFF5FB;">
        <%}%>
        <%
        if(!workflowSubmissionJobs.get(i).getStatus().equals(Constants.JOB_STATUS_FINISHED)){%>
            <td><%= workflowSubmissionJobs.get(i).getUuid() %></td>
        <%}
        else {%>
            <td><a href="<portlet:actionURL/>&<%=Constants.FETCH_RESULTS%>=<%= URLEncoder.encode(workflowSubmissionJobs.get(i).getUuid(), "UTF-8")%>#<%=Constants.RESULTS_ANCHOR%>"><%= workflowSubmissionJobs.get(i).getUuid() %></a></td>
        <%}%>
        <td><%= workflowSubmissionJobs.get(i).getWorkflowRunDescription() %></td>
        <td><%= (workflowSubmissionJobs.get(i).getWorkflow().getFileName()!=null) ? workflowSubmissionJobs.get(i).getWorkflow().getFileName() : "<a target=\"blank\" href=\""+workflowSubmissionJobs.get(i).getWorkflow().getMyExperimentWorkflowResource()+"?version="+workflowSubmissionJobs.get(i).getWorkflow().getMyExperimentWorkflowVersion()+"\">"+workflowSubmissionJobs.get(i).getWorkflow().getMyExperimentWorkflowResource()+"</a>" %></td>
        <td><%= workflowSubmissionJobs.get(i).getStatus() %></td>
        <td><%= dateFormat.format(workflowSubmissionJobs.get(i).getStartDate()) %></td>

        <%
        if(workflowSubmissionJobs.get(i).getEndDate()==null){%>
            <td>N/A</td>
        <%}
        else {%>
            <td><%= dateFormat.format(workflowSubmissionJobs.get(i).getEndDate())%></td>
        <%}%>

        <td><a onclick="return confirm_deletion('<%=confirmDeletionText%>');" href="<portlet:actionURL/>&<%=Constants.DELETE_JOB%>=<%= URLEncoder.encode(workflowSubmissionJobs.get(i).getUuid(), "UTF-8")%>"><img src="<%=renderRequest.getContextPath()%>/images/trash.png" alt="Delete job"></a></td>
        </tr>
    <%}%>
</table>
<%
} else{
%>
<span class="portlet-msg-info">Currently there are no workflows submitted for execution.</span>
<%
}
%>
