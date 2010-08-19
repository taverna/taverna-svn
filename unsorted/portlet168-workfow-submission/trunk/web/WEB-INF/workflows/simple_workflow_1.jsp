<%-- 
    Document   : simple_workflow_1.jsp
    Created on : Aug 18, 2010, 11:45:07 AM
    Author     : alex
    This is a JSP snipet that represents simple_workflow_1.t2flow workflow's input form.
--%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%!
// Constants
private static final String RUN_WORKFLOW = "run_workflow";
private static final String WORKFLOW_NAME = "workflow_name";
%>

<style type="text/css">
table.inputs
{
    border-collapse:collapse;
    margin-bottom:10px;
}
table.inputs, table.inputs th, table.inputs td
{
    border: 1px solid #5F5F5F;
    padding: 5px;
}
table.inputs th{
    background-color: #9F9F9F;
    color: white;
}

</style>

<b>Workflow: simple_workflow_1</b>
<br />
<br />
<b>Description:</b> This workflow simply pushes the input value given to the input parameter called "in" to the
workflow output called "out".
<br />
<br />
<b>Workflow inputs:</b>
<form action="<portlet:actionURL/>" method="post">
<table class="inputs">
    <tr>
        <th>Name</th>
        <th>Type</th>
        <th>Description</th>
        <th>Value</th>
    </tr>
    <tr>
        <td>in</td>
        <td>single value</td>
        <td>Any string</td>
        <!-- Input field for the workflow input port called "in"-->
        <td><textarea name="<portlet:namespace/>in" rows="2" cols="20" wrap="off"></textarea></td>
    </tr>
</table>
    <%-- Hidden field to convey which workflow we want to execute --%>
    <input type="hidden" name="<portlet:namespace/><%= WORKFLOW_NAME%>" value="simple_workflow_1" />
    <input type="submit" name="<portlet:namespace/><%= RUN_WORKFLOW%>" value="Run workflow" />
</form>