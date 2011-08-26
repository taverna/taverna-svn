package net.sf.taverna.service.datastore.bean;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class OwnedResource extends DatedResource {
	
	@ManyToOne(fetch=FetchType.LAZY)
	private User owner;

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.setLastModified();
		this.owner = owner;
	}
	
}
