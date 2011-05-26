package net.sf.taverna.t2.portal;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import javax.portlet.GenericPortlet;
import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderResponse;
import javax.portlet.PortletException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import net.sf.taverna.t2.portal.myexperiment.MyExperimentClient;
import net.sf.taverna.t2.portal.myexperiment.Resource;
import net.sf.taverna.t2.portal.myexperiment.SearchEngine;
import net.sf.taverna.t2.portal.myexperiment.ServerResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;
import sun.misc.BASE64Encoder;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.tool.api.SessionManager;

/**
 * Workflow Submission Portlet - enables user to select a workflow
 * from a list of pre-uploaded workflows or search myExperiment to find one,
 * specify the values for its inputs and submit it for execution to
 * a Taverna 2 Server.
 *
 * It also enables logged in users to upload new workflows to the list of
 * pre-selected workflows.
 *
 * @author Alex Nenadic
 */
public class WorkflowSubmissionPortlet extends GenericPortlet {

    // HTML form fields
    public static final String WORKFLOW_INPUTS_FORM = "workflow_inputs_form";
    public static final String WORKFLOW_FILE_NAME = "workflow_file_name";
    public static final String WORKFLOW_INPUT_CONTENT_SEPARATOR_SUFFIX = "_separator";
    public static final String WORKFLOW_INPUT_CONTENT_OTHER_SEPARATOR_SUFFIX = "_other_separator";
    public static final String NEW_LINE_LINUX_SEPARATOR = "new_line_linux";
    public static final String NEW_LINE_WINDOWS_SEPARATOR = "new_line_windows";
    public static final String BLANK_SEPARATOR = "blank";
    public static final String TAB_SEPARATOR = "tab";
    public static final String COMMA_SEPARATOR = "comma";
    public static final String COLON_SEPARATOR = "colon";
    public static final String SEMI_COLON_SEPARATOR = "semi_colon";
    public static final String DOT_SEPARATOR = "dot";
    public static final String PIPE_SEPARATOR = "pipe";
    public static final String MYEXPERIMENT_FILE_NAME_PREFIX = "myexperiment_workflow_id_";

    // Address of the T2 Server
    private String T2_SERVER_URL;

    // Directory where info for all submitted jobs for all users is persisted
    private File JOBS_DIR;

    // A list of workflow file names, which are
    // located in /WEB-INF/workflows folder in the app root.
    private static ArrayList<String> workflowFileNamesList;

    // A list of workflow objects, filled in with info parsed
    // from the workflow definition files.
    private static ArrayList<Workflow> workflowList;

    // A list of wrapped workflow XML objects that get
    // submitted for execution on the T2 Server
    private static ArrayList<Document> wrappedWorkflowXMLDocumentsList;

    // A list of lists of workflow input ports
    private static ArrayList<ArrayList<WorkflowInputPort>> workflowInputPortsList;

    // Namespace of this portlet
    private static String PORTLET_NAMESPACE;

    // Base URL of the myExperiment
    private String MYEXPERIMENT_BASE_URL;

    private MyExperimentClient myExperimentClient;

    public static int myExperimentResultCountLimit;

    // List of workflow resources fetched from myExperiment
    private ArrayList<net.sf.taverna.t2.portal.myexperiment.Workflow> myExperimentWorkflows = null;;
    private HashMap<String, String> myExperimentWorkflowsInputForms = null;

    /*
     * Do the init stuff one at portlet loading time.
     */
    @Override
    public void init(){

        // Get the URL of the T2 Server defined in web.xml as an
        // app-wide init parameter ( <context-param> element)
        T2_SERVER_URL = getPortletContext().getInitParameter(Constants.T2_SERVER_URL_PROPERTY);

        // Get the directory where info for submitted jobs for all users is persisted
        JOBS_DIR = new File(getPortletContext().getInitParameter(Constants.JOBS_DIRECTORY_PATH_PROPERTY));

        // Get the URL of the myExperiment instance defined in web.xml as an
        // app-wide init parameter ( <context-param> element)
        MYEXPERIMENT_BASE_URL = getPortletContext().getInitParameter(Constants.MYEXPERIMENT_BASE_URL_PROPERTY);

        myExperimentResultCountLimit = Integer.parseInt(getPortletContext().getInitParameter(Constants.MYEXPERIMENT_MAX_RESULTS_TO_DISPLAY_PROPERTY));

        // Create myExperiment client
        myExperimentClient = new MyExperimentClient(MYEXPERIMENT_BASE_URL);

        // Directory containing local workflows
        File dir = new File(getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY));
        System.out.println("Workflow Submission Portlet: Using workflows directory " + dir);

        // Load the local workflows once at initialisation time
        workflowFileNamesList = new ArrayList<String>();
        workflowList = new ArrayList<Workflow>();
        wrappedWorkflowXMLDocumentsList = new ArrayList<Document>();
        workflowInputPortsList = new ArrayList<ArrayList<WorkflowInputPort>>();

        String[] workflowFileNames = dir.list(WorkflowResultsPortlet.t2flowFileFilter);
        int numberOfLoadedWorkflowFiles = 0;
       
        for (String workflowFileName : workflowFileNames)
        { 
            if (addWorkflow(workflowFileName)){
                numberOfLoadedWorkflowFiles++;
            }
        }
        System.out.println("Workflow Submission Portlet: Successfully loaded " + numberOfLoadedWorkflowFiles + " out of " + workflowFileNames.length + " workflow files.\n");
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {

        // Just print all the parameters we have received, for testing purposes
        Enumeration names = request.getParameterNames();
        while(names.hasMoreElements()){
            String parameterName = (String) names.nextElement();
            System.out.println("\nWorkflow Submission Portlet processAction: parameter name: " + parameterName);
            System.out.println("Workflow Submission Portlet processAction: parameter value: " + request.getParameter(parameterName));
            System.out.println();
        }

        // Is this a multipart/form-data form submission request?
        // Forms used to submit files are multipart. This is inputs form submission reques then.

        // When form is multipart, you cannot get request parameters the
        // usual way - you have to parse the request yourself!!!
        boolean isMultipart = PortletFileUpload.isMultipartContent(request);
        if (isMultipart){

            // Create a factory for disk-based file items
            FileItemFactory factory = new DiskFileItemFactory();

            // Create a new file upload handler
            PortletFileUpload upload = new PortletFileUpload(factory);

            // Parse the request to get the submitted items
            List<FileItem> items = null;
            try{
                items = upload.parseRequest(request);
            }catch(FileUploadException fuex){
                System.out.println("Workflow Submission Portlet: Failed to parse the submitted input form values.");
                fuex.printStackTrace();
                request.setAttribute(Constants.ERROR_MESSAGE, "Failed to parse the submitted input form values.");
                return;
            }

            // Process the uploaded items
            Map<String, Object> formItems = new HashMap<String, Object>();
            for (FileItem item : items) {
                // Regular input form field
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    String fieldValue = item.getString();
                    // Get rid of the potlet namespace prefix
                    if (fieldName.startsWith(PORTLET_NAMESPACE)){
                        fieldName = fieldName.substring(PORTLET_NAMESPACE.length());
                    }
                    formItems.put(fieldName, fieldValue);
                    System.out.println("Workflow Submission Portlet: multipart form parameter name (without namespace prefix): " + fieldName + ", value: " + fieldValue);
                } else { // File upload form field
                    String fieldName = item.getFieldName();
                    String fileName = item.getName();
                    if (!fileName.equals("")){ // if a file was submitted
                        // Get rid of the potlet namespace prefix
                        if (fieldName.startsWith(PORTLET_NAMESPACE)){
                            fieldName = fieldName.substring(PORTLET_NAMESPACE.length());
                        }
                        InputStream uploadedStream = item.getInputStream();
                        formItems.put(fieldName, uploadedStream);
                        formItems.put(fieldName+"_file_name", fileName); // save the file name as well
                        System.out.println("Workflow Submission Portlet: multipart form parameter name (without namespace prefix): " + fieldName + ", file name: " + fileName);
                    }
                }
            }

            // Was there a request to run a workflow?
            if (formItems.keySet().contains(Constants.RUN_WORKFLOW)){

                // Workflow to run - either local or from myExperiment
                Workflow workflow;
                // Workflow's input ports
                ArrayList<WorkflowInputPort> workflowInputPorts;

                String workflowName;

                if (formItems.get(WORKFLOW_FILE_NAME) != null) { // request to run a local workflow
                    String workflowFileName =(String) formItems.get(WORKFLOW_FILE_NAME);
                    workflowName = workflowFileName;
                    int index = workflowFileNamesList.indexOf(workflowFileName);
                    workflow = workflowList.get(index);
                    workflowInputPorts = workflowInputPortsList.get(index);
                } else { //request to run a workflow from myExperiment
                    int index = -1;
                    String resource = URLDecoder.decode((String) formItems.get(Constants.WORKFLOW_RESOURCE_ON_MYEXPERIMENT), "UTF-8");
                    for (int i = 0; i < myExperimentWorkflows.size(); i++) {
                        if (myExperimentWorkflows.get(i).getResource().equals(resource)) {
                            index = i;
                            break;
                        }
                    } // if we do not find it in the list - we are in trouble
                    net.sf.taverna.t2.portal.myexperiment.Workflow myExperimentWorkflow = myExperimentWorkflows.get(index);
                    workflow = new Workflow();
                    workflow.setIsMyExperimentWorkflow(true);
                    workflow.setMyExperimentWorkflowResource(myExperimentWorkflow.getResource());
                    workflow.setMyExperimentWorkflowVersion(myExperimentWorkflow.getVersion());
                    workflow.setWorkflowDocument(myExperimentWorkflow.getWorkflowDocument());
                    workflow.setWorkflowInputPorts(myExperimentWorkflow.getWorkflowInputPorts());
                    workflowInputPorts = myExperimentWorkflow.getWorkflowInputPorts();
                    workflowName = myExperimentWorkflow.getResource();
               }

                // Description of the wf run
                String workflowRunDescription = (String) formItems.get(Constants.WORKFLOW_RUN_DESCRIPTION);

                boolean inputsSuccessfullyRead = true;

                // Get the workflow inputs from the submitted form and fill the
                // list of inputs with the submitted values
                for (WorkflowInputPort inputPort : workflowInputPorts){

                    // Do we have this input's content as a file or actual content was submitted?
                    // If we have a file that overrides the submitted content.
                    if (formItems.get(inputPort.getName() + Constants.WORKFLOW_INPUT_FILE_SUFFIX) != null){

                        // Read the contents of a file as a byte[]
                        InputStream is = (InputStream) formItems.get(inputPort.getName() + Constants.WORKFLOW_INPUT_FILE_SUFFIX);
                        byte[] theBytes;
                        try{
                            theBytes = new byte[is.available()];
                            is.read(theBytes);
                        }catch(Exception ex){
                            System.out.println("Workflow Submission Portlet: Failed to read the submitted file for input " + inputPort.getName() + " of workflow " + workflowName + ".");
                            ex.printStackTrace();
                            request.setAttribute(Constants.ERROR_MESSAGE, "Failed to read the submitted file for input " + inputPort.getName() + " of workflow " + workflowName + ".");
                            inputsSuccessfullyRead = false;
                            return;
                        }finally{
                            try{
                                is.close();
                            }catch (Exception ex2){
                                // Do nothing
                            }
                        }

                        // Is this a single value input?
                        if (inputPort.getDepth() == 0){
                            // Just set the input port value to the byte[] we have just read from the file.
                            inputPort.setValue(theBytes);
                            System.out.println("Workflow Submission Portlet: The value the user submitted (as a file) for the input port '"
                                    + inputPort.getName() + "'was : " + new String(theBytes, "UTF-8"));
                        } // Is this input a list?
                        // We have to split the contents of the file as list items.
                        else if (inputPort.getDepth() == 1) {
                            // Read byte[] into a String (hopefully it was a text file)
                            // using UTF-8 encoding and hope for the best. Then separate
                            // the string into items based on the submitted separator.
                            String valueToSeparate = new String(theBytes, "UTF-8");
                            String listSeparator;
                            String userDefinedListSeparator = (String) formItems.get(inputPort.getName() + WORKFLOW_INPUT_CONTENT_OTHER_SEPARATOR_SUFFIX);
                            if (userDefinedListSeparator != null
                                    && !userDefinedListSeparator.equals("")) {
                                listSeparator = getListSeparator(userDefinedListSeparator);
                            } else {
                                listSeparator = getListSeparator((String) formItems.get(inputPort.getName() + WORKFLOW_INPUT_CONTENT_SEPARATOR_SUFFIX));
                            }
                            String[] valueList = valueToSeparate.split(listSeparator);
                            inputPort.setValue(Arrays.asList(valueList));
                            System.out.println("Workflow Submission Portlet: The list the user submitted (as a file) for the input port '"
                                    + inputPort.getName() + "' was: " + valueToSeparate + " (hex value " + stringToHex(valueToSeparate.getBytes("UTF-8")) + ")");
                            System.out.println("Extracted list items:");
                            for (int i = 0; i < valueList.length; i++) {
                                System.out.println("Item " + (i + 1) + ": " + valueList[i] + " (hex value: " + stringToHex(valueList[i].getBytes("UTF-8")) + ")");
                            }
                        } // Is this input a list of (... lists of ...) lists?
                        // We currently support input list depths up to 1.
                        else {
                            System.out.println("Workflow Submission Portlet: Input " + inputPort.getName() + " of workflow " + workflowName + " expects a list of depth more than 1 which is currently not supported.");
                            request.setAttribute(Constants.ERROR_MESSAGE, "Input " + inputPort.getName() + " of workflow " + workflowName + " expects a list of depth more than 1 which is currently not supported.");
                            inputsSuccessfullyRead = false;
                            return;
                        }
                    } else if (formItems.get(inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX) != null
                            && !((String) formItems.get(inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX)).equals("")) {

                        // Is this a single value input? Just get whatever content was submitted.
                        if (inputPort.getDepth() == 0) {
                            inputPort.setValue((String) formItems.get(inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX));
                            System.out.println("Workflow Submission Portlet: The value the user submitted (from textarea input field) for the input port '"
                                    + inputPort.getName() + "' was: "
                                    + (String) inputPort.getValue());
                        } // Is this input a list? We have to split the string to get the list items.
                        else if (inputPort.getDepth() == 1) {

                            String valueToSeparate = (String) formItems.get(inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX);

                            String listSeparator;
                            String userDefinedListSeparator = (String) formItems.get(inputPort.getName() + WORKFLOW_INPUT_CONTENT_OTHER_SEPARATOR_SUFFIX);
                            if (userDefinedListSeparator != null
                                    && !userDefinedListSeparator.equals("")) {
                                listSeparator = getListSeparator(userDefinedListSeparator);
                            } else {
                                listSeparator = getListSeparator((String) formItems.get(inputPort.getName() + WORKFLOW_INPUT_CONTENT_SEPARATOR_SUFFIX));
                            }
                            String[] valueList;
                            // Since the enctype of the form is multipart/form-data,
                            // lines in a HTML text area are separated with unencoded CRLF (\r\n)
                            // regadless of the platform!
                            if (listSeparator.equals("\n") || listSeparator.equals("\r\n")) {
                                // Use "\r\n" in both these cases as that is what the text will be separated with
                                valueList = valueToSeparate.split("\r\n");
                            } else {
                                valueList = valueToSeparate.split(listSeparator);
                            }
                            inputPort.setValue(Arrays.asList(valueList));
                            System.out.println("Workflow Submission Portlet: The list the user submitted (as a file) for the input port '"
                                    + inputPort.getName() + "' was: " + valueToSeparate + " (hex value " + stringToHex(valueToSeparate.getBytes("UTF-8")) + ")");
                            System.out.println("Extracted list items:");
                            for (int i = 0; i < valueList.length; i++) {
                                System.out.println("Item " + (i + 1) + ": " + valueList[i] + " (hex value: " + stringToHex(valueList[i].getBytes("UTF-8")) + ")");
                            }
                        } // Is this input a list of (... lists of ...) lists?
                        // We currently support input list depths up to 1.
                        else {
                            System.out.println("Workflow Submission Portlet: Input " + inputPort.getName() + " of workflow " + workflowName + " expects a list of depth more than 1 which is currently not supported.");
                            request.setAttribute(Constants.ERROR_MESSAGE, "Input " + inputPort.getName() + " of workflow " + workflowName + " expects a list of depth more than 1 which is currently not supported.");
                            inputsSuccessfullyRead = false;
                            return;
                        }
                    } else {
                        // We do not have a value for this input port
                        System.out.println("Workflow Submission Portlet: Submitted value for input " + inputPort.getName() + " of workflow " + workflowName + " is null. Submission was cancelled as this will cause the workflow to fail.");
                        request.setAttribute(Constants.ERROR_MESSAGE, "Submitted value for input " + inputPort.getName() + " of workflow " + workflowName + " is null. Submission was cancelled as this will cause the workflow to fail.");
                        inputsSuccessfullyRead = false;
                        return;
                    }
                } // We have now finished reading all the inputs

                if (inputsSuccessfullyRead) {
                    // Submit the workflow to the T2 Server in preparation for execution
                    HttpResponse httpResponse = submitWorkflow(workflow, request);

                    // Submit the workflow's inputs to the Taverna 2 Server in
                    // prepartion for the workflow execution
                    if (httpResponse != null) { // null indicates something went wrong

                        // Extract the workflowResourceUUID returned by the T2 Server,
                        // something like http://<SERVER>/taverna-server/rest/runs/UUID,
                        // from the Location header, where UUID part identifies the submitted workflow on the T2 Server
                        String workflowResourceUUID;
                        workflowResourceUUID = httpResponse.getHeaders(Constants.LOCATION_HEADER_NAME)[0].getValue();
                        workflowResourceUUID = workflowResourceUUID.substring(workflowResourceUUID.lastIndexOf("/") + 1);
                        System.out.println("Workflow Submission Portlet: Workflow " + workflowName + " successfully submitted to the Server with UUID " + workflowResourceUUID + ".");

                        // Try to set the property that contains the name of the
                        // Baclava file where outputs are to be written to.
                        boolean outputBaclavaFilePropertySet = setBaclavaOutputFile(workflow, workflowResourceUUID, request);
                        if (outputBaclavaFilePropertySet) {

                            Document worfklowInputsDocument = buildWorkflowInputsBaclavaDocument(workflowInputPorts);
                            boolean inputsSubmitted = submitWorkflowInputs(workflow, workflowResourceUUID, worfklowInputsDocument, request);

                            // Run the workflow on the T2 Server
                            if (inputsSubmitted) {
                                Date startDate = new Date();
                                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
                                System.out.println("Workflow Submission Portlet: Inputs for workflow " + workflowName + " successfully submitted to the Server.");

                                boolean runSubmitted = runWorkflow(workflow, workflowResourceUUID, request);
                                if (runSubmitted) {
                                    System.out.println("Workflow Submission Portlet: Execution of workflow " + workflowName + " successfully initiated on the Server at " + dateFormat.format(startDate) + " with job id: " + workflowResourceUUID + ".");

                                    // Add this workflowResourceUUID to the list of submitted workflow UUIDs
                                    // to be read by the Workflow Results portlet and used to fetch results for
                                    // this run
                                    ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>) request.getPortletSession().
                                            getAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
                                            PortletSession.APPLICATION_SCOPE); // should not be null at this point

                                    WorkflowSubmissionJob job = new WorkflowSubmissionJob(workflowResourceUUID, workflow, Constants.JOB_STATUS_OPERATING, workflowRunDescription);
                                    job.setStartDate(startDate);
                                    workflowSubmissionJobs.add(0, job);

                                    // Persist the details of the newly created job on disk
                                    persistJobOnDisk(request, job, worfklowInputsDocument);

                                    request.getPortletSession().
                                            setAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
                                            workflowSubmissionJobs,
                                            PortletSession.APPLICATION_SCOPE);
                                    request.setAttribute(Constants.INFO_MESSAGE, "Workflow " + workflowName + " successfully submitted for execution with run id: " + workflowResourceUUID + ". You may use this id to monitor the progress of the workflow run.");
                                }
                            }
                        }
                    }
                }
            }
            // Was this a request to upload a new workflow file
            else if (formItems.keySet().contains(Constants.UPLOAD_WORKFLOW)){
                
                // User is submitting a new workflow file
                System.out.println("Workflow Submission Portlet: Request to upload a workflow file received.");

                // Workflow to upload
                String workflowFileName = (String) formItems.get(Constants.WORKFLOW_UPLOAD_FORM_FILE+"_file_name");
                InputStream uploadedWorkfowStream = (InputStream) formItems.get(Constants.WORKFLOW_UPLOAD_FORM_FILE);

                // Save the workflow stream to workflows directory
                File workflowsDir = new File(getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY));
                File workflowFile = new File(workflowsDir, workflowFileName);

                System.out.println("Workflow Submission Portlet: Workflow to upload file name: " +workflowFileName);

                // Workflow with the same name already in the workflow list? Warn the user and exit.
                String workflowFileNameNoExtension = workflowFileName.substring(0, workflowFileName.lastIndexOf('.')) + ".jsp";
                for (String name: workflowFileNamesList){
                    if (workflowFileNameNoExtension.equals(name)){
                        request.setAttribute(Constants.ERROR_MESSAGE, "A workflow with the same name ("+workflowFileNameNoExtension+") already exists. Please rename the file and try again.");
                        return;
                    }
                }

                // Upload the workflow file and save it.
                OutputStream out = null;
                try {
                    out = new FileOutputStream(workflowFile);
                    byte[] theBytes = new byte[uploadedWorkfowStream.available()];
                    uploadedWorkfowStream.read(theBytes);
                    out.write(theBytes);
                    request.setAttribute(Constants.INFO_MESSAGE, "Workflow file " + workflowFileName + " successfully uploaded.");                    

                    // Update the workflow list (and other related lists)
                    addWorkflow(workflowFileName);
                }
                catch (Exception ex) {
                    System.out.println("Workflow Submission Portlet: Failed to save the submitted workflow file to " + workflowFile.getAbsoluteFile() + ".");
                    ex.printStackTrace();
                    request.setAttribute(Constants.ERROR_MESSAGE, "Failed to save the submitted workflow file to " +workflowFile.getAbsolutePath() + ".");
                } finally {
                    try {
                        uploadedWorkfowStream.close();
                    } catch (Exception ex2) {
                        // Ignore
                    }
                    try {
                        out.close();
                    } catch (Exception ex2) {
                        // Ignore
                    }
                }
            }
        } else { // a standard application/x-www-form-urlencoded form submission request
            // Is this a request to search myExperiment for workflows
            if (request.getParameter(PORTLET_NAMESPACE + Constants.MYEXPERIMENT_WORKFLOW_SEARCH) != null) {

                System.out.println("myExperiment search terms:'" + request.getParameter(Constants.MYEXPERIMENT_SEARCH_TERMS) + "'");

                myExperimentWorkflows = new ArrayList<net.sf.taverna.t2.portal.myexperiment.Workflow>();
                myExperimentWorkflowsInputForms = new HashMap<String, String>();

                String errorMessage = null;
                if (request.getParameter(Constants.MYEXPERIMENT_SEARCH_TERMS).equals("")) {
                    //request.setAttribute(Constants.ERROR_MESSAGE, "Search criteria was empty. Please specify your search terms and try again.");
                    //return;

                    // Get all workflows
                    int page = 1;

                    outerloop:
                    while (true){
                        String queryURL = MYEXPERIMENT_BASE_URL +
                                    "/workflows.xml?elements=content-uri,content-type,type,thumbnail,thumbnail-big,title,description&page="+page;
                        //String queryURL = MYEXPERIMENT_BASE_URL + "/workflows.xml?all_elements=yes&page=1";
                        try {
                            System.out.println("Starting myExperiment search "+ queryURL);
                            ServerResponse results = myExperimentClient.doMyExperimentGET(queryURL);
                            Document responseBodyDocument = results.getResponseBody();
                            Element rootElement = responseBodyDocument.getRootElement();
                            List<Element> workflowElements = rootElement.getChildren();
                            if (workflowElements.isEmpty()){
                                break; // no more pages
                            }
                            else{
                                for (Element workflowElement : workflowElements){
                                    //net.sf.taverna.t2.portal.myexperiment.Workflow workflow = (net.sf.taverna.t2.portal.myexperiment.Workflow) net.sf.taverna.t2.portal.myexperiment.Workflow.buildFromXML(workflowElement, myExperimentClient);
                                    net.sf.taverna.t2.portal.myexperiment.Workflow workflow = new net.sf.taverna.t2.portal.myexperiment.Workflow();
                                    workflow.setResource(workflowElement.getAttributeValue("resource"));
                                    workflow.setURI(workflowElement.getAttributeValue("uri"));
                                    workflow.setContentType(workflowElement.getChildText("content-type"));
                                    workflow.setVersion(Integer.parseInt(workflowElement.getAttributeValue("version")));
                                    workflow.setTitle(workflowElement.getChildText("title"));
                                    workflow.setVisibleType(workflowElement.getChildText("type"));
                                    workflow.setDescription(workflowElement.getChildText("description"));
                                    workflow.setThumbnail(new URI(workflowElement.getChildText("thumbnail")));
                                    workflow.setThumbnailBig(new URI(workflowElement.getChildText("thumbnail-big")));
                                    System.out.println("myExperiment search, found workflow: " + workflow.getResource());
                                    if (workflow.isTaverna2Workflow()) { // only deal with T2 workflows
                                        myExperimentWorkflows.add(workflow);
                                        if (myExperimentWorkflows.size() > myExperimentResultCountLimit){
                                            break outerloop;
                                        }
                                    }
                                }
                                page++;
                            }
                        } catch (Exception ex) {
                            errorMessage = "Failed to get workflows from myExperiment";
                            System.out.println("myExperiment search: Failed to get workflows from myExperiment page " + queryURL);
                            ex.printStackTrace();
                            break;
                        }
                    }
                }
                else{
                    SearchEngine searchEngine = new SearchEngine(false, myExperimentClient); // no tag search

                    // Create a search that will return all workflows available from the myExp Base URL
                    SearchEngine.QuerySearchInstance getAllWorkflowsSearchQuery = new SearchEngine.QuerySearchInstance(request.getParameter(Constants.MYEXPERIMENT_SEARCH_TERMS), myExperimentResultCountLimit, true, false, false, false, false);

                    // Execute the search query to fetch the workflows from myExperiment
                    Map<Integer, ArrayList<Resource>> allResources = searchEngine.searchAndPopulateResults(getAllWorkflowsSearchQuery);

                    // We are expecting only workflow resources to be returned
                    if (!allResources.isEmpty()) {
                        for (int type : allResources.keySet()) {
                            if (type == Resource.WORKFLOW) {
                                for (Resource resource : allResources.get(type)) {
                                    net.sf.taverna.t2.portal.myexperiment.Workflow workflow = (net.sf.taverna.t2.portal.myexperiment.Workflow) resource;
                                    if (workflow.isTaverna2Workflow()) { // only deal with T2 workflows
                                        myExperimentWorkflows.add(workflow);
                                    }
                                    //System.out.println("Content URI: "+workflow.getContentUri() + " Resource: "+ workflow.getResource() +" version: "+ workflow.getVersion() +" Uploader: "+ workflow.getUploader() +" Preview: "+ workflow.getPreview());
                                    //System.out.println(" URI: "+workflow.getURI() + " Thumbnail URI: "+workflow.getThumbnail() + " Thumbnail Big: "+ workflow.getThumbnailBig() +" Title: "+ workflow.getTitle() );
                                }
                                break;
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    request.setAttribute(Constants.ERROR_MESSAGE, "Failed to get all workflows from myExperiment.");
                } else if (myExperimentWorkflows == null || myExperimentWorkflows.isEmpty()) {
                    request.setAttribute(Constants.ERROR_MESSAGE, "0 workflows were found on myExperiment that match your searh criteria.");
                }
            }
        }


        // Pass all request parameters and message attributes over to the doView() and other render stage methods
        response.setRenderParameters(request.getParameterMap());

        String errorMessageAttribute = (String) request.getAttribute(Constants.ERROR_MESSAGE);
        if (errorMessageAttribute != null){
            response.setRenderParameter(Constants.ERROR_MESSAGE, errorMessageAttribute);
        }
        String infoMessageAttribute = (String) request.getAttribute(Constants.INFO_MESSAGE);
        if (infoMessageAttribute != null){
            response.setRenderParameter(Constants.INFO_MESSAGE, infoMessageAttribute);
        }
    }

    @Override
    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {
        // Just print all the parameters we have received, for testing purposes
        Enumeration names = request.getParameterNames();
        while(names.hasMoreElements()){
            String parameterName = (String) names.nextElement();
            System.out.println("\nWorkflow Submission Portlet doView: parameter name: " + parameterName);
            System.out.println("Workflow Submission Portlet doView: parameter value: " + request.getParameter(parameterName));
            System.out.println();
        }
        
        response.setContentType("text/html");

        // Set the workflow file names list in the PortletSession if not already set.
        // This list is created in the init() method but at that time we do not have
        // the session object so we have to do it here.
        if (request.getAttribute(Constants.WORKFLOW_FILE_NAMES) == null){
            request.setAttribute(Constants.WORKFLOW_FILE_NAMES, workflowFileNamesList);
        }

        if (PORTLET_NAMESPACE == null){
            PORTLET_NAMESPACE = response.getNamespace();
        }

        // Get currently logged in user (nneded to figure out whether to show
        // "add a new workflow" option available only to logged in users)
        String user = (String)request.getPortletSession().
                                            getAttribute(Constants.USER,
                                            PortletSession.APPLICATION_SCOPE);
        if (user == null){
            SessionManager sessionManager = (SessionManager) ComponentManager.get(org.sakaiproject.tool.api.SessionManager.class); // Sakai-specific
            user = sessionManager.getCurrentSession().getUserEid(); // get user's display name - Sakai-specific

            if (user == null){ //if user is still null - then make them ANONYMOUS
                user = Constants.USER_ANONYMOUS;
            }

            System.out.println("Workflow Submission Portlet: Session started for user " + user + "." );
            request.getPortletSession().setAttribute(Constants.USER,
                    user,
                    PortletSession.APPLICATION_SCOPE);
        }

        // Show the wfs list and myExperiment seach box
        PortletRequestDispatcher dispatcher;
        dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowSubmission_view.jsp");
        dispatcher.include(request, response);

        if (request.getParameter(PORTLET_NAMESPACE + Constants.CLEAR) != null){ // request to clear the wf input form or myExp search results
            myExperimentWorkflows = null;
            myExperimentWorkflowsInputForms = null;
            return;
        }

        // If a workflow has been selected from the pre-canned wfs list - show its input form
        if (request.getParameter(PORTLET_NAMESPACE + Constants.WORKFLOW_SELECTION_SUBMISSION) != null){
            response.getWriter().println("<br />");
            response.getWriter().println("<hr />");
            response.getWriter().println("<br />");
            String selectedWorkflowFileName = request.getParameter(PORTLET_NAMESPACE + Constants.SELECTED_WORKFLOW);

            //<%-- Clear form button --%>
            response.getWriter().println("<form action=\""+response.createActionURL()+"\" method=\"post\">\n");
            response.getWriter().println("<p>\n");
            response.getWriter().println("<input type=\"image\" src=\"" + request.getContextPath() + "/images/close.gif\" style=\"border:0;\" >\n");
            response.getWriter().println("<input type=\"hidden\" name=\""+PORTLET_NAMESPACE + Constants.CLEAR+"\" value=\"true\">\n");
            response.getWriter().println("</p>\n");
            response.getWriter().println("</form>\n");
            response.getWriter().println("<br>");

            // By now we should have generated the corresponding JSP file containing
            // workflow's input form snippet. Dispatch to this file now.
            File selectedWorkflowJSPFile = new File(getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY +
                    Constants.FILE_SEPARATOR + selectedWorkflowFileName + ".jsp"));
            if (! selectedWorkflowJSPFile.exists()){ // if it does not exist (something is wrong!) - try generating it once again

                int index = workflowFileNamesList.indexOf(selectedWorkflowFileName);
                Workflow workflow = workflowList.get(index);
                ArrayList<WorkflowInputPort> workflowInputPorts = workflowInputPortsList.get(index);

                if (! createWorkflowInputFormJSPSnippetFile(selectedWorkflowFileName, workflow, workflowInputPorts)){
                    // OK, now we are in trouble - we definitely do not have the workflow input form
                    response.getWriter().println("<p color=\"red\">There was a problem with generating the input form for workflow " + selectedWorkflowFileName +".</p>");
                    return;
                }
            }
            dispatcher = getPortletContext().getRequestDispatcher(Constants.WORKFLOWS_DIRECTORY + "/" + selectedWorkflowFileName + ".jsp");
            dispatcher.include(request, response);
        }
        // This is a request to upload a new workflow to the list
        else if (request.getParameter(PORTLET_NAMESPACE + Constants.WORKFLOW_UPLOAD_SUMBISSION) != null){
            dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowUpload_view.jsp");
            dispatcher.include(request, response);
        }
        // This is a request to search for workflows on myExperiment or to display an input form
        // for one of the workflows found in the previous (search) request
        else if (request.getParameter(PORTLET_NAMESPACE + Constants.MYEXPERIMENT_WORKFLOW_SEARCH) != null ||
                (request.getParameter(PORTLET_NAMESPACE + Constants.MYEXPERIMENT_WORKFLOW_SHOW_INPUT_FORM)!= null)){
            if (myExperimentWorkflows != null && !myExperimentWorkflows.isEmpty()){
                response.getWriter().println("<br>");
                response.getWriter().println("<hr>");

                //<%-- Clear form button --%>
                response.getWriter().println("<form action=\""+response.createActionURL()+"\" method=\"post\">\n");
                response.getWriter().println("<p>\n");
                response.getWriter().println("<input type=\"image\" src=\"" + request.getContextPath() + "/images/close.gif\" style=\"border:0;\" >\n");
                response.getWriter().println("<input type=\"hidden\" name=\"" + PORTLET_NAMESPACE + Constants.CLEAR + "\" value=\"true\">\n");
                response.getWriter().println("<br>");
                response.getWriter().println("<b>Found "+myExperimentWorkflows.size()+" workflow"+(myExperimentWorkflows.size()>1?"s":"")+".</b>\n");
                response.getWriter().println("<hr>");
                response.getWriter().println("</p>\n");
                response.getWriter().println("</form>\n");
                response.getWriter().println("<br>");

                // Display all the workflows we have found
                for (net.sf.taverna.t2.portal.myexperiment.Workflow myExperimentWorkflow : myExperimentWorkflows) {
                    String content;
                    // If this was a request to show the input form for a workflow
                    if (request.getParameter(PORTLET_NAMESPACE + Constants.MYEXPERIMENT_WORKFLOW_SHOW_INPUT_FORM) != null &&

                        URLDecoder.decode(request.getParameter(PORTLET_NAMESPACE + Constants.MYEXPERIMENT_WORKFLOW_SHOW_INPUT_FORM), "UTF-8").equals(myExperimentWorkflow.getResource())){
                         
                        System.out.println("Workflow Submission Portlet: Showing input form for " + myExperimentWorkflow.getResource() + "." );

                        // Generate the HTML input form if not already generated
                        String workflowInputForm = null;
                        if (myExperimentWorkflowsInputForms.get(myExperimentWorkflow.getResource()) != null){
                            workflowInputForm = myExperimentWorkflowsInputForms.get(myExperimentWorkflow.getResource());
                        }
                        else{
                            // Download the workflow, parse it and generate its input form
                            workflowInputForm = createMyExperimentWorkflowInputForm(myExperimentWorkflow, request, response);
                            if (workflowInputForm == null){ // something went wrong, error message will say what
                                workflowInputForm = (String)request.getAttribute(Constants.ERROR_MESSAGE); // just set this to the error message
                            }
                            else{
                                myExperimentWorkflowsInputForms.put(myExperimentWorkflow.getResource(), workflowInputForm); // resource URI is the key
                            }
                        }
                        content = myExperimentWorkflow.createHTMLPreview(true, workflowInputForm, response);
                    }
                    else{
                        content = myExperimentWorkflow.createHTMLPreview(true, null, response);
                    }

                    // Write the JavaScript for validating the input form
                    dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/InputsValidationJavaScript.jsp");
                    dispatcher.include(request, response);

                    response.getWriter().println(content);
                    response.getWriter().println("<hr>");
                }
            }
        }
    }

    @Override
    public void doEdit(RenderRequest request,RenderResponse response) throws PortletException,IOException {
            response.setContentType("text/html");        
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowSubmission_edit.jsp");
        dispatcher.include(request, response);
    }

    @Override
    public void doHelp(RenderRequest request, RenderResponse response) throws PortletException,IOException {

        response.setContentType("text/html");        
        PortletRequestDispatcher dispatcher =
        getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowSubmission_help.jsp");
        dispatcher.include(request, response);
    }

    private boolean addWorkflow(String workflowFileName){

        System.out.println("Workflow Submission Portlet: Adding workflow " + workflowFileName + " to the list.");

        // Get the workflow filename (without the .t2flow extension)
        String workflowFileNameNoExtension = workflowFileName.substring(0, workflowFileName.lastIndexOf('.'));

        // Parse the workflow file
        FileInputStream workflowInputStream = null;
        Document workflowDocument;
        try {
            workflowInputStream = new FileInputStream(getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY)
                    + Constants.FILE_SEPARATOR + workflowFileName);
        } catch (FileNotFoundException e) {
            System.out.println("Workflow Submission Portlet: Could not find workflow file " + getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY)
                    + Constants.FILE_SEPARATOR + workflowFileName);
            e.printStackTrace();
            return false;
        }
        try {
            workflowDocument = Utils.parseWorkflow(workflowInputStream);
        } catch (JDOMException ex1) {
            System.out.println("Workflow Submission Portlet: Could not parse the workflow file " + getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY)
                    + Constants.FILE_SEPARATOR + workflowFileName);
            ex1.printStackTrace();
            return false;
        } catch (IOException ex2) {
            System.out.println("Workflow Submission Portlet: Could not open workflow file " + getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY)
                    + Constants.FILE_SEPARATOR + workflowFileName + " to parse it.");
            ex2.printStackTrace();
            return false;
        }

        // Get the workflow annotations, such as description, title etc.
        Element topWorkflow = Utils.getTopDataflow(workflowDocument.getRootElement()); // top workflow, ignore nested workflows
        Workflow workflow = new Workflow();
        Element workflowAnnotationsElement = topWorkflow.getChild(Constants.ANNOTATIONS_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE);
        workflow.setDescription(Utils.getLatestAnnotationAssertionImplElementValue(workflowAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_FREETEXT_CLASS, workflowFileName));
        workflow.setTitle(Utils.getLatestAnnotationAssertionImplElementValue(workflowAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_DESCRIPTIVETITLE_CLASS, workflowFileName));
        workflow.setAuthor(Utils.getLatestAnnotationAssertionImplElementValue(workflowAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_AUTHOR_CLASS, workflowFileName));
        workflow.setFileName(workflowFileNameNoExtension);
        workflow.setIsMyExperimentWorkflow(false);
        workflow.setWorkflowDocument(workflowDocument);

        System.out.println("Workflow Submission Portlet: Parsing workflow " + workflowFileNameNoExtension + " finished.");
        System.out.println("Workflow Submission Portlet: Workflow name: " + workflow.getTitle() + ", description: " + workflow.getDescription() + ".\n");
        System.out.println("Workflow Submission Portlet: Parsing inputs for workflow " + workflowFileNameNoExtension + ".");

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
            inputPort.setDescription(Utils.getLatestAnnotationAssertionImplElementValue(workflowInputPortAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_FREETEXT_CLASS, workflowFileName));
            inputPort.setExampleValue(Utils.getLatestAnnotationAssertionImplElementValue(workflowInputPortAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_EXAMPLEVALUE_CLASS, workflowFileName));
            workflowInputPorts.add(inputPort);

            System.out.println("Workflow Submission Portlet: Input port name: " + inputPortName + ", depth: " + inputPortDepth + ", description: " + inputPort.getDescription() + ", example value: " + inputPort.getExampleValue());
        }
        System.out.println();

        // Also generate the JSP snippet for the workflow's input form while
        // we are at it and save it to a JSP file, if such a file already does not exist.
        // We will dispatch to this form later, when user select this workflow.
        File workflowInputFormJSPSnippetFile = new File(getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY
                + Constants.FILE_SEPARATOR + workflowFileNameNoExtension + ".jsp"));
        if (!workflowInputFormJSPSnippetFile.exists()) {
            if (!createWorkflowInputFormJSPSnippetFile(workflowFileNameNoExtension, workflow, workflowInputPorts)) {
                // If we cannot generate the workflow inputs form - skip this workflow altogether
                System.out.println("Workflow Submission Portlet: Failed to generate JSP input form file for workflow " + workflowFileNameNoExtension);
                return false;
            }
        }

        // Wrap the workflow document inside a <workflow> element
        // in the T2 Server namespace as expected by the Server.
        Document wrappedWorkflowDocument = wrapWorkflowDocument(workflowDocument);

        workflowFileNamesList.add(workflowFileNameNoExtension);
        // Sort the list alphabetically
        Collections.sort(workflowFileNamesList);
        // Get the new index of the just added workflow
        int index = workflowFileNamesList.indexOf(workflowFileNameNoExtension);
        // Insert everything else in the right place
        workflowList.add(index, workflow);
        wrappedWorkflowXMLDocumentsList.add(index, wrappedWorkflowDocument);
        workflowInputPortsList.add(index, workflowInputPorts);
        return true;
    }

    /*
     * Wraps the workflow document inside a <workflow> element
     * in the T2 Server namespace as expected by the Server.
     */
    public Document wrapWorkflowDocument(Document originalWorkflowDocument){

        Element workflowWrapperElement = new Element(Constants.T2_SERVER_WORKFLOW_ELEMENT, Constants.T2_SERVER_NAMESPACE);
        Element oldWorkflowRootElement = originalWorkflowDocument.getRootElement();
        oldWorkflowRootElement.detach(); // detach it from the previous document
        workflowWrapperElement.addContent(oldWorkflowRootElement); // attach the old root to the new root
        return new Document(workflowWrapperElement);
    }

    /*
     * Generates the JSP snippet containing the inputs form for a given workflow
     * and saves it to a JSP file.
     */
    private boolean createWorkflowInputFormJSPSnippetFile(String workflowFileName, Workflow workflow, ArrayList<WorkflowInputPort> workflowInputPorts){

        File selectedWorkflowJSPFile = new File(getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY +
                Constants.FILE_SEPARATOR + workflowFileName + ".jsp"));

        if (selectedWorkflowJSPFile.exists()){
            return true;
        }
        
        StringBuffer inputFormJSP = new StringBuffer();

        // Various imports - portlet taglib, constants, CSS, JavaScript
        inputFormJSP.append("<%-- Various imports - portlet taglib, constants, CSS to style the input form table, JavaScript to validate form fields --%>\n");
        inputFormJSP.append("<%@ taglib uri=\"http://java.sun.com/portlet\" prefix=\"portlet\" %>\n");
        inputFormJSP.append("<portlet:defineObjects />\n\n");
        inputFormJSP.append("<%@ include file=\"/WEB-INF/jsp/InputsValidationJavaScript.jsp\" %>\n\n");
        inputFormJSP.append("<%@ include file=\"/WEB-INF/jsp/AjaxDataPreviewJavaScript.jsp\" %>\n\n");
        inputFormJSP.append("<%@ include file=\"/WEB-INF/jsp/InputsCSS.jsp\" %>\n\n");

        // Workflow name and description
        if (workflow.getTitle() != null && !workflow.getTitle().equals("")){
            inputFormJSP.append("<b>Workflow:</b> "+workflow.getTitle()+"\n");
        }
        else{
            inputFormJSP.append("<b>Workflow:</b> "+workflowFileName+"\n");
        }
        if (workflow.getDescription() != null && !workflow.getDescription().equals("")){
            inputFormJSP.append("<br>\n");
            inputFormJSP.append("<br>\n");
            inputFormJSP.append("<b>Description:</b> " + workflow.getDescription() +"\n");
        }
        inputFormJSP.append("<br>\n");
        inputFormJSP.append("<br>\n");

        // Workflow inputs form
        inputFormJSP.append("<p><b>Workflow inputs:</b></p>\n");

        if (workflowInputPorts.isEmpty()){
            inputFormJSP.append("The workflow does not require any inputs<br><br>\n");
            inputFormJSP.append("<form name=\"<portlet:namespace/><%= Constants.WORKFLOW_INPUTS_FORM%>\" action=\"<portlet:actionURL/>\" method=\"post\" enctype=\"multipart/form-data\" onSubmit=\"return validateForm(this)\">\n");
        }
        else{
            inputFormJSP.append("<form name=\"<portlet:namespace/><%= Constants.WORKFLOW_INPUTS_FORM%>\" action=\"<portlet:actionURL/>\" method=\"post\" enctype=\"multipart/form-data\" onSubmit=\"return validateForm(this)\">\n");

            inputFormJSP.append("<table class=\"inputs_entry\">\n");
            inputFormJSP.append("<tr>\n");
            inputFormJSP.append("<th>Name</th>\n");
            inputFormJSP.append("<th>Type</th>\n");
            inputFormJSP.append("<th>Description</th>\n");
            inputFormJSP.append("<th>Value</th>\n");
            inputFormJSP.append("</tr>\n");
        
            // Loop over the workflow inputs and create a row with input fields
            // in the table for each one of them
            int counter = 1;
            for (WorkflowInputPort inputPort : workflowInputPorts){
                if (inputPort.getDepth() == 0){ // single input
                    if (counter % 2 == 0){ // alternate row colours
                        inputFormJSP.append("<tr style=\"background-color: #F5F5F5;\" " + Constants.INPUT_PORT_NAME_ATTRIBUTE + "=\""+inputPort.getName()+"\">\n");
                    }
                    else{
                        inputFormJSP.append("<tr " + Constants.INPUT_PORT_NAME_ATTRIBUTE + "=\""+inputPort.getName()+"\">\n");
                    }
                    inputFormJSP.append("<td>" + inputPort.getName()+ "</td>\n");
                    inputFormJSP.append("<td>single value</td>\n");
                    String descriptionString = "";
                    if (inputPort.getDescription() != null && !inputPort.getDescription().equals("")){
                        descriptionString += inputPort.getDescription().trim();
                    }
                    if (inputPort.getExampleValue() != null && !inputPort.getExampleValue().equals("")){
                        if (!descriptionString.equals("")){
                            descriptionString += "<br><br>";
                        }
                        descriptionString += "<b>Example value:</b><br><textarea id=\"textarea_with_no_decoration"+
                                counter+
                                "\" readonly=\"true\" style=\"width:100%; border:none;\">"+
                                inputPort.getExampleValue() +
                                "</textarea><script language=\"javascript\">adjustRows(document.getElementById(\"textarea_with_no_decoration"+
                                counter+"\"));</script>";
                    }
                    inputFormJSP.append("<td>" + descriptionString + "</td>\n");
                    inputFormJSP.append("<td>\n");
                    inputFormJSP.append("Paste the value here: <br>\n");
                    inputFormJSP.append("<textarea name=\"<portlet:namespace/>"+inputPort.getName()+Constants.WORKFLOW_INPUT_CONTENT_SUFFIX+"\" rows=\"2\" cols=\"20\" wrap=\"off\"></textarea><br>\n");
                    inputFormJSP.append("Or load the value from a file: <br />\n");
                    inputFormJSP.append("<input type=\"file\" name=\"<portlet:namespace/>"+inputPort.getName()+Constants.WORKFLOW_INPUT_FILE_SUFFIX+"\" />\n");
                    inputFormJSP.append("</td>\n");
                    inputFormJSP.append("</tr>\n");
                }
                else if (inputPort.getDepth() == 1){
                    if (counter % 2 == 0){ // alternate row colours
                        inputFormJSP.append("<tr bgcolor=\"#F5F5F5\" "+Constants.INPUT_PORT_NAME_ATTRIBUTE+"=\""+inputPort.getName()+"\">\n");
                    }
                    else{
                        inputFormJSP.append("<tr "+Constants.INPUT_PORT_NAME_ATTRIBUTE+"\""+inputPort.getName()+"\">\n");
                    }
                    inputFormJSP.append("<td>" + inputPort.getName()+ "</td>\n");
                    inputFormJSP.append("<td>list</td>\n");
                    String descriptionString = "";
                    if (inputPort.getDescription() != null && !inputPort.getDescription().equals("")){
                        descriptionString += inputPort.getDescription().trim();
                    }
                    if (inputPort.getExampleValue() != null && !inputPort.getExampleValue().equals("")){
                        if (!descriptionString.equals("")){
                            descriptionString += "<br><br>";
                        }
                        descriptionString += "<b>Example value:</b><textarea id=\"textarea_with_no_decoration"+
                                counter+
                                "\" readonly=\"true\" style=\"width:100%; border:none;\">"+
                                inputPort.getExampleValue() +
                                "</textarea><script language=\"javascript\">adjustRows(document.getElementById(\"textarea_with_no_decoration"+
                                counter+"\"));</script>";
                    }
                    inputFormJSP.append("<td>" + descriptionString + "</td>\n");
                    inputFormJSP.append("<td>\n");
                    inputFormJSP.append("Paste the list values here: <br>\n");
                    inputFormJSP.append("<textarea name=\"<portlet:namespace/>"+inputPort.getName()+Constants.WORKFLOW_INPUT_CONTENT_SUFFIX+"\" rows=\"2\" cols=\"20\" wrap=\"off\"></textarea><br>\n");
                    inputFormJSP.append("Or load them from a file: <br />\n");
                    inputFormJSP.append("<input type=\"file\" name=\"<portlet:namespace/>"+inputPort.getName()+Constants.WORKFLOW_INPUT_FILE_SUFFIX+"\" /><br><hr/>\n");
                    inputFormJSP.append("Use the following character sequence as the list item separator:\n");
                    inputFormJSP.append("<select name=\"<portlet:namespace/>"+inputPort.getName()+"_separator\">\n");
                    inputFormJSP.append("<option value=\""+NEW_LINE_LINUX_SEPARATOR+"\">New line - Unix/Linux (\\n)</option>\n");
                    inputFormJSP.append("<option value=\""+NEW_LINE_WINDOWS_SEPARATOR+"\">New line - Windows (\\r\\n)</option>\n");
                    inputFormJSP.append("<option value=\""+BLANK_SEPARATOR+"\">Blank (' ')</option>\n");
                    inputFormJSP.append("<option value=\""+TAB_SEPARATOR+"\">Tab (\\t)</option>\n");
                    inputFormJSP.append("<option value=\""+COLON_SEPARATOR+"\">Colon (:)</option>\n");
                    inputFormJSP.append("<option value=\""+SEMI_COLON_SEPARATOR+"\">Semi-colon (;)</option>\n");
                    inputFormJSP.append("<option value=\""+COMMA_SEPARATOR+"\">Comma (,)</option>\n");
                    inputFormJSP.append("<option value=\""+DOT_SEPARATOR+"\">Dot (.)</option>\n");
                    inputFormJSP.append("<option value=\""+PIPE_SEPARATOR+"\">Pipe (|)</option>\n");
                    inputFormJSP.append("</select><br />\n");
                    inputFormJSP.append("Or specify your own separator:\n");
                    inputFormJSP.append("<input type=\"text\" name=\"<portlet:namespace/>"+inputPort.getName()+"_other_separator\" size=\"3\" />\n");
                    inputFormJSP.append("</td>\n");
                    inputFormJSP.append("</tr>\n");
                }
                else{ // We cannot handle workflows with input of depth more than 1
                    System.out.println("Workflow Submission Portlet: Workflow " + workflowFileName +" contains inputs of depth more than 1 (i.e. a list of lists or higher). This is not supported at the moment - skipping this workflow.");
                    return false;
                }
                counter++;
            }
            inputFormJSP.append("</table>\n");
        }

        inputFormJSP.append("<%-- A field for the user to enter a description for the wf run. --%>\n");
        inputFormJSP.append("<br><b>Enter a short description of the workflow run (so you can more easily identify it later):</b><br>");
        inputFormJSP.append("<input type=\"text\" size=\"50\" name=\"<portlet:namespace/><%= Constants.WORKFLOW_RUN_DESCRIPTION%>\"/><br><br>");

        inputFormJSP.append("<%-- Hidden field to convey which workflow we want to execute --%>\n");
        inputFormJSP.append("<input type=\"hidden\" name=\"<portlet:namespace/><%= Constants.WORKFLOW_FILE_NAME%>\" value=\""+ workflowFileName + "\" />\n");
        inputFormJSP.append("<input type=\"submit\" name=\"<portlet:namespace/><%= Constants.RUN_WORKFLOW%>\" value=\"Run workflow\" />\n");
        inputFormJSP.append("</form><br>\n");

        // Write this JSP snippet to a file
        try{
            FileUtils.writeStringToFile(selectedWorkflowJSPFile, inputFormJSP.toString());
        }
        catch(IOException ioex){
            System.out.println("Workflow Submission Portlet: Failed to write the JSP input form snippet for workflow " + workflowFileName + ".");
            ioex.printStackTrace();
            return false;
        }

        return true;
    }

    /*
     * Generates the HTML snippet containing the inputs form for a given
     * myExperiment workflow.
     */
    public static String createMyExperimentWorkflowInputForm(net.sf.taverna.t2.portal.myexperiment.Workflow workflow, RenderRequest request, RenderResponse response) {

        // Get the workflow file and convert it into an XML document
        InputStream workflowBytesStream = null;
        //System.out.println("myExperiment Workflow Submission Portlet: Workflow content URI: "+workflow.getContentUri());

        if (workflow.getContent() == null){
            URL url;
            try {
                // URL from where to download the wf
                url = new URL(workflow.getResource() + "/download?version=" + workflow.getVersion());
                //url = workflow.getContentUri().toURL();
                System.out.println("Fetching workflow resource " + url);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                workflowBytesStream = conn.getInputStream();
            } catch (IOException ex) {
                request.setAttribute(Constants.ERROR_MESSAGE, "Failed to download workflow " + workflow.getContentUri().toASCIIString());
                System.out.println("Workflow Submission Portlet: Failed to download workflow " + workflow.getContentUri().toASCIIString());
                ex.printStackTrace();
                return null;
            }
        }
        else{
            workflowBytesStream = new ByteArrayInputStream(workflow.getContent());
        }

        Document workflowDocument;
        try {
            workflowDocument = Utils.parseWorkflow(workflowBytesStream);
            // Save the workflow content so we do not have to download next time
            XMLOutputter outputter = new XMLOutputter();
            String str = outputter.outputString(workflowDocument);
            workflow.setContent(str.getBytes("UTF-8"));
            workflow.setWorkflowDocument(workflowDocument);
        } catch (JDOMException ex1) {
            request.setAttribute(Constants.ERROR_MESSAGE, "Failed to parse the workflow file " + workflow.getResource());
            System.out.println("Workflow Submission Portlet: " +
                    "Failed to parse the workflow file " + workflow.getResource());
            ex1.printStackTrace();
            return null;
        } catch (IOException ex2) {
            request.setAttribute(Constants.ERROR_MESSAGE, "Failed to open workflow file " + workflow.getResource());
            System.out.println("Workflow Submission Portlet: Failed to open workflow file " + workflow.getResource());
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

            //System.out.println("myExperiment Workflow Submission Portlet: Input port name: " + inputPortName + ", depth: " + inputPortDepth + ", description: " + inputPort.getDescription() + ", example value: " + inputPort.getExampleValue());
        }

        workflow.setWorkflowInputPorts(workflowInputPorts);

        StringBuffer inputForm = new StringBuffer();

        // Workflow inputs
        inputForm.append("<p><b>Workflow inputs:</b></p>\n");

        if (workflowInputPorts.isEmpty()) {
            inputForm.append("The workflow does not require any inputs<br><br>\n");
            inputForm.append("<form action=\""+response.createActionURL()+"\" method=\"post\" enctype=\"multipart/form-data\" onSubmit=\"return validateForm(this)\">\n");
        } else {
            inputForm.append("<form action=\""+response.createActionURL()+"\" method=\"post\" enctype=\"multipart/form-data\" onSubmit=\"return validateForm(this)\">\n");

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
                    inputForm.append("<textarea name=\"" + PORTLET_NAMESPACE + inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX + "\" rows=\"2\" cols=\"20\" wrap=\"off\"></textarea><br>\n");
                    inputForm.append("Or load the value from a file: <br />\n");
                    inputForm.append("<input type=\"file\" name=\"" + PORTLET_NAMESPACE + inputPort.getName() + Constants.WORKFLOW_INPUT_FILE_SUFFIX + "\" />\n");
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
                    inputForm.append("<textarea name=\"" + PORTLET_NAMESPACE + inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX + "\" rows=\"2\" cols=\"20\" wrap=\"off\"></textarea><br>\n");
                    inputForm.append("Or load them from a file: <br />\n");
                    inputForm.append("<input type=\"file\" name=\"" + PORTLET_NAMESPACE + inputPort.getName() + Constants.WORKFLOW_INPUT_FILE_SUFFIX + "\" /><br><hr/>\n");
                    inputForm.append("Use the following character sequence as the list item separator:\n");
                    inputForm.append("<select name=\"" + PORTLET_NAMESPACE + inputPort.getName() + "_separator\">\n");
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
                    inputForm.append("<input type=\"text\" name=\"" + PORTLET_NAMESPACE + inputPort.getName() + "_other_separator\" size=\"3\" />\n");
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

        inputForm.append("<!-- A field for the user to enter a description for the wf run. -->\n");
        inputForm.append("<br><b>Enter a short description of the workflow run (so you can more easily identify it later):</b><br>");
        inputForm.append("<input type=\"text\" size=\"50\" name=\"" + PORTLET_NAMESPACE + Constants.WORKFLOW_RUN_DESCRIPTION + "\"/><br><br>");

        inputForm.append("<!-- Hidden field to convey which workflow we want to execute -->\n");
        try {
            inputForm.append("<input type=\"hidden\" name=\"" + PORTLET_NAMESPACE + Constants.WORKFLOW_RESOURCE_ON_MYEXPERIMENT + "\" value=\"" + URLEncoder.encode(workflow.getResource(), "UTF-8") + "\" />\n");
        } catch (UnsupportedEncodingException ex) { // should not really happen
            inputForm.append("<input type=\"hidden\" name=\"" + PORTLET_NAMESPACE + Constants.WORKFLOW_RESOURCE_ON_MYEXPERIMENT + "\" value=\"" + workflow.getResource() + "\" />\n");
        }
        inputForm.append("<input type=\"submit\" name=\"" + PORTLET_NAMESPACE + Constants.RUN_WORKFLOW + "\" value=\"Run workflow\" />\n");
        inputForm.append("</form><br>\n");

        return inputForm.toString();
    }

  
    /*
     * HTTP POSTs a wrapped workflow Document to the T2 Server.
     */
    HttpResponse submitWorkflow(Workflow workflow, ActionRequest request) {

        HttpClient httpClient = new DefaultHttpClient();
        String runsURL = T2_SERVER_URL + Constants.RUNS_URL;

        HttpPost httpPost = new HttpPost(runsURL);
        httpPost.setHeader(Constants.CONTENT_TYPE_HEADER_NAME, Constants.CONTENT_TYPE_APPLICATION_XML);
        String workflowName = null;
        String wrappedWorkflowDocumentString = null;

        try {
            if (workflow.isMyExperimentWorkflow()) {
                workflowName = workflow.getMyExperimentWorkflowResource();
                System.out.println("Workflow Submission Portlet: Preparing to submit workflow to Server " + workflowName );
                // Get the workflow XML Document's string representation
                Document workflowDocument = (Document) workflow.getWorkflowDocument().clone();
                // Wrap it inside another <workflow> element as expected by the T2 Server
                wrappedWorkflowDocumentString = (new XMLOutputter()).outputString(wrapWorkflowDocument(workflowDocument));
            } else {
                workflowName = workflow.getFileName();
                System.out.println("Workflow Submission Portlet: Preparing to submit workflow to Server " + workflowName );
                // Get the XML Document that wraps the workflow document
                int workflowIndex = workflowFileNamesList.indexOf(workflowName); // get the workflow index
                wrappedWorkflowDocumentString = (new XMLOutputter()).outputString(wrappedWorkflowXMLDocumentsList.get(workflowIndex));
            }

            //System.out.println("Workflow Submission Portlet: Preparing to submit workflow to Server " + runsURL);
            //System.out.println(workflowString);

            StringEntity entity = new StringEntity(wrappedWorkflowDocumentString, "UTF-8");
            httpPost.setEntity(entity);
        } catch (UnsupportedEncodingException ex) {

            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity for workflow " + workflowName + " when POSTing the workflow to the Server.");
            ex.printStackTrace();
            request.setAttribute(Constants.ERROR_MESSAGE, "Failed to create message body for workflow " + workflowName + " when POSTing the worfkflow to the Server.");
            return null;
        }

        HttpResponse httpResponse = null;
        try {
            // Execute the request to upload the workflow file to the Server
            HttpContext localContext = new BasicHttpContext();
            httpResponse = httpClient.execute(httpPost, localContext);

            // Release resource
            httpClient.getConnectionManager().shutdown();

            if (httpResponse.getStatusLine().getStatusCode() != 201) { // HTTP/1.1 201 Created
                System.out.println("Workflow Submission Portlet: Failed to submit workflow " + workflowName + " for execution.\nServer responded with: " + httpResponse.getStatusLine());
                request.setAttribute(Constants.ERROR_MESSAGE, "Failed to submit workflow " + workflowName + " for execution.<br>Server responded with: " + httpResponse.getStatusLine());
                return null;
            }
        } catch (Exception ex) {
            System.out.println("Workflow Submission Portlet: Failed to POST request to submit workflow " + workflowName + " for execution.");
            ex.printStackTrace();
            request.setAttribute(Constants.ERROR_MESSAGE, "Failed to submit workflow " + workflowName + " for execution.<br>Error: " + ex.getMessage());
            return null;
        }

        return httpResponse;
    }
    
    /*
     * PUTs the value for property that keeps the name of the
     * output Baclava file where to save the results. This needs
     * to be done before the run is initiated.
     */
    private boolean setBaclavaOutputFile(Workflow workflow, String workflowResourceUUID, ActionRequest actionRequest){

        String workflowName;
        if (workflow.isMyExperimentWorkflow()){
            workflowName = workflow.getMyExperimentWorkflowResource();
        }
        else{
            workflowName = workflow.getFileName();
        }

        HttpClient httpClient2 = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();

        String baclavaOutputPropertyURL = T2_SERVER_URL + Constants.RUNS_URL + "/" + workflowResourceUUID + Constants.BACLAVA_OUTPUT_PROPERTY_URL;
        HttpPut httpPut = new HttpPut(baclavaOutputPropertyURL);
        httpPut.setHeader(Constants.CONTENT_TYPE_HEADER_NAME, Constants.CONTENT_TYPE_TEXT_PLAIN);

        try {
            StringEntity entity = new StringEntity(Constants.BACLAVA_OUTPUT_FILE_NAME, "UTF-8");
            httpPut.setEntity(entity);
        }
        catch (UnsupportedEncodingException ex) {
            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity containing the name of the Baclava XML file where outputs for workflow " + workflowName + " will be saved to.");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to create message body containing the name of the XML file where outputs for workflow " + workflowName + " will be saved to.");
            return false;
        }

        HttpResponse httpResponse2 = null;
        try{
            // Execute the request to send the name of the Baclava file
            // that will be used to save the workflow outputs to.
            httpResponse2 = httpClient2.execute(httpPut, localContext);

            // Release resource
            httpClient2.getConnectionManager().shutdown();

            if (httpResponse2.getStatusLine().getStatusCode() != 200){ // HTTP/1.1 200 OK
                System.out.println("Workflow Submission Portlet: Failed to set the name of the Baclava file where outputs are to be saved to for workflow " + workflowName + ". The Server responded with: " + httpResponse2.getStatusLine()+".");
                actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to set the name of the file where outputs are to be saved to for workflow " + workflowName + ". The Server responded with: " + httpResponse2.getStatusLine()+".");
                return false;
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: An error occured while setting the name of the Baclava file where outputs are to be saved to for workflow " + workflowName + " on the Server.");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "An error occured while setting the name of the file where outputs are to be saved to for workflow " + workflowName + " on the Server.");
            return false;
        }
        System.out.println("Workflow Submission Portlet: Property where to save the output Baclava file for workflow " + workflowName + " successfully set on the Server at " + baclavaOutputPropertyURL +".");
        return true;
    }

    /**
     * HTTP PUTs workflow inputs to the T2 Server as a Baclava XML document.
     * Returns false if it fails for any reason.
     */
    private boolean submitWorkflowInputs(Workflow workflow, String workflowResourceUUID, Document workflowInputsBaclavaDocument, ActionRequest actionRequest){

        String workflowName;
        String workflowFileName;
        if (workflow.isMyExperimentWorkflow()){
            String myExperimentWorkflowId = workflow.getMyExperimentWorkflowResource().substring(workflow.getMyExperimentWorkflowResource().lastIndexOf("/") + 1);
            workflowFileName = MYEXPERIMENT_FILE_NAME_PREFIX + myExperimentWorkflowId;
            workflowName = workflow.getMyExperimentWorkflowResource();
        }
        else{
            workflowFileName = workflow.getFileName();
            workflowName = workflow.getFileName();
        }

        HttpClient httpClient = new DefaultHttpClient();
        HttpContext localContext = new BasicHttpContext();
        String wdURL = T2_SERVER_URL + Constants.RUNS_URL + "/" + workflowResourceUUID + Constants.WD_URL;

        HttpPost httpPost = new HttpPost(wdURL);
        httpPost.setHeader(Constants.CONTENT_TYPE_HEADER_NAME, Constants.CONTENT_TYPE_APPLICATION_XML);

        HttpResponse httpResponse = null;

        XMLOutputter xmlOutputter = new XMLOutputter();

        // Write the workflow inputs' Baclava XML document
        String workflowInputsDocumentString = xmlOutputter.outputString(workflowInputsBaclavaDocument);

        System.out.println("Workflow Submission Portlet: Preparing to submit workflow inputs Baclava file to Server " + wdURL);

        // Name of the uploaded Baclava file on the Server
        String baclavaFileName = workflowFileName + ".baclava";

        Element uploadBaclavaFileElement = new Element(Constants.UPLOAD_FILE_ELEMENT, Constants.T2_SERVER_REST_NAMESPACE);
        uploadBaclavaFileElement.setAttribute(Constants.NAME_ATTRIBUTE, baclavaFileName, Constants.T2_SERVER_REST_NAMESPACE);
        try{
            String workflowInputsDocumentString_Base64 = new BASE64Encoder().encode(workflowInputsDocumentString.getBytes("UTF-8"));
            uploadBaclavaFileElement.setContent(new Text(workflowInputsDocumentString_Base64));
        }
        catch(UnsupportedEncodingException ex){
            System.out.println("Workflow Submission Portlet: Failed to Base64 encode the content of the Baclava XML file with workflow inputs for workflow " + workflowName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to Base64 encode the content of the XML file with workflow inputs for workflow " + workflowName + ".");
            return false;
        }

        String uploadBaclavaFileElementString = new XMLOutputter().outputString(uploadBaclavaFileElement);
        System.out.println("Workflow Submission Portlet: Uploading Base64 encoded workflow inputs Baclava file.");
        //System.out.println(uploadBaclavaFileElementString);
        try {
            StringEntity entity = new StringEntity(uploadBaclavaFileElementString, "UTF-8");
            httpPost.setEntity(entity);
        }
        catch (UnsupportedEncodingException ex) {

            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity containing Baclava XML document with workflow inputs for workflow " + workflowName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to create message body containing XML document with workflow inputs for workflow " + workflowName + ".");
            return false;
        }

        try{
            // Execute the request to upload the inputs Baclava file to the Server
            httpResponse = httpClient.execute(httpPost, localContext);

            // Release resource
            httpClient.getConnectionManager().shutdown();

            if (httpResponse.getStatusLine().getStatusCode() != 201){ // HTTP/1.1 201 Created
                System.out.println("Workflow Submission Portlet: Failed to upload the file with inputs for workflow " + workflowName + ". The Server responded with: " + httpResponse.getStatusLine()+".");
                actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to upload the file with inputs for workflow " + workflowName + ". The Server responded with: " + httpResponse.getStatusLine()+".");
                return false;
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: An error occured while trying to upload the XML Baclava file with inputs for workflow " + workflowName + " to the Server.");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to upload the file with inputs for workflow" + workflowName + " to the Server.");
            return false;
        }

        HttpClient httpClient2 = new DefaultHttpClient();

        String baclavaInputURL = T2_SERVER_URL + Constants.RUNS_URL + "/" + workflowResourceUUID + Constants.BACLAVA_INPUTS_PROPERTY_URL;
        HttpPut httpPut = new HttpPut(baclavaInputURL);
        httpPut.setHeader(Constants.CONTENT_TYPE_HEADER_NAME, Constants.CONTENT_TYPE_TEXT_PLAIN);

        try {
            StringEntity entity = new StringEntity(baclavaFileName, "UTF-8");
            httpPut.setEntity(entity);
        }
        catch (UnsupportedEncodingException ex) {

            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity containing the name of the Baclava XML document with workflow inputs for workflow " + workflowName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to create message body containing the name of the XML file with workflow inputs for workflow " + workflowName + ".");
            return false;
        }

        HttpResponse httpResponse2 = null;
        try{
            // Execute the request to send the name of the uploaded Baclava file
            // that contains the workflow inputs to the Server
            httpResponse2 = httpClient2.execute(httpPut, localContext);

            // Release resource
            httpClient2.getConnectionManager().shutdown();

            if (httpResponse2.getStatusLine().getStatusCode() != 200){ // HTTP/1.1 200 OK
                System.out.println("Workflow Submission Portlet: Failed to set the name of the file with inputs for workflow " + workflowName + ". The Server responded with: " + httpResponse2.getStatusLine()+".");
                actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to set the name of the file with inputs for workflow " + workflowName + ". The Server responded with: " + httpResponse2.getStatusLine()+".");
                return false;
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: An error occured while setting the name of the file with inputs for workflow " + workflowName + " on the Server.");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "An error occured while setting the name of the file with inputs for workflow " + workflowName + " on the Server.");
            return false;
        }

       return true;
    }

    /*
     * HTTP PUTs the status of the workflow to "Operating" to kick start its execution.
     * Returns false if it fails for any reason.
     */
    boolean runWorkflow(Workflow workflow, String workflowResourceUUID,  ActionRequest actionRequest){
        String workflowName;
        if (workflow.isMyExperimentWorkflow()){
            workflowName = workflow.getMyExperimentWorkflowResource();
        }
        else{
            workflowName = workflow.getFileName();
        }

        HttpClient httpClient = new DefaultHttpClient();
        String statusURL = T2_SERVER_URL + Constants.RUNS_URL + "/" + workflowResourceUUID + Constants.STATUS_URL;

        HttpPut httpPut = new HttpPut(statusURL);
        httpPut.setHeader(Constants.CONTENT_TYPE_HEADER_NAME, Constants.CONTENT_TYPE_TEXT_PLAIN);

        try {
            StringEntity entity = new StringEntity(Constants.JOB_STATUS_OPERATING, "UTF-8");
            httpPut.setEntity(entity);
        }
        catch (UnsupportedEncodingException ex) {

            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity containing the workflow run status set to 'Operating' for workflow " + workflowName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to create the status message body used for initiating the execution of workflow " + workflowName + ".");
            return false;
        }

        HttpResponse httpResponse = null;
        try{
            // Execute the request
            HttpContext localContext = new BasicHttpContext();
            httpResponse = httpClient.execute(httpPut, localContext);

            // Release resource
            httpClient.getConnectionManager().shutdown();
            
            if (httpResponse.getStatusLine().getStatusCode() != 200){ // HTTP/1.1 200 OK
               System.out.println("Workflow Submission Portlet: Failed to initiate the execution of workflow " + workflowName + ". The Server responded with: " + httpResponse.getStatusLine()+".");
               actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to initiate the execution of workflow " + workflowName + ". The Server responded with: " + httpResponse.getStatusLine() +".");
               return false;
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: An error occured while trying to initiate the execution of workflow " + workflowName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "An error occured while to initiate the execution of workflow " + workflowName +  ".");
            return false;
        }

        return true;
    }

    /*
     * Builds a Baclava XML document for workflow inputs to submit them all in one go
     * to the T2 Server.
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
     * Returns a map of input port names to DataThings from a map of input port names to a
     * value for that port (which can be a single value or a list of (lists of ...) of values).
     */
    Map<String, DataThing> bakeDataThingMap(Map<String, Object> inputsMap){

        Map<String, DataThing> dataThingMap = new HashMap<String, DataThing>();
        
        for (String inputPortName : inputsMap.keySet()) {
            dataThingMap.put(inputPortName, DataThingFactory.bake(inputsMap.get(inputPortName)));
        }
        return dataThingMap;
    }

    /*
     * Returns a org.jdom.Document from a map of input port names to DataThingS containing
     * the input port's values.
     */
    public static Document getDataDocument(Map<String, DataThing> dataThings) {
	Element rootElement = new Element("dataThingMap", Constants.BACLAVA_NAMESPACE);
	Document document = new Document(rootElement);
	for (String key : dataThings.keySet()) {
		DataThing value = (DataThing) dataThings.get(key);
		Element dataThingElement = new Element("dataThing", Constants.BACLAVA_NAMESPACE);
		dataThingElement.setAttribute("key", key);
		dataThingElement.addContent(value.getElement());
		rootElement.addContent(dataThingElement);
	}
	return document;
    }

    private String getListSeparator(String separatorName){

        String separatorValue = null;

        if (separatorName.equals(NEW_LINE_LINUX_SEPARATOR)){
            separatorValue = "\n";
        }
        else if(separatorName.equals(NEW_LINE_WINDOWS_SEPARATOR)){
            separatorValue = "\r\n";
        }
        else if(separatorName.equals(BLANK_SEPARATOR)){
            separatorValue = " ";
        }
        else if(separatorName.equals(TAB_SEPARATOR)){
            separatorValue = "\t";
        }
        else if(separatorName.equals(COMMA_SEPARATOR)){
            separatorValue = ",";
        }
        else if(separatorName.equals(COLON_SEPARATOR)){
            separatorValue = ":";
        }
        else if(separatorName.equals(SEMI_COLON_SEPARATOR)){
            separatorValue = ";";
        }
        else if(separatorName.equals(DOT_SEPARATOR)){
            separatorValue = "\\."; // or "[.]". Dot needs to be escaped as it is a special reg exp character
        }        else if(separatorName.equals(PIPE_SEPARATOR)){
            separatorValue = "\\|"; // or "[|]". Pipe needs to be escaped as it is a special reg exp character
        }
        else{ // this is a user defined separator - create a nice separator reg exp string
            separatorValue = "";
            for(char ch: separatorName.toCharArray()){
                separatorValue += "[" + ch + "]"; // create a chacacter class for every characted in the string
            }
        }

        return separatorValue;
    }

    /*
     * Persist the details of a job on a disk. A directory named after
     * the job's UUID will be created in the jobs directory for the owning user
     * and will contain:
     *  - workflow.properties file containing the workflow file name (for local workflows) or resource and version (for a workflow from myExperiment) as properties
     *  - an empty file initially named Operating.status to indicate the status of the job
     *  - the input Baclava document in input.baclava file to hold the job's inputs
     *  - the <job_directory>/inputs directory where input values are individually saved in a directory structure - first level sub-dirs are port names that inside contain the port value
     *  - an empty file named <long>.startdate where <long> represents the Date in miliseconds after the "epoch"
     *  - a file named workflow_run_description.txt to hold the user-entered description for the wf run
     */
    private void persistJobOnDisk(PortletRequest request, WorkflowSubmissionJob job, Document worfklowInputsDocument){

        // Get the current user
        String user = (String)request.getPortletSession().
                                    getAttribute(Constants.USER,
                                    PortletSession.APPLICATION_SCOPE); // should not be null at this point
        File userDir = new File (JOBS_DIR, user);

        // Create a new directory for this job
        File jobDir = new File(userDir, job.getUuid());
        try{ // should not have existed so far
            jobDir.mkdir();
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to create a directory "+jobDir.getAbsolutePath()+" to save the job.");
            ex.printStackTrace();
        }
        System.out.println("Workflow Submission Portlet: Job's directory " + jobDir.getAbsolutePath() + " created." );

        // Set the status to "Operating" by creating an empty file
        File statusFile = new File(jobDir, Constants.JOB_STATUS_OPERATING + Constants.STATUS_FILE_EXT);
        try{
            FileUtils.touch(statusFile);
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to create the job's status file " + statusFile.getAbsolutePath());
            ex.printStackTrace();
        }
        System.out.println("Workflow Submission Portlet: Job's status set at " + statusFile.getAbsolutePath());

        // Save the workflow file name as a property in workflow.properties file,
        // in case we are running a locally uploaded workflow.
        // In case when the workflow to be run is from myExperiment then save the myExperiment resource and version
        // as properties.
        Properties props = new Properties();
        if (job.getWorkflow().getFileName() != null){ // this is a local workflow
            // Save the local workflow file name as property
            props.setProperty(Constants.WORKFLOW_FILE_NAME, job.getWorkflow().getFileName());
        }
        else{ // this is a workflow from myExperiment
            // Save the info about the workflow, i.e. myExperiment resource for the wf and wf version, as properties
            props.setProperty(Constants.MYEXPERIMENT_WORKFLOW_RESOURCE, job.getWorkflow().getMyExperimentWorkflowResource());
            props.setProperty(Constants.MYEXPERIMENT_WORKFLOW_VERSION, new Integer(job.getWorkflow().getMyExperimentWorkflowVersion()).toString());
        }

        File workflowPropertiesFile = new File(jobDir, Constants.WORKFLOW_PROPERTIES_FILE);
        try {
            FileOutputStream fos = new FileOutputStream(workflowPropertiesFile);
            props.store(fos, null);
            try {
                fos.close();
            } catch (Exception ex2) {
                // Ignore
            }
        } catch (Exception ex) {
            System.out.println("Workflow Submission Portlet: Failed to create the workflow properties file " + workflowPropertiesFile.getAbsolutePath());
            ex.printStackTrace();
        }
        System.out.println("Workflow Submission Portlet: Job's workflow properties set at " + workflowPropertiesFile.getAbsolutePath());

        // Save the job's start date by creating an empty file named after the date
        File startdateFile = new File(jobDir, job.getStartDate().getTime() + Constants.STARTDATE_FILE_EXT);
        try{
            FileUtils.touch(startdateFile);
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to create the job's start date file " + startdateFile.getAbsolutePath());
            ex.printStackTrace();
        }
        System.out.println("Workflow Submission Portlet: Job's start date set at " + startdateFile.getAbsolutePath());

        // Save the job's input Baclava file in a file called inputs.baclava
        File inputsFile = new File(jobDir, Constants.INPUTS_BACLAVA_FILE);
        java.io.FileWriter writer = null;
        try{
            XMLOutputter out = new XMLOutputter();
            writer = new java.io.FileWriter(inputsFile);
            out.output(worfklowInputsDocument, writer);
            writer.flush();
            writer.close();
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to save job's inputs to file " + inputsFile.getAbsolutePath());
            ex.printStackTrace();
        }
        finally{
            try{
                writer.close();
            }
            catch (Exception ex2){
                // Ignore
            }
        }
        System.out.println("Workflow Submission Portlet: Job's inputs saved to Baclava file " + inputsFile.getAbsolutePath());

        // Also save the individual input data values in <job_directory>/inputs directory
        // where first level sub-dirs are port names that inside contain the port value
        File inputsDir = new File(jobDir, Constants.INPUTS_DIRECTORY_NAME);
        try{
            Map<String, DataThing> inputsDataThingMap = DataThingXMLFactory.parseDataDocument(worfklowInputsDocument);
            try{
                if (!inputsDir.exists()){ // should not exist but hey
                    inputsDir.mkdir();
                }
                Utils.saveDataThingMapToDisk(inputsDataThingMap, inputsDir);
            }
            catch(Exception ex){
                System.out.println("Workflow Submission Portlet: Failed to create directory "+inputsDir.getAbsolutePath()+" where individual values for all input ports are to be saved.");
                ex.printStackTrace();
            }
        }
        catch(Exception ex){ // not fatal, so return the result map rather than null
            System.out.println("Workflow Submission Portlet: An error occured while trying to save the Baclava file with inputs to " + inputsDir.getAbsolutePath());
            ex.printStackTrace();
        }

        // Save the job's description entered by the user in a file called workflow_run_description.txt
        File wfRunDescriptionFile = new File(jobDir, Constants.WORKFLOW_RUN_DESCRIPTION_FILE);
        String wfRunDescription = job.getWorkflowRunDescription(); // should not be null, at least empty string
        System.out.println("Workflow Submission Portlet: wfRunDescription " + wfRunDescription);
        if (wfRunDescription == null){
            wfRunDescription = ""; // empty description
        }
        try{
            FileUtils.writeStringToFile(wfRunDescriptionFile, wfRunDescription, "UTF-8");
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to save job's description to file " + wfRunDescriptionFile.getAbsolutePath());
            ex.printStackTrace();
        }
        System.out.println("Workflow Submission Portlet: Job's description saved to file " + wfRunDescriptionFile.getAbsolutePath());
    }

    static final String HEXES = "0123456789ABCDEF";
    public static String stringToHex( byte [] raw ) {
        if ( raw == null ) {
        return null;
        }
        final StringBuilder hex = new StringBuilder( 2 * raw.length );
        for ( final byte b : raw ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
            .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
  }

}