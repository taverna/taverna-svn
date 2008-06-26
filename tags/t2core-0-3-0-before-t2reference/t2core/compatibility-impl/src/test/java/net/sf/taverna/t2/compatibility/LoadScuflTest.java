package net.sf.taverna.t2.compatibility;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.junit.Test;
import static org.junit.Assert.*;

public class LoadScuflTest extends TranslatorTestHelper {

	@Test
	public void testLoadScufl() throws Exception {
		ScuflModel model = loadScufl("very_simple_workflow.xml");
		assertNotNull(model);
	}
}
