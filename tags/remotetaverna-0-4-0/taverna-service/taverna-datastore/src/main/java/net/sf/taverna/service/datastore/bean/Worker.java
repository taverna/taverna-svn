package net.sf.taverna.service.datastore.bean;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import net.sf.taverna.service.datastore.bean.Job.Status;

@Entity
public class Worker extends User {
	
	private String apiURI;
	
	@ManyToOne
	@JoinColumn(name="queue_fk")
	private Queue queue;
	
	@OneToMany(mappedBy = "worker", fetch=FetchType.EAGER)
	private List<Job> workerJobs = new ArrayList<Job>();
	
	private String workerPwd;
	
	public boolean isBusy() {
		for (Job j : getWorkerJobs()) {
			if (j.getStatus().equals(Status.RUNNING) || 
				j.getStatus().equals(Status.DEQUEUED)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isRunning() {
		if (isBusy()) {
			for (Job j : getWorkerJobs()) {
				if (j.getStatus().equals(Status.RUNNING)) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public void setPassword(String password) {
		super.setPassword(password);
		getWorkerPasswordStr(password);
	}

	public String getWorkerPasswordStr() {
		return workerPwd;
	}

	public void getWorkerPasswordStr(String passwordStr) {
		this.workerPwd = passwordStr;
	}

	public List<Job> getWorkerJobs() {
		return workerJobs;
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
	
	public void setJobs(List<Job> jobs) {
		this.jobs=jobs;
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
		job.setWorker(this);
		job.setStatus(Status.DEQUEUED);
		getWorkerJobs().add(job);
	}
	
	
	/**
	 * Disassociates all finished jobs from this worker.
	 * Returns the list of unassigned job beans, so that they can be passed to their DAO to update the database.
	 * @return List<Job> unassigned jobs
	 */
	public List<Job> unassignJobs() {
		List<Job> toRemove = new ArrayList<Job>();
		for (Job job : getWorkerJobs()) {
			if (job.isFinished()) {
				job.setWorker(null);
				toRemove.add(job);
			}
		}
		getWorkerJobs().removeAll(toRemove);
		return toRemove;
	}
	
	public Job getNextDequeuedJob() {
		for (Job job : getWorkerJobs()) {
			if (job.getStatus().equals(Status.DEQUEUED)) return job;
		}
		return null;
	}
}
