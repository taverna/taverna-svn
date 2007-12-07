package net.sf.taverna.t2.drizzle.bean;

import net.sf.taverna.t2.drizzle.model.ActivityPaletteModel;
import net.sf.taverna.t2.drizzle.util.StringKey;
import net.sf.taverna.t2.util.beanable.BeanableFactory;

public class StringKeyFactory extends BeanableFactory<StringKey, StringKey> {

	public StringKeyFactory() {
		super(StringKey.class, StringKey.class);
	}

}
