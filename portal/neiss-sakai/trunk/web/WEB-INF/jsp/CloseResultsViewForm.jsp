<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp --%>
<%@ page import="javax.portlet.*"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<%@ page import="javax.portlet.RenderRequest" %>
<%@ page import="javax.portlet.RenderResponse" %>
<%@ page import="net.sf.taverna.t2.portal.Constants" %>

<form name="<portlet:namespace/><%= Constants.CLOSE_RESULTS_VIEW%>" action="<portlet:renderURL/>" method="post">
<p>
    <input type="image" src="<%= renderRequest.getContextPath()%>/images/close.gif" style="border:0;">
</p>
</form>
