package net.sf.taverna.t2.activities.wsdl.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

public class WSDLQueryTest {

	private static String wsdlUrl;
	@BeforeClass
	public static void setup() {
		wsdlUrl=WSDLQueryTest.class.getResource("/kegg.wsdl").toExternalForm();
	}
	
	
	@Test
	public void testDoQuery() {
		WSDLQuery q = new WSDLQuery(wsdlUrl);
		q.doQuery();
		assertEquals("The query should be 69 items",69,q.size());
		WSDLActivityItem i = (WSDLActivityItem)q.toArray()[0];
		assertEquals("The type shoudl be WSDL","WSDL",i.getType());
		assertEquals("The use should be encoded","encoded",i.getUse());
		assertEquals("The style should be RPC","rpc",i.getStyle());
		assertNotNull("The operation should be set",i.getOperation());
		assertTrue("The operation should be have some content",i.getOperation().length()>2);
	}
	
}
