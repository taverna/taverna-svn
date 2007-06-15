package net.sf.taverna.service.datastore.bean;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import net.sf.taverna.service.datastore.bean.Job.Status;

@Entity
public class Worker extends User {
	
	private String apiURI;
	
	@ManyToOne
	@JoinColumn(name="queue_fk")
	private Queue queue;

	@OneToMany(mappedBy="worker")
	private List<Job> jobs;
	
	public boolean isBusy() {
		for (Job j : getJobs()) {
			if (j.getStatus().equals(Status.RUNNING) || j.getStatus().equals(Status.DEQUEUED)) return true;
		}
		return false;
	}
	
	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
	
	public void setQueue(Queue queue) {
		this.queue=queue;
	}
	
	public Queue getQueue() {
		return queue;
	}

	public String getApiURI() {
		return apiURI;
	}

	/**
	 * Set the URI for a web API directly communicating with the worker.
	 * If the worker doesn't have an accessible web API, this URI is null.
	 * 
	 * @param uri 
	 */
	public void setApiURI(String uri) {
		this.apiURI = uri;
		setLastModified();
	}

	public void assignJob(Job job) {
		job.setStatus(Status.DEQUEUED);
		if (getJobs()==null) setJobs(new ArrayList<Job>());
		getJobs().add(job);
	}
}
