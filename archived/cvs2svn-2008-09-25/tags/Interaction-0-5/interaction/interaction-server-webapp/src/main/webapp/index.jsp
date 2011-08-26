<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="net.sf.taverna.interaction.server.*,net.sf.taverna.interaction.server.http.*" errorPage="" %>
<html>
<!-- DW6 -->
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Interaction Service Status</title>
<link rel="stylesheet" href="emx_nav_left.css" type="text/css">
<style type="text/css">
<!--
.style1 {font-size: small}
-->
</style>
</head>
<body> 
<div id="masthead"> 
  <h1 id="siteName">Interaction Service Status </h1>
</div>
<!-- end masthead --> 
<div id="pagecell1"> 
  <!--pagecell1--> 
<div id="content">
  <h3 class="capsule ">Service Status</h3>
  <p class="capsule style1">This page shows the current status of your interaction service. If you're seeing this then you probably have a working service although you might like to check some of the parameters below to ensure full functionality. If there are undefined values in the basic service settings section you probably need to change the properties in the web.xml file and rebuild this application archive. If patterns aren't appearing as they should you need to ensure that the pattern .jar files are included in the classpath available to this application (i.e. in WEB-INF/lib) and that they contain appropriate implementations of the SPI as per the Apache commons discovery toolkit. </p>
  <table width="100%"  border="0" cellspacing="0" cellpadding="0">
    <tr valign="top">
      <td width="244"><img src="interactionlogo.png" width="244" height="301" align="top"></td>
      <td width="72%"><h3>Basic Service Settings</h3>
          <table width="100%" border="1" cellpadding="2" cellspacing="0" bgcolor="#F9F8FD">
            <tr bgcolor="#D3D2E4" class="important">
              <th width="148"><span class="style1">Property</span></th>
              <th width="433"><span class="style1">Value</span></th>
            </tr>
            <tr>
              <td><span class="style1">Base&nbsp;URL</span></td>
              <td><span class="style1"><code><%= SubmitServlet.getServer().getBaseURLString() %></code></span></td>
            </tr>
            <tr bgcolor="#FFFFFF">
              <td><span class="style1">SMTP&nbsp;relay</span></td>
              <td><span class="style1"><code><%= SubmitServlet.getServer().getSMTPRelayAddress() %></code></span></td>
            </tr>
            <tr>
              <td><span class="style1">From&nbsp;address</span></td>
              <td><span class="style1"><code><%= SubmitServlet.getServer().getMailFrom() %></code></span></td>
            </tr>
            <tr bgcolor="#FFFFFF">
              <td><span class="style1">Repository&nbsp;location</span></td>
              <td><span class="style1"><code><%= SubmitServlet.getServer().getRepository().toString() %></code></span></td>
            </tr>
            <tr>
              <td><span class="style1">Build date </span></td>
              <td><span class="style1"><code><%= net.sf.taverna.interaction.InteractionReleaseInfo.getBuildDate().toString() %></code></span></td>
            </tr>
          </table>
          <h3>Available Interaction Patterns</h3>
          <table width="100%"  border="1" cellpadding="2" cellspacing="0">
            <tr bgcolor="#D3D2E4" class="important">
              <th><span class="style1">Name</span></th>
              <th><span class="style1">Description</span></th>
            </tr>
            <%  String[] names = PatternRegistry.getPatternNames();
      for (int i = 0; i < names.length; i++) {
        String name = names[i];
	ServerInteractionPattern sip = PatternRegistry.patternForName(name);
	String description = sip.getDescription();
	out.println("<tr><td><span class=\"style1\">"+name+"</span></td><td><span class=\"style1\">"+description+"</span></td></tr>");
      }
  %>
          </table>
          <p class="style1">Pattern XML is located at : <a href="<%= SubmitServlet.getServer().getBaseURLString()+"patterns.xml" %>"><%= SubmitServlet.getServer().getBaseURLString()+"patterns.xml" %></a></p></td>
    </tr>
  </table>
  <h3>Using the Interaction Service with Taverna</h3>
  <p><img src="example.png" width="257" height="283" align="right"><span class="style1">The interaction service is intended to be used from the Taverna workflow workbench (<a href="http://taverna.sf.net">http://taverna.sf.net</a>). An example using the EBI interaction service is available <a href="fetchAndInteract.xml">here</a> - you will require Taverna 1.3.1 or recent CVS drops to run this example. </span></p>
  <p class="style1">The example fetches a clone from the EMBL database and passes this to the user specified by the 'emailAddress' input for review. The target user receives an email, this email includes a link which the user can select to launch a modified version of the Artemis sequence editor with the data (earlier fetched by the workflow) preloaded. The user can modify this data and add comments before submitting the modified annotation back at which point the workflow process completes. </p>
  <p class="style1">To use this interaction service in your own workflows you must add a new Interaction Service scavenger to the services panel. The plugin, if present, will prompt you for a service URL, use the 'base URL' from the table above and you should see the interaction patterns defined within this server as nodes within the service panel - these can then be added to the workflow as with any other service type.</p>
</div> 
</div>
<!--end pagecell1--> 
<br> 
</body>
</html>
