package net.sf.taverna.t2.cloudone.identifier;

import net.sf.taverna.t2.cloudone.bean.BeanableFactory;

import org.apache.log4j.Logger;

public class EntityListIdFactory extends BeanableFactory<EntityListIdentifier, String> {
	public EntityListIdFactory() {
		super(EntityListIdentifier.class, String.class);
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(EntityListIdFactory.class);
}
