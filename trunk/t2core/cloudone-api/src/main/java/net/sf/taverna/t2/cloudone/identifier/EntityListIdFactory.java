package net.sf.taverna.t2.cloudone.identifier;

import net.sf.taverna.t2.util.beanable.BeanableFactory;

public class EntityListIdFactory extends BeanableFactory<EntityListIdentifier, String> {
	public EntityListIdFactory() {
		super(EntityListIdentifier.class, String.class);
	}

}
