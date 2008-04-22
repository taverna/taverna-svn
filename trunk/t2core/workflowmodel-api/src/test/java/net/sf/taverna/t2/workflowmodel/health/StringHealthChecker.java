package net.sf.taverna.t2.workflowmodel.health;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;

public class StringHealthChecker implements HealthChecker<String> {

	public boolean canHandle(Object subject) {
		return subject!=null && subject instanceof String;
	}

	public HealthReport checkHealth(String subject) {
		return null;
	}

}
