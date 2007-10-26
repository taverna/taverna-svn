package net.sf.taverna.t2.activities.wsdl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.activities.wsdl.translator.WSDLActivityTranslator;

import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import org.junit.Test;

public class WSDLActivityTranslatorTest {

	@Test
	public void testTranslateAndInvoke() throws Exception {
		System.setProperty("raven.eclipse", "true");
		WSDLBasedProcessor proc = new WSDLBasedProcessor(null,"test",WSDLTestConstants.WSDL_TEST_BASE+"GUIDGenerator.wsdl","getGUID");
		WSDLActivity activity = (WSDLActivity) new WSDLActivityTranslator().doTranslation(proc);
		Map<String,Object> inputMap = new HashMap<String,Object>();
		Map<String,Object> results = ActivityInvoker.invokeAsyncActivity(activity, inputMap, Collections.singletonList("return"));
		assertEquals(1,results.size());
		assertNotNull(results.get("return"));
		assertTrue(results.get("return") instanceof String);
	}
}
