package net.sf.taverna.service.datastore.bean;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.validator.NotNull;

@Entity
public class QueueEntry extends AbstractBean<Long> implements Comparable<QueueEntry> {
	
	// Note: ID is used to order the entries and must be sequentially
	// rising
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	public Long getId() {
		return id;
	}
	
	@NotNull
	@ManyToOne
	private Queue queue;
	
	@NotNull
	@OneToOne(mappedBy="queueEntry")
	private Job job;

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Queue getQueue() {
		return queue;
	}

	public void setQueue(Queue queue) {
		this.queue = queue;
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (! (obj instanceof QueueEntry)) {
			return false;
		}
		QueueEntry other = (QueueEntry) obj;
		return getId().equals(other.getId());
	}

	public int compareTo(QueueEntry other) {
		return getId().compareTo(other.getId());
	}
	
}
