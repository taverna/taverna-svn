package net.sf.taverna.t2.activities.stringconstant.query;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class StringConstantActivityItemTest {
	
	@Test
	public void getIcon() {
		assertNotNull(new StringConstantActivityItem().getIcon());
	}
}
