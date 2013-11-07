/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package uk.org.taverna.platform.report;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import uk.org.taverna.scufl2.api.common.Ported;
import uk.org.taverna.scufl2.api.common.URITools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Report about the {@link State} of a workflow component.
 *
 * @author David Withers
 * @param <SUBJECT>
 *            the WorkflowBean that the report is about
 * @param <PARENT>
 *            the parent report type
 */
/**
 * @author David Withers
 * @param <SUBJECT>
 * @param <PARENT>
 */
public class StatusReport<SUBJECT extends Ported, PARENT extends StatusReport<?, ?>> {

	private final SUBJECT subject;

	private PARENT parentReport;

	private State state;

	private SortedSet<Invocation> invocations = new TreeSet<>();

	private Date createdDate, startedDate, pausedDate, resumedDate, cancelledDate, completedDate,
			failedDate;

	private final List<Date> pausedDates = new ArrayList<Date>(),
			resumedDates = new ArrayList<Date>();

	private List<ReportListener> reportListeners = new ArrayList<>();

	/**
	 * Constructs a new <code>StatusReport</code> for the subject and sets the created date to the
	 * current date.
	 *
	 * @param subject
	 *            the subject of the report
	 */
	public StatusReport(SUBJECT subject) {
		this.subject = subject;
		setCreatedDate(new Date());
	}

	/**
	 * Returns the subject of this report.
	 *
	 * @return the subject of this report
	 */
	@JsonIgnore
	public SUBJECT getSubject() {
		return subject;
	}

	@JsonProperty("subject")
	public URI getSubjectURI() {
	    return new URITools().uriForBean(subject);
	}

	/**
	 * Returns the parent report.
	 * <p>
	 * Returns null if this report has no parent.
	 *
	 * @return the parent report
	 */
	@JsonIgnore
	public PARENT getParentReport() {
		return parentReport;
	}

	/**
	 * Sets the parent report.
	 * <p>
	 * Can be null if this report has no parent.
	 *
	 * @param workflowReport
	 *            the parent report
	 */
	public void setParentReport(PARENT parentReport) {
		this.parentReport = parentReport;
	}

	/**
	 * Returns the current {@link State}.
	 * <p>
	 * A state can be CREATED, RUNNING, COMPLETED, PAUSED, CANCELLED or FAILED.
	 *
	 * @return the current <code>State</code>
	 */
	public State getState() {
		return state;
	}

	private void setState(State state) {
		this.state = state;
	}

	/**
	 * Returns the date that the status was set to CREATED.
	 *
	 * @return the the date that the status was set to CREATED
	 */
	public Date getCreatedDate() {
		return createdDate;
	}

	/**
	 * Sets the date that the status was set to CREATED.
	 *
	 * @param createdDate
	 *            the date that the status was set to CREATED
	 */
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
		setState(State.CREATED);
	}

	/**
	 * Returns the date that the status changed to RUNNING.
	 * <p>
	 * If the status has never been RUNNING <code>null</code> is returned.
	 *
	 * @return the date that the status changed to started
	 */
	public Date getStartedDate() {
		return startedDate;
	}

	/**
	 * Sets the date that the status changed to RUNNING.
	 *
	 * @param startedDate
	 *            the date that the status changed to RUNNING
	 */
	public void setStartedDate(Date startedDate) {
		if (this.startedDate == null) {
			this.startedDate = startedDate;
		}
		setState(State.RUNNING);
	}

	/**
	 * Returns the date that the status last changed to PAUSED.
	 * <p>
	 * If the status has never been PAUSED <code>null</code> is returned.
	 *
	 * @return the date that the status last changed to PAUSED
	 */
	public Date getPausedDate() {
		return pausedDate;
	}

	/**
	 * Sets the date that the status last changed to PAUSED.
	 *
	 * @param pausedDate
	 *            the date that the status last changed to PAUSED
	 */
	public void setPausedDate(Date pausedDate) {
		this.pausedDate = pausedDate;
		pausedDates.add(pausedDate);
		setState(State.PAUSED);
	}

	/**
	 * Returns the date that the status last changed form PAUSED to RUNNING.
	 * <p>
	 * If the status has never changed form PAUSED to RUNNING <code>null</code> is returned.
	 *
	 * @return the date that the status last changed form PAUSED to RUNNING
	 */
	public Date getResumedDate() {
		return resumedDate;
	}

	/**
	 * Sets the date that the status last changed form PAUSED to RUNNING.
	 *
	 * @param resumedDate
	 *            the date that the status last changed form PAUSED to RUNNING
	 */
	public void setResumedDate(Date resumedDate) {
		this.resumedDate = resumedDate;
		resumedDates.add(resumedDate);
		setState(State.RUNNING);
	}

	/**
	 * Returns the date that the status changed to CANCELLED.
	 * <p>
	 * If the status has never been CANCELLED <code>null</code> is returned.
	 *
	 * @return the date that the status changed to canceled
	 */
	public Date getCancelledDate() {
		return cancelledDate;
	}

	/**
	 * Sets the date that the status changed to CANCELLED.
	 *
	 * @param cancelledDate
	 *            the date that the status changed to CANCELLED
	 */
	public void setCancelledDate(Date cancelledDate) {
		this.cancelledDate = cancelledDate;
		setState(State.CANCELLED);
	}

	/**
	 * Returns the date that the status changed to COMPLETED.
	 * <p>
	 * If the status never been COMPLETED <code>null</code> is returned.
	 *
	 * @return the date that the status changed to COMPLETED
	 */
	public Date getCompletedDate() {
		return completedDate;
	}

	/**
	 * Sets the date that the status changed to COMPLETED.
	 *
	 * @param completedDate
	 *            the date that the status changed to COMPLETED
	 */
	public void setCompletedDate(Date completedDate) {
		this.completedDate = completedDate;
		setState(State.COMPLETED);
	}

	/**
	 * Returns the date that the status changed to FAILED. If the status has never been FAILED
	 * <code>null</code> is returned.
	 *
	 * @return the date that the status changed to failed
	 */
	public Date getFailedDate() {
		return failedDate;
	}

	/**
	 * Sets the date that the status changed to FAILED.
	 *
	 * @param failedDate
	 *            the date that the status changed to FAILED
	 */
	public void setFailedDate(Date failedDate) {
		this.failedDate = failedDate;
		setState(State.FAILED);
	}

	/**
	 * Returns the dates that the status changed to PAUSED.
	 * <p>
	 * If the status has never been PAUSED an empty list is returned.
	 *
	 * @return the dates that the status was paused
	 */
	public List<Date> getPausedDates() {
		return pausedDates;
	}

	/**
	 * Returns the dates that the status changed from PAUSED to RUNNING.
	 * <p>
	 * If the status has never changed from PAUSED to RUNNING an empty list is returned.
	 *
	 * @return the dates that the status was resumed
	 */
	public List<Date> getResumedDates() {
		return resumedDates;
	}

	/**
	 * Returns the invocations.
	 *
	 * @return the invocations
	 */
	public SortedSet<Invocation> getInvocations() {
		synchronized (invocations) {
			return new TreeSet<>(invocations);
		}
	}

	public void addInvocation(Invocation invocation) {
		synchronized (invocations) {
			invocations.add(invocation);
		}
	}

	/**
	 * Informs the report that an output value has been added.
	 * <p>
	 * Any <code>ReportListener</code>s registered with this report will be notified that an output
	 * value has been added.
	 *
	 * @param path
	 *            the path that the value was added to
	 * @param portName
	 *            the port that the value belongs to
	 * @param index
	 *            the position of the value
	 */
	public void outputAdded(Path path, String portName, int[] index) {
		synchronized (reportListeners) {
			for (ReportListener reportListener : reportListeners) {
				reportListener.outputAdded(path, portName, index);
			}
		}
	}

	public void addReportListener(ReportListener reportListener) {
		synchronized (reportListeners) {
			reportListeners.add(reportListener);
		}
	}

	public void removeReportListener(ReportListener reportListener) {
		synchronized (reportListeners) {
			reportListeners.remove(reportListener);
		}
	}

}