package net.sf.taverna.t2.activities.wsdl.query;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class WSDLActivityItemTest {

	@Test
	public void testIcon() {
		assertNotNull(new WSDLActivityItem().getIcon());
	}
}
