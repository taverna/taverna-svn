package net.sf.taverna.t2.workflowmodel.impl;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.HealthReport;
import net.sf.taverna.t2.workflowmodel.ProcessorHealthReport;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityHealthReport;

public class ProcessorHealthReportImpl implements ProcessorHealthReport {

	private List<ActivityHealthReport> activityHealthReports;
	private Status status;
	private String message;
	
	public ProcessorHealthReportImpl(List<ActivityHealthReport> activityHealthReports) {
		this.activityHealthReports=activityHealthReports;
		status = Status.OK;
		int severeCount = 0;
		for (HealthReport report : activityHealthReports) {
			if (report.getStatus()!=Status.OK) {
				status = Status.WARNING;
			}
			if (report.getStatus()==Status.SEVERE) severeCount++;
		}
		if (severeCount==activityHealthReports.size()) status=Status.SEVERE;
	}
	
	public List<ActivityHealthReport> getActivityHealthReports() {
		return activityHealthReports;
	}

	public String getMessage() {
		return message;
	}

	public Status getStatus() {
		return status;
	}

}
