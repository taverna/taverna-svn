package net.sf.taverna.t2.workflowmodel.serialization.xml;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.jdom.Element;

public interface XMLDeserializer {
	
	public Dataflow deserializeDataflow(Element element) throws DeserializationException,EditException;
	
}
