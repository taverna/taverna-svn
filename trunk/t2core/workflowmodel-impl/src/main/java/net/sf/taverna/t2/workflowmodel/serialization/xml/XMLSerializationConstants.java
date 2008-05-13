package net.sf.taverna.t2.workflowmodel.serialization.xml;

import org.jdom.Namespace;

public interface XMLSerializationConstants {

	static final String WORKFLOW_DOCUMENT_MIMETYPE="application/vnd.taverna.t2flow+xml";
	
	static final Namespace T2_WORKFLOW_NAMESPACE=Namespace.getNamespace("http://taverna.sf.net/2008/xml/t2flow");
	
	// XML element names
	static final String WORKFLOW = "workflow";
	
	static final String DATAFLOW = "dataflow";
	static final String DATAFLOW_INPUT_PORTS="inputPorts";
	static final String DATAFLOW_OUTPUT_PORTS="outputPorts";
	static final String DATAFLOW_PORT="port";
	
	static final String PROCESSOR = "processor";
	static final String PROCESSORS = "processors";
	static final String PROCESSOR_INPUT_PORTS = "inputPorts";
	static final String PROCESSOR_OUTPUT_PORTS = "outputPorts";
	
	static final String DISPATCH_LAYER = "dispatchLayer";
	
	static final String ACTIVITIES = "activities";
	static final String ACTIVITY = "activity";
	static final String CONFIG_BEAN="configBean";
	
	static final String NAME="name";
	
	static final String JAVA = "java";
	static final String OUTPUT_MAP = "outputMap";
	static final String TO = "to";
	static final String FROM = "from";
	static final String MAP = "map";
	static final String INPUT_MAP = "inputMap";
	static final String CLASS = "class";
	static final String VERSION = "version";
	static final String ARTIFACT = "artifact";
	static final String GROUP = "group";
	static final String RAVEN = "raven";
	static final String DISPATCH_STACK = "dispatchStack";
	static final String ITERATION_STRATEGY_STACK = "iterationStrategyStack";
	static final String CONDITIONS = "conditions";
	static final String CONDITION = "condition";
	static final String ANNOTATIONS = "annotations";
	static final String ANNOTATION = "annotation";
	
	static final String DATALINK = "datalink";
	static final String DATALINKS = "datalinks";
	static final String DATALINK_TYPE="type";
	
	public enum DATALINK_TYPES {
		PROCESSOR("processor"),
		DATAFLOW("dataflow"),
		MERGE("merge");
		
		String value;
		DATALINK_TYPES(String value) {
			this.value=value;
		}
		
		public String toString() {
			return value;
		}
	};
	
	static final String SINK = "sink";
	static final String SOURCE = "source";
	static final String PORT = "port";
	static final String PROCESSOR_PORT = "port";
	static final String DEPTH="depth";
	static final String GRANULAR_DEPTH="granularDepth";
	
	static final String MERGE="merge";
	static final String MERGES="merges";
	
	//Attribute names
	static final String BEAN_ENCODING="encoding";
	
	static final String JDOMXML_ENCODING="jdomxml";
	static final String XSTREAM_ENCODING="xstream";
	static final String DATAFLOW_ENCODING="dataflow";
	
	static final String DATAFLOW_ROLE="role";
	static final String DATAFLOW_REFERENCE="ref";
	static final String DATAFLOW_ID="id";
	
	//Attribute values
	static final String DATAFLOW_ROLE_TOP="top";
	static final String DATAFLOW_ROLE_NESTED="nested";
	
}
