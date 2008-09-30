package net.sf.taverna.service.rest.client;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestData extends ContextTest {

	@Test
	public void addData() throws NotSuccessException {
		DataREST data = user.getDatas().add(datadoc);
		assertEquals(user, data.getOwner());
	}
	
	@Test
	public void changeOwner() throws NotSuccessException {
		DataREST data = user.getDatas().add(datadoc);
		assertEquals(user, data.getOwner());
		data.setOwner(null);
		assertNull(data.getOwner());
		data.setOwner(user);
		assertEquals(user, data.getOwner());
	}
	
}
