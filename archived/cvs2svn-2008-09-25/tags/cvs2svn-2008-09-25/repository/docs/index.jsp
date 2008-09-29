<%@ page contentType="text/html; charset=iso-8859-1" language="java" import="net.sf.taverna.repository.server.*" errorPage="" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Workflow Repository Status</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<link href="styles.css" rel="stylesheet" type="text/css">
<style type="text/css">
<!--
.style1 {
	font-size: small;
}
-->
</style>
</head>

<body>
<h1>Workflow Repository Status</h1>
<p>This page shows the current status of your workflow repository service. If you're seeing this then you probably have a working service although you might like to check some of the parameters below to ensure full functionality.</p>
<h3>Basic Service Settings</h3>
<table width="95%" border="1" cellspacing="0" cellpadding="2">
  <tr>
    <th width="200" bgcolor="#FFE271"><div align="left">Property</div></th>
    <th bgcolor="#FFE271"><div align="left">Value</div></th>
  </tr>
  <tr>
    <td>Repository&nbsp;location</td>
    <td><code><%= SubmitServlet.getRepository().getLocation().toString() %></code></td>
  </tr>
  <tr>
    <td>Server build date </td>
    <td><code><%= net.sf.taverna.repository.RepositoryReleaseInfo.getBuildDate().toString() %></code>&nbsp;</td>
  </tr>
</table>
<h3>Workflow List </h3>
<table width="95%"  border="1" cellspacing="0" cellpadding="2">

  <%  Repository r = SubmitServlet.getRepository();
	  ScuflModelInfo[] models = r.getModels();
	  for (int i = 0; i < models.length; i++) {
		ScuflModelInfo info = models[i];
  %>
  <tr bgcolor="#CCFFCC">
    <th colspan="2" valign="top" bgcolor="#D4D4E9"><div align="left">Title :<em> 
      <% out.println(info.getTitle()); %>
    </em></div></th>
    <th valign="top" bgcolor="#D5D5E9"><div align="left">Author :<em> 
      <% out.println(info.getAuthor()); %>
    </em></div></th>
  </tr>

  <tr bgcolor="#FFFFCC">
    <td rowspan="2" valign="top" bgcolor="white"><a href="data?type=image&id=<%= info.getID() %>"><img border="0" src="data?type=thumb&id=<%= info.getID() %>"></a></td><th height="25" colspan="2" bgcolor="#FFE271"><div align="left">Description<em> <a href="data?type=summary&id=<%= info.getID() %>">full summary</a> <a href="data?type=definition&id=<%= info.getID() %>">xml definition</a> </em></div></th>
  </tr>
  <tr><td colspan="2" valign="top">
    <span class="style1">
    <% out.println(info.getDescription()); %>
    </span></td>
  </tr>
  <% } %>
</table>
<h3>Submit Workflow</h3>
<form action="submit" method="post" enctype="multipart/form-data" name="form1">
  Use the browse button to locate a workflow to upload from your machine  
  <input type="file" name="workflow">
  <input type="submit" name="Submit" value="Submit">
</form><br/>
<form name="form2" method="get" action="submit">
  Alternatively you can enter the URL to a workflow definition on the web here 
  <input type="text" name="workflowURL">
  <input type="submit" name="Submit2" value="Submit">
</form>
<p>&nbsp; </p>
</body>
</html>
