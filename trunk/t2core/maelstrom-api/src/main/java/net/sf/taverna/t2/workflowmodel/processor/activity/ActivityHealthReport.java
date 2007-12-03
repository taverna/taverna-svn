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
	private String subject;
	
	public ActivityHealthReport(String subject,String message) {
		this(subject,message,Status.OK);
	}

	public ActivityHealthReport(String subject,String message, Status status) {
		super();
		this.message = message;
		this.status = status;
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public Status getStatus() {
		return status;
	}

	public String getSubject() {
		return subject;
	}
	
	
	
}
