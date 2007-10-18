package net.sf.taverna.t2.activities.wsdl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.BeforeClass;
import org.junit.Test;
public class WSDLActivityTests {
	private static WSDLActivity activity=null;
	public static final String WSDL_TEST_BASE="http://www.mygrid.org.uk/taverna-tests/testwsdls/";
	
	@BeforeClass
	public static void initActivity() throws Exception {
		activity=new WSDLActivity();
		WSDLActivityConfigurationBean bean = new WSDLActivityConfigurationBean();
		bean.setWsdl(WSDL_TEST_BASE+"GUIDGenerator.wsdl");
		bean.setOperation("getGUID");
		activity.configure(bean);
	}
	
	@Test
	public void testBasicInvocation() throws Exception {
		Map<String,Object> inputMap = new HashMap<String,Object>();
		Map<String,Object> results = ActivityInvoker.invokeAsyncActivity(activity, inputMap, Collections.singletonList("return"));
		assertEquals(1,results.size());
		assertNotNull(results.get("return"));
		assertTrue(results.get("return") instanceof String);
	}
	
	
}
