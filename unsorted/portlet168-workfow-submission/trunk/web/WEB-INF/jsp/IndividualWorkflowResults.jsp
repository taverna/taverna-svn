<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp --%>
<%@ page import="javax.portlet.*"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%@ page import="java.net.URLDecoder" %>
<%@ page import="net.sf.taverna.t2.portal.WorkflowSubmissionJob" %>
<%@ page import="net.sf.taverna.t2.portal.Constants" %>

<%
// URL of the Baclava file with results
String workflowBaclavaOutputURL = (String) renderRequest.getAttribute(Constants.WORKFLOW_BACLAVA_OUTPUT_URL_ATTRIBUTE);

// Job submission object
WorkflowSubmissionJob workflowSubmissionJob =  (WorkflowSubmissionJob) renderRequest.getAttribute(Constants.WORKFLOW_SUBMISSION_JOB_ATTRIBUTE);
%>

<%
if (workflowSubmissionJob == null || workflowBaclavaOutputURL == null){%>
<p style="color:red;"><b>There was an error with fetching results for this workflow.</b></p>
<%}
else{
%>
<b>Workflow: <%= workflowSubmissionJob.getWorkflowFileName() %></b><br/>
<b>Job ID: <%= workflowSubmissionJob.getUuid() %></b><br/><br/>
Download the results as a <a href="<%=workflowBaclavaOutputURL %>">single XML file</a> (that can be viewed in Taverna's DataViewer tool).
<%
}
%>