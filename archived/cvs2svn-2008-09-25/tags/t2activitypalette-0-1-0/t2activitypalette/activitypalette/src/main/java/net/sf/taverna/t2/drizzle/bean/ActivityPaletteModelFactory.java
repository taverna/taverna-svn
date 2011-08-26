package net.sf.taverna.t2.drizzle.bean;

import net.sf.taverna.t2.drizzle.model.ActivityPaletteModel;
import net.sf.taverna.t2.util.beanable.BeanableFactory;

/**
 * @author alanrw
 *
 */
public class ActivityPaletteModelFactory extends BeanableFactory<ActivityPaletteModel, ActivityPaletteModelBean> {

	/**
	 * 
	 */
	public ActivityPaletteModelFactory() {
		super(ActivityPaletteModel.class, ActivityPaletteModelBean.class);
	}

}
