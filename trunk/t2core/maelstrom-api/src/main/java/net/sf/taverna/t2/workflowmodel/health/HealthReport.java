package net.sf.taverna.t2.workflowmodel.health;

import java.util.ArrayList;
import java.util.List;

public class HealthReport {
	
	public enum Status {OK,WARNING,SEVERE};

	private String message;
	private Status status;
	private String subject;
	private List<HealthReport> subReports = new ArrayList<HealthReport>();
	
	public HealthReport(String subject, String message, Status status) {
		this(subject,message,status,new ArrayList<HealthReport>());
	}
	public HealthReport(String subject, String message,Status status,  List<HealthReport> subReports) {
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
		Status result = status;
		for (HealthReport report : subReports) {
			if (report.getStatus().compareTo(result)>0) result=report.getStatus();
		}
		return result;
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
