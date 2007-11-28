package net.sf.taverna.t2.cloudone.entity;

import net.sf.taverna.t2.cloudone.bean.BeanableFactory;
import net.sf.taverna.t2.cloudone.bean.EntityListBean;

import org.apache.log4j.Logger;

public class EntityListFactory extends BeanableFactory<EntityList, EntityListBean> {
	public EntityListFactory() {
		super(EntityList.class, EntityListBean.class);
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EntityListFactory.class);
}
