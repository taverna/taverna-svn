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
    <th width="200" bgcolor="#FFCC66"><div align="left">Property</div></th>
    <th bgcolor="#FFCC66"><div align="left">Value</div></th>
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
    <th><div align="left">Title : <% out.println(info.getTitle()); %></div></th>
    <th><div align="left">Author : <% out.println(info.getAuthor()); %></div></th>
  </tr>

  <tr bgcolor="#FFFFCC">
    <th colspan="2"><div align="left">Description</div></th>
  </tr>
  <tr><td colspan="2">
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
</form>
<p>&nbsp; </p>
</body>
</html>
