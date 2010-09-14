<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp --%>
<%@ page import="javax.portlet.*"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%@ page import="java.util.ArrayList" %>
<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ page import="net.sf.taverna.t2.portal.WorkflowSubmissionJob" %>

<%-- Include various constants --%>
<%@ include file="CommonConstants.jsp" %>

<%-- Include the styling CSS --%>
<%@ include file="CommonCSS.jsp" %>

<%
// List of UUIDs of workflows submitted to the T2 Server.
ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)renderRequest.
        getPortletSession().
        getAttribute(WORKFLOW_JOB_UUIDS_PORTLET_ATTRIBUTE, PortletSession.APPLICATION_SCOPE);

%>

<%-- Print out a message to the user, if any --%>

<%-- Form for selecting the workflow to be sent for execution --%>
<% if (workflowSubmissionJobs != null && !workflowSubmissionJobs.isEmpty()){ %>
<table width="100%">
    <tr>
        <td>
            <b> Workflow submission jobs:</b>
        </td>
        <td align="right">
        <form action="<portlet:actionURL/>" method="post">
        <input type="submit" name="<portlet:namespace/><%= REFRESH_WORKFLOW_JOB_UUIDS %>" value="Refresh">
        </form>
        </td>
    </tr>
</table>
<br/>
<table class="results" width="100%">
    <tr>
        <th>Workflow name</th>
        <th>Job ID</th>
        <th>Status</th>
    </tr>
    <%
    for (int i=0; i< workflowSubmissionJobs.size(); i++ ) {
        if (i % 2 == 0){
    %>
        <tr>
            <td><%= workflowSubmissionJobs.get(i).getWorkflowFileName() %></td>
            <td><%= workflowSubmissionJobs.get(i).getUuid() %></td>
            <td><%= workflowSubmissionJobs.get(i).getStatus() %></td>
        </tr>
    <%}
        else{%>
        <tr bgcolor="#EFF5FB">
            <td><%= workflowSubmissionJobs.get(i).getWorkflowFileName() %></td>
            <td><%= workflowSubmissionJobs.get(i).getUuid() %></td>
            <td><%= workflowSubmissionJobs.get(i).getStatus() %></td>
        </tr>
        <%}
    }%>
</table>
<%
} else{
%>
Currently there are no workflows submitted for execution.
<%
}
%>
