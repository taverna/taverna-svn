package net.sf.taverna.t2.cloudone.impl;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;

public class BlobReferenceBean extends ReferenceBean {
	public static final String TYPE = "blob";

	private String id;

	private String namespace;

	public String getId() {
		return id;
	}

	public String getNamespace() {
		return namespace;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
}
