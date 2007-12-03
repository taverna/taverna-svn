package net.sf.taverna.t2.workflowmodel;

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
}
