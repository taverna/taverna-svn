package net.sf.taverna.t2.workflowmodel;

import java.util.ArrayList;
import java.util.List;

public class HealthReportImpl implements HealthReport {

	private String message;
	private Status status;
	private String subject;
	private List<HealthReport> subReports;
	
	public HealthReportImpl(String subject, String message, Status status) {
		this(subject,message,status,new ArrayList<HealthReport>());
	}
	public HealthReportImpl(String subject, String message,Status status,  List<HealthReport> subReports) {
		this.subject=subject;
		this.status=status;
		this.message=message;
		this.subReports=subReports;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public List<HealthReport> getSubReports() {
		return subReports;
	}
	public void setSubReports(List<HealthReport> subReports) {
		this.subReports = subReports;
	}
}
