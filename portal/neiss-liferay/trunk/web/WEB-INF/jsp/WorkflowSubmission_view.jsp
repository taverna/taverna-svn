<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp --%>
<%@ page import="javax.portlet.*"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%@ page import="java.util.ArrayList" %>
<%@ page import="java.io.File" %>
<%@ page import="java.io.FilenameFilter" %>
<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ page import="javax.portlet.PortletConfig" %>
<%@ page import="javax.portlet.PortletContext" %>
<%@ page import="net.sf.taverna.t2.portal.Constants" %>
<%@ page import="net.sf.taverna.t2.portal.WorkflowSubmissionPortlet" %>

<%-- Include the styling CSS --%>
<%@ include file="InputsCSS.jsp" %>
<%@ include file="MyExperimentCSS.jsp" %>

<%
// List of workflow file names. Workflow files are located in /WEB-INF/workflows folder in the app root.
ArrayList<String> workflowFileNamesList = (ArrayList<String>) renderRequest.getAttribute(Constants.WORKFLOW_FILE_NAMES);
// Current user
String user = (String) renderRequest.getPortletSession().getAttribute(Constants.USER, PortletSession.APPLICATION_SCOPE);
%>

<%-- Print out a message to the user, if any --%>
<%
    if (renderRequest.getAttribute(Constants.ERROR_MESSAGE) != null){%>
        <span class="portlet-msg-error"><b><%=renderRequest.getAttribute(Constants.ERROR_MESSAGE)%></b></span>
        <hr>
    <%}
    if (renderRequest.getAttribute(Constants.INFO_MESSAGE) != null){%>
        <span class="portlet-msg-info"><b><%=renderRequest.getAttribute(Constants.INFO_MESSAGE)%></b></span>
        <hr>
    <%}
%>


<table>
<tr><td style="padding-right:25px;">
<%-- Form for selecting pre-canned uploaded workflows to be sent for execution --%>
<%--<p><b>Select a workflow to run:</b></p>--%>

<form name="<portlet:namespace/><%= Constants.WORKFLOW_SELECTION_SUBMISSION_FORM%>" action="<portlet:actionURL/>" method="post">
<fieldset>
<legend>Select a workflow to run</legend>
<table>
<tr>
    <td style="padding:5px;">
        <select name="<portlet:namespace/><%= Constants.SELECTED_WORKFLOW%>">
            <%
            for (int i = 0; i < workflowFileNamesList.size(); i++ ) {
                // If this workflow was selected then show it as selected in the list
                if (workflowFileNamesList.get(i).equals(renderRequest.getParameter(Constants.SELECTED_WORKFLOW))){
            %>  
                    <option selected="selected" value="<%=workflowFileNamesList.get(i)%>"><%=workflowFileNamesList.get(i)%></option>
            <%
                }else{%>
                    <option value="<%=workflowFileNamesList.get(i)%>"><%=workflowFileNamesList.get(i)%></option>
                <%}
            }
            %>
        </select>
    </td>
    <td style="padding:5px;">
        <input type="submit" name="<portlet:namespace/><%= Constants.WORKFLOW_SELECTION_SUBMISSION%>" value="Select"/>
    </td>
</tr>
</table>
</fieldset>
</form>

<br>

<% if (user != null && !user.equals(Constants.USER_ANONYMOUS)){%>
<!-- Only logged in users are allowed to upload new workflows -->
<form action="<portlet:actionURL/>" method="post">
<table>
<tr>
    <td>
        <input type="submit" name="<portlet:namespace/><%= Constants.WORKFLOW_UPLOAD_SUMBISSION%>" value="Add a new workflow">
    </td>
</tr>
<%}%>
</table>
<br>
</form>
</td>

<td style="padding-left:25px; border-left-style:dotted; border-left-color:grey; border-left-width:1px; ">
<%-- Form for searhing the myExperiment workflows --%>
<%--<p><b>Enter terms to search myExperiment for workflows.<br>
        (All fields will be searched, including workflows, users, groups, tags, etc.).<br>
        Separate multiple search terms with a blank character or leave empty to get all workflows.
    </b>
</p>--%>
<form action="<portlet:actionURL/>" method="post">
<fieldset>
<legend>Enter terms to search myExperiment for workflows</legend>
<p>All fields will be searched, including workflows, users, groups, tags, etc.<br>
        Separate multiple search terms with a blank character or leave empty to get all workflows.
</p>
<table>
<tr>
    <td style="padding:5px;">
        <input type="text" name="<%= Constants.MYEXPERIMENT_SEARCH_TERMS%>" size="30"/>
    </td>
    <td style="padding:5px;">
        <input type="submit" name="<portlet:namespace/><%= Constants.MYEXPERIMENT_WORKFLOW_SEARCH%>" value="Search myExperiment"/>
    </td>
</tr>
<tr>
    <td><span style="color:gray;">(A maximum of <%= WorkflowSubmissionPortlet.myExperimentResultCountLimit%> results will be returned.)</span></td>
    <td> </td>
</tr>
</table>
</fieldset>
</form>
</td></tr>

</table>