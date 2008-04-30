package net.sf.taverna.t2.workflowmodel.serialization;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.workflowmodel.Condition;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.impl.BasicEventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.impl.MergeInputPortImpl;
import net.sf.taverna.t2.workflowmodel.impl.MergeOutputPortImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.IterationStrategyStack;
import net.sf.taverna.t2.workflowmodel.processor.iteration.impl.IterationStrategyStackImpl;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class SerializerImpl implements Serializer, SerializationConstants {

	public Element serializeDataflow(Dataflow dataflow)
			throws SerializationException {
		Element result = new Element(WORKFLOW, T2_WORKFLOW_NAMESPACE);
		Element dataflowElement = serializeDataflowToXML(dataflow);
		dataflowElement.setAttribute(DATAFLOW_ROLE, "top");
		result.addContent(dataflowElement);

		return result;
	}

	protected Element serializeDataflowToXML(Dataflow df)
			throws SerializationException {
		Element result = new Element(DATAFLOW, T2_WORKFLOW_NAMESPACE);
		try {

			// do dataflow inputs and outputs
			result.addContent(dataflowInputPortsToXML(df.getInputPorts()));
			result.addContent(dataflowOutputPortsToXML(df.getOutputPorts()));

			// do processors
			Element processors = new Element(PROCESSORS, T2_WORKFLOW_NAMESPACE);
			for (Processor processor : df.getProcessors()) {
				processors.addContent(processorToXML(processor));
			}
			result.addContent(processors);

			// do conditions
			result.addContent(conditionsToXML(df.getProcessors()));

			// do datalinks
			result.addContent(datalinksToXML(df.getLinks()));

			// do annotations
		}
		// FIXME: improve error reporting
		catch (JDOMException jdomException) {
			throw new SerializationException(
					"There was a problem generating the XML for the dataflow",
					jdomException);
		} catch (IOException ioException) {
			throw new SerializationException(
					"There was a problem generating the XML for the dataflow",
					ioException);
		}
		return result;
	}

	protected Element dataflowInputPortsToXML(
			List<? extends DataflowInputPort> inputPorts) {
		Element result = new Element(DATAFLOW_INPUT_PORTS,
				T2_WORKFLOW_NAMESPACE);
		for (DataflowInputPort port : inputPorts) {
			Element portElement = new Element(DATAFLOW_PORT,
					T2_WORKFLOW_NAMESPACE);
			Element name = new Element(NAME, T2_WORKFLOW_NAMESPACE);
			Element depth = new Element(DEPTH, T2_WORKFLOW_NAMESPACE);
			Element granularDepth = new Element(GRANULAR_DEPTH,
					T2_WORKFLOW_NAMESPACE);

			name.setText(port.getName());
			depth.setText(String.valueOf(port.getDepth()));
			granularDepth.setText(String.valueOf(port.getGranularInputDepth()));

			portElement.addContent(name);
			portElement.addContent(depth);
			portElement.addContent(granularDepth);
			result.addContent(portElement);
		}
		return result;
	}

	protected Element dataflowOutputPortsToXML(
			List<? extends DataflowOutputPort> outputPorts) {
		Element result = new Element(DATAFLOW_OUTPUT_PORTS,
				T2_WORKFLOW_NAMESPACE);
		for (DataflowOutputPort port : outputPorts) {
			Element portElement = new Element(DATAFLOW_PORT,
					T2_WORKFLOW_NAMESPACE);
			Element name = new Element(NAME, T2_WORKFLOW_NAMESPACE);
			name.setText(port.getName());

			portElement.addContent(name);
			result.addContent(portElement);
		}
		return result;

	}

	protected Element datalinksToXML(List<? extends Datalink> links)
			throws SerializationException {
		Element result = new Element(DATALINKS, T2_WORKFLOW_NAMESPACE);
		for (Datalink link : links) {
			if (determineDatalinkType(link.getSource()) != DATALINK_TYPES.MERGE) 
			{
				result.addContent(datalinkToXML(link));
			}
		}
		return result;
	}

	protected Element activityToXML(Activity<?> activity) throws JDOMException,
			IOException {
		Element activityElem = new Element(ACTIVITY, T2_WORKFLOW_NAMESPACE);

		ClassLoader cl = activity.getClass().getClassLoader();
		if (cl instanceof LocalArtifactClassLoader) {
			activityElem
					.addContent(ravenElement((LocalArtifactClassLoader) cl));
		}
		Element classNameElement = new Element(CLASS, T2_WORKFLOW_NAMESPACE);
		classNameElement.setText(activity.getClass().getName());
		activityElem.addContent(classNameElement);

		// Write out the mappings (processor input -> activity input, activity
		// output -> processor output)
		Element ipElement = new Element(INPUT_MAP, T2_WORKFLOW_NAMESPACE);
		for (String processorInputName : activity.getInputPortMapping()
				.keySet()) {
			Element mapElement = new Element(MAP, T2_WORKFLOW_NAMESPACE);
			mapElement.setAttribute(FROM, processorInputName);
			mapElement.setAttribute(TO, activity.getInputPortMapping().get(
					processorInputName));
			ipElement.addContent(mapElement);
		}
		activityElem.addContent(ipElement);

		Element opElement = new Element(OUTPUT_MAP, T2_WORKFLOW_NAMESPACE);
		for (String activityOutputName : activity.getOutputPortMapping()
				.keySet()) {
			Element mapElement = new Element(MAP, T2_WORKFLOW_NAMESPACE);
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

	protected Element dispatchLayerToXML(DispatchLayer<?> layer)
			throws IOException, JDOMException {
		Element result = new Element(DISPATCH_LAYER, T2_WORKFLOW_NAMESPACE);

		appendObjectDetails(layer, result);

		// Get element for configuration
		Object o = layer.getConfiguration();
		Element configElement = beanAsElement(o);
		result.addContent(configElement);
		return result;
	}

	protected Element annotationsToXML(Annotated<?> annotated) {
		Element result = new Element(ANNOTATIONS, T2_WORKFLOW_NAMESPACE);
		// TODO: add annotations to serialized XML
		return result;
	}

	protected Element dispatchStackToXML(DispatchStack stack)
			throws IOException, JDOMException {
		// TODO: dispatch stack in serialized XML
		Element result = new Element(DISPATCH_STACK, T2_WORKFLOW_NAMESPACE);
		for (DispatchLayer<?> layer : stack.getLayers()) {
			result.addContent(dispatchLayerToXML(layer));
		}
		return result;
	}

	protected Element iterationStrategyStackToXML(
			IterationStrategyStack strategy) {
		Element result = new Element(ITERATION_STRATEGY_STACK,
				T2_WORKFLOW_NAMESPACE);
		result.addContent(((IterationStrategyStackImpl) strategy).asXML());
		return result;
	}

	protected Element processorToXML(Processor processor) throws IOException,
			JDOMException {

		Element result = new Element(PROCESSOR, T2_WORKFLOW_NAMESPACE);
		Element nameElement = new Element(NAME, T2_WORKFLOW_NAMESPACE);
		nameElement.setText(processor.getLocalName());
		result.addContent(nameElement);

		// input and output ports
		Element inputPorts = processorInputPortsToXML(processor);
		Element outputPorts = processorOutputPortsToXML(processor);
		result.addContent(inputPorts);
		result.addContent(outputPorts);

		// annotations
		result.addContent(annotationsToXML(processor));

		// list of activities
		Element activities = new Element(ACTIVITIES, T2_WORKFLOW_NAMESPACE);
		for (Activity<?> activity : processor.getActivityList()) {
			activities.addContent(activityToXML(activity));
		}
		result.addContent(activities);

		// dispatch stack
		result.addContent(dispatchStackToXML(processor.getDispatchStack()));

		// iteration strategy
		result.addContent(iterationStrategyStackToXML(processor
				.getIterationStrategy()));

		return result;
	}

	private Element processorOutputPortsToXML(Processor processor) {
		Element outputPorts = new Element(PROCESSOR_OUTPUT_PORTS,
				T2_WORKFLOW_NAMESPACE);
		for (ProcessorOutputPort port : processor.getOutputPorts()) {
			Element portElement = new Element(PROCESSOR_PORT,
					T2_WORKFLOW_NAMESPACE);
			Element name = new Element(NAME, T2_WORKFLOW_NAMESPACE);
			Element depth = new Element(DEPTH, T2_WORKFLOW_NAMESPACE);
			Element granularDepth = new Element(GRANULAR_DEPTH,
					T2_WORKFLOW_NAMESPACE);
			name.setText(port.getName());
			depth.setText(String.valueOf(port.getDepth()));
			granularDepth.setText(String.valueOf(port.getGranularDepth()));
			portElement.addContent(name);
			portElement.addContent(depth);
			portElement.addContent(granularDepth);
			outputPorts.addContent(portElement);
		}
		return outputPorts;
	}

	private Element processorInputPortsToXML(Processor processor) {
		Element inputPorts = new Element(PROCESSOR_INPUT_PORTS,
				T2_WORKFLOW_NAMESPACE);
		for (ProcessorInputPort port : processor.getInputPorts()) {
			Element portElement = new Element(PROCESSOR_PORT,
					T2_WORKFLOW_NAMESPACE);
			Element name = new Element(NAME, T2_WORKFLOW_NAMESPACE);
			Element depth = new Element(DEPTH, T2_WORKFLOW_NAMESPACE);
			name.setText(port.getName());
			depth.setText(String.valueOf(port.getDepth()));
			portElement.addContent(name);
			portElement.addContent(depth);
			inputPorts.addContent(portElement);
		}
		return inputPorts;
	}

	/**
	 * Create the &lt;raven&gt; element for a given local artifact classloader.
	 * 
	 * @param classLoader
	 *            The {@link LocalArtifactClassLoader} for the artifact
	 * @return Populated &lt;raven&gt; element
	 */
	public Element ravenElement(LocalArtifactClassLoader classLoader) {
		Element element = new Element(RAVEN, T2_WORKFLOW_NAMESPACE);
		Artifact artifact = classLoader.getArtifact();
		// Group
		Element groupIdElement = new Element(GROUP, T2_WORKFLOW_NAMESPACE);
		groupIdElement.setText(artifact.getGroupId());
		element.addContent(groupIdElement);
		// Artifact ID
		Element artifactIdElement = new Element(ARTIFACT, T2_WORKFLOW_NAMESPACE);
		artifactIdElement.setText(artifact.getArtifactId());
		element.addContent(artifactIdElement);
		// Version
		Element versionElement = new Element(VERSION, T2_WORKFLOW_NAMESPACE);
		versionElement.setText(artifact.getVersion());
		element.addContent(versionElement);
		// Return assembled raven element
		return element;
	}

	protected Element beanAsElement(Object obj) throws JDOMException,
			IOException {
		// FIXME: turn into seperate handlers when adding the Dataflow case.
		// Don't want loads of if/elses
		Element bean = new Element(CONFIG_BEAN, T2_WORKFLOW_NAMESPACE);
		if (obj instanceof Element) {
			bean.setAttribute(BEAN_ENCODING, JDOMXML_ENCODING);
			bean.addContent((Element) obj);
		} else {
			bean.setAttribute(BEAN_ENCODING, XSTREAM_ENCODING);
			XStream xstream = new XStream(new DomDriver());
			SAXBuilder builder = new SAXBuilder();
			Element configElement = builder.build(
					new StringReader(xstream.toXML(obj))).getRootElement();
			configElement.getParent().removeContent(configElement);
			bean.addContent(configElement);
		}
		return bean;
	}

	protected Element datalinkToXML(Datalink link)
			throws SerializationException {
		Element element = new Element(DATALINK, T2_WORKFLOW_NAMESPACE);
		Element sink = new Element(SINK, T2_WORKFLOW_NAMESPACE);
		Element source = new Element(SOURCE, T2_WORKFLOW_NAMESPACE);
		
		DATALINK_TYPES dataLinkSinkType=determineDatalinkType(link.getSink());
		sink.setAttribute(DATALINK_TYPE,dataLinkSinkType.toString());
		
		if (dataLinkSinkType==DATALINK_TYPES.PROCESSOR) {
			ProcessorPort port = (ProcessorPort) link.getSink();
			Element proc = new Element(PROCESSOR, T2_WORKFLOW_NAMESPACE);
			proc.setText(port.getProcessor().getLocalName());
			sink.addContent(proc);
			Element portElement = new Element(PROCESSOR_PORT, T2_WORKFLOW_NAMESPACE);
			portElement.setText(link.getSink().getName());
			sink.addContent(portElement);
		}
		else if (dataLinkSinkType==DATALINK_TYPES.MERGE) {
			Merge m = ((MergeInputPortImpl)link.getSink()).getMergeInstance();
			ProcessorPort processorPort = (ProcessorPort)((Datalink)m.getOutputPort().getOutgoingLinks().toArray()[0]).getSink();
			Element proc = new Element(PROCESSOR, T2_WORKFLOW_NAMESPACE);
			proc.setText(processorPort.getProcessor().getLocalName());
			sink.addContent(proc);
			Element procPort = new Element(PROCESSOR_PORT,T2_WORKFLOW_NAMESPACE);
			procPort.setText(((InputPort)processorPort).getName());
			sink.addContent(procPort);
		}
		else if (dataLinkSinkType==DATALINK_TYPES.DATAFLOW) {
			Element portElement = new Element(PORT, T2_WORKFLOW_NAMESPACE);
			portElement.setText(link.getSink().getName());
			sink.addContent(portElement);
		}

		DATALINK_TYPES dataLinkSourceType=determineDatalinkType(link.getSource());
		source.setAttribute(DATALINK_TYPE,dataLinkSourceType.toString());
		
		if (dataLinkSourceType==DATALINK_TYPES.PROCESSOR) {
			ProcessorPort port = (ProcessorPort) link.getSource();
			Element proc = new Element(PROCESSOR, T2_WORKFLOW_NAMESPACE);
			proc.setText(port.getProcessor().getLocalName());
			source.addContent(proc);
			Element portElement = new Element(PROCESSOR_PORT, T2_WORKFLOW_NAMESPACE);
			portElement.setText(link.getSource().getName());
			source.addContent(portElement);
		}
		else if (dataLinkSourceType==DATALINK_TYPES.MERGE) {
			Merge m = ((MergeOutputPortImpl)link.getSource()).getMerge();
			ProcessorPort processorPort = (ProcessorPort)((Datalink)m.getOutputPort().getOutgoingLinks().toArray()[0]).getSink();
			Element proc = new Element(PROCESSOR, T2_WORKFLOW_NAMESPACE);
			proc.setText(processorPort.getProcessor().getLocalName());
			source.addContent(proc);
			Element procPort = new Element(PROCESSOR_PORT,T2_WORKFLOW_NAMESPACE);
			procPort.setText(((InputPort)processorPort).getName());
			source.addContent(procPort);
		}
		else if (dataLinkSourceType==DATALINK_TYPES.DATAFLOW) {
			Element portElement = new Element(PORT, T2_WORKFLOW_NAMESPACE);
			portElement.setText(link.getSource().getName());
			source.addContent(portElement);
		}

		element.addContent(sink);
		element.addContent(source);

		return element;
	}

	private DATALINK_TYPES determineDatalinkType(Port port) throws SerializationException {
		if (port instanceof MergeInputPort || port instanceof MergeOutputPort) {
			return DATALINK_TYPES.MERGE;
		}
		else if (port instanceof ProcessorPort) {
			return DATALINK_TYPES.PROCESSOR;
		}
		else if (port instanceof MergeInputPort || port instanceof MergeOutputPort) {
			return DATALINK_TYPES.MERGE;
		}
		else if (port instanceof BasicEventForwardingOutputPort || port instanceof EventHandlingInputPort) {
			return DATALINK_TYPES.DATAFLOW;
		}
		else {
			throw new SerializationException("Unable to determine link type connected to/from "+port);
		}
	}

	protected Element conditionsToXML(List<? extends Processor> processors) {
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

	private void appendObjectDetails(DispatchLayer<?> layer, Element result) {
		ClassLoader cl = layer.getClass().getClassLoader();
		if (cl instanceof LocalArtifactClassLoader) {
			result.addContent(ravenElement((LocalArtifactClassLoader) cl));
		}
		Element classNameElement = new Element(CLASS, T2_WORKFLOW_NAMESPACE);
		classNameElement.setText(layer.getClass().getName());
		result.addContent(classNameElement);
	}
}
