package WEB-INF.jsp;

<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp--%>
<%@ page import="javax.portlet.*,java.net.URL,java.util.List,org.springframework.web.context.support.*"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<portlet:defineObjects />
<%PortletPreferences prefs = renderRequest.getPreferences();%>

<form method="POST" action="<%=renderResponse.createRenderURL() %>">
    Number:
    <input type="text" name="number" size="10"/>
    <input type="submit" name="submit"/>
</form>