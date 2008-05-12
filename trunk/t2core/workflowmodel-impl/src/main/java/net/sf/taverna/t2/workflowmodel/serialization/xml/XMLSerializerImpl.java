package net.sf.taverna.t2.workflowmodel.serialization.xml;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;

import org.jdom.Element;

public class XMLSerializerImpl implements XMLSerializer, XMLSerializationConstants {

	public Element serializeDataflow(Dataflow dataflow)
			throws SerializationException {
		Element result = new Element(WORKFLOW, T2_WORKFLOW_NAMESPACE);
		Element dataflowElement = DataflowXMLSerializer.getInstance().serializeDataflow(dataflow);
		dataflowElement.setAttribute(DATAFLOW_ROLE, "top");
		result.addContent(dataflowElement);

		return result;
	}

	
}
