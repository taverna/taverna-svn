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

<%-- Include the styling CSS --%>
<%@ include file="MyExperimentCSS.jsp" %>

<%-- Include the JavaScript for dynamic loading of workflow input form --%>
<%@ include file="LoadInputFormForMyExperimentWorkflow.jsp" %>


<%-- Print out a message to the user, if any --%>
<%
    if (renderRequest.getAttribute(Constants.ERROR_MESSAGE) != null){%>
        <p style="color:red;"><b><%=renderRequest.getAttribute(Constants.ERROR_MESSAGE)%></b></p>
        <hr>
    <%}
    if (renderRequest.getAttribute(Constants.INFO_MESSAGE) != null){%>
        <p style="color:green;"><b><%=renderRequest.getAttribute(Constants.INFO_MESSAGE)%></b></p>
        <hr>
    <%}
%>

<%-- Form for searhing the myExperiment workflows --%>
<p><b>Enter terms to search myExperiment for workflows.<br>
        Separate the search terms with a blank character.</b></p>

<form action="<portlet:actionURL/>" method="post">
<table>
<tr>
    <td style="padding:5px;">
        <input type="text" name="<%= Constants.MYEXPERIMENT_SEARCH_TERMS%>" size="30"/>
    </td>
    <td style="padding:5px;">
        <input type="submit" name="<portlet:namespace/><%= Constants.MYEXPERIMENT_WORKFLOW_SEARCH%>" value="Search myExperiment"/>
    </td>
</tr>
</table>
<br>
</form>
