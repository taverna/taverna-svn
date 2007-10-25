package net.sf.taverna.t2.cloudone.impl;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;

public class BlobReferenceBean extends ReferenceBean {
	private String id;

	private String namespace;

	private String charset;

	public String getCharset() {
		return charset;
	}

	public String getId() {
		return id;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public Class<BlobReferenceSchemeImpl> getOwnerClass() {
		return BlobReferenceSchemeImpl.class;
	}
}
