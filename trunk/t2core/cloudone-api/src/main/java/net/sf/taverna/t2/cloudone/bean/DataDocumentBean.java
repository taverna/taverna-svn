package net.sf.taverna.t2.cloudone.bean;

import java.util.List;

public class DataDocumentBean {

	private String identifier;
	
	// TODO: Support other reference types
	private List<String> urlReferences;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public List<String> getUrlReferences() {
		return urlReferences;
	}

	public void setUrlReferences(List<String> urlReferences) {
		this.urlReferences = urlReferences;
	}
	
}
