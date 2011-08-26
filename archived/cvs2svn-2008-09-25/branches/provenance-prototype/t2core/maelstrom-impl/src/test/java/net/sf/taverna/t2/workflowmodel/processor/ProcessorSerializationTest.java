package net.sf.taverna.t2.workflowmodel.processor;

import java.io.IOException;

import junit.framework.TestCase;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.impl.CreateProcessorInputPortEdit;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.AddDispatchLayerEdit;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.impl.DispatchStackImpl;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.DummyInvokerLayer;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Parallelize;
import net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Retry;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

public class ProcessorSerializationTest extends TestCase {

	public void testProcessorAsXMLRoundTrip() throws EditException,
			ArtifactNotFoundException, ArtifactStateException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, JDOMException, IOException,
			ActivityConfigurationException {

		ProcessorImpl p = (ProcessorImpl)new EditsImpl().createProcessor("a_processor");
		DispatchStackImpl stack = p.getDispatchStack();
		new AddDispatchLayerEdit(stack, new Parallelize(), 0).doEdit();
		new AddDispatchLayerEdit(stack, new Retry(2, 50, 2000, 1), 1).doEdit();
		new AddDispatchLayerEdit(stack, new DummyInvokerLayer(), 2).doEdit();
		new CreateProcessorInputPortEdit(p, "Input1", 1).doEdit();
		new CreateProcessorInputPortEdit(p, "Input2", 0).doEdit();

		Element e = p.asXML();

		ProcessorImpl p2 = (ProcessorImpl)new EditsImpl().createProcessor("a_processor");
		p2.configureFromElement(e);
		Element e2 = p2.asXML();

		XMLOutputter xo = new XMLOutputter();
		assertTrue(xo.outputString(e).equals(xo.outputString(e2)));
	}

}
