#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page
	import="net.sf.taverna.t2.platform.taverna.*,org.springframework.web.context.support.*"%>
<%!// 
	// This just initializes the profile, you'll want to do something 
	// more interesting with it :)
	TavernaBaseProfile profile = null;

	public void jspInit() {
		profile = new TavernaBaseProfile(WebApplicationContextUtils
				.getWebApplicationContext(getServletContext()));
		// Do other initialization here if needed
	}%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Platform Initialized!</title>
</head>
<body>
<h1>Simple index page</h1>
If you're seeing this page you've just initialized the T2 platform in
your application server! Now to do something with it...
</body>
</html>