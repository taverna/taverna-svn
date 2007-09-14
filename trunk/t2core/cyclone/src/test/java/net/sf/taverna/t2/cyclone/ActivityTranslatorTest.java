package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.cyclone.translators.BeanshellActivity;
import net.sf.taverna.t2.cyclone.translators.BeanshellActivityTranslator;
import net.sf.taverna.t2.cyclone.translators.ActivityTranslator;
import net.sf.taverna.t2.cyclone.translators.ActivityTranslatorFactory;
import net.sf.taverna.t2.cyclone.translators.ActivityTranslatorNotFoundException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import org.junit.Test;

public class ActivityTranslatorTest extends TranslatorTestHelper {

	@Test
	public void testActivityTranslatorFactory() throws Exception {
		Class<? extends Processor> c = BeanshellProcessor.class;
		ActivityTranslator<?> translator = ActivityTranslatorFactory
				.getTranslator(c);
		assertEquals(BeanshellActivityTranslator.class, translator.getClass());
	}

	@Test(expected = ActivityTranslatorNotFoundException.class)
	public void testUnknownActivityTranslator() throws Exception {
		Class<? extends Processor> c = Processor.class;
		ActivityTranslatorFactory.getTranslator(c);
	}

	@Test
	public void testBeanshellActivityTranslator() throws Exception {
		System.setProperty("raven.eclipse", "true");
		setUpRavenRepository();
		ScuflModel model = loadScufl("beanshell.xml");
		Processor p = model.getProcessorsOfType(BeanshellProcessor.class)[0];

		assertEquals("beanshell", p.getName());

		ActivityTranslator<?> translator = ActivityTranslatorFactory
				.getTranslator(p.getClass());

		Activity<?> s = translator.doTranslation(p);

		assertEquals(BeanshellActivity.class, s.getClass());
	}
}
