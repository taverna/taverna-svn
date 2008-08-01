package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.jdom.Element;

/**
 * Implementation class that acts as the main entry point for deserialising a complete XML dataflow document into a dataflow instance.
 * @author Stuart Owen
 *
 */
public class XMLDeserializerImpl implements XMLDeserializer, XMLSerializationConstants {
	
	Edits edits = EditsRegistry.getEdits();

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer#deserializeDataflow(org.jdom.Element)
	 */
	public Dataflow deserializeDataflow(Element element)
			throws DeserializationException,EditException {
		Element topDataflow = findTopDataflow(element);
		if (topDataflow==null) throw new DeserializationException("No top level dataflow defined in the XML document");
		Map<String,Element> innerDataflowElements = gatherInnerDataflows(element);
		try {
			return DataflowXMLDeserializer.getInstance().deserializeDataflow(topDataflow,innerDataflowElements);
		} catch (Exception e) {
			throw new DeserializationException("An error occurred deserializing the dataflow:"+e.getMessage(),e);
		}
	}

	private Element findTopDataflow(Element element) {
		Element result = null;
		for (Object elObj : element.getChildren(DATAFLOW,T2_WORKFLOW_NAMESPACE)) {
			Element dataflowElement = (Element)elObj;
			if (DATAFLOW_ROLE_TOP.equals(dataflowElement.getAttribute(DATAFLOW_ROLE).getValue())) {
				result=dataflowElement;
			}
		}
		return result;
	}

	private Map<String, Element> gatherInnerDataflows(Element element) throws DeserializationException {
		Map<String,Element> result=new HashMap<String, Element>();
		for (Object elObj : element.getChildren(DATAFLOW,T2_WORKFLOW_NAMESPACE)) {
			Element dataflowElement = (Element)elObj;
			if (DATAFLOW_ROLE_NESTED.equals(dataflowElement.getAttribute(DATAFLOW_ROLE).getValue())) {
				String id = dataflowElement.getAttributeValue(DATAFLOW_ID);
				if (result.containsKey(id)) throw new DeserializationException("Duplicate dataflow id:"+id);
				result.put(id,dataflowElement);
			}
		}
		return result;
	}
}
