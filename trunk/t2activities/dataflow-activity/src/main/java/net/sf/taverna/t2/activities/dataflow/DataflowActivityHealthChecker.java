package net.sf.taverna.t2.activities.dataflow;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthCheckerFactory;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;

public class DataflowActivityHealthChecker implements HealthChecker<DataflowActivity> {

	public boolean canHandle(Object subject) {
		return subject!=null && subject instanceof DataflowActivity;
	}

	@SuppressWarnings("unchecked")
	public HealthReport checkHealth(DataflowActivity activity) {
		Dataflow dataflow = activity.getConfiguration().getDataflow();
		Status status = Status.OK;
		String message = "Everything seems fine";
		List<HealthReport> subReports = new ArrayList<HealthReport>();
		for (Processor processor : dataflow.getProcessors()) {
			for (HealthChecker checker : HealthCheckerFactory.getInstance().getHealthCheckersForObject(processor)) {
				HealthReport subReport = checker.checkHealth(processor);
				if (subReport.getStatus().equals(Status.WARNING)) {
					if (status.equals(Status.OK)) {
						status = Status.WARNING;
						message = "Some warnings reported";
					}
				} else if (subReport.getStatus().equals(Status.SEVERE)) {
					status = Status.SEVERE;
					message = "We have a problem";
				}
				subReports.add(subReport);
			}
			
		}
		return new HealthReport("Dataflow Activity", message, status, subReports);
	}

}
