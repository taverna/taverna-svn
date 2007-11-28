package net.sf.taverna.t2.cloudone.refscheme.http;

import net.sf.taverna.t2.cloudone.bean.BeanableFactory;

public class HttpReferenceSchemeFactory extends
		BeanableFactory<HttpReferenceScheme, HttpReferenceBean> {

	public HttpReferenceSchemeFactory() {
		super(HttpReferenceScheme.class, HttpReferenceBean.class);
	}

}
