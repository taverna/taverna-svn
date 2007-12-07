package net.sf.taverna.t2.workflowmodel.health.impl;

import static org.junit.Assert.fail;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

import org.junit.Test;

public class HealthCheckerRegistryTest {


	@Test
	public void testGetInstances() {
		HealthCheckerRegistry registry = new HealthCheckerRegistry();
		List<HealthChecker> checkers = registry.getInstances();
		for (HealthChecker<?> checker : checkers) {
			if (checker.canHandle("a string")) return;
		}
		fail("A checker should have been found that can handle String");
	}

}
