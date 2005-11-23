<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="net.sf.taverna.interaction.server.*,net.sf.taverna.interaction.server.http.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Untitled Document</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="styles.css" rel="stylesheet" type="text/css">
</head>

<body>
<h1>Interaction Service Home </h1>
<p>This page shows the current status of your interaction service. If you're seeing this then you probably have a working service although you might like to check some of the parameters below to ensure full functionality. If there are undefined values in the basic service settings section you probably need to change the properties in the web.xml file and rebuild this application archive. If patterns aren't appearing as they should you need to ensure that the pattern .jar files are included in the classpath available to this application (i.e. in WEB-INF/lib) and that they contain appropriate implementations of the SPI as per the Apache commons discovery toolkit. </p>
<table width="100%"  border="0" cellspacing="0" cellpadding="0">
  <tr valign="top">
    <td width="244"><img src="interactionlogo.png" width="244" height="301" align="top"></td>
    <td width="72%"><h3>Basic Service Settings</h3>
      <table width="95%" border="1" cellpadding="2" cellspacing="2" bordercolor="#000000">
        <tr class="important">
          <th width="200">Property</th>
          <th>Value</th>
        </tr>
        <tr>
          <td>Base&nbsp;URL</td>
          <td><code><%= SubmitServlet.getServer().getBaseURLString() %></code></td>
        </tr>
        <tr>
          <td>SMTP&nbsp;relay</td>
          <td><code><%= SubmitServlet.getServer().getSMTPRelayAddress() %></code></td>
        </tr>
        <tr>
          <td>From&nbsp;address</td>
          <td><code><%= SubmitServlet.getServer().getMailFrom() %></code></td>
        </tr>
        <tr>
          <td>Repository&nbsp;location</td>
          <td><code><%= SubmitServlet.getServer().getRepository().toString() %></code></td>
        </tr>
        <tr>
          <td>Build date </td>
          <td><code><%= net.sf.taverna.interaction.InteractionReleaseInfo.getBuildDate().toString() %></code></td>
        </tr>
      </table>
      <h3>Available Interaction Patterns</h3>
      <table width="95%"  border="1" cellpadding="2" cellspacing="2" bordercolor="#000000">
        <tr class="important">
          <th>Name</th>
          <th>Description</th>
        </tr>
        <%  String[] names = PatternRegistry.getPatternNames();
      for (int i = 0; i < names.length; i++) {
        String name = names[i];
	ServerInteractionPattern sip = PatternRegistry.patternForName(name);
	String description = sip.getDescription();
	out.println("<tr><td>"+name+"</td><td>"+description+"</td></tr>");
      }
  %>
      </table>
    <p>Pattern XML is located at : <a href="<%= SubmitServlet.getServer().getBaseURLString()+"patterns.xml" %>"><%= SubmitServlet.getServer().getBaseURLString()+"patterns.xml" %></a></p></td>
  </tr>
</table>
</body>
</html>
