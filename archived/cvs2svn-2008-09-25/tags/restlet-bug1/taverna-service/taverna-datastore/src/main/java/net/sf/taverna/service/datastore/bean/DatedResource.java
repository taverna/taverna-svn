package net.sf.taverna.service.datastore.bean;

import java.util.Date;

import javax.persistence.MappedSuperclass;

import org.hibernate.validator.NotNull;


@MappedSuperclass
public abstract class DatedResource extends UUIDResource {
	@NotNull
	private Date created = new Date();
	
	@NotNull
	private Date lastModified = new Date();

	public Date getCreated() {
		return created;
	}

	public Date getLastModified() {
		return lastModified;
	}
	
	/**
	 * Set the last modified date to the current time.
	 * This method should be called from other set methods 
	 * except for meta properties such as created and id.
	 * 
	 */
	public void setLastModified() {
		setLastModified(new Date());
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
}
