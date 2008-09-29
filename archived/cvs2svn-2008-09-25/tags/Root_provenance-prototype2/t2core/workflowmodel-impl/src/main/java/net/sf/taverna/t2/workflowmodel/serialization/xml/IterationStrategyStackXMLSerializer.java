package net.sf.taverna.t2.workflowmodel.serialization.xml;

import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

import org.jdom.Element;

public class IterationStrategyStackXMLSerializer extends AbstractXMLSerializer {
	
	private static IterationStrategyStackXMLSerializer instance = new IterationStrategyStackXMLSerializer();

	private IterationStrategyStackXMLSerializer() {
		
	}
	
	public Element iterationStrategyStackToXML(
			IterationStrategyStack strategyStack) {
		Element result = new Element(ITERATION_STRATEGY_STACK,
				T2_WORKFLOW_NAMESPACE);
		result.addContent(((IterationStrategyStackImpl) strategyStack).asXML());
		return result;
	}

	public static IterationStrategyStackXMLSerializer getInstance() {
		return instance;
	}
}
