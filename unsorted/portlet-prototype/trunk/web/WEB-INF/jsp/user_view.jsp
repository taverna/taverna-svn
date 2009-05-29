<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp--%>
<%@ page import="javax.portlet.*"%>
<%@ page
	import=" org.w3c.dom.*,java.util.StringTokenizer,javax.xml.parsers.*,javax.xml.xpath.*,net.sf.taverna.t2.platform.taverna.*,java.net.URL,java.util.List,net.sf.taverna.t2.facade.*,net.sf.taverna.t2.reference.*,net.sf.taverna.t2.workflowmodel.*,org.springframework.web.context.support.*"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<portlet:defineObjects />
<%PortletPreferences prefs = renderRequest.getPreferences();%>
<%PortletSession pSession = renderRequest.getPortletSession();%>



<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>
<%
	String search;
    search = request.getParameter("number");
    String type;
    type = request.getParameter("type");
    String inspect = null;
    inspect = request.getParameter("inspect");
    String meName =
        (String)pSession.getAttribute("meName",PortletSession.APPLICATION_SCOPE);
    String meAvatar =
        (String)pSession.getAttribute("meAvatar",PortletSession.APPLICATION_SCOPE);
    String meWorkflows =
        (String)pSession.getAttribute("meWFNames",PortletSession.APPLICATION_SCOPE);
    String meWFThumbs =
        (String)pSession.getAttribute("meWFThumbs",PortletSession.APPLICATION_SCOPE);
    String meWFURIs =
        (String)pSession.getAttribute("meWFURIs",PortletSession.APPLICATION_SCOPE);

%>

<form method="POST" action="<%=renderResponse.createRenderURL() %>">
<table border="3" bordercolor="white" cellpadding="8">
    <tr>
        <td valign="top" rowspan="4"><img src="<%=meAvatar%>"/></td>
        <td valign="top" align="center">
            <table align="center" cellpadding="8"><tr><td colspan="3" valign="top" align="left"><i><%=meName%></i>'s workflows</td></tr>
            <tr align="center">
                <%StringTokenizer WFStr = new StringTokenizer(meWorkflows, ",");
                while (WFStr.hasMoreTokens()) {%>
                
                <td align="center"><b><%=WFStr.nextToken()%></b></td>

                <%}%>
            </tr>
            <tr align="center">
                <%StringTokenizer thumbStr = new StringTokenizer(meWFThumbs, ",");
                while (thumbStr.hasMoreTokens()) {%>

                <td align="center"><img src="<%=thumbStr.nextToken()%>"/></td>

                <%}%>
            </tr>
            <tr align="center">
                <%StringTokenizer URIStr = new StringTokenizer(meWFURIs, ",");
                while (URIStr.hasMoreTokens()) {%>

                <td><input type="radio" name="inspect" value="<%=URIStr.nextToken()%>"/></td>

                <%}%>
            </tr>
            <tr><td align="center"><input type="submit" value="Load"/></td></tr>
            </table>
        </td>
   </tr>
</table>
</form>


</body>
</html>