package net.sf.taverna.t2.workflowmodel.serialization.xml;

import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;

import org.jdom.Element;

public class DispatchLayerXMLDeserializer extends AbstractXMLDeserializer {
	private static DispatchLayerXMLDeserializer instance = new DispatchLayerXMLDeserializer();

	private DispatchLayerXMLDeserializer() {

	}

	public static DispatchLayerXMLDeserializer getInstance() {
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public DispatchLayer<?> deserializeDispatchLayer(Element element) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		Element ravenElement = element.getChild(RAVEN,T2_WORKFLOW_NAMESPACE);
		ClassLoader cl = Tools.class.getClassLoader();
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
		Class<? extends DispatchLayer> c = (Class<? extends DispatchLayer>) cl
				.loadClass(className);
		DispatchLayer<Object> layer = c.newInstance();

		// Handle the configuration of the dispatch layer
		Element configElement = element.getChild(CONFIG_BEAN,T2_WORKFLOW_NAMESPACE);
		Object configObject = createBean(configElement, cl);
		layer.configure(configObject);

		return layer;
	}
}
