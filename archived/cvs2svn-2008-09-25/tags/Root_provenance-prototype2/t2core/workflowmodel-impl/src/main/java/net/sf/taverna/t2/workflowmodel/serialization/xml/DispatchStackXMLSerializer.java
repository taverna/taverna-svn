package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.io.IOException;

import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchStack;

import org.jdom.Element;
import org.jdom.JDOMException;

public class DispatchStackXMLSerializer extends AbstractXMLSerializer {

	private static DispatchStackXMLSerializer instance = new DispatchStackXMLSerializer();

	public static DispatchStackXMLSerializer getInstance() {
		return instance;
	}

	public Element dispatchStackToXML(DispatchStack stack) throws IOException,
			JDOMException {
		Element result = new Element(DISPATCH_STACK, T2_WORKFLOW_NAMESPACE);
		for (DispatchLayer<?> layer : stack.getLayers()) {
			result.addContent(dispatchLayerToXML(layer));
		}
		return result;
	}

	protected Element dispatchLayerToXML(DispatchLayer<?> layer)
			throws IOException, JDOMException {
		return DispatchLayerXMLSerializer.getInstance().dispatchLayerToXML(
				layer);
	}

}
