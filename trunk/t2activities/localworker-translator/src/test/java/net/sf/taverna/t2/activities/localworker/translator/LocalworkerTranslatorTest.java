package net.sf.taverna.t2.activities.localworker.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.OutputPort;

import org.apache.commons.io.IOUtils;
import org.embl.ebi.escience.scuflworkers.java.EchoList;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;
import org.junit.Before;
import org.junit.Test;

/**
 * Localworker translation tests
 * 
 * @author David Withers
 */
public class LocalworkerTranslatorTest {

	private LocalworkerTranslator translator;
	
	@Before
	public void setUp() throws Exception {
		translator = new LocalworkerTranslator();
	}

	@Test
	public void testCreateUnconfiguredActivity() {
		BeanshellActivity activity = translator.createUnconfiguredActivity();
		assertNotNull(activity);
		assertNull(activity.getConfiguration());
	}

	@Test
	public void testCreateConfigTypeProcessor() throws Exception {
		BeanshellActivityConfigurationBean bean = translator
		.createConfigType(new LocalServiceProcessor(null, "EchoList", new EchoList()));
		assertNotNull(bean);
		assertEquals(bean.getScript(), IOUtils.toString(LocalworkerTranslator.class.getResourceAsStream("/EchoList")));
	}

	@Test
	public void testCanHandle() throws Exception {
		assertTrue(translator.canHandle(new LocalServiceProcessor(null, "EchoList", new EchoList())));
	}

	@Test
	public void testDoTranslationProcessor() throws Exception {
		LocalServiceProcessor processor = new LocalServiceProcessor(null, "EchoList", new EchoList());
		BeanshellActivity activity = (BeanshellActivity) translator.doTranslation(processor);
		assertEquals(1, activity.getInputPorts().size());
		for (InputPort port : activity.getInputPorts()) {
			assertEquals("inputlist", port.getName());
		}
		assertEquals(1, activity.getOutputPorts().size());
		for (OutputPort port : activity.getOutputPorts()) {
			assertEquals("outputlist", port.getName());
		}
	}

}
