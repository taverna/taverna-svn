package net.sf.taverna.t2.cloudone.bean;

import java.util.List;

public class DataDocumentBean {

	private String identifier;
	
	private List<ReferenceBean> references;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public List<ReferenceBean> getReferences() {
		return references;
	}

	public void setReferences(List<ReferenceBean> references) {
		this.references = references;
	}
	
}
