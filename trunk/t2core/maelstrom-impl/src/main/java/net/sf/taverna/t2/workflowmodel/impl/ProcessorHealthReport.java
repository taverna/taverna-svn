package net.sf.taverna.t2.workflowmodel.impl;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;

public class ProcessorHealthReport extends HealthReport {

	public ProcessorHealthReport(String subject,List<HealthReport> activityHealthReports) {
		super(subject,"",Status.OK,activityHealthReports);
		Status status = Status.OK;
		int severeCount = 0;
		for (HealthReport report : activityHealthReports) {
			if (report.getStatus()!=Status.OK) {
				status = Status.WARNING;
			}
			if (report.getStatus()==Status.SEVERE) severeCount++;
		}
		if (severeCount==activityHealthReports.size()) status=Status.SEVERE;
		setStatus(status);
	}
}
