package net.sf.taverna.t2.activities.beanshell.translator;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;

import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import org.junit.Test;

public class BeanshellActivityTranlslatorTest {

	@Test
	public void testScript() throws Exception {
		BeanshellProcessor processor = new BeanshellProcessor(null,
				"simplebeanshell", "", new String[] { "input1", "input2" },
				new String[] { "output" });
		processor.setScript("this is a script");
		BeanshellActivity activity = (BeanshellActivity)new BeanshellActivityTranslator().doTranslation(processor);
		assertEquals("the getScript result was not what was expected","this is a script",activity.getConfiguration().getScript());
	}
}
