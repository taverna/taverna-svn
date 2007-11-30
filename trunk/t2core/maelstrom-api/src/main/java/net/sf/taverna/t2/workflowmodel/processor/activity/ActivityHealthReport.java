package net.sf.taverna.t2.workflowmodel.processor.activity;

import net.sf.taverna.t2.workflowmodel.HealthReport;

/**
 * @author Stuart Owen
 * @author David Withers
 *
 */
public class ActivityHealthReport implements HealthReport {

	private String message;
	private Status status;
	
	public ActivityHealthReport(String message) {
		this(message,Status.OK);
	}

	public ActivityHealthReport(String message, Status status) {
		super();
		this.message = message;
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public Status getStatus() {
		return status;
	}
	
}
