package net.sf.taverna.t2.monitor.impl;

import static org.junit.Assert.*;

import java.util.HashSet;

import org.junit.Test;

import net.sf.taverna.t2.monitor.Monitor;
import net.sf.taverna.t2.monitor.MonitorableProperty;

public class MonitorImplTest {

	@Test
	public void testAddNodes() throws InterruptedException {
		Monitor m = MonitorImpl.getMonitor();
		MonitorImpl.enableMonitoring(true);
		m.registerNode(this, new String[] { "foo" },
				new HashSet<MonitorableProperty<?>>());
		m.registerNode(this, new String[] { "foo", "bar" },
				new HashSet<MonitorableProperty<?>>());
		MonitorImpl.enableMonitoring(false);
	}

	@Test
	public void testAddNodesShouldFail() {
		Monitor m = MonitorImpl.getMonitor();
		MonitorImpl.enableMonitoring(true);
		m.registerNode(this, new String[] { "foo" },
				new HashSet<MonitorableProperty<?>>());
		try {
			m.registerNode(this, new String[] { "bar", "wibble" },
					new HashSet<MonitorableProperty<?>>());
			fail("Should have thrown index out of bounds exception");
		} catch (IndexOutOfBoundsException ioobe) {
			// Okay, should see this
		}
		MonitorImpl.enableMonitoring(false);

	}
}
