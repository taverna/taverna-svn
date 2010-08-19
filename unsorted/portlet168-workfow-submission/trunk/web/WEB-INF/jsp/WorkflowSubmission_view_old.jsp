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

<%
//RenderRequest renderRequest1 = (RenderRequest) request.getAttribute("javax.portlet.request");
//RenderResponse renderResponse1 = (RenderResponse) request.getAttribute("javax.portlet.response");
//PortletConfig portletConfig1 = (PortletConfig) request.getAttribute("javax.portlet.config");
//String contextPath = request.getContextPath();
%>

<%!
// Constants
private static final String WORKFLOWS_DIRECTORY = "/WEB-INF/workflows";
private static final String WORKFLOW_SUBMISSION_FORM = "workflow_submission_form";
private static final String WORKFLOW_SELECTION = "workflow_selection";

%>

<%!
// List of workflows loaded from a file. Workflows are located in /WEB-INF/workflows folder in the app root.
public static ArrayList<String> workflowNamesList;
%>

<%!
// Initialise stuff - load the workflows once
   public void jspInit(){
        // Load the workflows
        workflowNamesList = new ArrayList<String>();

        // Directory containing workflows
        File dir = new File(getServletContext().getRealPath(WORKFLOWS_DIRECTORY));
        System.out.println("----------- Workflow Submission Log Message ----------");
        System.out.println("Absolute Path to Workflow Dir is: " + dir.getAbsolutePath());
       
        // Filter only workflows i.e. files of type .t2flow
        FilenameFilter t2flowFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".t2flow");
            }
        };

        // Filter only jsp workflow interface files
        FilenameFilter jspFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jsp");
            }
        };

        String[] workflowFiles = dir.list(t2flowFilter);

        if (workflowFiles == null) { // Either dir does not exist or is not a directory
        }
        else {
            for (int i=0; i<workflowFiles.length; i++)
            { // Get the workflow filename
                workflowNamesList.add(workflowFiles[i]);
                System.out.println("----------- Workflow Submission Log Message ----------");
                System.out.println("Adding file: " + workflowFiles[i]);
            }
        }
   }
%>

<%-- Form for selecting the workflow to be sent for execution --%>
<b> Select a workflow to run </b>

<form name="<portlet:namespace/><%=WORKFLOW_SUBMISSION_FORM%>" action="<portlet:actionURL/>">
<table>
<tr>
    <td style="padding:5px;">
        <select name="<portlet:namespace/>workflow_selection">
            <%
            for (int i = 0; i < workflowNamesList.size(); i++ ) {
                // If this wf was selected then show it as selected in the list
            %>  
                <option value="<portlet:namespace/><%=workflowNamesList.get(i)%>"><%=workflowNamesList.get(i)%></option>
            <%
            }
            %>
        </select>
    </td>
    <td style="padding:5px;">
        <input type="submit" name="<portlet:namespace/><%=WORKFLOW_SELECTION%>" value="Select"/>
    </td>
</tr>
</table>
</form>

<%
while (renderRequest.getParameterNames().hasMoreElements()){
    String parameterName = (String) request.getParameterNames().nextElement();
    out.println("Parameter name: " + parameterName);
    out.println("Parameter value " + request.getParameterValues(parameterName));
    out.println("<br />");
}
if(renderRequest.getParameter((renderResponse.getNamespace()+WORKFLOW_SELECTION)) != null){
    out.println("<hr />");
    for (int i = 0; i < workflowNamesList.size(); i++ ) {
        // If any wf was selected then show its input form below by loading the appropriate jsp
        if (workflowNamesList.get(i).equals(renderRequest.getParameter((renderResponse.getNamespace() + workflowNamesList.get(i))))){
        %>
        Bla
        <%
        }
    }
}
else{%>
    Nothing selected
<%}
%>