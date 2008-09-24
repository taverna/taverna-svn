package net.sf.taverna.service.datastore.bean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import net.sf.taverna.service.datastore.dao.QueueEntryDAO;


@NamedQuery(name = Queue.NAMED_QUERY_NAME, query = "SELECT q FROM Queue q WHERE q.name=:name")
@Entity
public class Queue extends AbstractNamed {
	
	public static final String NAMED_QUERY_NAME = "queueByName";
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="queue")
	@OrderBy("id")
	private List<QueueEntry> entries = new ArrayList<QueueEntry>();

	@OneToMany(mappedBy="queue")
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
	
	public boolean hasJob(Job job) {
		return this.equals(job.getQueue());
	}

	/**
	 * Add a job to the queue by creating the new {@link QueueEntry}. The job must not
	 * already be on this or any other queues.
	 * <p>
	 * <strong>Note:</strong> The caller must call
	 * {@link QueueEntryDAO#create(QueueEntry)} on the returned
	 * {@link QueueEntry}.
	 * 
	 * @throws IllegalArgumentException
	 *             if the {@link Job} already had a queue entry set
	 * @param job {@link Job} to add to queue.
	 * @return The created {@link QueueEntry} that must be created with the {@link QueueEntryDAO}
	 */
	public QueueEntry addJob(Job job) throws IllegalArgumentException {
		if (job.getQueueEntry() != null) {
			throw new IllegalArgumentException("Job already has a queue: " + job);
		}
		QueueEntry entry = new QueueEntry();
		entry.setQueue(this);
		entry.setJob(job);
		job.setQueueEntry(entry);
		entries.add(entry);
		setLastModified();
		return entry;
	}
	
	/**
	 * Remove a job from queue by deleting it's {@link QueueEntry}
	 * <p>
	 * <strong>Note:</strong> The caller must call
	 * {@link QueueEntryDAO#delete(QueueEntry)} on the returned
	 * {@link QueueEntry}.
	 * 
	 * @param job {@link Job} to remove from queue.
	 * @return The {@link QueueEntry} that must be deleted with the {@link QueueEntryDAO}
	 */
	public QueueEntry removeJob(Job job) {
		QueueEntry removeEntry = job.getQueueEntry();
		if (removeEntry == null) {
			throw new IllegalArgumentException("Unknown job " + job);
		}
		job.setQueueEntry(null);
		entries.remove(removeEntry);
		setLastModified();
		return removeEntry;
	}

	public List<QueueEntry> getEntries() {
		return entries;
	}
	
}
