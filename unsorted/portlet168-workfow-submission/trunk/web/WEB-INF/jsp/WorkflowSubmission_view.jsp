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
<%@ page import="javax.portlet.PortletContext" %>

<%!
// Constants
private static final String WORKFLOW_FILE_NAMES = "workflow_file_names";
private static final String WORKFLOW_SELECTION_SUBMISSION = "workflow_selection";
private static final String SELECTED_WORKFLOW = "selected_workflow";
%>

<%
// List of workflow file names. Workflow files are located in /WEB-INF/workflows folder in the app root.
ArrayList<String> workflowFileNamesList = (ArrayList<String>) renderRequest.getPortletSession().getAttribute(WORKFLOW_FILE_NAMES);
%>

<%-- Form for selecting the workflow to be sent for execution --%>
<b> Select a workflow to run </b>

<form action="<portlet:actionURL/>" method="post">
<table>
<tr>
    <td style="padding:5px;">
        <select name="<portlet:namespace/><%=SELECTED_WORKFLOW%>">
            <%
            for (int i = 0; i < workflowFileNamesList.size(); i++ ) {
                // If this workflow was selected then show it as selected in the list
                if (workflowFileNamesList.get(i).equals(renderRequest.getParameter(SELECTED_WORKFLOW))){
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
        <input type="submit" name="<portlet:namespace/><%=WORKFLOW_SELECTION_SUBMISSION%>" value="Select"/>
    </td>
</tr>
</table>
</form>
