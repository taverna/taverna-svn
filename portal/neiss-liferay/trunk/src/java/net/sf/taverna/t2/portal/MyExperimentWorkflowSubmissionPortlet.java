package net.sf.taverna.t2.portal;
import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.portlet.GenericPortlet;
import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.portlet.PortletRequestDispatcher;
import net.sf.taverna.t2.portal.myexperiment.MyExperimentClient;
import net.sf.taverna.t2.portal.myexperiment.Resource;
import net.sf.taverna.t2.portal.myexperiment.SearchEngine;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Fetches workflows from myExperiment (e.g. from a particular group)
 * and lets the user submit them for execution.
 */
public class MyExperimentWorkflowSubmissionPortlet extends GenericPortlet {

    // Base URL of the myExperiment
    private String MYEXPERIMENT_BASE_URL;
    
    private MyExperimentClient myExperimentClient;

    private int resultCountLimit = 20;

    // List of workflow resources fetched from myExperiment
    private ArrayList<net.sf.taverna.t2.portal.myexperiment.Workflow> myExperimentWorkflows = null;;
    private Map<String, String> myExperimentWorkflowsInputForms = null;

    // Namespace of this portlet
    private String PORTLET_NAMESPACE;

    @Override
    public void init(){

        // Get the URL of the myExperiment instance defined in web.xml as an
        // app-wide init parameter ( <context-param> element)
        MYEXPERIMENT_BASE_URL = getPortletContext().getInitParameter(Constants.MYEXPERIMENT_BASE_URL);

        // Create myExperiment client
        myExperimentClient = new MyExperimentClient(MYEXPERIMENT_BASE_URL);

    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {

        if (request.getParameter(Constants.MYEXPERIMENT_WORKFLOW_SEARCH) != null){

            System.out.println("myExperiment search terms:'" + request.getParameter(Constants.MYEXPERIMENT_SEARCH_TERMS) +"'");

            if (request.getParameter(Constants.MYEXPERIMENT_SEARCH_TERMS).equals("")){
                request.setAttribute(Constants.ERROR_MESSAGE, "Search criteria was empty. Please specify your search terms and try again.");
                return;
            }

            SearchEngine searchEngine = new SearchEngine(false, myExperimentClient); // no tag search

            // Create a search that will return all workflows available from the myExp Base URL
            SearchEngine.QuerySearchInstance getAllWorkflowsSearchQuery = new SearchEngine.QuerySearchInstance(request.getParameter(Constants.MYEXPERIMENT_SEARCH_TERMS), resultCountLimit, true, false, false, false, false);

            // Execute the search query to fetch the workflows from myExperiment
            Map<Integer, ArrayList<Resource>> allResources = searchEngine.searchAndPopulateResults(getAllWorkflowsSearchQuery);

            // We are expecting only workflow resources to be returned
            if (!allResources.isEmpty()) {
                for (int type : allResources.keySet()) {
                    if (type == Resource.WORKFLOW) {
                        myExperimentWorkflows = new ArrayList<net.sf.taverna.t2.portal.myexperiment.Workflow>();
                        myExperimentWorkflowsInputForms = new HashMap<String, String>();
                        for (Resource resource : allResources.get(type)) {
                            net.sf.taverna.t2.portal.myexperiment.Workflow workflow = (net.sf.taverna.t2.portal.myexperiment.Workflow) resource;
                            myExperimentWorkflows.add(workflow);
                            //myExperimentWorkflowsInputForms.put(resource.getResource(), createWorkflowInputForm(workflow)); // resource URI is the key
                            System.out.println("Content URI: "+workflow.getContentUri() + " Resource: "+ workflow.getResource() +" version: "+ workflow.getVersion() +" Uploader: "+ workflow.getUploader() +" Preview: "+ workflow.getPreview());
                            System.out.println(" URI: "+workflow.getURI() + " Thumbnail URI: "+workflow.getThumbnail() + " Thumbnail Big: "+ workflow.getThumbnailBig() +" Title: "+ workflow.getTitle() );
                        }
                        break;
                    }
                }
            }

            if (myExperimentWorkflows == null || myExperimentWorkflows.isEmpty()) {
                request.setAttribute(Constants.ERROR_MESSAGE, "0 workflows were found on myExperiment that match your searh criteria.");
            }
        }

        // Pass all request parameters over to the doView() and other render stage methods
        response.setRenderParameters(request.getParameterMap());

    }
    
    @Override
    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {

        if (PORTLET_NAMESPACE == null){
            PORTLET_NAMESPACE = response.getNamespace();
        }

        response.setContentType("text/html");

        PortletRequestDispatcher dispatcher;
        dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/MyExperimentWorkflowSubmission_view.jsp");
        dispatcher.include(request, response);

        if (request.getParameter(Constants.MYEXPERIMENT_WORKFLOW_SEARCH) != null){
            if (myExperimentWorkflows != null && !myExperimentWorkflows.isEmpty()){
                response.getWriter().println("<hr>");

                //<%-- Close form button --%>
                response.getWriter().println("<form name=\""+PORTLET_NAMESPACE+Constants.CLOSE_RESULTS_VIEW+"\" action=\"" + response.createRenderURL()+"\" >\n");
                response.getWriter().println("<p>\n");
                response.getWriter().println("<input type=\"image\" src=\""+request.getContextPath()+"/images/close.gif\" style=\"border:0;\" >\n");
                response.getWriter().println("</p>\n");
                response.getWriter().println("</form>\n");
                response.getWriter().println("<p><b>Showing top "+resultCountLimit+" results</b></p>");
                response.getWriter().println("<hr>");

                for (net.sf.taverna.t2.portal.myexperiment.Workflow myExprimentWorkflow : myExperimentWorkflows) {
                    String content = myExprimentWorkflow.createHTMLPreview(true);
                    response.getWriter().println(content);
                    response.getWriter().println("<hr>");
                }
            }
        }
        
    }

    @Override
    public void doEdit(RenderRequest request,RenderResponse response) throws PortletException,IOException {
            //Uncomment below lines to see the output
        //response.setContentType("text/html");
        //PrintWriter writer = response.getWriter(); 
        //writer.println("Edit Mode");
    }

    @Override
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException {
    }

    /*
     * Generates the HTML snippet containing the inputs form for a given
     * myExperiment workflow.
     */
    private String createWorkflowInputForm(net.sf.taverna.t2.portal.myexperiment.Workflow workflow) {
        // Get the workflow file and convert it into an XML document
        InputStream workflowBytes = null;
            System.out.println("myExperiment Workflow Submission Portlet: Workflow content URI: "+workflow.getContentUri());

        URL url;
        try {
            url = workflow.getContentUri().toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            workflowBytes = conn.getInputStream();
        } catch (IOException ex) {
            System.out.println("myExperiment Workflow Submission Portlet: Failed to download workflow "+workflow.getContentUri());
            ex.printStackTrace();
            return null;
        }

        Document workflowDocument;
        SAXBuilder builder = new SAXBuilder();
        try {
            workflowDocument = builder.build(workflowBytes);
        } catch (JDOMException ex1) {
            System.out.println("myExperiment Workflow Submission Portlet: " +
                    "Could not parse the workflow file " + workflow.getResource());
            ex1.printStackTrace();
            return null;
        } catch (IOException ex2) {
            System.out.println("Workflow Submission Portlet: Could not open workflow file " + workflow.getResource());
            ex2.printStackTrace();
            return null;
        }

        Element topWorkflow = Utils.getTopDataflow(workflowDocument.getRootElement()); // top workflow, ignore nested workflows

        // Get the workflow input parameters and their annotations, if any.
        ArrayList<WorkflowInputPort> workflowInputPorts = new ArrayList<WorkflowInputPort>();
        Element workflowInputPortsElement = topWorkflow.getChild(Constants.DATAFLOW_INPUT_PORTS_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE);
        for (Element inputPortElement : (List<Element>) workflowInputPortsElement.getChildren(Constants.DATAFLOW_PORT, Constants.T2_WORKFLOW_NAMESPACE)) {

            Element workflowInputPortAnnotationsElement = inputPortElement.getChild(Constants.ANNOTATIONS_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE);

            // Get the input port's name and depth
            String inputPortName = inputPortElement.getChildText(Constants.NAME_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE);
            int inputPortDepth = Integer.valueOf(inputPortElement.getChildText(Constants.DEPTH_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE));

            WorkflowInputPort inputPort = new WorkflowInputPort();
            inputPort.setName(inputPortName);
            inputPort.setDepth(inputPortDepth);
            inputPort.setDescription(Utils.getLatestAnnotationAssertionImplElementValue(workflowInputPortAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_FREETEXT_CLASS, workflow.getResource()));
            inputPort.setExampleValue(Utils.getLatestAnnotationAssertionImplElementValue(workflowInputPortAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_EXAMPLEVALUE_CLASS, workflow.getResource()));
            workflowInputPorts.add(inputPort);

            System.out.println("myExperiment Workflow Submission Portlet: Input port name: " + inputPortName + ", depth: " + inputPortDepth + ", description: " + inputPort.getDescription() + ", example value: " + inputPort.getExampleValue());
        }

        StringBuffer inputForm = new StringBuffer();

        // Workflow inputs        
        inputForm.append("<p><b>Workflow inputs:</b></p>\n");

        if (workflowInputPorts.isEmpty()) {
            inputForm.append("The workflow does not require any inputs<br><br>\n");
            inputForm.append("<form name=\"<portlet:namespace/><%= Constants.WORKFLOW_INPUTS_FORM%>\" action=\"<portlet:actionURL/>\" method=\"post\" enctype=\"multipart/form-data\" onSubmit=\"return validateForm(this)\">\n");
        } else {
            inputForm.append("<form name=\"<portlet:namespace/><%= Constants.WORKFLOW_INPUTS_FORM%>\" action=\"<portlet:actionURL/>\" method=\"post\" enctype=\"multipart/form-data\" onSubmit=\"return validateForm(this)\">\n");

            inputForm.append("<table class=\"inputs_entry\">\n");
            inputForm.append("<tr>\n");
            inputForm.append("<th>Name</th>\n");
            inputForm.append("<th>Type</th>\n");
            inputForm.append("<th>Description</th>\n");
            inputForm.append("<th>Value</th>\n");
            inputForm.append("</tr>\n");

            // Loop over the workflow inputs and create a row with input fields
            // in the table for each one of them
            int counter = 1;
            for (WorkflowInputPort inputPort : workflowInputPorts) {
                if (inputPort.getDepth() == 0) { // single input
                    if (counter % 2 == 0) { // alternate row colours
                        inputForm.append("<tr style=\"background-color: #F5F5F5;\" " + Constants.INPUT_PORT_NAME_ATTRIBUTE + "=\"" + inputPort.getName() + "\">\n");
                    } else {
                        inputForm.append("<tr " + Constants.INPUT_PORT_NAME_ATTRIBUTE + "=\"" + inputPort.getName() + "\">\n");
                    }
                    inputForm.append("<td>" + inputPort.getName() + "</td>\n");
                    inputForm.append("<td>single value</td>\n");
                    String descriptionString = "";
                    if (inputPort.getDescription() != null && !inputPort.getDescription().equals("")) {
                        descriptionString += inputPort.getDescription().trim();
                    }
                    if (inputPort.getExampleValue() != null && !inputPort.getExampleValue().equals("")) {
                        if (!descriptionString.equals("")) {
                            descriptionString += "<br><br>";
                        }
                        descriptionString += "<b>Example value:</b><textarea id=\"textarea_with_no_decoration"
                                + counter
                                + "\" readonly=\"true\" style=\"border:none;overflow:hidden;\">"
                                + inputPort.getExampleValue()
                                + "</textarea><script language=\"javascript\">adjustRows(document.getElementById(\"textarea_with_no_decoration"
                                + counter + "\"));</script>";
                    }
                    inputForm.append("<td>" + descriptionString + "</td>\n");
                    inputForm.append("<td>\n");
                    inputForm.append("Paste the value here: <br>\n");
                    inputForm.append("<textarea name=\"<portlet:namespace/>" + inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX + "\" rows=\"2\" cols=\"20\" wrap=\"off\"></textarea><br>\n");
                    inputForm.append("Or load the value from a file: <br />\n");
                    inputForm.append("<input type=\"file\" name=\"<portlet:namespace/>" + inputPort.getName() + Constants.WORKFLOW_INPUT_FILE_SUFFIX + "\" />\n");
                    inputForm.append("</td>\n");
                    inputForm.append("</tr>\n");
                } else if (inputPort.getDepth() == 1) {
                    if (counter % 2 == 0) { // alternate row colours
                        inputForm.append("<tr bgcolor=\"#F5F5F5\" " + Constants.INPUT_PORT_NAME_ATTRIBUTE + "=\"" + inputPort.getName() + "\">\n");
                    } else {
                        inputForm.append("<tr " + Constants.INPUT_PORT_NAME_ATTRIBUTE + "\"" + inputPort.getName() + "\">\n");
                    }
                    inputForm.append("<td>" + inputPort.getName() + "</td>\n");
                    inputForm.append("<td>list</td>\n");
                    String descriptionString = "";
                    if (inputPort.getDescription() != null && !inputPort.getDescription().equals("")) {
                        descriptionString += inputPort.getDescription().trim();
                    }
                    if (inputPort.getExampleValue() != null && !inputPort.getExampleValue().equals("")) {
                        if (!descriptionString.equals("")) {
                            descriptionString += "<br><br>";
                        }
                        descriptionString += "<b>Example value:</b><textarea id=\"textarea_with_no_decoration"
                                + counter
                                + "\" readonly=\"true\" style=\"border:none;overflow:hidden;\">"
                                + inputPort.getExampleValue()
                                + "</textarea><script language=\"javascript\">adjustRows(document.getElementById(\"textarea_with_no_decoration"
                                + counter + "\"));</script>";
                    }
                    inputForm.append("<td>" + descriptionString + "</td>\n");
                    inputForm.append("<td>\n");
                    inputForm.append("Paste the list values here: <br>\n");
                    inputForm.append("<textarea name=\"<portlet:namespace/>" + inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX + "\" rows=\"2\" cols=\"20\" wrap=\"off\"></textarea><br>\n");
                    inputForm.append("Or load them from a file: <br />\n");
                    inputForm.append("<input type=\"file\" name=\"<portlet:namespace/>" + inputPort.getName() + Constants.WORKFLOW_INPUT_FILE_SUFFIX + "\" /><br><hr/>\n");
                    inputForm.append("Use the following character sequence as the list item separator:\n");
                    inputForm.append("<select name=\"<portlet:namespace/>" + inputPort.getName() + "_separator\">\n");
                    inputForm.append("<option value=\"" + WorkflowSubmissionPortlet.NEW_LINE_LINUX_SEPARATOR + "\">New line - Unix/Linux (\\n)</option>\n");
                    inputForm.append("<option value=\"" + WorkflowSubmissionPortlet.NEW_LINE_WINDOWS_SEPARATOR + "\">New line - Windows (\\r\\n)</option>\n");
                    inputForm.append("<option value=\"" + WorkflowSubmissionPortlet.BLANK_SEPARATOR + "\">Blank (' ')</option>\n");
                    inputForm.append("<option value=\"" + WorkflowSubmissionPortlet.TAB_SEPARATOR + "\">Tab (\\t)</option>\n");
                    inputForm.append("<option value=\"" + WorkflowSubmissionPortlet.COLON_SEPARATOR + "\">Colon (:)</option>\n");
                    inputForm.append("<option value=\"" + WorkflowSubmissionPortlet.SEMI_COLON_SEPARATOR + "\">Semi-colon (;)</option>\n");
                    inputForm.append("<option value=\"" + WorkflowSubmissionPortlet.COMMA_SEPARATOR + "\">Comma (,)</option>\n");
                    inputForm.append("<option value=\"" + WorkflowSubmissionPortlet.DOT_SEPARATOR + "\">Dot (.)</option>\n");
                    inputForm.append("<option value=\"" + WorkflowSubmissionPortlet.PIPE_SEPARATOR + "\">Pipe (|)</option>\n");
                    inputForm.append("</select><br />\n");
                    inputForm.append("Or specify your own separator:\n");
                    inputForm.append("<input type=\"text\" name=\"<portlet:namespace/>" + inputPort.getName() + "_other_separator\" size=\"3\" />\n");
                    inputForm.append("</td>\n");
                    inputForm.append("</tr>\n");
                } else { // We cannot handle workflows with input of depth more than 1
                    System.out.println("Workflow "+workflow.getResource()+" contains inputs of depth more than 1 (i.e. a list of lists or higher). This is not supported at the moment - skipping this workflow.");
                    return null;
                }
                counter++;
            }
            inputForm.append("</table>\n");
        }

        inputForm.append("<%-- A field for the user to enter a description for the wf run. --%>\n");
        inputForm.append("<br><b>Enter a short description of the workflow run (so you can more easily identify it later):</b><br>");
        inputForm.append("<input type=\"text\" size=\"50\" name=\"<portlet:namespace/><%= Constants.WORKFLOW_RUN_DESCRIPTION%>\"/><br><br>");

        inputForm.append("<%-- Hidden field to convey which workflow we want to execute --%>\n");
        inputForm.append("<input type=\"hidden\" name=\"<portlet:namespace/><%= Constants.WORKFLOW_FILE_NAME%>\" value=\"" + workflow.getResource() + "\" />\n");
        inputForm.append("<input type=\"submit\" name=\"<portlet:namespace/><%= Constants.RUN_WORKFLOW%>\" value=\"Run workflow\" />\n");
        inputForm.append("</form><br>\n");

        return inputForm.toString();
    }

    
}