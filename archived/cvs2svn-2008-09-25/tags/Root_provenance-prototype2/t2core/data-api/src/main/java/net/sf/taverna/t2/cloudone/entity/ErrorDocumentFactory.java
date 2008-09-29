package net.sf.taverna.t2.cloudone.entity;

import net.sf.taverna.t2.cloudone.bean.ErrorDocumentBean;
import net.sf.taverna.t2.util.beanable.BeanableFactory;

public class ErrorDocumentFactory extends
		BeanableFactory<ErrorDocument, ErrorDocumentBean> {
	public ErrorDocumentFactory() {
		super(ErrorDocument.class, ErrorDocumentBean.class);
	}

}
