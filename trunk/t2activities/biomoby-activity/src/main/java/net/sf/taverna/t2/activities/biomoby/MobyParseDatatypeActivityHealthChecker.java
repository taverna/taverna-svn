package net.sf.taverna.t2.activities.biomoby;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

public class MobyParseDatatypeActivityHealthChecker implements HealthChecker<MobyParseDatatypeActivity> {

	public boolean canHandle(Object subject) {
		return subject!=null && subject instanceof MobyParseDatatypeActivity;
	}

	public HealthReport checkHealth(MobyParseDatatypeActivity subject) {
		return new HealthReport("MobyParseDatatypeActivity","MobyParseDatatypeActivity is not yet implemented",Status.SEVERE);
	}

}
