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

import org.hibernate.validator.NotNull;

@Entity
@NamedQueries(
	value={
		@NamedQuery(name=Job.NAMED_QUERY_ALL,
			query="SELECT j FROM Job j ORDER BY j.created DESC")
	}
)
public class Job extends OwnedResource {
	
	public static final String NAMED_QUERY_ALL = "allJobs";
	
	// Different from Freefluo:
	// QUEUED, DEQUEUED
	public enum State {
		NEW, QUEUED, DEQUEUED, RUNNING, PAUSED, FAILING, CANCELLING, CANCELLED, COMPLETE, FAILED, DESTROYED
	}
	
	@NotNull
	@ManyToOne(fetch=FetchType.LAZY)
	private Workflow workflow;

	@NotNull
	@Enumerated(value=EnumType.STRING)
	private State state = State.NEW;

	@ManyToOne(fetch = FetchType.LAZY)
	private DataDoc inputDoc;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private DataDoc outputDoc;
	
	@Lob
	@Column(length=65535)
	private String progressReport;

	public Job() {
	}

	public String getProgressReport() {
		return progressReport;
	}

	public void setProgressReport(String progressReport) {
		this.progressReport = progressReport;
		setLastModified();
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
		setLastModified();
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

	public boolean isFinished() {
		return getState().compareTo(State.RUNNING) > 1;
	}

	public DataDoc getResultDoc() {
		return outputDoc;
	}

	public void setResultDoc(DataDoc resultDoc) {
		this.outputDoc = resultDoc;
		setLastModified();
	}
	
}
