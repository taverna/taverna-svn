package net.sf.taverna.t2.cyclone;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.cyclone.translators.BeanshellService;
import net.sf.taverna.t2.cyclone.translators.BeanshellServiceTranslator;
import net.sf.taverna.t2.cyclone.translators.ServiceTranslator;
import net.sf.taverna.t2.cyclone.translators.ServiceTranslatorFactory;
import net.sf.taverna.t2.cyclone.translators.ServiceTranslatorNotFoundException;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import org.junit.Test;

public class ServiceTranslatorTest extends TranslatorTestHelper {

	@Test
	public void testServiceTranslatorFactory() throws Exception {
		Class<? extends Processor> c = BeanshellProcessor.class;
		ServiceTranslator<?> translator = ServiceTranslatorFactory
				.getTranslator(c);
		assertEquals(BeanshellServiceTranslator.class, translator.getClass());
	}

	@Test(expected = ServiceTranslatorNotFoundException.class)
	public void testUnknownServiceTranslator() throws Exception {
		Class<? extends Processor> c = Processor.class;
		ServiceTranslatorFactory.getTranslator(c);
	}

	@Test
	public void testBeanshellServiceTranslator() throws Exception {
		System.setProperty("raven.eclipse", "true");
		setUpRavenRepository();
		ScuflModel model = loadScufl("beanshell.xml");
		Processor p = model.getProcessorsOfType(BeanshellProcessor.class)[0];

		assertEquals("beanshell", p.getName());

		ServiceTranslator<?> translator = ServiceTranslatorFactory
				.getTranslator(p.getClass());

		Activity<?> s = translator.doTranslation(p);

		assertEquals(BeanshellService.class, s.getClass());
	}
}
