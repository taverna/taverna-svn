package net.sf.taverna.t2.activities.wsdl.query;

import net.sf.taverna.t2.partition.ActivityItem;

public class WSDLActivityItem implements ActivityItem {
	
	private String use;
	private String url;
	private String style;
	private String operation;
	
	public String getUse() {
		return use;
	}
	
	public void setUse(String use) {
		this.use = use;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getStyle() {
		return style;
	}
	
	public void setStyle(String style) {
		this.style = style;
	}
	
	public String getType() {
		return "SOAP";
	}

	@Override
	public String toString() {
		return operation;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
	
}
