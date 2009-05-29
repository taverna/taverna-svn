package WEB-INF.jsp;

<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp--%>
<%@ page import="javax.portlet.*"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<portlet:defineObjects />
<%PortletPreferences prefs = renderRequest.getPreferences();%>

<form method="POST" action="<%=renderResponse.createRenderURL() %>">
    <input type="text" name="number" size="10"/>
    <select name="type">
        <option value="workflow">Workflows</option>
        <option value="user">Users</option>
        <option value="file">Files</option>
        <option value="group">Groups</option>
    </select>
    <br>
    <br>
    <input type="submit" name="submit" value="Search"/>
</form>