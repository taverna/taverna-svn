package net.sf.taverna.service.datastore.bean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.validator.NotNull;

@Entity
@NamedQueries(value = { @NamedQuery(name = Job.NAMED_QUERY_ALL, query = "SELECT j FROM Job j ORDER BY j.created DESC") })
public class Job extends AbstractOwned {

	public static final int MAX_REPORT_SIZE = 65535;
	
	public static final int MAX_CONSOLE_SIZE = 65535;

	// 5 minutes in xsd:duration format
	public static final String DEFAULT_UPDATE_INTERVAL = "PT5M";

	public static final String NAMED_QUERY_ALL = "allJobs";

	/**
	 * In addition to the statuses of Freefluo:
	 * <p>
	 * QUEUED: Placed on a queue <br>
	 * DEQUEUED: Worker has taken job and is downloading the data for the job
	 * <p>
	 * In addition, NEW means that the job is not yet on a queue, and DESTROYED
	 * means that some or all of the data of this job is no longer available in
	 * the data store. CANCELLING in this context means that the worker process is
	 * to be terminated, leading to CANCELLED.
	 */
	public enum Status {
		NEW, QUEUED, DEQUEUED, RUNNING, PAUSED, FAILING, CANCELLING, CANCELLED, COMPLETE, FAILED, DESTROYED
	}

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private Workflow workflow;

	@ManyToOne(fetch = FetchType.LAZY)
	private DataDoc inputDoc;

	@NotNull
	@Enumerated(value = EnumType.STRING)
	private Status status = Status.NEW;

	/**
	 *  In milliseconds, how long to wait between posting back updates
	 */
	private String updateInterval = DEFAULT_UPDATE_INTERVAL;

	@ManyToOne(fetch = FetchType.LAZY)
	private DataDoc outputDoc;

	@Lob
	@Column(length = MAX_REPORT_SIZE)
	private String progressReport;
	
	@Lob
	@Column(length = MAX_CONSOLE_SIZE)
	private String console;

	@ManyToOne(fetch = FetchType.LAZY)
	private Worker worker;

	@OneToOne
	private QueueEntry queueEntry;

	public Job() {
	}

	public Workflow getWorkflow() {
		return workflow;
	}

	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
		setLastModified();
	}

	public DataDoc getInputs() {
		return inputDoc;
	}

	public void setInputs(DataDoc inputDoc) {
		this.inputDoc = inputDoc;
		setLastModified();
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
		setLastModified();
	}

	public boolean hasStarted() {
		return getStatus().compareTo(Status.QUEUED) > 0;
	}
	
	public boolean isFinished() {
		return getStatus().compareTo(Status.CANCELLING) > 0;
	}

	public String getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(String updateInterval) {
		this.updateInterval = updateInterval;
	}

	public String getProgressReport() {
		return progressReport;
	}

	public void setProgressReport(String progressReport) {
		this.progressReport = progressReport;
		setLastModified();
	}

	public DataDoc getOutputs() {
		return outputDoc;
	}

	public void setOutputs(DataDoc resultDoc) {
		outputDoc = resultDoc;
		setLastModified();
	}

	public Worker getWorker() {
		return worker;
	}

	public void setWorker(Worker worker) {
		this.worker = worker;
		setLastModified();
	}

	public QueueEntry getQueueEntry() {
		return queueEntry;
	}

	public void setQueueEntry(QueueEntry queueEntry) {
		this.queueEntry = queueEntry;
		if (queueEntry != null) {
			setStatus(Status.QUEUED);
		} else if (getStatus().equals(Status.QUEUED)) {
			setStatus(Status.NEW);
		}
		setLastModified();
	}

	@Transient
	public synchronized Queue getQueue() {
		if (getQueueEntry() == null) {
			return null;
		}
		return getQueueEntry().getQueue();
	}
	
	@Override
	public void setOwner(User owner) {
		if (getOwner() != null) {
			getOwner().getJobs().remove(this);
		}
		super.setOwner(owner);
		if (owner != null) {
			owner.getJobs().add(this);
		}
	}


	public String getConsole() {
		return console;
	}

	public void setConsole(String console) {
		this.console = console;
		setLastModified();
	}

}
