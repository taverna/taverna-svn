package net.sf.taverna.service.datastore.bean;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class AbstractNamed extends AbstractDated {
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
