package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;

import org.jdom.Element;

public class XMLSerializerImpl implements XMLSerializer, XMLSerializationConstants {

	public Element serializeDataflow(Dataflow dataflow)
			throws SerializationException {
		List<Dataflow> innerDataflows = new ArrayList<Dataflow>();
		
		gatherDataflows(dataflow,innerDataflows);
		
		Element result = new Element(WORKFLOW, T2_WORKFLOW_NAMESPACE);
		Element dataflowElement = DataflowXMLSerializer.getInstance().serializeDataflow(dataflow);
		dataflowElement.setAttribute(DATAFLOW_ROLE, DATAFLOW_ROLE_TOP);
		result.addContent(dataflowElement);
		
		for (Dataflow innerDataflow : innerDataflows) {
			Element innerDataflowElement = DataflowXMLSerializer.getInstance().serializeDataflow(innerDataflow);
			innerDataflowElement.setAttribute(DATAFLOW_ROLE,DATAFLOW_ROLE_NESTED);
			innerDataflowElement.setAttribute(DATAFLOW_ID,innerDataflow.getLocalName());
			result.addContent(innerDataflowElement);
		}

		return result;
	}

	private void gatherDataflows(Dataflow dataflow,
			List<Dataflow> innerDataflows) {
		for (Processor p : dataflow.getProcessors()) {
			for (Activity<?> a : p.getActivityList()) {
				if (a.getConfiguration() instanceof Dataflow) {
					Dataflow df = (Dataflow) a.getConfiguration();
					if (!innerDataflows.contains(df)) {
						innerDataflows.add(df);
						gatherDataflows(df, innerDataflows);	
					}
				}
			}
		}
		
	}

	
}
