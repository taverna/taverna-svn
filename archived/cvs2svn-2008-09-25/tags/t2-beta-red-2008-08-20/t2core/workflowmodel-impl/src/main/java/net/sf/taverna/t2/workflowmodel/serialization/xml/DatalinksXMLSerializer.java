package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.Merge;
import net.sf.taverna.t2.workflowmodel.MergeInputPort;
import net.sf.taverna.t2.workflowmodel.MergeOutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.ProcessorPort;
import net.sf.taverna.t2.workflowmodel.impl.BasicEventForwardingOutputPort;
import net.sf.taverna.t2.workflowmodel.impl.MergeInputPortImpl;
import net.sf.taverna.t2.workflowmodel.impl.MergeOutputPortImpl;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;

import org.jdom.Element;

public class DatalinksXMLSerializer extends AbstractXMLSerializer {
	private static DatalinksXMLSerializer instance = new DatalinksXMLSerializer();

	private DatalinksXMLSerializer() {

	}

	public static DatalinksXMLSerializer getInstance() {
		return instance;
	}

	public Element datalinkToXML(Datalink link) throws SerializationException {
		Element element = new Element(DATALINK, T2_WORKFLOW_NAMESPACE);
		Element sink = new Element(SINK, T2_WORKFLOW_NAMESPACE);
		Element source = new Element(SOURCE, T2_WORKFLOW_NAMESPACE);

		DATALINK_TYPES dataLinkSinkType = determineDatalinkType(link.getSink());
		sink.setAttribute(DATALINK_TYPE, dataLinkSinkType.toString());

		if (dataLinkSinkType == DATALINK_TYPES.PROCESSOR) {
			ProcessorPort port = (ProcessorPort) link.getSink();
			Element proc = new Element(PROCESSOR, T2_WORKFLOW_NAMESPACE);
			proc.setText(port.getProcessor().getLocalName());
			sink.addContent(proc);
			Element portElement = new Element(PROCESSOR_PORT,
					T2_WORKFLOW_NAMESPACE);
			portElement.setText(link.getSink().getName());
			sink.addContent(portElement);
		} else if (dataLinkSinkType == DATALINK_TYPES.MERGE) {
			Merge m = ((MergeInputPortImpl) link.getSink()).getMergeInstance();
			ProcessorPort processorPort = (ProcessorPort) ((Datalink) m
					.getOutputPort().getOutgoingLinks().toArray()[0]).getSink();
			Element proc = new Element(PROCESSOR, T2_WORKFLOW_NAMESPACE);
			proc.setText(processorPort.getProcessor().getLocalName());
			sink.addContent(proc);
			Element procPort = new Element(PROCESSOR_PORT,
					T2_WORKFLOW_NAMESPACE);
			procPort.setText(((InputPort) processorPort).getName());
			sink.addContent(procPort);
		} else if (dataLinkSinkType == DATALINK_TYPES.DATAFLOW) {
			Element portElement = new Element(PORT, T2_WORKFLOW_NAMESPACE);
			portElement.setText(link.getSink().getName());
			sink.addContent(portElement);
		}

		DATALINK_TYPES dataLinkSourceType = determineDatalinkType(link
				.getSource());
		source.setAttribute(DATALINK_TYPE, dataLinkSourceType.toString());

		if (dataLinkSourceType == DATALINK_TYPES.PROCESSOR) {
			ProcessorPort port = (ProcessorPort) link.getSource();
			Element proc = new Element(PROCESSOR, T2_WORKFLOW_NAMESPACE);
			proc.setText(port.getProcessor().getLocalName());
			source.addContent(proc);
			Element portElement = new Element(PROCESSOR_PORT,
					T2_WORKFLOW_NAMESPACE);
			portElement.setText(link.getSource().getName());
			source.addContent(portElement);
		} else if (dataLinkSourceType == DATALINK_TYPES.MERGE) {
			Merge m = ((MergeOutputPortImpl) link.getSource()).getMerge();
			ProcessorPort processorPort = (ProcessorPort) ((Datalink) m
					.getOutputPort().getOutgoingLinks().toArray()[0]).getSink();
			Element proc = new Element(PROCESSOR, T2_WORKFLOW_NAMESPACE);
			proc.setText(processorPort.getProcessor().getLocalName());
			source.addContent(proc);
			Element procPort = new Element(PROCESSOR_PORT,
					T2_WORKFLOW_NAMESPACE);
			procPort.setText(((InputPort) processorPort).getName());
			source.addContent(procPort);
		} else if (dataLinkSourceType == DATALINK_TYPES.DATAFLOW) {
			Element portElement = new Element(PORT, T2_WORKFLOW_NAMESPACE);
			portElement.setText(link.getSource().getName());
			source.addContent(portElement);
		}

		element.addContent(sink);
		element.addContent(source);

		return element;
	}

	private DATALINK_TYPES determineDatalinkType(Port port)
			throws SerializationException {
		if (port instanceof MergeInputPort || port instanceof MergeOutputPort) {
			return DATALINK_TYPES.MERGE;
		} else if (port instanceof ProcessorPort) {
			return DATALINK_TYPES.PROCESSOR;
		} else if (port instanceof MergeInputPort
				|| port instanceof MergeOutputPort) {
			return DATALINK_TYPES.MERGE;
		} else if (port instanceof BasicEventForwardingOutputPort
				|| port instanceof EventHandlingInputPort) {
			return DATALINK_TYPES.DATAFLOW;
		} else {
			throw new SerializationException(
					"Unable to determine link type connected to/from " + port);
		}
	}

	public Element datalinksToXML(List<? extends Datalink> links)
			throws SerializationException {
		Element result = new Element(DATALINKS, T2_WORKFLOW_NAMESPACE);
		for (Datalink link : links) {
			if (determineDatalinkType(link.getSource()) != DATALINK_TYPES.MERGE) {
				result.addContent(datalinkToXML(link));
			}
		}
		return result;
	}
}
