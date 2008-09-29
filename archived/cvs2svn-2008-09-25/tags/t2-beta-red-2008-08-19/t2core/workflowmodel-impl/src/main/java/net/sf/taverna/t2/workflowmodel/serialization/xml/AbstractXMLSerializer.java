package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.io.IOException;
import java.io.StringReader;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * An abstract base class that contains serialisation methods common across all dataflow elements.
 * 
 * @author Stuart Owen
 *
 */
public abstract class AbstractXMLSerializer implements XMLSerializationConstants {

	/**
	 * Create the &lt;raven&gt; element for a given local artifact classloader.
	 * 
	 * @param classLoader
	 *            The {@link LocalArtifactClassLoader} for the artifact
	 * @return Populated &lt;raven&gt; element
	 */
	protected Element ravenElement(LocalArtifactClassLoader classLoader) {
		Element element = new Element(RAVEN, T2_WORKFLOW_NAMESPACE);
		Artifact artifact = classLoader.getArtifact();
		// Group
		Element groupIdElement = new Element(GROUP, T2_WORKFLOW_NAMESPACE);
		groupIdElement.setText(artifact.getGroupId());
		element.addContent(groupIdElement);
		// Artifact ID
		Element artifactIdElement = new Element(ARTIFACT, T2_WORKFLOW_NAMESPACE);
		artifactIdElement.setText(artifact.getArtifactId());
		element.addContent(artifactIdElement);
		// Version
		Element versionElement = new Element(VERSION, T2_WORKFLOW_NAMESPACE);
		versionElement.setText(artifact.getVersion());
		element.addContent(versionElement);
		// Return assembled raven element
		return element;
	}

	protected Element beanAsElement(Object obj) throws JDOMException,
			IOException {
		Element bean = new Element(CONFIG_BEAN, T2_WORKFLOW_NAMESPACE);
		if (obj instanceof Element) {
			populateBeanElementForElement((Element)obj, bean);
		} 
		else if (obj instanceof Dataflow) {
			populateBeanElementForDataflow((Dataflow)obj, bean);
		} 
		else {
			populateBeanElementFromXStream(obj, bean);
		}
		return bean;
	}

	private void populateBeanElementFromXStream(Object obj, Element bean)
			throws JDOMException, IOException {
		bean.setAttribute(BEAN_ENCODING, XSTREAM_ENCODING);
		XStream xstream = new XStream(new DomDriver());
		SAXBuilder builder = new SAXBuilder();
		Element configElement = builder.build(
				new StringReader(xstream.toXML(obj))).getRootElement();
		configElement.getParent().removeContent(configElement);
		bean.addContent(configElement);
	}

	private void populateBeanElementForDataflow(Dataflow dataflow, Element bean) {
		
		bean.setAttribute(BEAN_ENCODING,DATAFLOW_ENCODING);
		Element dataflowElement = new Element(DATAFLOW,T2_WORKFLOW_NAMESPACE);
		
		dataflowElement.setAttribute(DATAFLOW_REFERENCE,dataflow.getInternalIdentier());
		
		bean.addContent(dataflowElement);
	}

	private void populateBeanElementForElement(Element el, Element bean) {
		bean.setAttribute(BEAN_ENCODING, JDOMXML_ENCODING);
		bean.addContent((Element)el.clone());
	}
	
	protected Element annotationsToXML(Annotated<?> annotated) {
		Element result = new Element(ANNOTATIONS, T2_WORKFLOW_NAMESPACE);
		// TODO: add annotations to serialized XML
		return result;
	}
}
