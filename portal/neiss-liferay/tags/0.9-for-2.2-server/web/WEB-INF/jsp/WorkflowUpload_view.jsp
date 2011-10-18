<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp --%>
<%@ page import="javax.portlet.*"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ page import="javax.portlet.PortletConfig" %>
<%@ page import="javax.portlet.PortletContext" %>
<%@ page import="net.sf.taverna.t2.portal.Constants" %>

<%@ include file="InputsValidationJavaScript.jsp" %>

<hr>

<%-- Close form button --%>
<form action="<portlet:actionURL/>" method="post">
<p>
    <input type="image" src="<%= renderRequest.getContextPath()%>/images/close.gif" style="border:0;">
    <input type="hidden" name="<portlet:namespace/><%= Constants.CLEAR%>" value="true">
</p>
</form>

<%-- Form for uploading a new workflow --%>
<%--<p><b>Select a workflow to upload:</b></p>--%>
<form name="<portlet:namespace/><%= Constants.WORKFLOW_UPLOAD_FORM%>" action="<portlet:actionURL/>" method="post" enctype="multipart/form-data" onSubmit="return validateFileUploadField()">
<fieldset>
<legend>Select a workflow to upload</legend>
    <table style="width:100%">
        <tr>
            <td>
            <input type="file" name="<portlet:namespace/><%= Constants.WORKFLOW_UPLOAD_FORM_FILE%>" >
            </td>
        </tr>
        <tr>
            <td>
            <input type="checkbox" checked name="<portlet:namespace/><%= Constants.WORKFLOW_UPLOAD_FORM_SHARING_PUBLIC%>"> Share this workflow with others
            </td>
        </tr>
        <tr>
            <td>
            <input type="submit" name="<portlet:namespace/><%= Constants.UPLOAD_WORKFLOW%>" value="Upload a workflow">
            </td>
        </tr>
    </table>
</fieldset>
</form>
<br>
