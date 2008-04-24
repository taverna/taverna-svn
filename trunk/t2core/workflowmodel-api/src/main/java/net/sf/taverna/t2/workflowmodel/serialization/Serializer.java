package net.sf.taverna.t2.workflowmodel.serialization;

import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.jdom.Element;

public interface Serializer {
	
	Element serializeDataflowToXML(Dataflow df) throws SerializationException;

}
