package net.sf.taverna.t2.workflowmodel.impl;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.health.HealthReport;

/**
 * A HealthReport assocatied with Processors.<br>
 * In particular the behaviour for producing an overall status is specialised.
 * @author Stuart Owen
 *
 * @see ProcessorHealthReport#getStatus()
 */
public class ProcessorHealthReport extends HealthReport {

	public ProcessorHealthReport(String subject,List<HealthReport> activityHealthReports) {
		super(subject,"",Status.OK,activityHealthReports);
		
	}

	/**
	 * the overall status is SEVERE if all sub reports are SEVERE, OK if all are OK, otherwise WARNING.
	 * return 
	 */
	@Override
	public Status getStatus() {
		Status result = super.getStatus();
		int severeCount = 0;
		for (HealthReport report : getSubReports()) {
			if (report.getStatus()!=Status.OK) {
				result = Status.WARNING;
			}
			if (report.getStatus()==Status.SEVERE) severeCount++;
		}
		if (severeCount==getSubReports().size()) result=Status.SEVERE;
		return result;
	}
	
	
}
