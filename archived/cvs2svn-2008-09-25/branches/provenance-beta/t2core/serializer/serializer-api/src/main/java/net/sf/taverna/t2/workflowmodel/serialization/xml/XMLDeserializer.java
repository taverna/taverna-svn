package net.sf.taverna.t2.workflowmodel.serialization.xml;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;

import org.jdom.Element;

/**
 * The API that defines the entry point for deserialising a complete dataflow XML document into a dataflow instance.
 * 
 * @author Stuart Owen
 *
 */
public interface XMLDeserializer {
	
	/**
	 * Deserialises a complete dataflow document into a Dataflow instance.
	 * 
	 * @param element a jdom element holding the XML document that represents the dataflow
	 * @return an instance of the Dataflow
	 * @throws DeserializationException
	 * @throws EditException - should an error occur whilst constructing the dataflow via Edits
	 * 
	 * @see Edits
	 */
	public Dataflow deserializeDataflow(Element element) throws DeserializationException,EditException;
	
}
