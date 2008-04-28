package net.sf.taverna.t2.workflowmodel.serialization;

import java.util.List;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.BasicArtifact;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class DeserializerImpl implements Deserializer, SerializationConstants {

	public Dataflow deserializeDataflow(Element element)
			throws DeserializationException {
		// TODO Auto-generated method stub
		return null;
	}

	protected Activity<?> deserializeActivityFromXML(Element element)
			throws ActivityConfigurationException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Element ravenElement = element.getChild(RAVEN,T2_WORKFLOW_NAMESPACE);
		ClassLoader cl = DeserializerImpl.class.getClassLoader();
		if (ravenElement != null) {
			try {
				cl = getRavenLoader(ravenElement);
			} catch (Exception ex) {
				System.out.println("Exception loading raven classloader "
						+ "for Activity instance");
				ex.printStackTrace();
				// TODO - handle this properly, either by logging correctly or
				// by going back to the repository and attempting to fetch the
				// offending missing artifacts
			}
		}
		String className = element.getChild(CLASS,T2_WORKFLOW_NAMESPACE).getTextTrim();
		Class<? extends Activity> c = (Class<? extends Activity>) cl
				.loadClass(className);
		Activity<Object> activity = c.newInstance();

		Element ipElement = element.getChild(INPUT_MAP,T2_WORKFLOW_NAMESPACE);
		for (Element mapElement : (List<Element>) (ipElement.getChildren(MAP,T2_WORKFLOW_NAMESPACE))) {
			String processorInputName = mapElement.getAttributeValue(FROM);
			String activityInputName = mapElement.getAttributeValue(TO);
			activity.getInputPortMapping().put(processorInputName,
					activityInputName);
		}

		Element opElement = element.getChild(OUTPUT_MAP,T2_WORKFLOW_NAMESPACE);
		for (Element mapElement : (List<Element>) (opElement.getChildren(MAP,T2_WORKFLOW_NAMESPACE))) {
			String activityOutputName = mapElement.getAttributeValue(FROM);
			String processorOutputName = mapElement.getAttributeValue(TO);
			activity.getOutputPortMapping().put(activityOutputName,
					processorOutputName);
		}

		// Handle the configuration of the activity
		Element configElement = element.getChild(CONFIG_BEAN,T2_WORKFLOW_NAMESPACE);
		Object configObject = createBean(configElement, cl);
		activity.configure(configObject);
		return activity;
	}

	protected Object createBean(Element configElement, ClassLoader cl) {
		String encoding = configElement.getAttributeValue(BEAN_ENCODING);
		Object result=null;
		if (encoding.equals("xstream")) {
			//FIXME: throw Exception if children.size!=1
			Element beanElement = (Element)configElement.getChildren().get(0);
			XStream xstream = new XStream(new DomDriver());
			xstream.setClassLoader(cl);
			result = xstream.fromXML(new XMLOutputter()
					.outputString(beanElement));
		}

		return result;

	}

	private ClassLoader getRavenLoader(Element ravenElement)
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
		String groupId = ravenElement.getChildTextTrim(GROUP);
		String artifactId = ravenElement.getChildTextTrim(ARTIFACT);
		String version = ravenElement.getChildTextTrim(VERSION);
		Artifact artifact = new BasicArtifact(groupId, artifactId, version);
		return repository.getLoader(artifact, null);
	}

	protected Processor deserializeProcessorFromXML(Element el) {
		return null;
	}

}
