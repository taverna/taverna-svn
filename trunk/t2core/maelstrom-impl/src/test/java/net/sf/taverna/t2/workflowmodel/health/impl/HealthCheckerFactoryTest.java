package net.sf.taverna.t2.workflowmodel.health.impl;

import static org.junit.Assert.*;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

import org.junit.Before;
import org.junit.Test;

public class HealthCheckerFactoryTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetHealthCheckerForObject() {
		String str = "A String";
		HealthChecker<?> checker = HealthCheckerFactory.getInstance().getHealthCheckerForObject(str);
		assertNotNull("The factory should find a StringHealthChecker",checker);
		
		Long l = new Long(123);
		checker = HealthCheckerFactory.getInstance().getHealthCheckerForObject(l);
		assertNull("The factory should not find a checker for a Long",checker);
	}

}
