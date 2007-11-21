package net.sf.taverna.service.datastore.bean;

import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractUUID extends AbstractBean<String> implements Comparable<AbstractUUID> {

	@Id
	private String id = UUID.randomUUID().toString();

	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " " + getId();
	}

	public int compareTo(AbstractUUID o) {
		return getId().compareTo(o.getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AbstractUUID)) {
			return false;
		}
		AbstractUUID resource = (AbstractUUID) obj;
		return getId().equals(resource.getId());
	}

	@Override
	public int hashCode() {
		return getId().hashCode();
	}

}