package net.sf.taverna.t2.activities.soaplab.query;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SoaplabActivityItemTest {

	@Test
	public void getIcon() {
		assertNotNull(new SoaplabActivityItem().getIcon());
	}
}
