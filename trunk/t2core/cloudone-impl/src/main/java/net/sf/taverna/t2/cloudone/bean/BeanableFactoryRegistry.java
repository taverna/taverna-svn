package net.sf.taverna.t2.cloudone.bean;

import net.sf.taverna.t2.cloudone.bean.BeanableFactory;
import net.sf.taverna.t2.spi.SPIRegistry;

import org.apache.log4j.Logger;

@SuppressWarnings("unchecked")
public class BeanableFactoryRegistry extends SPIRegistry<BeanableFactory> {
	private static BeanableFactoryRegistry instance;

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(BeanableFactoryRegistry.class);

	protected BeanableFactoryRegistry() {
		super(BeanableFactory.class);
	}

	public static synchronized BeanableFactoryRegistry getInstance() {
		if (instance == null) {
			instance = new BeanableFactoryRegistry();
		}
		return instance;
	}

	public BeanableFactory getFactoryForBeanableType(String beanableClassName) {
		for (BeanableFactory factory : getInstances()) {
			if (factory.getBeanableType().getCanonicalName().equals(beanableClassName)) {
				return factory;
			}
		}
		throw new IllegalArgumentException("Can't find Beanable class " + beanableClassName);
	}

}
