package net.sf.taverna.t2.cloudone.refscheme.http;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;

@XmlRootElement(namespace = "http://taverna.sf.net/t2/cloudone/refscheme/http/", name = "httpReferenceScheme")
@XmlType(namespace = "http://taverna.sf.net/t2/cloudone/refscheme/http/", name = "httpReferenceScheme")
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
