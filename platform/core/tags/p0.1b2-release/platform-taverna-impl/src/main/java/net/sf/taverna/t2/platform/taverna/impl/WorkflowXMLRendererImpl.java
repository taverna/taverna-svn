package net.sf.taverna.t2.platform.taverna.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.taverna.WorkflowXMLRenderer;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.serialization.SerializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLSerializerImpl;

import org.w3c.dom.Element;

/**
 * Implementation of WorkflowXMLRenderer
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 */
public class WorkflowXMLRendererImpl implements WorkflowXMLRenderer {
	
	private static final String UTF_8 = "UTF-8";
	private XMLSerializer serializer;

	public WorkflowXMLRendererImpl() {
		//
	}

	public void setPluginManager(PluginManager manager) {
		this.serializer = new XMLSerializerImpl(manager);
	}

	public Element renderDataflow(Dataflow workflow)
			throws SerializationException {
		return serializer.serializeDataflow(workflow);
	}

	public void renderDataFlowToOutputStream(Dataflow workflow, OutputStream outputStream) throws SerializationException {
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer serializer;
		BufferedOutputStream baos = new BufferedOutputStream(outputStream);
		try {
			serializer = tfactory.newTransformer();
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			// Just to be sure.. (the specs says UTF-16 could also be used)
			serializer.setOutputProperty(OutputKeys.ENCODING, UTF_8);
			serializer.transform(new DOMSource(renderDataflow(workflow)
					.getOwnerDocument()), new StreamResult(baos));
			baos.flush();
		} catch (TransformerException e) {
			throw new SerializationException("Unable to write serialized XML",
					e);
		} catch (IOException e) {
			throw new SerializationException("Unable to write serialized XML",
					e);
		}
	}
	
	public String renderDataflowToString(Dataflow workflow)
			throws SerializationException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		renderDataFlowToOutputStream(workflow, baos);
		try {
			return baos.toString(UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Could not decode workflow using UTF-8");
		}
	}

}
