package net.sf.taverna.t2.workflowmodel.serialization;

import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class SerializerImpl implements Serializer,SerializationElementConstants {

	public Element serializeDataflowToXML(Dataflow df) throws SerializationException {
		// TODO Auto-generated method stub
		
		return null;
	}

	protected Element activityToXML(Activity<?> activity) throws JDOMException,IOException {
		Element activityElem = new Element(ACTIVITY);

		ClassLoader cl = activity.getClass().getClassLoader();
		if (cl instanceof LocalArtifactClassLoader) {
			activityElem
					.addContent(ravenElement((LocalArtifactClassLoader) cl));
		}
		Element classNameElement = new Element(CLASS);
		classNameElement.setText(activity.getClass().getName());
		activityElem.addContent(classNameElement);

		// Write out the mappings (processor input -> activity input, activity
		// output -> processor output)
		Element ipElement = new Element(INPUT_MAP);
		for (String processorInputName : activity.getInputPortMapping()
				.keySet()) {
			Element mapElement = new Element(MAP);
			mapElement.setAttribute(FROM, processorInputName);
			mapElement.setAttribute(TO, activity.getInputPortMapping().get(
					processorInputName));
			ipElement.addContent(mapElement);
		}
		activityElem.addContent(ipElement);

		Element opElement = new Element(OUTPUT_MAP);
		for (String activityOutputName : activity.getOutputPortMapping()
				.keySet()) {
			Element mapElement = new Element(MAP);
			mapElement.setAttribute(FROM, activityOutputName);
			mapElement.setAttribute(TO, activity.getOutputPortMapping().get(
					activityOutputName));
			opElement.addContent(mapElement);
		}
		activityElem.addContent(opElement);

		// Get element for configuration
		Object o = activity.getConfiguration();
		Element configElement = beanAsElement(o);
		activityElem.addContent(configElement);

		return activityElem;
	}
	
	protected Element dispatchLayerToXML(DispatchLayer<?> layer) throws IOException,JDOMException {
		Element result = new Element(DISPATCH_LAYER);

		appendObjectDetails(layer, result);

		// Get element for configuration
		Object o = layer.getConfiguration();
		Element configElement = beanAsElement(o);
		result.addContent(configElement);
		return result;
	}
	
	protected Element annotationsToXML(Annotated<?> annotated) {
		Element result = new Element(ANNOTATIONS);
		//TODO: add annotations to serialized XML
		return result;
	}
	
	protected Element dispatchStackToXML(DispatchStack stack) throws IOException,JDOMException {
		//TODO: dispatch stack in serialized XML
		Element result = new Element(DISPATCH_STACK);
		for (DispatchLayer<?> layer : stack.getLayers()) {
			result.addContent(dispatchLayerToXML(layer));
		}
		return result;
	}
	
	protected Element iterationStrategyStackToXML(IterationStrategyStack strategy) {
		Element result = new Element(ITERATION_STRATEGY_STACK);
		result.addContent(((IterationStrategyStackImpl)strategy).asXML());
		return result;
	}
	
	protected Element processorToXML(Processor processor)  throws IOException,JDOMException {
		
		Element result = new Element(PROCESSOR);
		Element nameElement = new Element(NAME);
		nameElement.setText(processor.getLocalName());
		result.addContent(nameElement);
		
		//input and output ports
		Element inputPorts = new Element(PROCESSOR_INPUT_PORTS);
		Element outputPorts = new Element(PROCESSOR_OUTPUT_PORTS);
		result.addContent(inputPorts);
		result.addContent(outputPorts);
		
		//annotations
		result.addContent(annotationsToXML(processor));
		
		//list of activities
		Element activities = new Element(ACTIVITIES);
		for (Activity<?> activity : processor.getActivityList()) {
			activities.addContent(activityToXML(activity));
		}
		result.addContent(activities);
		
		//dispatch stack
		result.addContent(dispatchStackToXML(processor.getDispatchStack()));
		
		//iteration strategy
		result.addContent(iterationStrategyStackToXML(processor.getIterationStrategy()));
		
		return result;
	}
	
	/**
	 * Create the &lt;raven&gt; element for a given local artifact classloader.
	 * 
	 * @param classLoader
	 *            The {@link LocalArtifactClassLoader} for the artifact
	 * @return Populated &lt;raven&gt; element
	 */
	public Element ravenElement(LocalArtifactClassLoader classLoader) {
		Element element = new Element(RAVEN);
		Artifact artifact = classLoader.getArtifact();
		// Group
		Element groupIdElement = new Element(GROUP);
		groupIdElement.setText(artifact.getGroupId());
		element.addContent(groupIdElement);
		// Artifact ID
		Element artifactIdElement = new Element(ARTIFACT);
		artifactIdElement.setText(artifact.getArtifactId());
		element.addContent(artifactIdElement);
		// Version
		Element versionElement = new Element(VERSION);
		versionElement.setText(artifact.getVersion());
		element.addContent(versionElement);
		// Return assembled raven element
		return element;
	}
	
	/**
	 * Get the &lt;java&gt; element from the {@link XMLEncoder} for the given
	 * bean as a JDOM {@link Element}.
	 * 
	 * @see net.sf.taverna.t2.util.beanable.jaxb.BeanSerialiser
	 * @param obj
	 *            Object to serialise
	 * @return &lt;java&gt; element for serialised bean
	 * @throws JDOMException
	 * @throws IOException
	 */
	protected Element beanAsElement(Object obj) throws JDOMException,
			IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		XMLEncoder xenc = new XMLEncoder(bos);
		xenc.writeObject(obj);
		xenc.close();
		byte[] bytes = bos.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		Element configElement = new SAXBuilder().build(bis).getRootElement();
		configElement.getParent().removeContent(configElement);
		return configElement;
	}
	
	protected Element dataLinkToXML(Datalink link) {
		return null;
	}
	
	protected Element conditionsToXML(List<Processor> processors) {
		Element result = new Element(CONDITIONS);
		
		//gather conditions
		Set<Condition> conditions=new HashSet<Condition>();
		for (Processor p : processors) {
			for (Condition c : p.getControlledPreconditionList()) {
				conditions.add(c);
			}
		}
		for (Condition c : conditions) {
			Element conditionElement = new Element(CONDITION);
			conditionElement.setAttribute("control", c.getControl().getLocalName());
			conditionElement.setAttribute("target", c.getTarget().getLocalName());
			result.addContent(conditionElement);
		}
		return result;
	}
	
	private void appendObjectDetails(DispatchLayer<?> layer, Element result) {
		ClassLoader cl = layer.getClass().getClassLoader();
		if (cl instanceof LocalArtifactClassLoader) {
			result.addContent(ravenElement((LocalArtifactClassLoader) cl));
		}
		Element classNameElement = new Element(CLASS);
		classNameElement.setText(layer.getClass().getName());
		result.addContent(classNameElement);
	}
}
