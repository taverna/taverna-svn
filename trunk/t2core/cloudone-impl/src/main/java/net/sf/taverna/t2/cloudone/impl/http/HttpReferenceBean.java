package net.sf.taverna.t2.cloudone.impl.http;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;

public class HttpReferenceBean extends ReferenceBean {
		
	String url;

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public Class<HttpReferenceScheme> getOwnerClass() {
		return HttpReferenceScheme.class;
	}
	
}
