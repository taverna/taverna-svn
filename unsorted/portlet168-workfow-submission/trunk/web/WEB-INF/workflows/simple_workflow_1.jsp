<%-- 
    Document   : simple_workflow_1.jsp
    Created on : Aug 18, 2010, 11:45:07 AM
    Author     : Alex Nenadic
    This is a JSP snipet that represents simple_workflow_1.t2flow workflow's input form.
--%>

<%-- Add portlet taglibs to JSP --%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%-- Include various JavaScript functions, e.g. for form validation --%>
<%@ include file="/WEB-INF/jsp/CommonJavaScript.jsp" %>

<%-- Include the styling CSS --%>
<%-- Constants already imported through CommonJavaScript.jsp <%@ include file="/WEB-INF/jsp/CommonCSS.jsp" %> --%>

<%-- Include various constants 
<%@ include file="/WEB-INF/jsp/CommonConstants.jsp" %> --%>

<b>Workflow: simple_workflow_1</b>
<br />
<br />
<b>Description:</b> This workflow simply pushes the input value given to the input parameter called "in" to the
workflow output called "out".
<br />
<br />
<b>Workflow inputs:</b>
<form name="<portlet:namespace/><%= WORKFLOW_INPUTS_FORM%>" action="<portlet:actionURL/>" method="post" onSubmit="return validateForm()">
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