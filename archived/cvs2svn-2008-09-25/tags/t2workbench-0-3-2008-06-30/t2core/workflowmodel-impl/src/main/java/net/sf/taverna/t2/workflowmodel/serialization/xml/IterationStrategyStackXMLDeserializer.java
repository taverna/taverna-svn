package net.sf.taverna.t2.workflowmodel.serialization.xml;

import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

import org.jdom.Element;

public class IterationStrategyStackXMLDeserializer implements XMLSerializationConstants{
	private static IterationStrategyStackXMLDeserializer instance = new IterationStrategyStackXMLDeserializer();

	private IterationStrategyStackXMLDeserializer() {

	}

	public static IterationStrategyStackXMLDeserializer getInstance() {
		return instance;
	}
	
	public void deserializeIterationStrategyStack(Element element,IterationStrategyStack stack) {
		((IterationStrategyStackImpl)stack).configureFromElement(element.getChild(ITERATION_STRATEGY,T2_WORKFLOW_NAMESPACE));
	}
}
