package net.sf.taverna.t2.workflowmodel.serialization;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.jdom.Element;

public interface Deserializer {
	
	public Dataflow deserializeDataflow(Element element) throws DeserializationException,EditException;
	
}
