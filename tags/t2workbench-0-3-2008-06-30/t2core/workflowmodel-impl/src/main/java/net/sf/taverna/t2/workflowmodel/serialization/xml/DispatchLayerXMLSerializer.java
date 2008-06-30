package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.io.IOException;

import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;

import org.jdom.Element;
import org.jdom.JDOMException;

public class DispatchLayerXMLSerializer extends AbstractXMLSerializer {

	private static DispatchLayerXMLSerializer instance = new DispatchLayerXMLSerializer();
	
	public static DispatchLayerXMLSerializer getInstance() {
		return instance;
	}
	
	public Element dispatchLayerToXML(DispatchLayer<?> layer)
			throws IOException, JDOMException {
		Element result = new Element(DISPATCH_LAYER, T2_WORKFLOW_NAMESPACE);

		appendObjectDetails(layer, result);

		// Get element for configuration
		Object o = layer.getConfiguration();
		Element configElement = beanAsElement(o);
		result.addContent(configElement);
		return result;
	}
	
	private void appendObjectDetails(DispatchLayer<?> layer, Element result) {
		ClassLoader cl = layer.getClass().getClassLoader();
		if (cl instanceof LocalArtifactClassLoader) {
			result.addContent(ravenElement((LocalArtifactClassLoader) cl));
		}
		Element classNameElement = new Element(CLASS, T2_WORKFLOW_NAMESPACE);
		classNameElement.setText(layer.getClass().getName());
		result.addContent(classNameElement);
	}
}
