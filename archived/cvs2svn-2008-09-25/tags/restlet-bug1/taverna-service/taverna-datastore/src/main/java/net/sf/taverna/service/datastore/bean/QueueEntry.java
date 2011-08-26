package net.sf.taverna.service.datastore.bean;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.validator.NotNull;

@Entity
public class QueueEntry {
	
	// Note: ID is used to order the entries and must be sequentially
	// rising
	@Id @GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	public long getId() {
		return id;
	}
	
	@NotNull
	@ManyToOne
	private Queue queue;
	
	@NotNull
	@OneToOne
	@ManyToOne
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

	
}
