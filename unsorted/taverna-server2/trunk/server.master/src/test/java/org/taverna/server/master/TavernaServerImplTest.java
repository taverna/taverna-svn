package org.taverna.server.master;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

public class TavernaServerImplTest extends AbstractJUnit4SpringContextTests {
	private TavernaServerImpl tavernaServerImpl;

	public void setTavernaServerImpl(TavernaServerImpl o) {
		this.tavernaServerImpl = o;
	}

	protected String[] getConfigLocations() {
		return new String[] { "test-tavserv.xml" };
	}

	@Test
	public void testExample() {
		assertNotNull(tavernaServerImpl);
		assertEquals(tavernaServerImpl.getMaxSimultaneousRuns(), 1);
	}
}
