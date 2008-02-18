package net.sf.taverna.t2.activities.biomoby;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

public class BiomobyObjectActivityHealthChecker implements HealthChecker<BiomobyObjectActivity> {

	public boolean canHandle(Object subject) {
		return (subject!=null && subject instanceof BiomobyObjectActivity);
	}

	public HealthReport checkHealth(BiomobyObjectActivity subject) {
		return new HealthReport("BiomobyObjectActivity","The BiomobyObjectActivity is not yet implemented",Status.SEVERE);
	}

}
