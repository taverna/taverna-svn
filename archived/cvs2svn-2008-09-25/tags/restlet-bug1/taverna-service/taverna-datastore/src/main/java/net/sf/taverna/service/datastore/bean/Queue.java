package net.sf.taverna.service.datastore.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

@Entity
public class Queue extends DatedResource {
	
	@OneToMany(cascade=CascadeType.ALL)
	@OrderBy("id")
	private List<QueueEntry> entries = new ArrayList<QueueEntry>();

	@ManyToMany
	private Set<Worker> workers = new HashSet<Worker>();
	
	public Set<Worker> getWorkers() {
		return workers;
	}
	
	@Transient
	public List<Job> getJobs() {
		List<Job> jobs = new ArrayList<Job>();
		for (QueueEntry entry : entries) {
			jobs.add(entry.getJob());
		}
		return jobs;
	}

	public void addJob(Job job) {
		QueueEntry entry = new QueueEntry();
		entry.setQueue(this);
		entry.setJob(job);
		entries.add(entry);
		setLastModified();
	}
	
	public QueueEntry removeJob(Job job) {
		QueueEntry removeEntry = null;
		for (QueueEntry entry : entries) {
			if (entry.getJob().equals(job)) {
				removeEntry = entry;
				break;
			}
		}
		if (removeEntry == null) {
			throw new IllegalArgumentException("Unknown job " + job);
		}
		entries.remove(removeEntry);
		setLastModified();
		return removeEntry;
	}
	
}
