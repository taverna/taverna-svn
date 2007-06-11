package net.sf.taverna.service.datastore.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

@Entity
public class Worker extends User {
	
	private String apiURI;
	
	@ManyToMany(mappedBy="workers")
	private Set<Queue> queues;

	@OneToMany(mappedBy="worker")
	private List<Job> jobs;
	
	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}

	public Set<Queue> getQueues() {
		return queues;
	}

	public void setQueues(Set<Queue> queues) {
		this.queues = queues;
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

}
