package net.sf.taverna.t2.monitor.impl;

import static org.junit.Assert.fail;

import java.util.HashSet;

import net.sf.taverna.t2.monitor.MonitorableProperty;

import org.junit.Test;

public class MonitorTreeModelTest {

	@Test
	public void testAddNodes() throws InterruptedException {
		MonitorTreeModel m = MonitorTreeModel.getInstance();
		m.registerNode(this, new String[] { "foo" },
				new HashSet<MonitorableProperty<?>>());
		m.registerNode(this, new String[] { "foo", "bar" },
				new HashSet<MonitorableProperty<?>>());
	}

	@Test
	public void testAddNodesShouldFail() {
		MonitorTreeModel m = MonitorTreeModel.getInstance();
		m.registerNode(this, new String[] { "foo" },
				new HashSet<MonitorableProperty<?>>());
		try {
			m.registerNode(this, new String[] { "bar", "wibble" },
					new HashSet<MonitorableProperty<?>>());
			fail("Should have thrown index out of bounds exception");
		} catch (IndexOutOfBoundsException ioobe) {
			// Okay, should see this
		}

	}
}
