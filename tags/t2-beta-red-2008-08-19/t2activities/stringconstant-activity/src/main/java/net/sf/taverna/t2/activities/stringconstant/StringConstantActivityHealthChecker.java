package net.sf.taverna.t2.activities.stringconstant;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

public class StringConstantActivityHealthChecker implements HealthChecker<StringConstantActivity> {

	public boolean canHandle(Object subject) {
		return subject!=null && subject instanceof StringConstantActivity;
	}

	public HealthReport checkHealth(StringConstantActivity activity) {
		String value = activity.getConfiguration().getValue();
		if (value==null) {
			return new HealthReport("StringConstant Activity","The value is null",Status.SEVERE);
		}
		if ("edit me!".equals(value)) {
			return new HealthReport("StringConstant Activity","The value is still the default",Status.WARNING);
		}
		return new HealthReport("StringConstant Activity","OK",Status.OK);
	}

}
