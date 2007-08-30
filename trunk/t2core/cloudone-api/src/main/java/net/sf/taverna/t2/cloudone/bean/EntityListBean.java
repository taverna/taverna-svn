package net.sf.taverna.t2.cloudone.bean;

import java.util.List;

public class EntityListBean {
	private String identifier;

	private List<String> content;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public List<String> getContent() {
		return content;
	}

	public void setContent(List<String> content) {
		this.content = content;
	}

}
