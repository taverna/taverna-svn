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

<%-- Include various constants --%>
<%@ include file="CommonConstants.jsp" %>

<%
// List of workflow file names. Workflow files are located in /WEB-INF/workflows folder in the app root.
ArrayList<String> workflowFileNamesList = (ArrayList<String>) renderRequest.getPortletSession().getAttribute(WORKFLOW_FILE_NAMES);
%>

<%-- Print out a message to the user, if any --%>
<%
    if (renderRequest.getAttribute(ERROR_MESSAGE) != null){%>
        <p style="color:red;"><b><%=renderRequest.getAttribute(ERROR_MESSAGE)%></b></p>
        <br />
        <hr />
        <br />
    <%}
    if (renderRequest.getAttribute(INFO_MESSAGE) != null){%>
        <p><b><%=renderRequest.getAttribute(INFO_MESSAGE)%></b></p>
        <br />
        <hr />
        <br />
    <%}
%>

<%-- Form for selecting the workflow to be sent for execution --%>
<b> Select a workflow to run </b>

<form name="<portlet:namespace/><%= WORKFLOW_SELECTION_SUBMISSION_FORM%>" action="<portlet:actionURL/>" method="post">
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
