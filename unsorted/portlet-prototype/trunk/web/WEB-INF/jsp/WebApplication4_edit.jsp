<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp--%>
<%@ page import="javax.portlet.*"%>
<%@ page
	import="net.sf.taverna.t2.platform.taverna.*,java.net.URL,java.util.List,net.sf.taverna.t2.facade.*,net.sf.taverna.t2.reference.*,net.sf.taverna.t2.workflowmodel.*,org.springframework.web.context.support.*"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<portlet:defineObjects />
<%PortletPreferences prefs = renderRequest.getPreferences();%> 

<%!//

	// The fields here are accessible from the rest of the JSP
	Dataflow workflow = null;
	Enactor enactor = null;
	TavernaBaseProfile profile = null;
	ReferenceService rs = null;


	// This method is called once per JSP, rather than once per invocation.
	// Use it to set the fields above in the same way as the command line
	// example.
	public void jspInit() {
		// In the command line version of this application you have to
		// explicitly create the application context. For a web application,
		// however, the context is loaded for you. The definition of this,
		// where to find the context xml files and suchlike can be found
		// in the WEB-INF/web.xml file.


		profile = new TavernaBaseProfile(WebApplicationContextUtils
				.getWebApplicationContext(getServletContext()));
		rs = profile.getReferenceService();




		// Get the parser, use it to load the workflow definition and store
		// it in the class level field.
		WorkflowParser parser = profile.getWorkflowParser();
		try {
			// Load the workflow from the context resource 'exampleWorkflow.xml'
			workflow = parser.createDataflow("exampleWorkflow.xml");
		} catch (Exception ex) {
			System.out.println("EXCEPTION!");
		}
		// Get the enactor and reference service and stash them for later use
		enactor = profile.getEnactor();
	}%>

<!-- Standard HTML headers, in a JSP anything not in a code block is treated as
	 HTML and converted to out.println(..) statements in the generated servlet -->
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Run the simple example workflow!</title>
</head>
<body>

<!-- Get the pathway ID from the request object and run the workflow -->
<%
	String pathwayId;
	T2Reference input, result = null;
	WorkflowInstanceFacade instance;
	pathwayId = request.getParameter("pathway");
	if (pathwayId != null) {
		if (!pathwayId.startsWith("path:")) {
			pathwayId = "path:" + pathwayId;
		}
		input = rs.register(pathwayId, 0, true, null);
		instance = enactor.createFacade(workflow);
		// Send input data into the workflow instance
		enactor.pushData(instance, "Input", input);
		// Get the result by using the blocking method on Enactor
		result = enactor.waitForResult(instance, "Output");
	}
%>

<!-- It's generally a good idea to tell your users what's going on...
	 one of the benefits of the JSP syntax is that it's easy to combine
	 java and regular HTML in this way. There are more sophisticated
	 frameworks out there but this is a simple example. -->
<h1>Workflow Invocation</h1>
<p>This form allows you to run a trivial workflow which looks up
drug identifiers from KEGG given a pathway identifier. You can enter a
pathway ID in the form below and hit return to see the associated drug
identifiers.</p>
<p>Pathway IDs are of the form <em>path:map07026</em>, if you omit
the initial <em>path:</em> the page will add it for you.</p>
<p>
<table border="1">
	<tr>
		<th>
		<form method="POST" action="<%=renderResponse.createRenderURL() %>">Pathway ID : <input type="text"
			name="pathway"
			value="<%if (pathwayId != null) {
				out.print(pathwayId);
			}%>" /></form>
		</th>
	</tr>
	<!-- Display the results as rows in the table -->
	<%
		if (pathwayId != null) {
			// The result of the workflow is in the form of a T2Reference
			// as before. As before this can be resolved to a list of string
			// values, use the reference service to do this then iterate over
			// them printing them to the console.
			List<String> stringResults;
			ReferenceService rs = profile.getReferenceService();
			stringResults = (List<String>) rs.renderIdentifier(result,
					String.class, null);
			for (String drugId : stringResults) {
	%>
	<tr>
		<td><%=drugId%></td>
	</tr>
	<%
		}
		}
	%>
</table>
</p>
</body>
</html>