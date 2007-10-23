package net.sf.taverna.t2.activities.biomart.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.activities.biomart.BiomartActivityConfigurationBean;
import net.sf.taverna.t2.activities.testutils.DummyProcessor;
import net.sf.taverna.t2.activities.testutils.TranslatorTestHelper;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslationException;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.biomart.BiomartProcessor;
import org.junit.Before;
import org.junit.Test;

public class BiomartActivityTranslatorTest extends TranslatorTestHelper {

	private BiomartActivityTranslator translator;

	private BiomartProcessor biomartProcessor;
	
	private Set<String> inputPortNames;

	private Set<String> outputPortNames;

	@Before
	public void setUp() throws Exception {
		System.setProperty("raven.eclipse", "true");
		setUpRavenRepository();
		ScuflModel model = loadScufl("biomart-workflow-t1.xml");
		Processor[] processors = model.getProcessors();

		assertEquals(1, processors.length);
		assertTrue(processors[0] instanceof BiomartProcessor);
		biomartProcessor = (BiomartProcessor) processors[0];
		
		inputPortNames = new HashSet<String>();
		for (org.embl.ebi.escience.scufl.InputPort port : biomartProcessor.getInputPorts()) {
			inputPortNames.add(port.getName());
		}
		outputPortNames = new HashSet<String>();
		for (org.embl.ebi.escience.scufl.OutputPort port : biomartProcessor.getOutputPorts()) {
			outputPortNames.add(port.getName());
		}

		translator = new BiomartActivityTranslator();
	}

	@Test
	public void testCreateUnconfiguredActivity() {
		BiomartActivity activity = translator.createUnconfiguredActivity();
		assertNotNull(activity);
		assertNull(activity.getConfiguration());
	}

	@Test
	public void testCreateConfigTypeProcessor()
			throws ActivityTranslationException {
		BiomartActivityConfigurationBean bean = translator
				.createConfigType(biomartProcessor);
		assertNotNull(bean);
		assertEquals(biomartProcessor.getQuery(), bean.getQuery());
	}

	@Test
	public void testCanHandle() throws Exception {
		assertTrue(translator.canHandle(biomartProcessor));
		assertFalse(translator.canHandle(new DummyProcessor()));
		assertFalse(translator.canHandle(null));
	}

	@Test
	public void testDoTranslationProcessor() throws Exception {
		BiomartActivity activity = (BiomartActivity) translator
				.doTranslation(biomartProcessor);
		assertEquals(biomartProcessor.getQuery(), activity.getConfiguration()
				.getQuery());
		assertEquals(inputPortNames.size(), activity.getInputPorts().size());
		for (InputPort port : activity.getInputPorts()) {
			assertTrue(inputPortNames.remove(port.getName()));
		}
		assertEquals(outputPortNames.size(), activity.getOutputPorts().size());
		for (OutputPort port : activity.getOutputPorts()) {
			assertTrue(outputPortNames.remove(port.getName()));
		}
	}

}
