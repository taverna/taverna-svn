package net.sf.taverna.t2.activities.dataflow.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.activities.dataflow.DataflowActivityConfigurationBean;
import net.sf.taverna.t2.activities.testutils.DummyProcessor;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor;
import org.junit.Before;
import org.junit.Test;

/**
 * Dataflow Activity Translator Tests
 * 
 * @author David Withers
 * 
 */
public class DataflowActivityTranslatorTest {

	private DataflowActivityTranslator translator;

	private WorkflowProcessor processor;

	@Before
	public void setUp() throws Exception {
		translator = new DataflowActivityTranslator();
		ScuflModel scuflModel = new ScuflModel();
		processor = new WorkflowProcessor(scuflModel, "test");
	}

	@Test
	public void testCreateUnconfiguredActivity() {
		DataflowActivity activity = translator.createUnconfiguredActivity();
		assertNotNull(activity);
		assertNull(activity.getConfiguration());
	}

	@Test
	public void testCreateConfigTypeProcessor() throws Exception {
		DataflowActivityConfigurationBean bean = translator
				.createConfigType(processor);
		assertNotNull(bean.getDataflow());
	}

	@Test
	public void testCanHandle() throws Exception {
		assertTrue(translator.canHandle(processor));
		assertFalse(translator.canHandle(new DummyProcessor()));
		assertFalse(translator.canHandle(null));
	}

	@Test
	public void testDoTranslationProcessor() throws Exception {
		DataflowActivity activity = (DataflowActivity) translator
				.doTranslation(processor);
		assertEquals(0, activity.getInputPorts().size());
		assertEquals(0, activity.getOutputPorts().size());
	}

}
