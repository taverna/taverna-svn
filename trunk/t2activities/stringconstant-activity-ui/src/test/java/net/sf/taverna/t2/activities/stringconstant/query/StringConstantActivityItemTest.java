package net.sf.taverna.t2.activities.stringconstant.query;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringConstantActivityItemTest {
	
	@Test
	public void getIcon() {
		assertNotNull(new StringConstantActivityItem().getIcon());
	}
}
