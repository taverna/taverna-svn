package net.sf.taverna.t2.workflowmodel.processor;

import java.io.IOException;

import junit.framework.TestCase;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.processor.service.ServiceConfigurationException;

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
			IOException, ServiceConfigurationException {
		AsynchEchoService service = new AsynchEchoService();
		service.configure(new EchoConfig("blah"));
		Tools.buildFromService(service);
	}

	public void testRoundTripSerializationFromFactory()
			throws ServiceConfigurationException, EditException, JDOMException,
			IOException, ArtifactNotFoundException, ArtifactStateException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		AsynchEchoService service = new AsynchEchoService();
		service.configure(new EchoConfig("blah"));
		ProcessorImpl p = Tools.buildFromService(service);

		ProcessorImpl p2 = new ProcessorImpl();
		p2.configureFromElement(p.asXML());
		XMLOutputter xo = new XMLOutputter();
		assertTrue(xo.outputString(p.asXML()).equals(
				xo.outputString(p2.asXML())));
	}

}
