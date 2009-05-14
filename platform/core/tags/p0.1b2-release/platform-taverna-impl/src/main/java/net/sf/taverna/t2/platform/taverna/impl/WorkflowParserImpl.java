package net.sf.taverna.t2.platform.taverna.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.pom.ArtifactParseException;
import net.sf.taverna.t2.platform.taverna.WorkflowParser;
import net.sf.taverna.t2.platform.util.DomUtils;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerImpl;

/**
 * Implementation of WorkflowParser that uses the XMLDeserializerImpl from the
 * workflowmodel-impl module
 * 
 * @author Tom Oinn
 */
public class WorkflowParserImpl implements WorkflowParser {

	private XMLDeserializer deserializer;

	private static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
			.newInstance();

	public void setPluginManager(PluginManager manager) {
		this.deserializer = new XMLDeserializerImpl(manager);
	}

	public Dataflow createDataflow(URL sourceURL)
			throws DeserializationException, EditException {
		try {
			Document doc = DomUtils.readXML(sourceURL);
			Element root = doc.getDocumentElement();
			return deserializer.deserializeDataflow(root);
		} catch (ArtifactParseException ape) {
			throw new DeserializationException(
					"Unable to parse XML into DOM prior to workflow construction",
					ape);
		}
	}

	public Dataflow createDataflow(InputStream stream)
			throws DeserializationException, EditException {
		try {
			try {
				DocumentBuilder builder = documentBuilderFactory
						.newDocumentBuilder();
				Document doc;
				try {
					doc = builder.parse(stream);
				} finally {
					stream.close();
				}
				Element root = doc.getDocumentElement();
				return deserializer.deserializeDataflow(root);
			} catch (ParserConfigurationException pce) {
				throw new ArtifactParseException("Can't configure parser for workflow",
						pce);
			} catch (SAXException se) {
				throw new ArtifactParseException("Can't parse workflow", se);
			} catch (IOException ioe) {
				throw new ArtifactParseException("Can't read workflow", ioe);
			}
		} catch (ArtifactParseException ape) {
			throw new DeserializationException(
					"Could not parse artifact for workflow",
					ape);
		}
	}

	public Dataflow createDataflow(String classPathResource)
			throws DeserializationException, EditException {
		URL dataflowURL = Thread.currentThread().getContextClassLoader()
				.getResource(classPathResource);
		if (dataflowURL == null) {
			throw new DeserializationException(
					"Unable to locate classpath resource '" + classPathResource
							+ "' to parse workflow definition from.");
		}
		return createDataflow(dataflowURL);
	}

}
