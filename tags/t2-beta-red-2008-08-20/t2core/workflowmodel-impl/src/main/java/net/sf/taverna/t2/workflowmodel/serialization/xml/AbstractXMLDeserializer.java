package net.sf.taverna.t2.workflowmodel.serialization.xml;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * An abstract base class that contains deserialisation methods common across all dataflow elements.
 * 
 * @author Stuart Owen
 *
 */
public abstract class AbstractXMLDeserializer implements
		XMLSerializationConstants {
	
	protected Edits edits = new EditsImpl();

	protected Object createBean(Element configElement, ClassLoader cl) {
		String encoding = configElement.getAttributeValue(BEAN_ENCODING);
		Object result = null;
		if (encoding.equals(XSTREAM_ENCODING)) {
			if (configElement.getChildren().isEmpty()) {
				throw new IllegalArgumentException("XStream encoding expected in element");
			}
			Element beanElement = (Element) configElement.getChildren().get(0);
			XStream xstream = new XStream(new DomDriver());
			xstream.setClassLoader(cl);
			result = xstream.fromXML(new XMLOutputter()
					.outputString(beanElement));
		} else if (encoding.equals(JDOMXML_ENCODING)) {
			if (configElement.getChildren().isEmpty()) {
				throw new IllegalArgumentException("XML encoding expected in element");
			}
			result = (Element) configElement.getChildren().get(0);
		//} else if (encoding.equals(DATAFLOW_ENCODING)) {
		//	// Oh noe
		//	System.out.println(configElement);
		} else {
			throw new IllegalArgumentException("Unknown encoding " + encoding);
		}

		return result;

	}

	protected ClassLoader getRavenLoader(Element ravenElement)
			throws ArtifactNotFoundException, ArtifactStateException {
		// Try to get the current Repository object, if there isn't one we can't
		// do this here
		Repository repository = null;
		try {
			LocalArtifactClassLoader lacl = (LocalArtifactClassLoader) (Tools.class
					.getClassLoader());
			repository = lacl.getRepository();

		} catch (ClassCastException cce) {
			return Tools.class.getClassLoader();
			// TODO - should probably warn that this is happening as it's likely
			// to be because of an error in API usage. There are times it won't
			// be though so leave it for now.
		}
		String groupId = ravenElement.getChildTextTrim(GROUP,
				T2_WORKFLOW_NAMESPACE);
		String artifactId = ravenElement.getChildTextTrim(ARTIFACT,
				T2_WORKFLOW_NAMESPACE);
		String version = ravenElement.getChildTextTrim(VERSION,
				T2_WORKFLOW_NAMESPACE);
		Artifact artifact = new BasicArtifact(groupId, artifactId, version);
		return repository.getLoader(artifact, null);
	}
	
	protected String elementToString(Element element) {
		return new XMLOutputter().outputString(element);
	}
}
