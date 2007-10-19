package net.sf.taverna.t2.activities.wsdl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;

import org.junit.Test;
public class WSDLActivityTests {
	
	
	
	public WSDLActivity initActivity(String wsdl, String operation) throws Exception {
		WSDLActivity activity=new WSDLActivity();
		WSDLActivityConfigurationBean bean = new WSDLActivityConfigurationBean();
		bean.setWsdl(WSDLTestConstants.WSDL_TEST_BASE+wsdl);
		bean.setOperation(operation);
		activity.configure(bean);
		return activity;
	}
	
	@Test
	public void testBasicInvocation() throws Exception {
		WSDLActivity activity = initActivity("GUIDGenerator.wsdl", "getGUID");
		Map<String,Object> inputMap = new HashMap<String,Object>();
		Map<String,Object> results = ActivityInvoker.invokeAsyncActivity(activity, inputMap, Collections.singletonList("return"));
		assertEquals(1,results.size());
		assertNotNull(results.get("return"));
		assertTrue(results.get("return") instanceof String);
	}
	
	@Test
	public void testWithArrayReturned() throws Exception {
		WSDLActivity activity = initActivity("KEGG.wsdl", "get_pathways_by_genes");
		Map<String,Object> inputMap = new HashMap<String, Object>();
		List<String> inputs = new ArrayList<String>();
		inputs.add("eco:b0077");
		inputs.add("eco:b0078");
		inputMap.put("genes_id_list", inputs);
		
		Map<String,Object> results = ActivityInvoker.invokeAsyncActivity(activity, inputMap, Collections.singletonList("return"));
		assertEquals(1,results.size());
		assertNotNull(results.get("return"));
		assertTrue(results.get("return") instanceof List);
	}
	
}
