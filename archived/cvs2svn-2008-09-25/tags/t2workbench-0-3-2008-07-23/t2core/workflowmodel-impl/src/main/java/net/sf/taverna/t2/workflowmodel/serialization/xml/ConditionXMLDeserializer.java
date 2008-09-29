package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.jdom.Element;

public class ConditionXMLDeserializer extends AbstractXMLDeserializer {
	private static ConditionXMLDeserializer instance = new ConditionXMLDeserializer();

	private ConditionXMLDeserializer() {

	}

	public static ConditionXMLDeserializer getInstance() {
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public void buildConditions(Dataflow df, Element conditionsElement,
			Map<String, Processor> createdProcessors) throws DeserializationException, EditException {
		for (Element conditionElement : (List<Element>)conditionsElement.getChildren(CONDITION,T2_WORKFLOW_NAMESPACE)) {
			String control=conditionElement.getAttributeValue("control");
			String target=conditionElement.getAttributeValue("target");
			Processor controlProcessor=createdProcessors.get(control);
			Processor targetProcessor=createdProcessors.get(target);
			if (controlProcessor==null) throw new DeserializationException("Unable to find start processor for control link, named:"+control);
			if (targetProcessor==null) throw new DeserializationException("Unable to find target processor for control link, named:"+target);
			edits.getCreateConditionEdit(controlProcessor, targetProcessor).doEdit();
		}		
	}
}
