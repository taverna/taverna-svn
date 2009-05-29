<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp--%>
<%@ page import="javax.portlet.*"%>
<%@ page
	import=" org.w3c.dom.*,javax.xml.parsers.*,javax.xml.xpath.*,net.sf.taverna.t2.platform.taverna.*,java.net.URL,java.util.List,net.sf.taverna.t2.facade.*,net.sf.taverna.t2.reference.*,net.sf.taverna.t2.workflowmodel.*,org.springframework.web.context.support.*"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<portlet:defineObjects />
<%PortletPreferences prefs = renderRequest.getPreferences();%> 



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
%>


<p>
<table border="0">
	<tr>
		
		<form method="POST" action="<%=renderResponse.createRenderURL() %>">
    <input type="text" name="number" size="30"/>
    <select name="type">
        <option value="workflow">Workflows</option>
        <option value="user">Users</option>
        <option value="file">Files</option>
        <option value="group">Groups</option>
    </select>
    <br>
    <br>
    <input type="submit" name="submit" value="Search"/>

		
	</tr>

	<%
		if (search != null) {
			try{
        DocumentBuilderFactory Factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = Factory.newDocumentBuilder();
        Document doc = builder.parse("http://www.myexperiment.org/search.xml?query="+search+"&type="+type);

        //session.setAttribute("workflow", inspect,PortletSession.APPLICATION_SCOPE);

        //creating an XPathFactory:
        XPathFactory factory = XPathFactory.newInstance();
        //using this factory to create an XPath object:
        XPath xpath = factory.newXPath();
        //XPath object created compiles the XPath expression:
        XPathExpression Resexpr = xpath.compile("//"+type+"/text()");

        XPathExpression URIexpr = xpath.compile("//"+type+"/@uri");

        //out.print(inspect);

        //expression is evaluated with respect to a certain context node which is doc.
        Object result = Resexpr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodeList = (NodeList) result;
        Object result2 = URIexpr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodeList2 = (NodeList) result2; %>

        <hr>

        <table>
            <tr>
            <%=nodeList.getLength()+"  "+type%>s found associated with '<b><i><%=search%></i></b>'
            </tr>
        

        
        <%for (int i = 0; i < nodeList.getLength(); i++) { %>
            <tr>
            <td align="center">
            <input type="radio" name="inspect" value="<%=nodeList2.item(i).getNodeValue()%>" />
            </td>
            <td>
            <a href="<%=nodeList2.item(i).getNodeValue()%>">
            <%=nodeList.item(i).getNodeValue()%>
            </a>
            </td>
            
            </tr>
        <% } %>
        <tr>
        <td> <input type="submit" value="Inspect" />
        </td></tr>
        </form>
        </table>
        <%}
        catch(Exception e){}

		}
	%>
</table>
</p>
</body>
</html>