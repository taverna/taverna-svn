<%-- 
    Document   : simple_workflow_2
    Created on : Aug 18, 2010, 11:46:52 AM
    Author     : alex
    This is a JSP snipet that represents simple_workflow_2.t2flow workflow's input form.

--%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%!
// Constants
private static final String RUN_WORKFLOW = "run_workflow";
private static final String WORKFLOW_NAME = "workflow_name";
%>

<b>Workflow: simple_workflow_2</b>
<br />
<br />
<b>Description:</b> This workflow has no inputs and simply returns the words "Hello there" on
the workflow output called "out".
<br />
<br />
<form action="<portlet:actionURL/>" method="post">
    <%-- Hidden field to convey which workflow we want to execute --%>
    <input type="hidden" name="<portlet:namespace/><%= WORKFLOW_NAME%>" value="simple_workflow_2" />
    <input type="submit" name="<portlet:namespace/><%= RUN_WORKFLOW%>" value="Run workflow">
</form>