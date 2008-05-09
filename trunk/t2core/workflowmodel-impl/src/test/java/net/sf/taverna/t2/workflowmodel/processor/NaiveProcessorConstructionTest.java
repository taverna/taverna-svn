package net.sf.taverna.t2.workflowmodel.processor;

import java.io.IOException;

import junit.framework.TestCase;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.impl.Tools;
import net.sf.taverna.t2.workflowmodel.impl.ProcessorImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

/**
 * Tests the processor factory along with activity serialisation logic
 * 
 * @author Tom Oinn
 * 
 */
public class NaiveProcessorConstructionTest extends TestCase {

	public void testProcessorFactory() throws EditException, JDOMException,
			IOException, ActivityConfigurationException {
		AsynchEchoActivity activity = new AsynchEchoActivity();
		activity.configure(new EchoConfig("blah"));
		Tools.buildFromActivity(activity);
	}
}
