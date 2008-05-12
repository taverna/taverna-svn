package net.sf.taverna.t2.workflowmodel.serialization.xml;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DispatchLayer;

import org.jdom.Element;

public class DispatchStackXMLDeserializer extends AbstractXMLDeserializer {
	private static DispatchStackXMLDeserializer instance = new DispatchStackXMLDeserializer();

	private DispatchStackXMLDeserializer() {

	}

	public static DispatchStackXMLDeserializer getInstance() {
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public void deserializeDispatchStack(Processor processor,
			Element dispatchStack) throws ClassNotFoundException, InstantiationException, IllegalAccessException, EditException {
		int layers=0;
		for (Element layer : (List<Element>)dispatchStack.getChildren(DISPATCH_LAYER,T2_WORKFLOW_NAMESPACE)) {
			DispatchLayer<?> dispatchLayer = DispatchLayerXMLDeserializer.getInstance().deserializeDispatchLayer(layer);
			edits.getAddDispatchLayerEdit(processor.getDispatchStack(), dispatchLayer, layers++).doEdit();
		}
		
	}
}
