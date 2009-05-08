package net.sf.taverna.t2.platform.taverna;

import java.io.OutputStream;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;

import org.w3c.dom.Element;

/**
 * Methods to render a Dataflow instance to XML, producing an XML document that
 * can be stored to disk, sent to myExperiment etc. This is the counterpart to
 * the WorkflowParser
 * 
 * @author Tom Oinn
 */
public interface WorkflowXMLRenderer {

	/**
	 * Construct a representation of the supplied workflow as an XML element
	 * object.
	 * 
	 * @param workflow
	 *            the workflow to render as XML
	 * @return a w3c DOM Element containing the serialized workflow
	 * 
	 * @throws SerializationException
	 *             If the workflow can't be serialized, for instance if the
	 *             workflow is invalid.
	 */
	Element renderDataflow(Dataflow workflow) throws SerializationException;

	/**
	 * Construct a representation of the supplied workflow, rendering the
	 * document generated as a string of indented XML text. This is a
	 * convenience wrapper around the method that returns a w3c DOM.
	 * 
	 * @param workflow
	 *            the workflow to render as XML
	 * @return a string containing the XML as indented text
	 * 
	 * @throws SerializationException
	 *             If the workflow can't be serialized, for instance if there is
	 *             a problem with the XML libraries
	 */
	String renderDataflowToString(Dataflow workflow)
			throws SerializationException;

	/**
	 * Construct a representation of the supplied workflow, rendering the
	 * document as indented XML text to the supplied OutputStream (encoded as
	 * UTF-8). This is a convenience wrapper around the method that returns a
	 * w3c DOM.
	 * 
	 * @param workflow
	 *            the workflow to render as XML
	 * @param outputStream
	 *            output stream to write XML to
	 * 
	 * @throws SerializationException
	 *             If the workflow can't be serialized, for instance if the
	 *             stream can't be written to
	 */
	void renderDataFlowToOutputStream(Dataflow workflow,
			OutputStream outputStream) throws SerializationException;

}
