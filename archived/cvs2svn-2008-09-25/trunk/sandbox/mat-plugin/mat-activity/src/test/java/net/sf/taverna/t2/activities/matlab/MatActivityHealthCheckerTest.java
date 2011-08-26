package net.sf.taverna.t2.activities.matlab;

import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for MatActivityHealthCkecker
 * @author petarj
 */
public class MatActivityHealthCheckerTest
{

    private MatActivity activity;
    private MatActivityHealthChecker activityHealthChecker;

    @Before
    public void setUp() throws Exception
    {
        activity = new MatActivity();
        activityHealthChecker = new MatActivityHealthChecker();
    }

    @Test
    public void testCanHandle()
    {
        assertFalse(activityHealthChecker.canHandle(null));
        assertFalse(activityHealthChecker.canHandle(new Object()));
        assertFalse(activityHealthChecker.canHandle(new AbstractActivity<Object>()
        {

            @Override
            public void configure(Object conf) throws ActivityConfigurationException
            {
            }

            @Override
            public Object getConfiguration()
            {
                return null;
            }
        }));
        assertTrue(activityHealthChecker.canHandle(activity));
    }
    
    @Test
    public void testCheckHealth()
    {
        HealthReport healthReport = activityHealthChecker.checkHealth(activity);
        assertNotNull(healthReport);
        assertEquals(Status.WARNING, healthReport.getStatus());
    }
}
