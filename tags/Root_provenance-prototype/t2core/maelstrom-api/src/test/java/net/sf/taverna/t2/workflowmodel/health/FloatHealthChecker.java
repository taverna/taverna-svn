package net.sf.taverna.t2.workflowmodel.health;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;

public class FloatHealthChecker implements HealthChecker<Float> {

	public boolean canHandle(Object subject) {
		return subject!=null && subject instanceof Float;
	}

	public HealthReport checkHealth(Float subject) {
		return null;
	}

}
