package net.sf.taverna.t2.cloudone.entity.impl;

import net.sf.taverna.t2.cloudone.bean.BeanableFactory;
import net.sf.taverna.t2.cloudone.bean.DataDocumentBean;

public class DataDocumentFactory extends BeanableFactory<DataDocumentImpl, DataDocumentBean> {

	public DataDocumentFactory() {
		super(DataDocumentImpl.class, DataDocumentBean.class);
	}

}
