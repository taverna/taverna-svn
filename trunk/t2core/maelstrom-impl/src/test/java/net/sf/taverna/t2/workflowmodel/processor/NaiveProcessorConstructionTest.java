package net.sf.taverna.t2.workflowmodel.processor;

import java.io.IOException;

import junit.framework.TestCase;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

/**
 * Tests the processor factory along with service serialization logic
 * 
 * @author Tom Oinn
 * 
 */
public class NaiveProcessorConstructionTest extends TestCase {

	public void testProcessorFactory() throws EditException, JDOMException,
			IOException, ActivityConfigurationException {
		AsynchEchoActivity service = new AsynchEchoActivity();
		service.configure(new EchoConfig("blah"));
		Tools.buildFromActivity(service);
	}

	public void testRoundTripSerializationFromFactory()
			throws ActivityConfigurationException, EditException, JDOMException,
			IOException, ArtifactNotFoundException, ArtifactStateException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		AsynchEchoActivity service = new AsynchEchoActivity();
		service.configure(new EchoConfig("blah"));
		ProcessorImpl p = Tools.buildFromActivity(service);

		ProcessorImpl p2 = new ProcessorImpl();
		p2.configureFromElement(p.asXML());
		XMLOutputter xo = new XMLOutputter();
		assertTrue(xo.outputString(p.asXML()).equals(
				xo.outputString(p2.asXML())));
	}

}
