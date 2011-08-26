package net.sf.taverna.t2.cloudone.refscheme.http;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.taverna.t2.cloudone.bean.ReferenceBean;
/**
 * Used for serialising a {@link HttpReferenceScheme}
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
@Entity
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
