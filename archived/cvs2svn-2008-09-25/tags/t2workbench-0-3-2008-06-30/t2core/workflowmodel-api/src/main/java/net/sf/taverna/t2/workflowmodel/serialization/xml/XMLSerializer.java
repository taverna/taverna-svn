package net.sf.taverna.t2.workflowmodel.serialization.xml;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;

import org.jdom.Element;


/**
 * The main interface that defines the entry point for serialising a Dataflow instance into a JDOM Element
 * <br>
 * If the dataflow internally contains DataflowActivities (i.e. nested dataflows) then the resulting XML will also include
 * a definition of these.
 * 
 * @author Stuart Owen
 *
 */
public interface XMLSerializer {
	
	/**
	 * Serialises the dataflow into a JDOM element
	 * @param dataflow
	 * @return
	 * @throws SerializationException if there is a problem serializing the dataflow
	 */
	Element serializeDataflow(Dataflow dataflow) throws SerializationException;

}
