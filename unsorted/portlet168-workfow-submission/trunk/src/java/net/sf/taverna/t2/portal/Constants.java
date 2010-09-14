package net.sf.taverna.t2.portal;

import org.jdom.Namespace;

/**
 *
 * @author Alex Nenadic
 */
public class Constants {

    public static final String WORKFLOWS_DIRECTORY = "/WEB-INF/workflows";

    public static final String ERROR_MESSAGE = "error_message";
    public static final String INFO_MESSAGE = "info_message";

    public static final String WORKFLOW_JOB_UUIDS_PORTLET_ATTRIBUTE = "workflow_job_uuids";

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
    public static final String T2_SERVER_URL_PARAMETER = "t2_server_url";
    public static final Namespace T2_SERVER_NAMESPACE = Namespace.getNamespace("http://ns.taverna.org.uk/2010/xml/server/");
    public static final Namespace T2_SERVER_REST_NAMESPACE = Namespace.getNamespace("t2sr", "http://ns.taverna.org.uk/2010/xml/server/rest/");
    public static final String T2_SERVER_WORKFLOW_ELEMENT = "workflow";
    public static final String RUNS_URL = "/rest/runs";
    public static final String WD_URL = "/wd";
    public static final String BACLAVA_INPUTS_URL = "/input/baclava";
    public static final String STATUS_URL = "/status";
    public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
    public static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    public static final String LOCATION_HEADER_NAME = "Location";
    public static final String JOB_STATUS_OPERATING = "Operating";

    // XML upload file element
    public static final String UPLOAD_FILE_ELEMENT = "upload";
    public static final String NAME_ATTRIBUTE = "name";
    public static final String INPUT_ELEMENT = "runInput";
    public static final String FILE_ELEMENT = "file";

}
