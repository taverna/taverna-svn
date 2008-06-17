package net.sf.taverna.t2.activities.soaplab.query;

import org.junit.Test;
import static org.junit.Assert.*;

public class SoaplabActivityItemTest {

	@Test
	public void getIcon() {
		assertNotNull(new SoaplabActivityItem().getIcon());
	}
}
