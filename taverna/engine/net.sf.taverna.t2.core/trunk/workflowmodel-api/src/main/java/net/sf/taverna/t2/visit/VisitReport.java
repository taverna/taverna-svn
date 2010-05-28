/**
 * 
 */
package net.sf.taverna.t2.visit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author alanrw
 * 
 */
public class VisitReport {

	/**
	 * Enumeration of the possible status's in increasing severity: OK,
	 * WARNING,SEVERE
	 */
	public enum Status {
		OK, WARNING, SEVERE
	};

	/**
	 * A short message describing the state of the report
	 */
	private String message;

	/**
	 * An integer indicating the outcome of a visit relative to the VisitKind
	 */
	private int resultId;

	/**
	 * 
	 */
	private Status status;

	/**
	 * The object about which the report is made
	 */
	private Object subject;

	/**
	 * The sub-reports of the VisitReport
	 */
	private Collection<VisitReport> subReports = new ArrayList<VisitReport>();

	/**
	 * The kind of visit that was made e.g. to check the health of a service or
	 * examine its up-stream error fragility
	 */
	private VisitKind kind;

	/**
	 * An indication of whether the visit report was generated by a time
	 * consuming visitor. This is used to check whether the VisitReport can be
	 * automatically junked.
	 */
	private boolean wasTimeConsuming;
	
	private Map<String, Object> propertyMap = new HashMap<String, Object>();

	/**
	 * @return whether the VisitReport was generated by a time consuming visitor
	 */
	public boolean wasTimeConsuming() {
		return wasTimeConsuming;
	}

	/**
	 * @param wasTimeConsuming whether the VisitReport was generated by a time consuming visitot
	 */
	public void setWasTimeConsuming(boolean wasTimeConsuming) {
		this.wasTimeConsuming = wasTimeConsuming;
	}

	/**
	 * Constructs the Visit Report. The sub reports default to an empty list.
	 * 
	 * @param kind
	 *            - the type of visit performed
	 * @param subject
	 *            - the thing being tested.
	 * @param message
	 *            - a summary of the result of the test.
	 * @param resultId
	 *            - an identification of the type of result relative to the
	 *            VisitKind
	 * @param status
	 *            - the overall Status.
	 */
	public VisitReport(VisitKind kind, Object subject, String message,
			int resultId, Status status) {
		this(kind, subject, message, resultId, status,
				new ArrayList<VisitReport>());
	}

	/**
	 * Constructs the Visit Report
	 * 
	 * @param kind
	 *            - the type of visit performed
	 * @param subject
	 *            - the thing being tested.
	 * @param message
	 *            - a summary of the result of the test.
	 * @param resultId
	 *            - an identification of the type of result relative to the
	 *            VisitKind
	 * @param status - the overall Status.
	 * @param subReports
	 *            - a List of sub reports.
	 */
	public VisitReport(VisitKind kind, Object subject, String message,
			int resultId, Status status, Collection<VisitReport> subReports) {
		this.kind = kind;
		this.subject = subject;
		this.status = status;
		this.message = message;
		this.resultId = resultId;
		this.subReports = subReports;
		this.wasTimeConsuming = false;
	}

	/**
	 * @param kind The type of visit performed
	 * @param subject The thing that was visited
	 * @param message A summary of the result of the test
	 * @param resultId An indication of the type of the result relative to the kind of visit
	 * @param subReports A list of sub-reports
	 */
	public VisitReport(VisitKind kind, Object subject, String message,
			int resultId, Collection<VisitReport> subReports) {
		this(kind, subject, message, resultId, getWorstStatus(subReports),
				subReports);
	}

	/**
	 * @return An indication of the type of the result relative to the kind of visit
	 */
	public int getResultId() {
		return resultId;
	}

	/**
	 * @param resultId The type of the result of the visit relative to the kind of visit
	 */
	public void setResultId(int resultId) {
		this.resultId = resultId;
	}

	/**
	 * @return a message summarizing the report
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message
	 * 
	 * @param message
	 *            a message summarizing the report
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Determines the overall Status. This is the most severe status of this
	 * report and all its sub reports.
	 * 
	 * @return the overall status
	 */
	public Status getStatus() {
		Status result = status;
		for (VisitReport report : subReports) {
			if (report.getStatus().compareTo(result) > 0)
				result = report.getStatus();
		}
		return result;
	}

	/**
	 * Sets the status of this report. Be aware that the overall status of this
	 * report may also be affected by its sub reports if they have a more severe
	 * Status.
	 * 
	 * @param status
	 * @see #getStatus
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return an Object representing the subject of this visit report
	 */
	public Object getSubject() {
		return subject;
	}

	/**
	 * @param subject
	 *            an Object representing the subject of this visit report
	 */
	public void setSubject(Object subject) {
		this.subject = subject;
	}

	/**
	 * Provides a list of sub reports. This list defaults an empty list, so it
	 * is safe to add new reports through this method.
	 * 
	 * @return a list of sub reports associated with this Visit Report
	 */
	public Collection<VisitReport> getSubReports() {
		return subReports;
	}

	/**
	 * Replaces the List of sub reports with those provided.
	 * 
	 * @param subReports
	 *            a list of sub reports
	 */
	public void setSubReports(Collection<VisitReport> subReports) {
		this.subReports = subReports;
	}

	/**
	 * 
	 * @return the kind of visit that was made.
	 */
	public VisitKind getKind() {
		return kind;
	}

	/**
	 * @param kind Specify the kind of visit that was made
	 */
	public void setKind(VisitKind kind) {
		this.kind = kind;
	}
	
	public void setProperty(String key, Object value) {
		propertyMap.put(key, value);
	}
	
	public Object getProperty(String key) {
		return propertyMap.get(key);
	}

	/**
	 * Find the most recent ancestor (earliest in the list) of a given class from the list of ancestors
	 * 
	 * @param ancestors The list of ancestors to examine
	 * @param ancestorClass The class to search for
	 * @return The most recent ancestor, or null if no suitable ancestor
	 */
	public static Object findAncestor(List<Object> ancestors,
			Class ancestorClass) {
		Object result = null;
		for (Object o : ancestors) {
			if (ancestorClass.isInstance(o)) {
				return o;
			}
		}
		return result;
	}

	/**
	 * Determine the worst status from a collection of reports
	 * 
	 * @param reports The collection of reports to examine
	 * @return The worst status 
	 */
	public static Status getWorstStatus(Collection<VisitReport> reports) {
		Status currentStatus = Status.OK;
		for (VisitReport report : reports) {
			if (currentStatus.compareTo(report.getStatus()) < 0) {
				currentStatus = report.getStatus();
			}
		}
		return currentStatus;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if ((o == null) || !(o instanceof VisitReport)) {
			return false;
		}
		VisitReport vr = (VisitReport) o;
		return (vr.getClass().equals(this.getClass())
				&& (vr.getKind().equals(this.getKind()))
				&& (vr.getMessage().equals(this.getMessage()))
				&& (vr.getResultId() == this.getResultId())
				&& (vr.getStatus().equals(this.getStatus())) && (vr
				.getSubject().equals(this.getSubject())));
	}
}
