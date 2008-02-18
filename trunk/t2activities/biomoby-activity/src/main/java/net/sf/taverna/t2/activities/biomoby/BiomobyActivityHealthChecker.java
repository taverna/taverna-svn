package net.sf.taverna.t2.activities.biomoby;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

public class BiomobyActivityHealthChecker implements HealthChecker<BiomobyActivity> {

	public boolean canHandle(Object subject) {
		return (subject != null && subject instanceof BiomobyActivity);
	}

	public HealthReport checkHealth(BiomobyActivity subject) {
		return new HealthReport("Biomoby Activity",
				"The Biomoby Activity is not yet implemented", Status.SEVERE);
	}

}
