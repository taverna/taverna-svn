package net.sf.taverna.t2.cloudone.impl.url;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;

public class URLReferenceBean extends ReferenceBean {
		
	public static final String TYPE = "url";
	
	String url;

	@Override
	public String getType() {
		return TYPE;
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
}
