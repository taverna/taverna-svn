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

import org.apache.log4j.Logger;
import org.hibernate.validator.NotNull;

@Entity
@NamedQueries(value = { 
	@NamedQuery(name = Job.NAMED_QUERY_ALL, 
		query = "SELECT j FROM Job j ORDER BY j.created DESC"),
	@NamedQuery(name = Job.NAMED_QUERY_STATUS, 
		query = "SELECT j FROM Job j WHERE j.status=:status ORDER BY j.created DESC") }

)
public class Job extends AbstractOwned {
	
	private static Logger logger = Logger.getLogger(Job.class);

	public static final int MAX_REPORT_SIZE = 65535;
	
	public static final int MAX_CONSOLE_SIZE = 65535;

	// 1 minutes in xsd:duration format.
	public static final String DEFAULT_UPDATE_INTERVAL = "PT60S";

	public static final String NAMED_QUERY_ALL = "allJobs";
	
	public static final String NAMED_QUERY_STATUS = "jobsByStatus";

	/**
	 * Status enumeration. Basically follows Freefluo's statuses, but in
	 * addition there's:
	 * <p>
	 * QUEUED: Placed on a queue <br>
	 * INITIALISING: Worker has taken job and is downloading the data for the job
	 * <p>
	 * In addition, NEW means that the job is not yet (or no longer) on a queue,
	 * and DESTROYED means that some or all of the data of this job is no longer
	 * available in the data store. CANCELLING in this context means that the
	 * worker process is to be terminated, leading to CANCELLED.
	 */
	public enum Status {
		// NOTE: Also update service.xsd StatusType
		NEW, QUEUED, INITIALISING, RUNNING, PAUSED, FAILING, CANCELLING, 
		CANCELLED, COMPLETE, FAILED, DESTROYED
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

	public synchronized void setStatus(Status status) {
		if (status.compareTo(getStatus()) < 0) {
			logger.warn("Attempt to reverse status from " + this.status + " to " + status);
			throw new IllegalArgumentException("Can't change status from " + this.status + " to " + status);
		}
		this.status = status;
		setLastModified();
	}

	public boolean hasStarted() {
		return getStatus().compareTo(Status.QUEUED) > 0;
	}
	
	public boolean isFinished() {
		return getStatus().compareTo(Status.CANCELLING) > 0;
	}
	
	public boolean isCancelling() {
		return getStatus().equals(Status.CANCELLING);
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

	public synchronized void setQueueEntry(QueueEntry queueEntry) {
		this.queueEntry = queueEntry;
		if (queueEntry != null && getStatus().equals(Status.NEW)) {
			setStatus(Status.QUEUED);
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
