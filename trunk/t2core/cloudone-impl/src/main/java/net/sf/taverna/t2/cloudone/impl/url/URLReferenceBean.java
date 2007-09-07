package net.sf.taverna.t2.cloudone.impl.url;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;

public class URLReferenceBean extends ReferenceBean {
		
	String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String getType() {
		return "url";
	}
	
}
