package net.sf.taverna.t2.activities.soaplab.query;

import net.sf.taverna.t2.partition.ActivityItem;

public class SoaplabActivityItem implements ActivityItem {
	private String category;
	private String operation;
	private String url;
	
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getType() {
		return "Soaplab";
	}
	@Override
	public String toString() {
		return this.operation;
	}
	
	
}
