package net.sf.taverna.t2.workflowmodel;

import java.util.List;

/**
 * @author Stuart Owen
 * @author David Withers
 *
 */
public interface HealthReport {
	public enum Status { OK, WARNING, SEVERE }; 
	public Status getStatus();
	public String getMessage();
	public String getSubject();
	public List<HealthReport> getSubReports();
}
