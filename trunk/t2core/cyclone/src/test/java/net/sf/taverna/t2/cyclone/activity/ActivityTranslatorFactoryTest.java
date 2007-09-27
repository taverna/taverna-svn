package net.sf.taverna.t2.cyclone.activity;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.cyclone.TranslatorTestHelper;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslator;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslatorFactory;
import net.sf.taverna.t2.cyclone.activity.ActivityTranslatorNotFoundException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import org.junit.Test;

public class ActivityTranslatorFactoryTest extends TranslatorTestHelper {

	@Test
	public void testActivityTranslatorFactory() throws Exception {
		Processor processor = new BeanshellProcessor(null,"beanshell","",new String[]{},new String[]{});
		ActivityTranslator<?> translator = ActivityTranslatorFactory
				.getTranslator(processor);
		assertEquals("net.sf.taverna.t2.activities.beanshell.BeanshellActivityTranslator", translator.getClass().getName());
	}

	@Test(expected = ActivityTranslatorNotFoundException.class)
	public void testUnknownActivityTranslator() throws Exception {
		Processor p = new DummyProcessor(); 
		ActivityTranslatorFactory.getTranslator(p);
	}

	@Test
	public void testBeanshellActivityTranslator() throws Exception {
		System.setProperty("raven.eclipse", "true");
		setUpRavenRepository();
		ScuflModel model = loadScufl("beanshell.xml");
		Processor p = model.getProcessorsOfType(BeanshellProcessor.class)[0];

		assertEquals("beanshell", p.getName());

		ActivityTranslator<?> translator = ActivityTranslatorFactory
				.getTranslator(p);

		Activity<?> s = translator.doTranslation(p);

		assertEquals("net.sf.taverna.t2.activities.beanshell.BeanshellActivity", s.getClass().getName());
	}
	
	@SuppressWarnings("serial")
	private class DummyProcessor extends BeanshellProcessor {
		public DummyProcessor() throws ProcessorCreationException, DuplicateProcessorNameException {
			super(null,"beanshell","",new String[]{},new String[]{});
		}
	};
}
