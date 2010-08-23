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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.portlet.PortletRequestDispatcher;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.jdom.output.XMLOutputter;

/**
 * WorkflowSubmission Portlet - enables user to select a workflow,
 * specify the values for its inputs and submit it for execution to
 * a Taverna 2 Server.
 *
 * @author Alex Nenadic
 */
public class WorkflowSubmissionPortlet extends GenericPortlet {

    public static final String WORKFLOWS_DIRECTORY = "/WEB-INF/workflows";

    // HTML form fields
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
    // Baclava documents XML namespace
    private static Namespace BACLAVA_NAMESPACE = Namespace.getNamespace("b","http://org.embl.ebi.escience/baclava/0.1alpha");
    // XML workflow elements
    public static final String DATAFLOW_ELEMENT = "dataflow";
    public static final String DATAFLOW_ROLE = "role";
    public static final String DATAFLOW_ROLE_TOP  = "top";
    public static final String DATAFLOW_INPUT_PORTS_ELEMENT = "inputPorts";
    public static final String DATAFLOW_PORT = "port";
    public static final String NAME = "name";
    public static final String DEPTH = "depth";

    // REST
    public static final String T2_SERVER_URL_PARAMETER = "t2_server_url";
    public static final String T2_SERVER_NAMESPACE = "http://ns.taverna.org.uk/2010/xml/server/";
    public static final String T2_SERVER_WORKFLOW_ELEMENT = "workflow";
    public static final String RUNS_URL = "/rest/runs";
    public static final String BACLAVA_INPUTS_URL = "/input/baclava";
    public static final String STATUS_URL = "/status";
    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    public static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";
    public static final String LOCATION_HEADER_NAME = "Location";
    // XML input message element
    public static final String WORKFLOW_INPUT_ELEMENT = "runInput";


    // Address of the T2 Server
    String t2ServerURL;

    // List of workflow file names, which are located in /WEB-INF/workflows folder in the app root.
    private static ArrayList<String> workflowFileNamesList;

    // List of wrapped workflow XML objects that get submitted for execution on the T2 Server
    private static ArrayList<Document> wrappedWorkflowXMLDocumentsList;

    // String representation of the workflow XML objects
    private static ArrayList<String> wrappedWorkflowXMLDocumentStringsList;

    // Map of workflow file names to a list of workflow's inputs
    private static HashMap<String, ArrayList<WorkflowInputPort>> workflowNamesToInputsMap;

    @Override
    public void init(){

        // Get the URL of the T2 Server
        t2ServerURL = getPortletConfig().getInitParameter(T2_SERVER_URL_PARAMETER);

        // Load the workflows once at initialisation time
        workflowFileNamesList = new ArrayList<String>();
        wrappedWorkflowXMLDocumentsList = new ArrayList<Document>();
        wrappedWorkflowXMLDocumentStringsList = new ArrayList<String>();
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
       
        SAXBuilder builder = new SAXBuilder();
        XMLOutputter xmlOutputter = new XMLOutputter();

        for (int i=0; i<workflowFileNames.length; i++)
        { 
            // Get the workflow filename (without the .t2flow extension)
            String workflowFileName = workflowFileNames[i].substring(0, workflowFileNames[i].lastIndexOf('.'));

            // Parse the workflow file
            FileInputStream workflowInputStream = null;
            Document workflowDocument;
            try {
                workflowInputStream = new FileInputStream(getPortletContext().getRealPath(WORKFLOWS_DIRECTORY) + "/" + workflowFileNames[i]);
            } catch (FileNotFoundException e) {
                System.out.println("Workflow Submission Portlet: could not find workflow file " + getPortletContext().getRealPath(WORKFLOWS_DIRECTORY) + "/" + workflowFileNames[i]);
                continue;
            }
            try {
            	workflowDocument = builder.build(workflowInputStream);
            } catch (JDOMException ex1) {
                System.out.println("Workflow Submission Portlet: could not parse the workflow file " + getPortletContext().getRealPath(WORKFLOWS_DIRECTORY) + "/" + workflowFileNames[i]);
                ex1.printStackTrace();
                continue;
            } catch (IOException ex2) {
                System.out.println("Workflow Submission Portlet: could not open workflow file " + getPortletContext().getRealPath(WORKFLOWS_DIRECTORY) + "/" +  workflowFileNames[i]+ " to parse it.");
                ex2.printStackTrace();
                continue;
            }
            // Get the input parameters, if any, from the workflow file
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

            // Wrap the workflow document inside a <workflow> element
            // in the T2 Server namespace as expected by the Server
            Element workflowWrapperElement = new Element(T2_SERVER_WORKFLOW_ELEMENT, T2_SERVER_NAMESPACE);
            Element oldWorkflowRootElement = workflowDocument.getRootElement();
            oldWorkflowRootElement.detach(); // detach it from the previous document
            workflowWrapperElement.addContent(oldWorkflowRootElement); // attach the old root to the new root
            Document wrappedWorkflowDocument = new Document(workflowWrapperElement);

            workflowFileNamesList.add(workflowFileName);
            wrappedWorkflowXMLDocumentsList.add(wrappedWorkflowDocument);
            wrappedWorkflowXMLDocumentStringsList.add(xmlOutputter.outputString(wrappedWorkflowDocument));
            workflowNamesToInputsMap.put(workflowFileName, workflowInputsList);
            numberOfLoadedWorkflowFiles++;
        }
        System.out.println("Workflow Submission Portlet: Successfully loaded " + numberOfLoadedWorkflowFiles + " out of " + workflowFileNames.length + " workflow files.");
    }

    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {
        Enumeration names = request.getParameterNames();
        while(names.hasMoreElements()){
            String parameterName = (String) names.nextElement();
            System.out.println("Workflow Submission Portlet: parameter name: " + parameterName);
            System.out.println("Workflow Submission Portlet:: parameter value: " + request.getParameter(parameterName));
            System.out.println();
        }
        System.out.println();

        // If there was a request to run a workflow
        if (request.getParameter(RUN_WORKFLOW) != null){

            // Workflow to run
            String workflowFileName = request.getParameter(WORKFLOW_NAME);

            // Get the workflow inputs from the submitted form and fill the
            // list of inputs with the submitted values
            for (WorkflowInputPort inputPort : workflowNamesToInputsMap.get(workflowFileName)){
                inputPort.setValue(request.getParameter(inputPort.getName()));
            }
            // Submit the workflow to the T2 Server in preparation for execution
            HttpResponse httpResponse = submitWorkflow(t2ServerURL + RUNS_URL, workflowFileName, request);

            // Submit the workflow's inputs to the Taverna 2 Server in
            // prepartion for workflow execution
            if (httpResponse != null){ // null indicates something went wrong

                // Extract the workflowID, you get something like http://<SERVER>/taverna-server/rest/runs/UUID
                // in the Location header, where UUID part identifies the submitted workflow on the T2 Server
                String workflowID;
                workflowID = httpResponse.getHeaders(LOCATION_HEADER_NAME)[0].getValue();
                workflowID = workflowID.substring(workflowID.lastIndexOf("/") + 1);
                System.out.println("Workflow Submission Portlet: Workflow " + workflowFileName + " successfully submitted to the Server with UUID " + workflowID);

                boolean inputsSubmitted = submitWorkflowInputs(t2ServerURL + RUNS_URL + "/" + workflowID + BACLAVA_INPUTS_URL, workflowFileName, workflowID, workflowNamesToInputsMap.get(workflowFileName), request);

                // Run the workflow on the Taverna 2 Server
                if (inputsSubmitted){
                    System.out.println("Workflow Submission Portlet: Inputs for workflow " + workflowFileName + " successfully submitted to the Server");

                    boolean runSubmitted = runWorkflow(t2ServerURL + RUNS_URL + "/" + workflowID + STATUS_URL, workflowFileName, request);
                    if (runSubmitted){
                        System.out.println("Workflow Submission Portlet: An execution of workflow " + workflowFileName + " successfully initiated on the Server");
                    }
                }
            }
        }

        // Pass all request parameters over to the doView() and other render stage methods
        response.setRenderParameters(request.getParameterMap());
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

    /*
     HTTP POSTs workflow document to the T2 Server.
     */
    HttpResponse submitWorkflow(String url, String workflowFileName, ActionRequest request){

        HttpClient httpClient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_APPLICATION_XML);

       try {
            // Convert the workflow XML document into a string
            int workflowIndex = workflowFileNamesList.indexOf(workflowFileName); // get the workflow index
            String workflowString = wrappedWorkflowXMLDocumentStringsList.get(workflowIndex);

            System.out.println("Workflow Submission Portlet: Preparing to submit workflow to Server " + url);
            System.out.println(workflowString);

            StringEntity entity = new StringEntity(workflowString, "UTF-8");
            httpPost.setEntity(entity);
        }
        catch (UnsupportedEncodingException ex) {
        
            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity for workflow " + workflowFileName + " when POSTing the workflow to the Server.");
            ex.printStackTrace();
            request.setAttribute(ERROR_MESSAGE, "Failed to create message body for workflow " + workflowFileName + " when POSTing the worfkflow to the Server.");
            return null;
        }
        
        HttpResponse httpResponse = null;
        try{
            // Execute the request
            HttpContext localContext = new BasicHttpContext();
            httpResponse = httpClient.execute(httpPost, localContext);

            // Release resource
            httpClient.getConnectionManager().shutdown();
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to POST request to submit workflow " + workflowFileName + " for execution.");
            ex.printStackTrace();
            request.setAttribute(ERROR_MESSAGE, "Failed to submit workflow " + workflowFileName + " for execution: HTTP POST failed.");
            return null;
        }

        if (httpResponse.getStatusLine().getStatusCode() != 201){ // HTTP/1.1 201 Created
            System.out.println("Workflow Submission Portlet: Failed to submit workflow " + workflowFileName + " for execution.\nServer responded with: " + httpResponse.getStatusLine());
            request.setAttribute(ERROR_MESSAGE, "Failed to submit workflow " + workflowFileName + " for execution.\nServer responded with: " + httpResponse.getStatusLine());
            return null;
        }

        return httpResponse;
    }

    /*
     HTTP PUTs workflow inputs to the T2 Server.
     */
    boolean submitWorkflowInputs(String url, String workflowFileName, String workflowID, ArrayList<WorkflowInputPort> workflowInputs, ActionRequest actionRequest){

        HttpClient httpClient = new DefaultHttpClient();

        HttpContext localContext = new BasicHttpContext();

        HttpPut httpPut = new HttpPut(url);
        //httpPut.setHeader(CONTENT_TYPE_HEADER_NAME, CONTENT_TYPE_APPLICATION_XML);

        HttpResponse httpResponse = null;

        // Get the workflow inputs as a Baclava XML document
        Document worfklowInputsDocument = buildWorkflowInputsBaclavaDocument(workflowInputs);
        String workflowDocumentString = new XMLOutputter().outputString(worfklowInputsDocument);

        try {
            StringEntity entity = new StringEntity(workflowDocumentString, "UTF-8");
            httpPut.setEntity(entity);
        }
        catch (UnsupportedEncodingException ex) {
        
            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity containing Baclava XML document with workflow inputs for workflow " + workflowFileName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(ERROR_MESSAGE, "Failed to create message body containing XML document with workflow inputs for workflow " + workflowFileName + ".");
            return false;
        }

        try{
            // Execute the request
            httpResponse = httpClient.execute(httpPut, localContext);

            // Release resource
            httpClient.getConnectionManager().shutdown();

            if (httpResponse.getStatusLine().getStatusCode() != 201){ // HTTP/1.1 201 Created
                System.out.println("Workflow Submission Portlet: Resource for the inputs of workflow " + workflowFileName + " not created on the Server. The Server responded with: " + httpResponse.getStatusLine());
                actionRequest.setAttribute(ERROR_MESSAGE, "An error occured when submitting inputs for workflow " + workflowFileName + ".\nThe Server responded with: " + httpResponse.getStatusLine());
                return false;
            }
       }
       catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to PUT the inputs of workflow " + workflowFileName + " to the Server.");
            ex.printStackTrace();
            actionRequest.setAttribute(ERROR_MESSAGE, "Failed to submit inputs of workflow " + workflowFileName + " to the Server.");
            return false;
       }
        
       return true;
    }

    /*
     HTTP PUTs the status of the workflow to "Operating" to kick start its execution.
     */
    boolean runWorkflow(String url, String workflowFileName, ActionRequest actionRequest){
        
        HttpClient httpClient = new DefaultHttpClient();

        HttpPut httpPut = new HttpPut(url);

        HttpResponse httpResponse = null;
        try{
            // Execute the request
            HttpContext localContext = new BasicHttpContext();
            httpResponse = httpClient.execute(httpPut, localContext);

            // Release resource
            httpClient.getConnectionManager().shutdown();
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to initiate workflow run for " + workflowFileName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(ERROR_MESSAGE, "An error occured when initiating an execution of workflow " + workflowFileName +  ".");
            return false;
        }
        
        if (httpResponse.getStatusLine().getStatusCode() != 200){ // HTTP/1.1 200 OK
           System.out.println("Workflow Submission Portlet: Failed to initiate an execution of workflow " + workflowFileName + ".\nThe Server responded with: " + httpResponse.getStatusLine());
           actionRequest.setAttribute(ERROR_MESSAGE, "Failed to initiate an execution of workflow " + workflowFileName + ".\nThe Server responded with: " + httpResponse.getStatusLine());
           return false;
        }
        return true;
    }

    /*
     Builds a Baclava XML document for workflow inputs to submit them all in one go
     to the T2 Server.
     */
    Document buildWorkflowInputsBaclavaDocument(ArrayList<WorkflowInputPort> workflowInputs){

        // Build the DataThing map from the inputPanelMap
	Map<String, Object> valueMap = new HashMap<String, Object>();
	for (WorkflowInputPort workflowInputPort : workflowInputs) {
            String inputPortName = workflowInputPort.getName();
            Object inputPortValue = workflowInputPort.getValue();
            valueMap.put(inputPortName, inputPortValue);
	}
	Map<String, DataThing> dataThings = bakeDataThingMap(valueMap);

	// Build the XML Baclava document containing the workflow inputs
	Document document = getDataDocument(dataThings);

        return document;
    }

    /*
    Returns a map of input port names to DataThings from a map of input port names to a
    value for that port (which can be a single value or a list of (lists of ...) of values).
    */
    Map<String, DataThing> bakeDataThingMap(Map<String, Object> inputsMap){

        Map<String, DataThing> dataThingMap = new HashMap<String, DataThing>();
        
        for (String inputPortName : inputsMap.keySet()) {
            dataThingMap.put(inputPortName, DataThingFactory.bake(inputsMap.get(inputPortName)));
        }
        return dataThingMap;
    }

    /*
    Returns a org.jdom.Document from a map of input port names to DataThingS containing
    the input port's values.
    */
    public static Document getDataDocument(Map<String, DataThing> dataThings) {
	Element rootElement = new Element("dataThingMap", BACLAVA_NAMESPACE);
	Document document = new Document(rootElement);
	for (String key : dataThings.keySet()) {
		DataThing value = (DataThing) dataThings.get(key);
		Element dataThingElement = new Element("dataThing", BACLAVA_NAMESPACE);
		dataThingElement.setAttribute("key", key);
		dataThingElement.addContent(value.getElement());
		rootElement.addContent(dataThingElement);
	}
	return document;
    }
}