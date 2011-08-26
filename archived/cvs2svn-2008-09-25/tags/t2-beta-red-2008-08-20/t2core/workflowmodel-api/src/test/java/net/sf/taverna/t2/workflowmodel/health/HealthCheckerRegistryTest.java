package net.sf.taverna.t2.workflowmodel.health;

import static org.junit.Assert.fail;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthCheckerRegistry;

import org.junit.Test;

public class HealthCheckerRegistryTest {


	@SuppressWarnings("unchecked")
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
