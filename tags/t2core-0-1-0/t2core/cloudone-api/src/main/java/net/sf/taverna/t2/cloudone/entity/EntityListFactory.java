package net.sf.taverna.t2.cloudone.entity;

import net.sf.taverna.t2.cloudone.bean.EntityListBean;
import net.sf.taverna.t2.util.beanable.BeanableFactory;

public class EntityListFactory extends
		BeanableFactory<EntityList, EntityListBean> {
	public EntityListFactory() {
		super(EntityList.class, EntityListBean.class);
	}

}
