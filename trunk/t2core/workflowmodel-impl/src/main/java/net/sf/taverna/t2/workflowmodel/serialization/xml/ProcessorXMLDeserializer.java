package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.jdom.Element;

public class ProcessorXMLDeserializer extends AbstractXMLDeserializer {
	private static ProcessorXMLDeserializer instance = new ProcessorXMLDeserializer();

	private ProcessorXMLDeserializer() {

	}

	public static ProcessorXMLDeserializer getInstance() {
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public Processor deserializeProcessor(Element el,Map<String,Element>innerDataflowElements) throws EditException, ActivityConfigurationException, ClassNotFoundException, InstantiationException, IllegalAccessException, DeserializationException {
		String name=el.getChildText(NAME,T2_WORKFLOW_NAMESPACE);
		Processor result=edits.createProcessor(name);
		
		//activities
		Element activities=el.getChild(ACTIVITIES,T2_WORKFLOW_NAMESPACE);
		for (Element activity : (List<Element>)activities.getChildren(ACTIVITY,T2_WORKFLOW_NAMESPACE)) {
			Activity<?> a = ActivityXMLDeserializer.getInstance().deserializeActivity(activity,innerDataflowElements);
			edits.getAddActivityEdit(result, a).doEdit();
		}
		
		//ports
		Element inputPorts = el.getChild(PROCESSOR_INPUT_PORTS,T2_WORKFLOW_NAMESPACE);
		Element outputPorts = el.getChild(PROCESSOR_OUTPUT_PORTS,T2_WORKFLOW_NAMESPACE);
		
		for (Element inputPort : (List<Element>)inputPorts.getChildren(PROCESSOR_PORT,T2_WORKFLOW_NAMESPACE)) {
			String portName=inputPort.getChildText(NAME,T2_WORKFLOW_NAMESPACE);
			int portDepth = Integer.valueOf(inputPort.getChildText(DEPTH,T2_WORKFLOW_NAMESPACE));
			edits.getCreateProcessorInputPortEdit(result, portName, portDepth).doEdit();
		}
		
		for (Element outputPort : (List<Element>)outputPorts.getChildren(PROCESSOR_PORT,T2_WORKFLOW_NAMESPACE)) {
			String portName=outputPort.getChildText(NAME,T2_WORKFLOW_NAMESPACE);
			int portDepth = Integer.valueOf(outputPort.getChildText(DEPTH,T2_WORKFLOW_NAMESPACE));
			int granularDepth = Integer.valueOf(outputPort.getChildText(GRANULAR_DEPTH,T2_WORKFLOW_NAMESPACE));
			edits.getCreateProcessorOutputPortEdit(result, portName, portDepth, granularDepth).doEdit();
		}
		
		//TODO: annotations
		
		//Dispatch stack
		Element dispatchStack = el.getChild(DISPATCH_STACK,T2_WORKFLOW_NAMESPACE);
		DispatchStackXMLDeserializer.getInstance().deserializeDispatchStack(result, dispatchStack);
		
		
		//Iteration strategy
		Element iterationStrategyStack = el.getChild(ITERATION_STRATEGY_STACK,T2_WORKFLOW_NAMESPACE);
		IterationStrategyStackXMLDeserializer.getInstance().deserializeIterationStrategyStack(iterationStrategyStack, result.getIterationStrategy());
		
		return result;
		
	}
}
