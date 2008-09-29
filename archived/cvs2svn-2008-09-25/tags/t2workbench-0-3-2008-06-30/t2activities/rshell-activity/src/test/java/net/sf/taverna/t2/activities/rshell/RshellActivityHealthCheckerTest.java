package net.sf.taverna.t2.activities.rshell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.RshellActivityHealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for RshellActivityHealthChecker.
 * 
 */
public class RshellActivityHealthCheckerTest {

	private RshellActivity activity;
	
	private RshellActivityHealthChecker activityHealthChecker;
	
	@Before
	public void setUp() throws Exception {
		activity = new RshellActivity();
		activityHealthChecker = new RshellActivityHealthChecker();
	}

	@Test
	public void testCanHandle() {
		assertFalse(activityHealthChecker.canHandle(null));
		assertFalse(activityHealthChecker.canHandle(new Object()));
		assertFalse(activityHealthChecker.canHandle(new AbstractActivity<Object>() {
			public void configure(Object conf) throws ActivityConfigurationException {
			}
			public Object getConfiguration() {
				return null;
			}
		}));
		assertTrue(activityHealthChecker.canHandle(activity));
	}

	@Test
	public void testCheckHealth() {
		HealthReport healthReport = activityHealthChecker.checkHealth(activity);
		assertNotNull(healthReport);
		assertEquals(Status.WARNING, healthReport.getStatus());
	}

}
