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

<%-- Include the styling CSS --%>
<%@ include file="CommonCSS.jsp" %>

<%
// List of UUIDs of workflows submitted to the T2 Server.
ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)renderRequest.
        getPortletSession().
        getAttribute(Constants.WORKFLOW_JOB_UUIDS_PORTLET_ATTRIBUTE, PortletSession.APPLICATION_SCOPE);

%>

<%-- Print out a message to the user, if any --%>

<%-- Form for selecting the workflow to be sent for execution --%>
<% if (workflowSubmissionJobs != null && !workflowSubmissionJobs.isEmpty()){ %>
<b>To view results of a workflow submission job, click on the job ID, once its status becomes "Finished".</b><br/>
<b>To refresh job statuses, click on the "Refresh" button.</b><br/><br/>
<table width="100%">
    <tr>
        <td>
            <b> Workflow submission jobs:</b>
        </td>
        <td align="right">
        <form action="<portlet:actionURL/>" method="post">
        <input type="submit" name="<portlet:namespace/><%= Constants.REFRESH_WORKFLOW_JOB_UUIDS %>" value="Refresh">
        </form>
        </td>
    </tr>
</table>
<br/>
<form>
<table class="results" width="100%">
    <tr>
        <th>Workflow name</th>
        <th>Job ID</th>
        <th>Status</th>
    </tr>
    <%
    for (int i=0; i< workflowSubmissionJobs.size(); i++ ) {
        if (i % 2 == 0){%>
            <tr>
        <%}
        else{%>
            <tr bgcolor="#EFF5FB">
        <%}%>
                <td><%= workflowSubmissionJobs.get(i).getWorkflowFileName() %></td>
                <%
                if(!workflowSubmissionJobs.get(i).getStatus().equals(Constants.JOB_STATUS_FINISHED)){%>
                    <td><%= workflowSubmissionJobs.get(i).getUuid() %></td>
                <%}
                else {%>
                <td><a href="<portlet:actionURL/>&<portlet:namespace/><%=Constants.FETCH_RESULTS%>=<%= URLEncoder.encode(workflowSubmissionJobs.get(i).getUuid(), "UTF-8")%>"><%= workflowSubmissionJobs.get(i).getUuid() %></a></td>
                <%}%>
                <td><%= workflowSubmissionJobs.get(i).getStatus() %></td>
            </tr>
    <%}%>
</table>
</form>
<%
} else{
%>
Currently there are no workflows submitted for execution.
<%
}
%>
