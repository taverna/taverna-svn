package net.sf.taverna.t2.workflowmodel.serialization.xml;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;

import org.jdom.Element;


public interface XMLSerializer {
	
	Element serializeDataflow(Dataflow dataflow) throws SerializationException;

}
