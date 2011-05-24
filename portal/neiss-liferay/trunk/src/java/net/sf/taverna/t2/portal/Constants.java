package net.sf.taverna.t2.portal;

import org.jdom.Namespace;

/**
 *
 * @author Alex Nenadic
 */
public class Constants {
    // File system separator
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static final String WORKFLOWS_DIRECTORY = "/WEB-INF/workflows"; // relative URL path from the root of the app
    public static final String T2FLOW_FILE_EXT = ".t2flow";
    public static final String PROPERTIES_FILE_EXT = ".properties"; // when saving info about wf as part of submitted job
    public static final String STATUS_FILE_EXT = ".status";
    public static final String STARTDATE_FILE_EXT = ".startdate";
    public static final String ENDDATE_FILE_EXT = ".enddate";
    public static final String WORKFLOW_PROPERTIES_FILE = "workflow.properties";
    public static final String WORKFLOW_RUN_DESCRIPTION_FILE = "workflow_run_description.txt";
    public static final String INPUTS_BACLAVA_FILE = "inputs.baclava";// saved on local disk
    public static final String INPUTS_DIRECTORY_NAME = "inputs"; // name of the dir where individual outputs  for all port are saved
    public static final String OUTPUTS_BACLAVA_FILE = "outputs.baclava"; // saved on local disk
    public static final String OUTPUTS_DIRECTORY_NAME = "outputs"; // name of the dir where individual outputs  for all port are saved
    public static final String DATA_FILE_PATH = "data_file_path"; // absolute path to the file with data

    // URL parameters
    public static final String FETCH_RESULTS="fetch_results";
    public static final String DELETE_JOB = "delete_job";
    public static final String MIME_TYPE="mime_type";
    public static final String FULL_DATA="full_data";
    public static final String DATA_SIZE_IN_KB = "data_size_in_kb";

    // Anonymous user
    public static final String USER_ANONYMOUS = "anonymous";

    // Application scope attributes
    public static final String WORKFLOW_SUBMISSION_JOBS = "workflow_submission_jobs";
    public static final String WORKFLOW_RESULTS_BACLAVA_FILE_URL = "workflow_results_baclava_file_URL";
    public static final String WORKFLOW_SUBMISSION_JOB = "workflow_submission_job";
    public static final String USER = "user";
    public static final String MYEXPERIMENT_WORKFLOWS_INPUT_FORM = "myexperiment_workflows_input_forms";

    // Portlet scope attributes
    public static final String ERROR_MESSAGE = "error_message";
    public static final String INFO_MESSAGE = "info_message";
    public static final String OUTPUTS_MAP_ATTRIBUTE = "outputs_map";
    public static final String INPUTS_MAP_ATTRIBUTE = "inputs_map";

    // Init parameters
    public static final String T2_SERVER_URL_PROPERTY = "T2_SERVER_URL";
    public static final String FILE_SERVLET_URL_PROPERTY = "FILE_SERVLET_URL";
    public static final String JOBS_DIRECTORY_PATH_PROPERTY = "JOBS_DIRECTORY_PATH";
    public static final String MAX_PREVIEW_DATA_SIZE_IN_KB_PROPERTY = "MAX_PREVIEW_DATA_SIZE_IN_KB";
    public static final String MYEXPERIMENT_BASE_URL_PROPERTY = "MYEXPERIMENT_BASE_URL";

    // .t2flow XML namespace
    public static final Namespace T2_WORKFLOW_NAMESPACE = Namespace.getNamespace("http://taverna.sf.net/2008/xml/t2flow");
    // Baclava documents XML namespace
    public static Namespace BACLAVA_NAMESPACE = Namespace.getNamespace("b","http://org.embl.ebi.escience/baclava/0.1alpha");
    // XML workflow elements
    public static final String DATAFLOW_ELEMENT = "dataflow";
    public static final String DATAFLOW_ROLE = "role";
    public static final String DATAFLOW_ROLE_TOP  = "top";
    public static final String DATAFLOW_INPUT_PORTS_ELEMENT = "inputPorts";
    public static final String DATAFLOW_PORT = "port";
    public static final String NAME_ELEMENT = "name";
    public static final String DEPTH_ELEMENT = "depth";
    public static final String GRANULAR_DEPTH_ELEMENT = "granularDepth";
    public static final String ANNOTATIONS_ELEMENT = "annotations";
    public static final String ANNOTATION_CHAIN_ELEMENT = "annotation_chain";
    public static final String ANNOTATION_CHAIN_IMPL_ELEMENT = "net.sf.taverna.t2.annotation.AnnotationChainImpl";
    public static final String ANNOTATION_ASSERTIONS_ELEMENT = "annotationAssertions";
    public static final String ANNOTATION_ASSERTION_IMPL_ELEMENT = "net.sf.taverna.t2.annotation.AnnotationAssertionImpl";
    public static final String ANNOTATION_BEAN_ELEMENT = "annotationBean";
    public static final String ANNOTATION_BEAN_ELEMENT_CLASS_ATTRIBUTE = "class";
    public static final String ANNOTATION_BEAN_ELEMENT_FREETEXT_CLASS = "net.sf.taverna.t2.annotation.annotationbeans.FreeTextDescription";
    public static final String ANNOTATION_BEAN_ELEMENT_EXAMPLEVALUE_CLASS = "net.sf.taverna.t2.annotation.annotationbeans.ExampleValue";
    public static final String ANNOTATION_BEAN_ELEMENT_DESCRIPTIVETITLE_CLASS = "net.sf.taverna.t2.annotation.annotationbeans.DescriptiveTitle";
    public static final String ANNOTATION_BEAN_ELEMENT_AUTHOR_CLASS = "net.sf.taverna.t2.annotation.annotationbeans.Author";
    public static final String TEXT_ELEMENT = "text";
    public static final String DATE_ELEMENT = "date";

    // REST
    public static final Namespace T2_SERVER_NAMESPACE = Namespace.getNamespace("http://ns.taverna.org.uk/2010/xml/server/");
    public static final Namespace T2_SERVER_REST_NAMESPACE = Namespace.getNamespace("t2sr", "http://ns.taverna.org.uk/2010/xml/server/rest/");
    public static final String T2_SERVER_WORKFLOW_ELEMENT = "workflow";
    public static final String RUNS_URL = "/rest/runs";
    public static final String WD_URL = "/wd";
    public static final String BACLAVA_INPUTS_PROPERTY_URL = "/input/baclava";
    public static final String BACLAVA_OUTPUT_PROPERTY_URL = "/output";
    public static final String STATUS_URL = "/status";
    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    public static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public static final String CONTENT_TYPE_APPLICATION_OCTETSTREAM = "application/octet-stream";
    public static final String LOCATION_HEADER_NAME = "Location";
    public static final String JOB_STATUS_OPERATING = "Operating";
    public static final String JOB_STATUS_FINISHED = "Finished";
    public static final String JOB_STATUS_EXPIRED = "Expired"; // no longer available on the T2 Server - this is local to the app, no such status exists on the Server
    public static final String UNKNOWN_RUN_UUID = "unknown run UUID";
    public static final String BACLAVA_OUTPUT_FILE_NAME = "output.baclava";

    // XML upload file element
    public static final String UPLOAD_FILE_ELEMENT = "upload";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String INPUT_ELEMENT = "runInput";
    public static final String FILE_ELEMENT = "file";

    // HTML forms
    public static final String WORKFLOW_FILE_NAMES = "workflow_file_names";
    public static final String WORKFLOW_SELECTION_SUBMISSION_FORM = "workflow_selection_form";
    public static final String WORKFLOW_SELECTION_SUBMISSION = "workflow_selection";
    public static final String WORKFLOW_RUN_DESCRIPTION = "workflow_run_description";
    public static final String SELECTED_WORKFLOW = "selected_workflow";
    public static final String WORKFLOW_INPUTS_FORM = "workflow_inputs_form";
    public static final String RUN_WORKFLOW = "run_workflow";
    public static final String WORKFLOW_FILE_NAME = "workflow_file_name";
    public static final String WORKFLOW_RESOURCE_ON_MYEXPERIMENT = "workflow_resource_on_myexperiment"; // resource URI on myExpriment identifying a workflow
    public static final String WORKFLOW_UPLOAD_SUMBISSION = "workflow_uploading"; // action on the submit button to take the user to the page to upload a file
    public static final String WORKFLOW_UPLOAD_FORM = "workflow_upload_form";
    public static final String WORKFLOW_UPLOAD_FORM_FILE = "workflow_upload_form_file";
    public static final String WORKFLOW_UPLOAD_FORM_SHARING_PUBLIC = "workflow_upload_sharing_public";
    public static final String UPLOAD_WORKFLOW = "upload_workflow"; // action on the submit button to upload a file
    public static final String CLEAR = "clear"; // action to clear the upload wf form, input form or search results or workflow run results
    public static final String MYEXPERIMENT_WORKFLOW_SHOW_INPUT_FORM = "show_input_form"; // action to show the input form for a workflow from myExpriment
    public static final String MYEXPERIMENT_WORKFLOW_SEARCH = "myexperiment_workflow_search;";
    public static final String MYEXPERIMENT_SEARCH_TERMS = "myexperiment_search_terms;";
    public static final String MYEXPERIMENT_SEARCH_ALL = "myexperiment_search_all;";

    public static final String INPUT_PORT_NAME_ATTRIBUTE = "inputPortName";
    public static final String WORKFLOW_INPUT_CONTENT_SUFFIX = "_content";
    public static final String WORKFLOW_INPUT_FILE_SUFFIX = "_file";

    public static final String REFRESH_WORKFLOW_JOBS = "refresh_workflow_jobs";

    public static final String RESULTS_ANCHOR = "results_anchor";

    public static final String MYEXPERIMENT_WORKFLOW_RESOURCE = "myExperiment_workflow_resource";
    public static final String MYEXPERIMENT_WORKFLOW_VERSION = "myExperiment_workflow_version";
}
