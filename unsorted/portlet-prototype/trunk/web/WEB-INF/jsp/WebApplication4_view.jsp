<%@page contentType="text/html"%>
<%@page pageEncoding="UTF-8"%>

<%-- Uncomment below lines to add portlet taglibs to jsp--%>
<%@ page import="javax.portlet.*"%>
<%@ page
	import="net.sf.taverna.t2.platform.taverna.*,net.sf.taverna.t2.invocation.WorkflowDataToken,net.sf.taverna.t2.invocation.ProcessIdentifier,net.sf.taverna.t2.invocation.InvocationContext,net.sf.taverna.t2.facade.WorkflowInstanceListener,net.sf.taverna.t2.facade.WorkflowInstanceStatus,java.net.URL,java.util.List,net.sf.taverna.t2.facade.*,net.sf.taverna.t2.reference.*,net.sf.taverna.t2.workflowmodel.*,org.springframework.web.context.support.*"%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"%>

<portlet:defineObjects />
<%PortletPreferences prefs = renderRequest.getPreferences();%>

<%!//

	// The fields here are accessible from the rest of the JSP
	Dataflow workflow = null;
	Enactor enactor = null;
	TavernaBaseProfile profile = null;
	ReferenceService rs = null;


	public void jspInit() {
		

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


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>

<!-- Get the pathway ID from the request object and run the workflow -->
<%
	String pathwayId;
	T2Reference input, result = null;
	WorkflowInstanceFacade instance = null;
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




<table cellpadding="5" cellspacing="5">
	<tr>
		<th>
		<form method="POST" action="<%=renderResponse.createRenderURL() %>">Input <input type="text"
			name="pathway"
			value="<%if (pathwayId != null) {
				out.print(pathwayId);
			}%>" />
            <input type="submit" value="RUN"/>
            </form>
		</th>
	</tr>
    <tr><th>Output</th></tr>
    </table>
 
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
                int count = 1;
                count ++;
	%>
	
		<%=drugId+", "%>
	
	<%
		} %>
        END
		<%}
	%>


</body>
</html>