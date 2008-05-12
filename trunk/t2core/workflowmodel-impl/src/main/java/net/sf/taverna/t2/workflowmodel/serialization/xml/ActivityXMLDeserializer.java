package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.jdom.Element;

public class ActivityXMLDeserializer extends AbstractXMLDeserializer {
	private static ActivityXMLDeserializer instance = new ActivityXMLDeserializer();

	private ActivityXMLDeserializer() {

	}

	public static ActivityXMLDeserializer getInstance() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	public Activity<?> deserializeActivity(Element element)
			throws ActivityConfigurationException, ClassNotFoundException,
			InstantiationException, IllegalAccessException {
		Element ravenElement = element.getChild(RAVEN, T2_WORKFLOW_NAMESPACE);
		ClassLoader cl = XMLDeserializerImpl.class.getClassLoader();
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
		String className = element.getChild(CLASS, T2_WORKFLOW_NAMESPACE)
				.getTextTrim();
		Class<? extends Activity> c = (Class<? extends Activity>) cl
				.loadClass(className);
		Activity<Object> activity = c.newInstance();

		Element ipElement = element.getChild(INPUT_MAP, T2_WORKFLOW_NAMESPACE);
		for (Element mapElement : (List<Element>) (ipElement.getChildren(MAP,
				T2_WORKFLOW_NAMESPACE))) {
			String processorInputName = mapElement.getAttributeValue(FROM);
			String activityInputName = mapElement.getAttributeValue(TO);
			activity.getInputPortMapping().put(processorInputName,
					activityInputName);
		}

		Element opElement = element.getChild(OUTPUT_MAP, T2_WORKFLOW_NAMESPACE);
		for (Element mapElement : (List<Element>) (opElement.getChildren(MAP,
				T2_WORKFLOW_NAMESPACE))) {
			String activityOutputName = mapElement.getAttributeValue(FROM);
			String processorOutputName = mapElement.getAttributeValue(TO);
			activity.getOutputPortMapping().put(activityOutputName,
					processorOutputName);
		}

		// Handle the configuration of the activity
		Element configElement = element.getChild(CONFIG_BEAN,
				T2_WORKFLOW_NAMESPACE);
		Object configObject = createBean(configElement, cl);
		activity.configure(configObject);
		return activity;
	}
}
