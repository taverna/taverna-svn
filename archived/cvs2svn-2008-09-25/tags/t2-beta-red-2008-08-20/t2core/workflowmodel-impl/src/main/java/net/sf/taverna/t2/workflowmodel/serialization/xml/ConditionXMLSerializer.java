package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Processor;

import org.jdom.Element;

public class ConditionXMLSerializer extends AbstractXMLSerializer {
	private static ConditionXMLSerializer instance = new ConditionXMLSerializer();

	private ConditionXMLSerializer() {

	}

	public static ConditionXMLSerializer getInstance() {
		return instance;
	}
	
	public Element conditionsToXML(List<? extends Processor> processors) {
		Element result = new Element(CONDITIONS, T2_WORKFLOW_NAMESPACE);

		// gather conditions
		Set<Condition> conditions = new HashSet<Condition>();
		for (Processor p : processors) {
			for (Condition c : p.getControlledPreconditionList()) {
				conditions.add(c);
			}
		}
		for (Condition c : conditions) {
			Element conditionElement = new Element(CONDITION,
					T2_WORKFLOW_NAMESPACE);
			conditionElement.setAttribute("control", c.getControl()
					.getLocalName());
			conditionElement.setAttribute("target", c.getTarget()
					.getLocalName());
			result.addContent(conditionElement);
		}
		return result;
	}
}
