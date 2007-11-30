package net.sf.taverna.t2.workflowmodel;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityHealthReport;

public interface ProcessorHealthReport extends HealthReport {

	public List<ActivityHealthReport> getActivityHealthReports();
}
