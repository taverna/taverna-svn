package net.sf.taverna.t2.portal;

import java.io.FilenameFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javax.portlet.GenericPortlet;
import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import javax.portlet.PortletRequestDispatcher;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

/**
 * WorkflowSubmission Portlet - enables user to select a workflow,
 * specify the values for its inputs and submit it for execution to
 * a Taverna 2 Server.
 *
 * @author Alex Nenadic
 */
public class WorkflowSubmissionPortlet extends GenericPortlet {

    public static final String WORKFLOWS_DIRECTORY = "/WEB-INF/workflows";
    public static final String WORKFLOW_FILE_NAMES = "workflow_file_names";
    public static final String WORKFLOW_SELECTION_SUBMISSION_FORM = "workflow_selection_form";
    public static final String WORKFLOW_SELECTION_SUBMISSION = "workflow_selection";
    public static final String WORKFLOW_INPUTS_FORM = "workflow_inputs_form";
    public static final String SELECTED_WORKFLOW = "selected_workflow";
    public static final String RUN_WORKFLOW = "run_workflow";
    public static final String WORKFLOW_NAME = "workflow_name";

    public static final String ERROR_MESSAGE = "error_message";
    public static final String INFO_MESSAGE = "info_message";

    // .t2flow XML namespace
    public static final Namespace T2_WORKFLOW_NAMESPACE = Namespace.getNamespace("http://taverna.sf.net/2008/xml/t2flow");
    // XML elements
    public static final String DATAFLOW_ELEMENT = "dataflow";
    public static final String DATAFLOW_ROLE = "role";
    public static final String DATAFLOW_ROLE_TOP  = "top";
    public static final String DATAFLOW_INPUT_PORTS_ELEMENT = "inputPorts";
    public static final String DATAFLOW_PORT = "port";
    public static final String NAME = "name";
    public static final String DEPTH = "depth";

    // REST
    public static final String T2_SERVER_URL = "t2_server_url";
    public static final String T2_SERVER_NAMESPACE = "http://ns.taverna.org.uk/2010/xml/server/";
    public static final String RUNS = "/rest/runs";

    // Address of the Taverna 2 Server
    String t2ServerURL;

    // List of workflow file names. Workflow files are located in /WEB-INF/workflows folder in the app root.
    private static ArrayList<String> workflowFileNamesList;

    // Map of workflow file names to a list of workflow's inputs
    private static HashMap<String, ArrayList<WorkflowInputPort>> workflowNamesToInputsMap;

    @Override
    public void init(){

        // Get the URL of the t2Server
        t2ServerURL = getPortletConfig().getInitParameter(T2_SERVER_URL);
        System.out.println("Workflow Submission Portlet: using Taverna 2 Server " + t2ServerURL);

        // Load the workflows once at initialisation time
        workflowFileNamesList = new ArrayList<String>();
        workflowNamesToInputsMap = new HashMap<String, ArrayList<WorkflowInputPort>>();

        // Directory containing workflows
        File dir = new File(getPortletContext().getRealPath(WORKFLOWS_DIRECTORY));
       
        // Filter only workflows i.e. files of type .t2flow
        FilenameFilter t2flowFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".t2flow");
            }
        };

        String[] workflowFileNames = dir.list(t2flowFilter);
        int numberOfLoadedWorkflowFiles = 0;
        for (int i=0; i<workflowFileNames.length; i++)
        { 
            // Get the workflow filename (without the .t2flow extension)
            String workflowFileName = workflowFileNames[i].substring(0, workflowFileNames[i].lastIndexOf('.'));
            workflowFileNamesList.add(workflowFileName);

            // Parse the workflow file
            FileInputStream workflowInputStream = null;
            try {
                workflowInputStream = new FileInputStream(getPortletContext().getRealPath(WORKFLOWS_DIRECTORY) + "/" + workflowFileNames[i]);
            } catch (FileNotFoundException e) {
                System.out.println("Workflow Submission Portlet: could not find workflow file " + getPortletContext().getRealPath(WORKFLOWS_DIRECTORY) + "/" + workflowFileNames[i]);
                return;
            }

            SAXBuilder builder = new SAXBuilder();
            Document workflowDocument;
            try {
            	workflowDocument = builder.build(workflowInputStream);
            } catch (JDOMException e) {
                System.out.println("Workflow Submission Portlet: could not parse the workflow file " + getPortletContext().getRealPath(WORKFLOWS_DIRECTORY) + "/" + workflowFileNames[i]);
                continue;
            } catch (IOException e) {
                System.out.println("Workflow Submission Portlet: could not open workflow file " + getPortletContext().getRealPath(WORKFLOWS_DIRECTORY) + "/" +  workflowFileNames[i]+ " to parse it.");
                continue;
            }

            // Parse the workflow file to determine its input parameters, if any
            ArrayList<WorkflowInputPort> workflowInputsList = new ArrayList<WorkflowInputPort>();
            Element workflowInputPortsElement = getTopDataflow(workflowDocument.getRootElement()).getChild(DATAFLOW_INPUT_PORTS_ELEMENT, T2_WORKFLOW_NAMESPACE);
            for (Element inputPortElement : (List<Element>)workflowInputPortsElement.getChildren(DATAFLOW_PORT, T2_WORKFLOW_NAMESPACE)) {
                String inputPortName = inputPortElement.getChildText(NAME, T2_WORKFLOW_NAMESPACE);
                int inputPortDepth = Integer.valueOf(inputPortElement.getChildText(DEPTH, T2_WORKFLOW_NAMESPACE));
		// Annotations
		//annotationsFromXml(dataflowInputPort, port, df.getClass().getClassLoader());

                WorkflowInputPort inputPort = new WorkflowInputPort(inputPortName, inputPortDepth);
                workflowInputsList.add(inputPort);
            }
            workflowNamesToInputsMap.put(workflowFileName, workflowInputsList);
            numberOfLoadedWorkflowFiles++;
        }
        System.out.println("Workflow Submission Portlet: successfully loaded " + numberOfLoadedWorkflowFiles + " out of " + workflowFileNames.length + " workflow files.");
    }

    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {
        System.out.println("Workflow Submission Portlet: procesing action");
        System.out.println();

        Enumeration names = request.getParameterNames();
        while(names.hasMoreElements()){
            String parameterName = (String) names.nextElement();
            System.out.println("Workflow Submission Portlet: parameter name: " + parameterName);
            System.out.println("Workflow Submission Portlet:: parameter value: " + request.getParameter(parameterName));
            System.out.println();
        }
        System.out.println();

        // Pass the input parameters over to the doView() and other render stage methods
        response.setRenderParameters(request.getParameterMap());

        // If there was a request to run a workflow
        if (request.getParameter(RUN_WORKFLOW) != null){

            // Workflow to run
            String workflowFileName = request.getParameter(WORKFLOW_NAME);
            File workflowFile = new File(getPortletContext().getRealPath(WORKFLOWS_DIRECTORY + "/" + workflowFileName + ".t2flow"));
            if (!workflowFile.exists()){
                // We are in trouble - the workflow file does not exist
                System.out.println("Workflow Submission Portlet: workflow file " + workflowFile.getAbsolutePath() + " is missing.");
                request.setAttribute(ERROR_MESSAGE, "Failed to submit workflow for execution: workflow file " + workflowFileName + " is missing.");
                return;
            }
            else{
                // Get the workflow inputs from the submitted form and fill the
                // list of inputs with the submitted values
                for (WorkflowInputPort inputPort : workflowNamesToInputsMap.get(workflowFileName)){
                    inputPort.setValue(request.getParameter(inputPort.getName()));
                }
                // Submit the workflow to the Taverna 2 Server

                // Submit the workflow's inputs to the Taverna 2 Server

                // Run the workflow on the Taverna 2 Server
            }
        }
    }

    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {
        response.setContentType("text/html");

        // Set the workflow file names list in the PortletSession if not already set
        if (request.getPortletSession().getAttribute(WORKFLOW_FILE_NAMES) == null){
            request.getPortletSession().setAttribute(WORKFLOW_FILE_NAMES, workflowFileNamesList);
        }

        // If a workflow has been selected - then also print the
        // input form for the selected workflow
        PortletRequestDispatcher dispatcher;
        dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowSubmission_view.jsp");
        dispatcher.include(request, response);
        if (request.getParameter(WORKFLOW_SELECTION_SUBMISSION) != null){
            response.getWriter().println("<br />");
            response.getWriter().println("<hr />");
            response.getWriter().println("<br />");
            String selectedWorkflowFileName = request.getParameter(SELECTED_WORKFLOW);
            // If we have a corresponding JSP file containing workflow's input form - dispatch to it
            File selectedWorkflowJSPFile = new File(getPortletContext().getRealPath(WORKFLOWS_DIRECTORY + "/" + selectedWorkflowFileName + ".jsp"));
            if (selectedWorkflowJSPFile.exists()){
                dispatcher = getPortletContext().getRequestDispatcher(WORKFLOWS_DIRECTORY + "/" + selectedWorkflowFileName + ".jsp");
                dispatcher.include(request, response);
            }
            else{ // We have to parse the workflow file and figure out the inputs ourselves

            }

        }
    }

    public void doEdit(RenderRequest request,RenderResponse response) throws PortletException,IOException {
            response.setContentType("text/html");        
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowSubmission_edit.jsp");
        dispatcher.include(request, response);
    }
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");        
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowSubmission_help.jsp");
        dispatcher.include(request, response);
    }

    private Element getTopDataflow(Element element) {
	Element result = null;
	for (Object elObj : element.getChildren(DATAFLOW_ELEMENT, T2_WORKFLOW_NAMESPACE)) {
		Element dataflowElement = (Element)elObj;
		if (DATAFLOW_ROLE_TOP.equals(dataflowElement.getAttribute(DATAFLOW_ROLE).getValue())) {
			result=dataflowElement;
		}
	}
	return result;
    }
}