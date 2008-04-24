package net.sf.taverna.t2.workflowmodel.serialization;

import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.jdom.Element;

public interface Deserializer {
	public Dataflow deserializeDataflowFromXML(Element element) throws DeserializationException;
}
