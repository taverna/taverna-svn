package net.sf.taverna.t2.activities.wsdl.query;

import org.junit.Test;
import static org.junit.Assert.*;

public class WSDLActivityItemTest {

	@Test
	public void testIcon() {
		assertNotNull(new WSDLActivityItem().getIcon());
	}
}
