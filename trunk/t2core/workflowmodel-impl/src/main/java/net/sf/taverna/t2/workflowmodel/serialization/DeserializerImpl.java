package net.sf.taverna.t2.workflowmodel.serialization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class DeserializerImpl implements Deserializer, SerializationConstants {
	
	Edits edits = new EditsImpl();

	public Dataflow deserializeDataflow(Element element)
			throws DeserializationException,EditException {
		Element topDataflow = element.getChild(DATAFLOW,T2_WORKFLOW_NAMESPACE);
		return deserializeDataflowFromXML(topDataflow);
	}
	
	protected DispatchLayer<?> deserializeDispatchLayer(Element element) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Element ravenElement = element.getChild(RAVEN,T2_WORKFLOW_NAMESPACE);
		ClassLoader cl = Tools.class.getClassLoader();
		if (ravenElement != null) {
			try {
				cl = getRavenLoader(ravenElement);
			} catch (Exception ex) {
				System.out.println("Exception loading raven classloader "
						+ "for Activity instance");
				ex.printStackTrace();
				// TODO - handle this properly, either by logging correctly or
				// by going back to the repository and attempting to fetch the
				// offending missing artifacts
			}
		}
		String className = element.getChild(CLASS,T2_WORKFLOW_NAMESPACE).getTextTrim();
		Class<? extends DispatchLayer> c = (Class<? extends DispatchLayer>) cl
				.loadClass(className);
		DispatchLayer<Object> layer = c.newInstance();

		// Handle the configuration of the dispatch layer
		Element configElement = element.getChild(CONFIG_BEAN,T2_WORKFLOW_NAMESPACE);
		Object configObject = createBean(configElement, cl);
		layer.configure(configObject);

		return layer;
	}

	protected Activity<?> deserializeActivityFromXML(Element element)
			throws ActivityConfigurationException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Element ravenElement = element.getChild(RAVEN,T2_WORKFLOW_NAMESPACE);
		ClassLoader cl = DeserializerImpl.class.getClassLoader();
		if (ravenElement != null) {
			try {
				cl = getRavenLoader(ravenElement);
			} catch (Exception ex) {
				System.out.println("Exception loading raven classloader "
						+ "for Activity instance");
				ex.printStackTrace();
				// TODO - handle this properly, either by logging correctly or
				// by going back to the repository and attempting to fetch the
				// offending missing artifacts
			}
		}
		String className = element.getChild(CLASS,T2_WORKFLOW_NAMESPACE).getTextTrim();
		Class<? extends Activity> c = (Class<? extends Activity>) cl
				.loadClass(className);
		Activity<Object> activity = c.newInstance();

		Element ipElement = element.getChild(INPUT_MAP,T2_WORKFLOW_NAMESPACE);
		for (Element mapElement : (List<Element>) (ipElement.getChildren(MAP,T2_WORKFLOW_NAMESPACE))) {
			String processorInputName = mapElement.getAttributeValue(FROM);
			String activityInputName = mapElement.getAttributeValue(TO);
			activity.getInputPortMapping().put(processorInputName,
					activityInputName);
		}

		Element opElement = element.getChild(OUTPUT_MAP,T2_WORKFLOW_NAMESPACE);
		for (Element mapElement : (List<Element>) (opElement.getChildren(MAP,T2_WORKFLOW_NAMESPACE))) {
			String activityOutputName = mapElement.getAttributeValue(FROM);
			String processorOutputName = mapElement.getAttributeValue(TO);
			activity.getOutputPortMapping().put(activityOutputName,
					processorOutputName);
		}

		// Handle the configuration of the activity
		Element configElement = element.getChild(CONFIG_BEAN,T2_WORKFLOW_NAMESPACE);
		Object configObject = createBean(configElement, cl);
		activity.configure(configObject);
		return activity;
	}

	protected Object createBean(Element configElement, ClassLoader cl) {
		String encoding = configElement.getAttributeValue(BEAN_ENCODING);
		Object result=null;
		if (encoding.equals("xstream")) {
			//FIXME: throw Exception if children.size!=1
			Element beanElement = (Element)configElement.getChildren().get(0);
			XStream xstream = new XStream(new DomDriver());
			xstream.setClassLoader(cl);
			result = xstream.fromXML(new XMLOutputter()
					.outputString(beanElement));
		}

		return result;

	}

	private ClassLoader getRavenLoader(Element ravenElement)
			throws ArtifactNotFoundException, ArtifactStateException {
		// Try to get the current Repository object, if there isn't one we can't
		// do this here
		Repository repository = null;
		try {
			LocalArtifactClassLoader lacl = (LocalArtifactClassLoader) (Tools.class
					.getClassLoader());
			repository = lacl.getRepository();

		} catch (ClassCastException cce) {
			return Tools.class.getClassLoader();
			// TODO - should probably warn that this is happening as it's likely
			// to be because of an error in API usage. There are times it won't
			// be though so leave it for now.
		}
		String groupId = ravenElement.getChildTextTrim(GROUP,T2_WORKFLOW_NAMESPACE);
		String artifactId = ravenElement.getChildTextTrim(ARTIFACT,T2_WORKFLOW_NAMESPACE);
		String version = ravenElement.getChildTextTrim(VERSION,T2_WORKFLOW_NAMESPACE);
		Artifact artifact = new BasicArtifact(groupId, artifactId, version);
		return repository.getLoader(artifact, null);
	}

	protected Processor deserializeProcessorFromXML(Element el) throws EditException {
		String name=el.getChildText(NAME,T2_WORKFLOW_NAMESPACE);
		Processor result=edits.createProcessor(name);
		
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
		
		
		//Iteration strategy
		Element iterationStrategy = el.getChild(ITERATION_STRATEGY_STACK,T2_WORKFLOW_NAMESPACE);
		((IterationStrategyStackImpl)result.getIterationStrategy()).configureFromElement(iterationStrategy.getChild("iteration",T2_WORKFLOW_NAMESPACE));
		
		
		return result;
		
	}

	public Dataflow deserializeDataflowFromXML(Element element) throws EditException, DeserializationException {
		Dataflow df = edits.createDataflow();
		
		Element inputPorts = element.getChild(DATAFLOW_INPUT_PORTS,T2_WORKFLOW_NAMESPACE);
		Element outputPorts = element.getChild(DATAFLOW_OUTPUT_PORTS,T2_WORKFLOW_NAMESPACE);
		
		//dataflow ports
		addDataflowPorts(df,inputPorts,outputPorts);
		
		Map<String,Processor> createdProcessors = new HashMap<String, Processor>();
		//processors
		Element processorsElement = element.getChild(PROCESSORS,T2_WORKFLOW_NAMESPACE);
		for(Element procElement : (List<Element>)processorsElement.getChildren(PROCESSOR,T2_WORKFLOW_NAMESPACE)) {
			Processor p = deserializeProcessorFromXML(procElement);
			createdProcessors.put(p.getLocalName(),p);
			edits.getAddProcessorEdit(df, p).doEdit();
		}
		
		//conditions
		Element conditions = element.getChild(CONDITIONS,T2_WORKFLOW_NAMESPACE);
		addConditions(df,conditions,createdProcessors);		
		//datalinks
		
		return df;
	}
	
	private void addConditions(Dataflow df, Element conditionsElement,
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

	private void addDataflowPorts(Dataflow df, Element inputPortsElement,Element outputPortsElement) throws EditException {
		for (Element port : (List<Element>)inputPortsElement.getChildren(DATAFLOW_PORT,T2_WORKFLOW_NAMESPACE)) {
			String name=port.getChildText(NAME,T2_WORKFLOW_NAMESPACE);
			int portDepth = Integer.valueOf(port.getChildText(DEPTH,T2_WORKFLOW_NAMESPACE));
			int granularDepth = Integer.valueOf(port.getChildText(GRANULAR_DEPTH,T2_WORKFLOW_NAMESPACE));
			edits.getCreateDataflowInputPortEdit(df, name, portDepth, granularDepth).doEdit();
		}
		
		for (Element port : (List<Element>)outputPortsElement.getChildren(DATAFLOW_PORT,T2_WORKFLOW_NAMESPACE)) {
			String name=port.getChildText(NAME,T2_WORKFLOW_NAMESPACE);
			edits.getCreateDataflowOutputPortEdit(df, name).doEdit();
		}
	}

}
