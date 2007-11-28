package net.sf.taverna.t2.cloudone.entity;

import net.sf.taverna.t2.cloudone.bean.BeanableFactory;
import net.sf.taverna.t2.cloudone.bean.ErrorDocumentBean;

import org.apache.log4j.Logger;

public class ErrorDocumentFactory extends BeanableFactory<ErrorDocument, ErrorDocumentBean> {
	public ErrorDocumentFactory() {
		super(ErrorDocument.class, ErrorDocumentBean.class);
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ErrorDocumentFactory.class);
}
