package net.sf.taverna.t2.activities.matlab;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;

/**
 * A health checker for MatActivity.
 * @author petarj
 */
public class MatActivityHealthChecker implements HealthChecker<MatActivity>
{

    public boolean canHandle(Object subject)
    {
        return (subject instanceof MatActivity);
    }

    public HealthReport checkHealth(MatActivity subject)
    {
        return new HealthReport("MatActivity", "HealthCheck not implemented", HealthReport.Status.WARNING);
    }
}
