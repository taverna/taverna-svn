<%--
    Document   : simple_workflow_4.jsp
    Created on : Aug 18, 2010, 11:45:07 AM
    Author     : Alex Nenadic
    This is a JSP snipet that represents simple_workflow_4.t2flow workflow's input form.
--%>

<%-- Add portlet taglibs to JSP --%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%-- Include various JavaScript functions, e.g. for form validation --%>
<%@ include file="/WEB-INF/jsp/CommonJavaScript.jsp" %>

<%-- Include the styling CSS --%>
<%@ include file="/WEB-INF/jsp/CommonCSS.jsp" %>

<%-- Include various constants --%>
<%-- Constants already imported through CommonJavaScript.jsp
<%@ include file="/WEB-INF/jsp/CommonConstants.jsp" %> --%>

<b>Workflow: simple_workflow_4</b>
<br />
<br />
<b>Description:</b> This workflow simply pushes the input list given to the input parameter called "in" to the
workflow output called "out".
<br />
<br />
<b>Workflow inputs:</b>
<form name="<portlet:namespace/><%= WORKFLOW_INPUTS_FORM%>" action="<portlet:actionURL/>" method="post" enctype="multipart/form-data" onSubmit="return validateForm()">
<table class="inputs">
    <tr>
        <th>Name</th>
        <th>Type</th>
        <th>Description</th>
        <th>Value</th>
    </tr>
    <tr>
        <td>in</td>
        <td>a list</td>
        <td>a list of values</td>
        <td>
            <!-- Input field for the workflow input port called "in"-->
            Paste the list values here: <br/>
            <textarea name="<portlet:namespace/>in_content" rows="2" cols="20" wrap="off"></textarea><br />
            Or load them from a file: <br />
            <input type="file" name="<portlet:namespace/>in_file" /><br /><hr />
            Use the following character as a list item separator: 
            <select name="<portlet:namespace/>in_separator">
                <option value="new_line_linux">New line - Unix/Linux (\n)</option>
                <option value="new_line_windows">New line - Windows (\r\n)</option>
                <option value="blank">Blank (' ')</option>
                <option value="tab">Tab (\t)</option>
                <option value="colon">Colon (:)</option>
                <option value="semi_colon">Semi-colon (;)</option>
                <option value="comma">Comma (,)</option>
                <option value="pipe">Pipe (|)</option>
                <option value="other">Other</option>
            </select><br />
            Or specify your own separator: 
            <input type="text" name="<portlet:namespace/>in_other_separator" size="3" />
        </td>
    </tr>
</table>
    <%-- Hidden field to convey which workflow we want to execute --%>
    <input type="hidden" name="<portlet:namespace/><%= WORKFLOW_NAME%>" value="simple_workflow_4" />
    <input type="submit" name="<portlet:namespace/><%= RUN_WORKFLOW%>" value="Run workflow" />
</form>