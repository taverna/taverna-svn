package net.sf.taverna.t2.portal;

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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
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
import org.jdom.input.SAXBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.jdom.Text;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import sun.misc.BASE64Encoder;

/**
 * Workflow Submission Portlet - enables user to select a workflow,
 * specify the values for its inputs and submit it for execution to
 * a Taverna 2 Server.
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

    /*
     * Do the init stuff one at portlet loading time.
     */
    @Override
    public void init(){

        // Get the URL of the T2 Server defined in web.xml as an
        // app-wide init parameter ( <context-param> element)
        T2_SERVER_URL = getPortletContext().getInitParameter(Constants.T2_SERVER_URL);

        // Get the directory where info for submitted jobs for all users is persisted
        JOBS_DIR = new File(getPortletContext().getInitParameter(Constants.JOBS_DIRECTORY_PATH),
                Constants.JOBS_DIRECTORY_NAME);

        // Directory containing workflows
        File dir = new File(getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY));
        System.out.println("Workflow Submission Portlet: Using workflows directory " + dir);

        // Load the workflows once at initialisation time
        workflowFileNamesList = new ArrayList<String>();
        workflowList = new ArrayList<Workflow>();
        wrappedWorkflowXMLDocumentsList = new ArrayList<Document>();
        workflowInputPortsList = new ArrayList<ArrayList<WorkflowInputPort>>();

        // Filter only workflows i.e. files of type .t2flow
        /*FilenameFilter t2flowFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(Constants.T2_FLOW_FILE_EXT);
            }
        };*/
        String[] workflowFileNames = dir.list(WorkflowResultsPortlet.t2flowFileFilter);
        int numberOfLoadedWorkflowFiles = 0;
       
        SAXBuilder builder = new SAXBuilder();

        for (int i=0; i<workflowFileNames.length; i++)
        { 
            // Get the workflow filename (without the .t2flow extension)
            String workflowFileName = workflowFileNames[i].substring(0, workflowFileNames[i].lastIndexOf('.'));

            // Parse the workflow file
            FileInputStream workflowInputStream = null;
            Document workflowDocument;
            try {
                workflowInputStream = new FileInputStream(getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY) +
                        Constants.FILE_SEPARATOR + workflowFileNames[i]);
            } catch (FileNotFoundException e) {
                System.out.println("Workflow Submission Portlet: could not find workflow file " + getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY) + 
                        Constants.FILE_SEPARATOR + workflowFileNames[i]);
                e.printStackTrace();
                continue;
            }
            try {
            	workflowDocument = builder.build(workflowInputStream);
            } catch (JDOMException ex1) {
                System.out.println("Workflow Submission Portlet: could not parse the workflow file " + getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY) + 
                        Constants.FILE_SEPARATOR + workflowFileNames[i]);
                ex1.printStackTrace();
                continue;
            } catch (IOException ex2) {
                System.out.println("Workflow Submission Portlet: could not open workflow file " + getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY) + 
                        Constants.FILE_SEPARATOR +  workflowFileNames[i]+ " to parse it.");
                ex2.printStackTrace();
                continue;
            }

            // Get the workflow annotations, such as description, title etc.
            Element topWorkflow = getTopDataflow(workflowDocument.getRootElement()); // top workflow, ignore nested workflows
            Workflow workflow = new Workflow();
            Element workflowAnnotationsElement = topWorkflow.
                            getChild(Constants.ANNOTATIONS_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE);
            workflow.setDescription(getLatestAnnotationAssertionImplElementValue(workflowAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_FREETEXT_CLASS, workflowFileName));
            workflow.setTitle(getLatestAnnotationAssertionImplElementValue(workflowAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_DESCRIPTIVETITLE_CLASS, workflowFileName));
            workflow.setAuthor(getLatestAnnotationAssertionImplElementValue(workflowAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_AUTHOR_CLASS, workflowFileName));
            
            System.out.println("Workflow Submission Portlet: Parsing workflow " +workflowFileName + " finished.");
            System.out.println("Workflow Submission Portlet: Workflow name: " + workflow.getTitle() + ", description: " + workflow.getDescription() +".\n");
            System.out.println("Workflow Submission Portlet: Parsing inputs for workflow " +workflowFileName + ".");

            // Get the workflow input parameters and their annotations, if any.
            ArrayList<WorkflowInputPort> workflowInputPorts = new ArrayList<WorkflowInputPort>();
            Element workflowInputPortsElement = topWorkflow.getChild(Constants.DATAFLOW_INPUT_PORTS_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE);
            for (Element inputPortElement : (List<Element>)workflowInputPortsElement.getChildren(Constants.DATAFLOW_PORT, Constants.T2_WORKFLOW_NAMESPACE)) {

                Element workflowInputPortAnnotationsElement = inputPortElement
                        .getChild(Constants.ANNOTATIONS_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE);

                // Get the input port's name and depth
                String inputPortName = inputPortElement.getChildText(Constants.NAME_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE);
                int inputPortDepth = Integer.valueOf(inputPortElement.getChildText(Constants.DEPTH_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE));

                WorkflowInputPort inputPort = new WorkflowInputPort();
                inputPort.setName(inputPortName);
                inputPort.setDepth(inputPortDepth);
                inputPort.setDescription(getLatestAnnotationAssertionImplElementValue(workflowInputPortAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_FREETEXT_CLASS, workflowFileName));
                inputPort.setExampleValue(getLatestAnnotationAssertionImplElementValue(workflowInputPortAnnotationsElement, Constants.ANNOTATION_BEAN_ELEMENT_EXAMPLEVALUE_CLASS, workflowFileName));
                workflowInputPorts.add(inputPort);

                System.out.println("Workflow Submission Portlet: Input port name: " + inputPortName + ", depth: " + inputPortDepth + ", description: " + inputPort.getDescription() + ", example value: " + inputPort.getExampleValue());
            }
            System.out.println();

            // Also generate the JSP snippet for the workflow's input form while
            // we are at it and save it to a JSP file, if such a file already does not exist.
            // We will dispatch to this form later, when user select this workflow.
            File workflowInputFormJSPSnippetFile = new File(getPortletContext().getRealPath(Constants.WORKFLOWS_DIRECTORY +
                    Constants.FILE_SEPARATOR + workflowFileName + ".jsp"));
            if (!workflowInputFormJSPSnippetFile.exists()){
                if (! createWorkflowInputFormJSPSnippetFile(workflowFileName, workflow, workflowInputPorts)){
                    // If we cannot generate the workflow inputs form - skip this workflow altogether
                    continue;
                }
            }

            // Wrap the workflow document inside a <workflow> element
            // in the T2 Server namespace as expected by the Server.
            Element workflowWrapperElement = new Element(Constants.T2_SERVER_WORKFLOW_ELEMENT, Constants.T2_SERVER_NAMESPACE);
            Element oldWorkflowRootElement = workflowDocument.getRootElement();
            oldWorkflowRootElement.detach(); // detach it from the previous document
            workflowWrapperElement.addContent(oldWorkflowRootElement); // attach the old root to the new root
            Document wrappedWorkflowDocument = new Document(workflowWrapperElement);

            workflowFileNamesList.add(workflowFileName);
            workflowList.add(workflow);
            wrappedWorkflowXMLDocumentsList.add(wrappedWorkflowDocument);
            workflowInputPortsList.add(workflowInputPorts);
            numberOfLoadedWorkflowFiles++;
        }
        System.out.println("Workflow Submission Portlet: Successfully loaded " + numberOfLoadedWorkflowFiles + " out of " + workflowFileNames.length + " workflow files.\n");
    }

    @Override
    public void processAction(ActionRequest request, ActionResponse response) throws PortletException,IOException {

        // Just print all the parameters we have received, for testing purposes
        Enumeration names = request.getParameterNames();
        while(names.hasMoreElements()){
            String parameterName = (String) names.nextElement();
            System.out.println("\nWorkflow Submission Portlet: parameter name: " + parameterName);
            System.out.println("Workflow Submission Portlet: parameter value: " + request.getParameter(parameterName));
            System.out.println();
        }

        // Is this a multipart/form-data form submission request?
        // This is the case when user is submitting input values for the selected workflow.
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
            }
            catch(FileUploadException fuex){
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
                        System.out.println("Workflow Submission Portlet: multipart form parameter name (without namespace prefix): " + fieldName + ", file name: " + fileName);
                    }
                }
            }

            // Was there a request to run a workflow?
            if (formItems.keySet().contains(Constants.RUN_WORKFLOW)){

                // Workflow to run
                String workflowFileName = (String) formItems.get(WORKFLOW_FILE_NAME);
                
                // Workflow's input ports
                ArrayList<WorkflowInputPort> workflowInputPorts = workflowInputPortsList.get(workflowFileNamesList.indexOf(workflowFileName));

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
                        }
                        catch(Exception ex){
                            System.out.println("Workflow Submission Portlet: Failed to read the submitted file for input " + inputPort.getName() + " of workflow " + workflowFileName + ".");
                            ex.printStackTrace();
                            request.setAttribute(Constants.ERROR_MESSAGE, "Failed to read the submitted file for input " + inputPort.getName() + " of workflow " + workflowFileName + ".");
                            inputsSuccessfullyRead = false;
                            return;
                        }
                        finally{
                            try{
                                is.close();
                            }
                            catch(Exception ex2){
                                // Do nothing
                            }
                        }

                        // Is this a single value input?
                        if (inputPort.getDepth() == 0){
                            // Just set the input port value to the byte[] we have just read from the file.
                            inputPort.setValue(theBytes);
                            System.out.println("Workflow Submission Portlet: The value the user submitted (as a file) for the input port '" + 
                                    inputPort.getName() + "'was : " + new String(theBytes, "UTF-8"));
                        }
                        // Is this input a list?
                        // We have to split the contents of the file as list items.
                        else if (inputPort.getDepth() == 1){
                            // Read byte[] into a String (hopefully it was a text file)
                            // using UTF-8 encoding and hope for the best. Then separate
                            // the string into items based on the submitted separator.
                            String valueToSeparate = new String(theBytes, "UTF-8");
                            String listSeparator;
                            String userDefinedListSeparator = (String) formItems.get(inputPort.getName() + WORKFLOW_INPUT_CONTENT_OTHER_SEPARATOR_SUFFIX);
                            if (userDefinedListSeparator != null &&
                                    !userDefinedListSeparator.equals("")){
                                listSeparator = getListSeparator(userDefinedListSeparator);
                            }
                            else{
                                listSeparator = getListSeparator((String) formItems.get(inputPort.getName() + WORKFLOW_INPUT_CONTENT_SEPARATOR_SUFFIX));
                            }
                            String[] valueList = valueToSeparate.split(listSeparator);
                            inputPort.setValue(Arrays.asList(valueList));
                            System.out.println("Workflow Submission Portlet: The list the user submitted (as a file) for the input port '" +
                                    inputPort.getName() + "' was: " + valueToSeparate + " (hex value " + stringToHex(valueToSeparate.getBytes("UTF-8")) +")");
                            System.out.println("Extracted list items:");
                            for (int i=0; i< valueList.length; i++) {
                                System.out.println("Item " + (i+1) + ": " + valueList[i] + " (hex value: " +stringToHex(valueList[i].getBytes("UTF-8"))+ ")");
                            }
                        }
                        // Is this input a list of (... lists of ...) lists?
                        // We currently support input list depths up to 1.
                        else {
                            System.out.println("Workflow Submission Portlet: Input " + inputPort.getName() + " of workflow " + workflowFileName + " expects a list of depth more than 1 which is currently not supported.");
                            request.setAttribute(Constants.ERROR_MESSAGE, "Input " + inputPort.getName() + " of workflow " + workflowFileName + " expects a list of depth more than 1 which is currently not supported.");
                            inputsSuccessfullyRead = false;
                            return;
                        }
                    }
                    else if(formItems.get(inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX) != null &&
                            !((String) formItems.get(inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX)).equals("")){

                        // Is this a single value input? Just get whatever content was submitted.
                        if (inputPort.getDepth() == 0){
                            inputPort.setValue((String) formItems.get(inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX));
                            System.out.println("Workflow Submission Portlet: The value the user submitted (from textarea input field) for the input port '" +
                                    inputPort.getName() + "' was: " +
                                    (String)inputPort.getValue());
                        }
                        // Is this input a list? We have to split the string to get the list items.
                        else if (inputPort.getDepth() == 1){
                            
                            String valueToSeparate = (String) formItems.get(inputPort.getName() + Constants.WORKFLOW_INPUT_CONTENT_SUFFIX);

                            String listSeparator;
                            String userDefinedListSeparator = (String) formItems.get(inputPort.getName() + WORKFLOW_INPUT_CONTENT_OTHER_SEPARATOR_SUFFIX);
                            if (userDefinedListSeparator != null &&
                                    !userDefinedListSeparator.equals("")){
                                listSeparator = getListSeparator(userDefinedListSeparator);
                            }
                            else{
                                listSeparator = getListSeparator((String) formItems.get(inputPort.getName() + WORKFLOW_INPUT_CONTENT_SEPARATOR_SUFFIX));
                            }
                            String[] valueList;
                            // Since the enctype of the form is multipart/form-data,
                            // lines in a HTML text area are separated with unencoded CRLF (\r\n)
                            // regadless of the platform!
                            if (listSeparator.equals("\n") || listSeparator.equals("\r\n")){
                                // Use "\r\n" in both these cases as that is what the text will be separated with
                               valueList = valueToSeparate.split("\r\n");
                            }
                            else{
                               valueList = valueToSeparate.split(listSeparator);
                            }
                            inputPort.setValue(Arrays.asList(valueList));
                            System.out.println("Workflow Submission Portlet: The list the user submitted (as a file) for the input port '" +
                                    inputPort.getName() + "' was: " + valueToSeparate + " (hex value " + stringToHex(valueToSeparate.getBytes("UTF-8")) +")");
                            System.out.println("Extracted list items:");
                            for (int i=0; i< valueList.length; i++) {
                                System.out.println("Item " + (i+1) + ": " + valueList[i] + " (hex value: " +stringToHex(valueList[i].getBytes("UTF-8"))+ ")");
                            }
                        }
                        // Is this input a list of (... lists of ...) lists?
                        // We currently support input list depths up to 1.
                        else {
                            System.out.println("Workflow Submission Portlet: Input " + inputPort.getName() + " of workflow " + workflowFileName + " expects a list of depth more than 1 which is currently not supported.");
                            request.setAttribute(Constants.ERROR_MESSAGE, "Input " + inputPort.getName() + " of workflow " + workflowFileName + " expects a list of depth more than 1 which is currently not supported.");
                            inputsSuccessfullyRead = false;
                            return;
                        }
                    }
                    else{
                        // We do not have a value for this input port
                        System.out.println("Workflow Submission Portlet: Submitted value for input " + inputPort.getName() + " of workflow " + workflowFileName + " is null. Submission was cancelled as this will cause the workflow to fail.");
                        request.setAttribute(Constants.ERROR_MESSAGE, "Submitted value for input " + inputPort.getName() + " of workflow " + workflowFileName + " is null. Submission was cancelled as this will cause the workflow to fail.");
                        inputsSuccessfullyRead = false;
                        return;
                    }
                } // We have now finished reading all the inputs

                if (inputsSuccessfullyRead){
                    // Submit the workflow to the T2 Server in preparation for execution
                    HttpResponse httpResponse = submitWorkflow(workflowFileName, request);

                    // Submit the workflow's inputs to the Taverna 2 Server in
                    // prepartion for the workflow execution
                    if (httpResponse != null){ // null indicates something went wrong

                        // Extract the workflowResourceUUID returned by the T2 Server,
                        // something like http://<SERVER>/taverna-server/rest/runs/UUID,
                        // from the Location header, where UUID part identifies the submitted workflow on the T2 Server
                        String workflowResourceUUID;
                        workflowResourceUUID = httpResponse.getHeaders(Constants.LOCATION_HEADER_NAME)[0].getValue();
                        workflowResourceUUID = workflowResourceUUID.substring(workflowResourceUUID.lastIndexOf("/") + 1);
                        System.out.println("Workflow Submission Portlet: Workflow " + workflowFileName + " successfully submitted to the Server with UUID " + workflowResourceUUID +".");

                        // Try to set the property that contains the name of the
                        // Baclava file where outputs are to be written to.
                        boolean outputBaclavaFilePropertySet = setBaclavaOutputFile(workflowFileName, workflowResourceUUID, request);
                        if (outputBaclavaFilePropertySet){

                            Document worfklowInputsDocument = buildWorkflowInputsBaclavaDocument(workflowInputPorts);
                            boolean inputsSubmitted = submitWorkflowInputs(workflowFileName, workflowResourceUUID, worfklowInputsDocument, request);

                            // Run the workflow on the T2 Server
                            if (inputsSubmitted){
                                System.out.println("Workflow Submission Portlet: Inputs for workflow " + workflowFileName + " successfully submitted to the Server.");

                                boolean runSubmitted = runWorkflow(workflowFileName, workflowResourceUUID, request);
                                if (runSubmitted){
                                    System.out.println("Workflow Submission Portlet: Execution of workflow " + workflowFileName + " successfully initiated on the Server. Job ID: " + workflowResourceUUID +".");

                                    // Add this workflowResourceUUID to the list of submitted workflow UUIDs
                                    // to be read by the Workflow Results portlet and used to fetch results for
                                    // this run
                                    ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)request.getPortletSession().
                                            getAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
                                            PortletSession.APPLICATION_SCOPE); // should not be null at this point
                                            
                                    WorkflowSubmissionJob job = new WorkflowSubmissionJob(workflowResourceUUID, workflowFileName, Constants.JOB_STATUS_OPERATING);
                                    job.setStartDate(new Date());
                                    workflowSubmissionJobs.add(0,job);

                                    // Persist the detains of the newly created job on disk
                                    persistJobOnDisk(request, job, worfklowInputsDocument);
                                   
                                    request.getPortletSession().
                                            setAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
                                            workflowSubmissionJobs,
                                            PortletSession.APPLICATION_SCOPE);
                                    request.setAttribute(Constants.INFO_MESSAGE, "Workflow "+workflowFileName+" successfully submitted for execution with job ID: " + workflowResourceUUID + ". You may use this ID to monitor the progress of the workflow run.");
                                }
                            }
                        }
                    }
                }
            }
        }
        else{ // just a standard application/x-www-form-urlencoded form submission request

            // Was there a request to run a workflow?
            if (request.getParameter(Constants.RUN_WORKFLOW) != null){

                // Workflow to run
                String workflowFileName = request.getParameter(WORKFLOW_FILE_NAME);

                // Workflow's input ports
                ArrayList<WorkflowInputPort> workflowInputPorts = workflowInputPortsList.get(workflowFileNamesList.indexOf(workflowFileName));

                // Get the workflow inputs from the submitted form and fill the
                // list of inputs with the submitted values
                for (WorkflowInputPort inputPort : workflowInputPorts){
                    inputPort.setValue(request.getParameter(inputPort.getName()));
                }
                // Submit the workflow to the T2 Server in preparation for execution
                HttpResponse httpResponse = submitWorkflow(workflowFileName, request);

                // Submit the workflow's inputs to the Taverna 2 Server in
                // prepartion for workflow execution
                if (httpResponse != null){ // null indicates something went wrong

                    // Extract the workflowResourceUUID returned by the T2 Server,
                    // something like http://<SERVER>/taverna-server/rest/runs/UUID,
                    // from the Location header, where UUID part identifies the submitted workflow on the T2 Server
                    String workflowResourceUUID;
                    workflowResourceUUID = httpResponse.getHeaders(Constants.LOCATION_HEADER_NAME)[0].getValue();
                    workflowResourceUUID = workflowResourceUUID.substring(workflowResourceUUID.lastIndexOf("/") + 1);
                    System.out.println("Workflow Submission Portlet: Workflow " + workflowFileName + " successfully submitted to the Server with UUID " + workflowResourceUUID +".");

                    // Try to set the property that contains the name of the
                    // Baclava file where outputs are to be written to.
                    boolean outputBaclavaFilePropertySet = setBaclavaOutputFile(workflowFileName, workflowResourceUUID, request);
                    if (outputBaclavaFilePropertySet){

                        Document worfklowInputsDocument = buildWorkflowInputsBaclavaDocument(workflowInputPorts);
                        
                        boolean inputsSubmitted = submitWorkflowInputs(workflowFileName, workflowResourceUUID, worfklowInputsDocument, request);

                        // Run the workflow on the T2 Server
                        if (inputsSubmitted){
                            System.out.println("Workflow Submission Portlet: Inputs for workflow " + workflowFileName + " successfully submitted to the Server.");

                            boolean runSubmitted = runWorkflow(workflowFileName, workflowResourceUUID, request);
                            if (runSubmitted){
                                System.out.println("Workflow Submission Portlet: Execution of workflow " + workflowFileName + " successfully initiated on the Server.");

                                // Add this workflowResourceUUID to the list of submitted workflow UUIDs
                                // to be read by the Workflow Results portlet and used to fetch results for
                                // the runs.

                                ArrayList<WorkflowSubmissionJob> workflowSubmissionJobs = (ArrayList<WorkflowSubmissionJob>)request.getPortletSession().
                                        getAttribute(Constants.WORKFLOW_SUBMISSION_JOBS, PortletSession.APPLICATION_SCOPE);
                                WorkflowSubmissionJob job = new WorkflowSubmissionJob(workflowResourceUUID, workflowFileName, Constants.JOB_STATUS_OPERATING);
                                workflowSubmissionJobs.add(job);
                                job.setStartDate(new Date());

                                // Persist the detains of the newly created job on disk
                                persistJobOnDisk(request, job, worfklowInputsDocument);

                                request.getPortletSession().
                                        setAttribute(Constants.WORKFLOW_SUBMISSION_JOBS,
                                        workflowSubmissionJobs,
                                        PortletSession.APPLICATION_SCOPE);
                            }
                        }
                    }
                }
            }
        }

        // Pass all request parameters over to the doView() and other render stage methods
        response.setRenderParameters(request.getParameterMap());
    }

    @Override
    public void doView(RenderRequest request,RenderResponse response) throws PortletException,IOException {
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

        // If a workflow has been selected - then also print its input form
        PortletRequestDispatcher dispatcher;
        dispatcher = getPortletContext().getRequestDispatcher("/WEB-INF/jsp/WorkflowSubmission_view.jsp");
        dispatcher.include(request, response);
        if (request.getParameter(Constants.WORKFLOW_SELECTION_SUBMISSION) != null){
            response.getWriter().println("<br />");
            response.getWriter().println("<hr />");
            response.getWriter().println("<br />");
            String selectedWorkflowFileName = request.getParameter(Constants.SELECTED_WORKFLOW);

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

    private Element getTopDataflow(Element element) {
	Element result = null;
	for (Object elObj : element.getChildren(Constants.DATAFLOW_ELEMENT, Constants.T2_WORKFLOW_NAMESPACE)) {
		Element dataflowElement = (Element)elObj;
		if (Constants.DATAFLOW_ROLE_TOP.equals(dataflowElement.getAttribute(Constants.DATAFLOW_ROLE).getValue())) {
			result=dataflowElement;
		}
	}
	return result;
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
        inputFormJSP.append("<%@ include file=\"/WEB-INF/jsp/InputsCSS.jsp\" %>\n\n");

        // Workflow name and description
        if (workflow.getTitle() != null && !workflow.getTitle().equals("")){
            inputFormJSP.append("<b>Workflow:</b> "+workflow.getTitle()+"\n");
        }
        else{
            inputFormJSP.append("<b>Workflow:</b> "+workflowFileName+"\n");
        }
        if (workflow.getDescription() != null && !workflow.getDescription().equals("")){
            inputFormJSP.append("<br/>\n");
            inputFormJSP.append("<br/>\n");
            inputFormJSP.append("<b>Description:</b> " + workflow.getDescription() +"\n");
        }
        inputFormJSP.append("<br/>\n");
        inputFormJSP.append("<br/>\n");

        // Workflow inputs form
        inputFormJSP.append("<b>Workflow inputs:</b>\n");

        if (workflowInputPorts.isEmpty()){
            inputFormJSP.append("The workflow does not require any inputs<br/><br/>\n");
            inputFormJSP.append("<form name=\"<portlet:namespace/><%= Constants.WORKFLOW_INPUTS_FORM%>\" action=\"<portlet:actionURL/>\" method=\"post\" enctype=\"multipart/form-data\" onSubmit=\"return validateForm(this)\">\n");
        }
        else{
            inputFormJSP.append("<form name=\"<portlet:namespace/><%= Constants.WORKFLOW_INPUTS_FORM%>\" action=\"<portlet:actionURL/>\" method=\"post\" enctype=\"multipart/form-data\" onSubmit=\"return validateForm(this)\">\n");

            inputFormJSP.append("<table class=\"inputs\">\n");
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
                    String descriptionString = " ";
                    if (inputPort.getDescription() != null){
                        descriptionString += inputPort.getDescription();
                    }
                    if (inputPort.getExampleValue() != null && !inputPort.getExampleValue().equals("")){
                        descriptionString += "<br/><br/><b>Example value:</b> "+inputPort.getExampleValue();
                    }
                    inputFormJSP.append("<td>" + descriptionString + "</td>\n");
                    inputFormJSP.append("<td>\n");
                    inputFormJSP.append("Paste the value here: <br/>\n");
                    inputFormJSP.append("<textarea name=\"<portlet:namespace/>"+inputPort.getName()+Constants.WORKFLOW_INPUT_CONTENT_SUFFIX+"\" rows=\"2\" cols=\"20\" wrap=\"off\"></textarea><br/>\n");
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
                    String descriptionString = " ";
                    if (inputPort.getDescription() != null){
                        descriptionString += inputPort.getDescription();
                    }
                    if (inputPort.getExampleValue() != null && !inputPort.getExampleValue().equals("")){
                        descriptionString += "<br/><br/><b>Example value:</b> "+inputPort.getExampleValue();
                    }
                    inputFormJSP.append("<td>" + descriptionString + "</td>\n");
                    inputFormJSP.append("<td>\n");
                    inputFormJSP.append("Paste the list values here: <br/>\n");
                    inputFormJSP.append("<textarea name=\"<portlet:namespace/>"+inputPort.getName()+Constants.WORKFLOW_INPUT_CONTENT_SUFFIX+"\" rows=\"2\" cols=\"20\" wrap=\"off\"></textarea><br/>\n");
                    inputFormJSP.append("Or load them from a file: <br />\n");
                    inputFormJSP.append("<input type=\"file\" name=\"<portlet:namespace/>"+inputPort.getName()+Constants.WORKFLOW_INPUT_FILE_SUFFIX+"\" /><br/><hr/>\n");
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
        

        inputFormJSP.append("<%-- Hidden field to convey which workflow we want to execute --%>\n");
        inputFormJSP.append("<input type=\"hidden\" name=\"<portlet:namespace/><%= Constants.WORKFLOW_FILE_NAME%>\" value=\""+ workflowFileName + "\" />\n");
        inputFormJSP.append("<input type=\"submit\" name=\"<portlet:namespace/><%= Constants.RUN_WORKFLOW%>\" value=\"Run workflow\" />\n");
        inputFormJSP.append("</form>\n");

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
     * HTTP POSTs a wrapped workflow Document to the T2 Server.
     */
    HttpResponse submitWorkflow(String workflowFileName, ActionRequest request){

        HttpClient httpClient = new DefaultHttpClient();
        String runsURL = T2_SERVER_URL + Constants.RUNS_URL;

        HttpPost httpPost = new HttpPost(runsURL);
        httpPost.setHeader(Constants.CONTENT_TYPE_HEADER_NAME, Constants.CONTENT_TYPE_APPLICATION_XML);

       try {
            // Get the workflow XML Document's string representation
            int workflowIndex = workflowFileNamesList.indexOf(workflowFileName); // get the workflow index
            String workflowString = (new XMLOutputter()).outputString(wrappedWorkflowXMLDocumentsList.get(workflowIndex));

            System.out.println("Workflow Submission Portlet: Preparing to submit workflow to Server " + runsURL);
            //System.out.println(workflowString);

            StringEntity entity = new StringEntity(workflowString, "UTF-8");
            httpPost.setEntity(entity);
        }
        catch (UnsupportedEncodingException ex) {
        
            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity for workflow " + workflowFileName + " when POSTing the workflow to the Server.");
            ex.printStackTrace();
            request.setAttribute(Constants.ERROR_MESSAGE, "Failed to create message body for workflow " + workflowFileName + " when POSTing the worfkflow to the Server.");
            return null;
        }
        
        HttpResponse httpResponse = null;
        try{
            // Execute the request to upload the workflow file to the Server
            HttpContext localContext = new BasicHttpContext();
            httpResponse = httpClient.execute(httpPost, localContext);

            // Release resource
            httpClient.getConnectionManager().shutdown();

            if (httpResponse.getStatusLine().getStatusCode() != 201){ // HTTP/1.1 201 Created
                System.out.println("Workflow Submission Portlet: Failed to submit workflow " + workflowFileName + " for execution.\nServer responded with: " + httpResponse.getStatusLine());
                request.setAttribute(Constants.ERROR_MESSAGE, "Failed to submit workflow " + workflowFileName + " for execution.<br/>Server responded with: " + httpResponse.getStatusLine());
                return null;
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to POST request to submit workflow " + workflowFileName + " for execution.");
            ex.printStackTrace();
            request.setAttribute(Constants.ERROR_MESSAGE, "Failed to submit workflow " + workflowFileName + " for execution.<br/>Error: " +  ex.getMessage());
            return null;
        }

        return httpResponse;
    }

    /*
     * PUTs the value for property that keeps the name of the
     * output Baclava file where to save the results. This needs
     * to be done before the run is initiated.
     */
    private boolean setBaclavaOutputFile(String workflowFileName, String workflowResourceUUID, ActionRequest actionRequest){

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
            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity containing the name of the Baclava XML file where outputs for workflow " + workflowFileName + " will be saved to.");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to create message body containing the name of the XML file where outputs for workflow " + workflowFileName + " will be saved to.");
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
                System.out.println("Workflow Submission Portlet: Failed to set the name of the Baclava file where outputs are to be saved to for workflow " + workflowFileName + ". The Server responded with: " + httpResponse2.getStatusLine()+".");
                actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to set the name of the file where outputs are to be saved to for workflow " + workflowFileName + ". The Server responded with: " + httpResponse2.getStatusLine()+".");
                return false;
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: An error occured while setting the name of the Baclava file where outputs are to be saved to for workflow " + workflowFileName + " on the Server.");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "An error occured while setting the name of the file where outputs are to be saved to for workflow " + workflowFileName + " on the Server.");
            return false;
        }
        System.out.println("Workflow Submission Portlet: Property where to save the output Baclava file for workflow " + workflowFileName + " successfully set on the Server at " + baclavaOutputPropertyURL +".");
        return true;
    }

    /*
     * HTTP PUTs workflow inputs to the T2 Server as a Baclava XML document.
     * Returns false if it fails for any reason.
     */
    private boolean submitWorkflowInputs(String workflowFileName, String workflowResourceUUID, Document workflowInputsBaclavaDocument, ActionRequest actionRequest){

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
            System.out.println("Workflow Submission Portlet: Failed to Base64 encode the content of the Baclava XML file with workflow inputs for workflow " + workflowFileName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to Base64 encode the content of the XML file with workflow inputs for workflow " + workflowFileName + ".");
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

            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity containing Baclava XML document with workflow inputs for workflow " + workflowFileName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to create message body containing XML document with workflow inputs for workflow " + workflowFileName + ".");
            return false;
        }

        try{
            // Execute the request to upload the inputs Baclava file to the Server
            httpResponse = httpClient.execute(httpPost, localContext);

            // Release resource
            httpClient.getConnectionManager().shutdown();

            if (httpResponse.getStatusLine().getStatusCode() != 201){ // HTTP/1.1 201 Created
                System.out.println("Workflow Submission Portlet: Failed to upload the file with inputs for workflow " + workflowFileName + ". The Server responded with: " + httpResponse.getStatusLine()+".");
                actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to upload the file with inputs for workflow " + workflowFileName + ". The Server responded with: " + httpResponse.getStatusLine()+".");
                return false;
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: An error occured while trying to upload the XML Baclava file with inputs for workflow " + workflowFileName + " to the Server.");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "An error occured while trying to upload the file with inputs for workflow" + workflowFileName + " to the Server.");
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

            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity containing the name of the Baclava XML document with workflow inputs for workflow " + workflowFileName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to create message body containing the name of the XML file with workflow inputs for workflow " + workflowFileName + ".");
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
                System.out.println("Workflow Submission Portlet: Failed to set the name of the file with inputs for workflow " + workflowFileName + ". The Server responded with: " + httpResponse2.getStatusLine()+".");
                actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to set the name of the file with inputs for workflow " + workflowFileName + ". The Server responded with: " + httpResponse2.getStatusLine()+".");
                return false;
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: An error occured while setting the name of the file with inputs for workflow " + workflowFileName + " on the Server.");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "An error occured while setting the name of the file with inputs for workflow " + workflowFileName + " on the Server.");
            return false;
        }

       return true;
    }

    /*
     * HTTP PUTs the status of the workflow to "Operating" to kick start its execution.
     * Returns false if it fails for any reason.
     */
    boolean runWorkflow(String workflowFileName, String workflowResourceUUID,  ActionRequest actionRequest){
        
        HttpClient httpClient = new DefaultHttpClient();
        String statusURL = T2_SERVER_URL + Constants.RUNS_URL + "/" + workflowResourceUUID + Constants.STATUS_URL;

        HttpPut httpPut = new HttpPut(statusURL);
        httpPut.setHeader(Constants.CONTENT_TYPE_HEADER_NAME, Constants.CONTENT_TYPE_TEXT_PLAIN);

        try {
            StringEntity entity = new StringEntity(Constants.JOB_STATUS_OPERATING, "UTF-8");
            httpPut.setEntity(entity);
        }
        catch (UnsupportedEncodingException ex) {

            System.out.println("Workflow Submission Portlet: Failed to create an HTTP entity containing the workflow run status set to 'Operating' for workflow " + workflowFileName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to create the status message body used for initiating the execution of workflow " + workflowFileName + ".");
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
               System.out.println("Workflow Submission Portlet: Failed to initiate the execution of workflow " + workflowFileName + ". The Server responded with: " + httpResponse.getStatusLine()+".");
               actionRequest.setAttribute(Constants.ERROR_MESSAGE, "Failed to initiate the execution of workflow " + workflowFileName + ". The Server responded with: " + httpResponse.getStatusLine() +".");
               return false;
            }
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: An error occured while trying to initiate the execution of workflow " + workflowFileName + ".");
            ex.printStackTrace();
            actionRequest.setAttribute(Constants.ERROR_MESSAGE, "An error occured while to initiate the execution of workflow " + workflowFileName +  ".");
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
     * Given an <annotations> element from the .t2flow file,
     * it finds the latest <net.sf.taverna.t2.annotation.AnnotationAssertionImpl> 
     * element regardless of its location in the <annotations>, element whose 
     * <annotationBean> sub-element has a class attribute that matches the
     * passed value. It then returns the value of the <text> element inside that
     * <annotationBean> element.
     */
    public String getLatestAnnotationAssertionImplElementValue(Element annotationsElement, String annotationBeanClassName, String workflowFileName){

        //System.out.println("Getting annotations with class='" + annotationBeanClassName + "' from element " + new XMLOutputter().outputString(annotationsElement) + "\n");

        // Select all <net.sf.taverna.t2.annotation.AnnotationAssertionImpl>
        // elements no matter where they are located inside the <assertions> element passed.
        List<Element> annotationAssertionImplElements = null;
        try{
            //JDOMXPath path = new JDOMXPath(".//"+Constants.ANNOTATION_ASSERTION_IMPL_ELEMENT);
            //annotationAssertionImplElements = path.selectNodes(annotationsElement);

            annotationAssertionImplElements = XPath.selectNodes(annotationsElement,".//"+Constants.ANNOTATION_ASSERTION_IMPL_ELEMENT);
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to parse the annotations element when looking for " + annotationBeanClassName + " in worklow " + workflowFileName +".");
            ex.printStackTrace();
            return null;
        }

        // Loop over all the annotation assertion implementation elements
        // and find the latest that has an annotation bean whose class
        // matches the one we are looking for.
        String latestValue = null;
        Date latestDate = new Date(0);
        if (annotationAssertionImplElements != null){
            for (Element annotationAssertionImplElement : annotationAssertionImplElements){

                Element annotationBeanElement = annotationAssertionImplElement
                        .getChild(Constants.ANNOTATION_BEAN_ELEMENT);

                Date date = null;
                String pattern = "yyyy-MM-dd HH:mm:ss.SSS z";
                SimpleDateFormat format = new SimpleDateFormat(pattern);
                if (annotationBeanElement.getAttributeValue(Constants.ANNOTATION_BEAN_ELEMENT_CLASS_ATTRIBUTE).equals(annotationBeanClassName)){
                    String value = annotationBeanElement.getChildText(Constants.TEXT_ELEMENT);

                    try {
                        date = format.parse(annotationAssertionImplElement.getChildText(Constants.DATE_ELEMENT));
                        if (latestDate.before(date)){
                            latestValue = value;
                            latestDate = date;
                        }
                    } catch (ParseException ex) {
                        System.out.println("Workflow Submission Portlet: Failed to parse the annotation bean date for " + annotationBeanClassName + " in workflow "+workflowFileName+". Skipping this element.");
                        ex.printStackTrace();
                        continue;
                    }
                }
            }
        }
        return latestValue;
    }

    /*
     * Persist the details of a job on a disk. A directory named after
     * the job's UUID will be created in the jobs directory for the owning user
     * and will contain:
     *  - an empty .t2flow file named after the workflow file just to hold the worflow file name
     *  - an empty file initially named Operating.status to indicate the status of the job
     *  - the input Baclava document in input.baclava file to hold the job's inputs
     *  - an empty file named <long>.startdate where <long> represents the Date in miliseconds after the "epoch"
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

        // Save the workflow name by creating an empty file with the same name
        File workflowFile = new File(jobDir, job.getWorkflowFileName() + Constants.T2_FLOW_FILE_EXT);
        try{
            FileUtils.touch(workflowFile);
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to create the job's status file " + workflowFile.getAbsolutePath());
            ex.printStackTrace();
        }
        System.out.println("Workflow Submission Portlet: Job's workflow name set at " + workflowFile.getAbsolutePath());

        // Save the job's start date by creating an empty file named after the date
        File startdateFile = new File(jobDir, job.getStartDate().getTime() + Constants.STARTDATE_FILE_EXT);
        try{
            FileUtils.touch(startdateFile);
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to create the job's start date file " + startdateFile.getAbsolutePath());
            ex.printStackTrace();
        }
        System.out.println("Workflow Submission Portlet: Job's start date file name set at " + startdateFile.getAbsolutePath());

        // Save the job's input Baclava file in a file called inputs.baclava
        File inputsFile = new File(jobDir, Constants.INPUTS_BACLAVA_FILE);
        try{
            XMLOutputter out = new XMLOutputter();
            java.io.FileWriter writer = new java.io.FileWriter(inputsFile);
            out.output(worfklowInputsDocument, writer);
            writer.flush();
            writer.close();
        }
        catch(Exception ex){
            System.out.println("Workflow Submission Portlet: Failed to save job's inputs to file " + inputsFile.getAbsolutePath());
            ex.printStackTrace();
        }
        System.out.println("Workflow Submission Portlet: Job's inputs saved to Baclava file " + inputsFile.getAbsolutePath());
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