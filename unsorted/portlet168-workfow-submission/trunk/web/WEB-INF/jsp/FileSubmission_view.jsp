<%-- 
    Document   : FileSubmission
    Created on : Sep 29, 2010, 5:57:43 PM
    Author     : alex
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%@ page import="net.sf.taverna.t2.portal.Constants" %>

<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects />

<!--<img src="/FileServingServlet?filename=LoveFilmList-2.png" alt="does not work">-->

<% String fileServletURL = renderRequest.getContextPath() + portletConfig.getPortletContext().getInitParameter(Constants.FILE_SERVLET_PATH); %>

<a href="<%=fileServletURL%>?filename=test">Show file!</a>

