package net.sf.taverna.t2.drizzle.bean;

import net.sf.taverna.t2.drizzle.model.ActivitySetModel;
import net.sf.taverna.t2.util.beanable.BeanableFactory;

/**
 * @author alanrw
 *
 */
public class ActivitySetModelFactory extends BeanableFactory<ActivitySetModel, ActivitySetModelBean> {

	/**
	 * 
	 */
	public ActivitySetModelFactory() {
		super(ActivitySetModel.class, ActivitySetModelBean.class);
	}

}
