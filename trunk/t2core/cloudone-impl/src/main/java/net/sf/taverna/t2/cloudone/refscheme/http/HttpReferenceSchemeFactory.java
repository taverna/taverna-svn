package net.sf.taverna.t2.cloudone.refscheme.http;

import net.sf.taverna.t2.util.beanable.BeanableFactory;

public class HttpReferenceSchemeFactory extends
		BeanableFactory<HttpReferenceScheme, HttpReferenceBean> {

	public HttpReferenceSchemeFactory() {
		super(HttpReferenceScheme.class, HttpReferenceBean.class);
	}

}
