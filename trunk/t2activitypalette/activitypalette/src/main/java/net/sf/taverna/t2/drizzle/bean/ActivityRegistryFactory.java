package net.sf.taverna.t2.drizzle.bean;

import net.sf.taverna.t2.drizzle.model.ActivityRegistry;
import net.sf.taverna.t2.util.beanable.BeanableFactory;

public class ActivityRegistryFactory extends BeanableFactory<ActivityRegistry, ActivityRegistryBean> {

	public ActivityRegistryFactory() {
		super(ActivityRegistry.class, ActivityRegistryBean.class);
	}

}
