package net.sf.taverna.service.datastore.bean;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.hibernate.validator.NotNull;

@Entity
public class Worker extends UUIDResource {
	
	@NotNull
	@Column(unique=true, nullable=false)
	private String uri;
	
	@ManyToMany
	private List<Queue> queues;
}
