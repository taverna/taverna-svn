package net.sf.taverna.t2.workflowmodel.health;

import static org.junit.Assert.assertEquals;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthCheckerFactory;

import org.junit.Before;
import org.junit.Test;

public class HealthCheckerFactoryTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetHealthCheckerForObject() {
		String str = "A String";
		List<HealthChecker<?>> checkers = HealthCheckerFactory.getInstance().getHealthCheckersForObject(str);
		assertEquals("There should be 1 checker for String",1,checkers.size());
		
		Long l = new Long(123);
		checkers = HealthCheckerFactory.getInstance().getHealthCheckersForObject(l);
		assertEquals("There should be 0 checkers for Long",0,checkers.size());
		
		Float f = new Float(2.5f);
		checkers = HealthCheckerFactory.getInstance().getHealthCheckersForObject(f);
		assertEquals("There should be 2 checkers for Float",2,checkers.size());
	}

}
