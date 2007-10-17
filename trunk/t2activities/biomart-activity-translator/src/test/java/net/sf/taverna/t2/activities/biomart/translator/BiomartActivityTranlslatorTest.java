package net.sf.taverna.t2.activities.biomart.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.activities.testutils.TranslatorTestHelper;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.biomart.BiomartProcessor;
import org.junit.Test;

/**
 * @author David Withers
 *
 */
public class BiomartActivityTranlslatorTest extends TranslatorTestHelper { 

	@Test
	public void testScript() throws Exception {
		System.setProperty("raven.eclipse", "true");
		setUpRavenRepository();
		ScuflModel model = loadScufl("biomart-workflow-t1.xml");
		Processor[] processors = model.getProcessors();
		
		assertEquals(1, processors.length);
		assertTrue(processors[0] instanceof BiomartProcessor);
		BiomartProcessor biomartProcessor = (BiomartProcessor) processors[0];
		
		BiomartActivity activity = (BiomartActivity) new BiomartActivityTranslator().doTranslation(biomartProcessor);
		assertEquals(biomartProcessor.getQuery(), activity.getConfiguration().getQuery());
		assertEquals(3, activity.getInputPorts().size());
		assertEquals(2, activity.getOutputPorts().size());

	}
}
