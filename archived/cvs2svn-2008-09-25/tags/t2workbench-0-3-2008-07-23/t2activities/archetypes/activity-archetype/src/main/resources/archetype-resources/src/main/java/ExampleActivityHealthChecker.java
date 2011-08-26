package ${packageName};

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

/**
 * A health checker for the ${artifactId} activity.
 * 
 */
public class ${artifactId}ActivityHealthChecker implements HealthChecker<${artifactId}Activity> {

	public boolean canHandle(Object subject) {
		return (subject instanceof ${artifactId}Activity);
	}

	public HealthReport checkHealth(${artifactId}Activity activity) {
		return new HealthReport("${artifactId} Activity", "Health check not implemented", Status.WARNING);
	}

}