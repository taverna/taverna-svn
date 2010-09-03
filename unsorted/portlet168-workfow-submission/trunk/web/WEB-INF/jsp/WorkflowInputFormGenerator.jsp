<%-- 
    Document   : WorkflowInputFormGenerator.jsp
    Created on : Sep 3, 2010, 12:40:57 PM
    Author     : Alex Nenadic
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

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

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>
    </body>
</html>
