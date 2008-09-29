package net.sf.taverna.t2.activities.rshell;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

/**
 * A health checker for the Rshell activity.
 * 
 */
public class RshellActivityHealthChecker implements HealthChecker<RshellActivity> {

	public boolean canHandle(Object subject) {
		return (subject instanceof RshellActivity);
	}

	public HealthReport checkHealth(RshellActivity activity) {
		return new HealthReport("Rshell Activity", "Health check not implemented", Status.WARNING);
	}

}