package net.sf.taverna.t2.workflowmodel.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthCheckerFactory;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * A Health Checker associated with Processors. This iterates over the processor activities
 * invoking each HealthChecker available for each Activity to generate an overal ProcessorHealthReport
 * @author Stuart Owen
 *
 */
public class ProcessorHealthChecker implements HealthChecker<Processor> {

	public boolean canHandle(Object subject) {
		return subject!=null && subject instanceof Processor;
	}

	@SuppressWarnings("unchecked")
	public HealthReport checkHealth(Processor subject) {
		List<HealthReport> activityReports = new ArrayList<HealthReport>();
		for (Activity<?> a : subject.getActivityList()) {
			List<HealthChecker<?>> checkers = HealthCheckerFactory
					.getInstance().getHealthCheckersForObject(a);
			if (checkers.size() > 0) {
				List<HealthReport> reports = new ArrayList<HealthReport>();
				for (HealthChecker checker : checkers) {
					reports.add(checker.checkHealth(a));
				}
				if (reports.size() == 1) {
					activityReports.add(reports.get(0));
				} else {
					activityReports.add(new HealthReport("Activity tests...", "",
							Status.OK, reports));
				}
			}
		}
		HealthReport processorHealthReport = new ProcessorHealthReport(
				subject.getLocalName() + " Processor", activityReports);
		return processorHealthReport;
	}

}
