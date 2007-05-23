package net.sf.taverna.service.datastore.bean;

import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class UUIDResource implements Comparable<UUIDResource> {

	@Id
	private String id = UUID.randomUUID().toString();

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + getId();
	}

	public int compareTo(UUIDResource o) {
		return getId().compareTo(o.getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UUIDResource)) {
			return false;
		}
		UUIDResource resource = (UUIDResource) obj;
		return getId().equals(resource.getId());
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

}