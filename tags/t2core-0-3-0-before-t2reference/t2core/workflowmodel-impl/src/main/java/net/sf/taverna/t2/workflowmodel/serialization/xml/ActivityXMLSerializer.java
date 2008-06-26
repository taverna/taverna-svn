package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.io.IOException;

import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.jdom.Element;
import org.jdom.JDOMException;

public class ActivityXMLSerializer extends AbstractXMLSerializer {
	private static ActivityXMLSerializer instance = new ActivityXMLSerializer();

	public static ActivityXMLSerializer getInstance() {
		return instance;
	}

	public Element activityToXML(Activity<?> activity) throws JDOMException,
			IOException {
		Element activityElem = new Element(ACTIVITY, T2_WORKFLOW_NAMESPACE);

		ClassLoader cl = activity.getClass().getClassLoader();
		if (cl instanceof LocalArtifactClassLoader) {
			activityElem
					.addContent(ravenElement((LocalArtifactClassLoader) cl));
		}
		Element classNameElement = new Element(CLASS, T2_WORKFLOW_NAMESPACE);
		classNameElement.setText(activity.getClass().getName());
		activityElem.addContent(classNameElement);

		// Write out the mappings (processor input -> activity input, activity
		// output -> processor output)
		Element ipElement = new Element(INPUT_MAP, T2_WORKFLOW_NAMESPACE);
		for (String processorInputName : activity.getInputPortMapping()
				.keySet()) {
			Element mapElement = new Element(MAP, T2_WORKFLOW_NAMESPACE);
			mapElement.setAttribute(FROM, processorInputName);
			mapElement.setAttribute(TO, activity.getInputPortMapping().get(
					processorInputName));
			ipElement.addContent(mapElement);
		}
		activityElem.addContent(ipElement);

		Element opElement = new Element(OUTPUT_MAP, T2_WORKFLOW_NAMESPACE);
		for (String activityOutputName : activity.getOutputPortMapping()
				.keySet()) {
			Element mapElement = new Element(MAP, T2_WORKFLOW_NAMESPACE);
			mapElement.setAttribute(FROM, activityOutputName);
			mapElement.setAttribute(TO, activity.getOutputPortMapping().get(
					activityOutputName));
			opElement.addContent(mapElement);
		}
		activityElem.addContent(opElement);

		// Get element for configuration
		Object o = activity.getConfiguration();
		Element configElement = beanAsElement(o);
		activityElem.addContent(configElement);

		return activityElem;
	}

}
