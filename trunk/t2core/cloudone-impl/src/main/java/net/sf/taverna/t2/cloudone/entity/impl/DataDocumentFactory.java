package net.sf.taverna.t2.cloudone.entity.impl;

import net.sf.taverna.t2.cloudone.bean.DataDocumentBean;
import net.sf.taverna.t2.util.beanable.BeanableFactory;

public class DataDocumentFactory extends BeanableFactory<DataDocumentImpl, DataDocumentBean> {

	public DataDocumentFactory() {
		super(DataDocumentImpl.class, DataDocumentBean.class);
	}

}
