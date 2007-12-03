package net.sf.taverna.t2.util.beanable;

import java.util.HashSet;
import java.util.Set;

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
		checkFactories();
	}

	public void checkFactories() {
		Set<Class> beanTypes = new HashSet<Class>();
		Set<Class> beanableTypes = new HashSet<Class>();
		Set<String> beanableTypeNames = new HashSet<String>();

		
		for (BeanableFactory factory : getInstances()) {
			Class beanType = factory.getBeanType();
			if (beanTypes.contains(beanType)) {
				logger.warn("Factory  " + factory + " has non-unique bean type " + beanType);
			}
			beanTypes.add(beanType);
			
			Class beanableType = factory.getBeanableType();
			if (beanableTypes.contains(beanableType)) {
				logger.warn("Factory  " + factory + " has non-unique beanable type " + beanableType);
			}
			beanableTypes.add(beanableType);
			// TODO: Remove non-unique entries to highlight the problem
			
			String beanableTypeName = beanableType.getCanonicalName();
			if (beanableTypeNames.contains(beanableTypeName)) {
				logger.warn("Factory " + factory + " has non-unique beanable class name " + beanableTypeName);
			}
		}
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
		throw new IllegalArgumentException("Can't find factory for beanable class " + beanableClassName);
	}

	public BeanableFactory getFactoryForBeanType(
			Class beanType) {
		for (BeanableFactory factory : getInstances()) {
			if (factory.getBeanType().equals(beanType)) {
				return factory;
			}
		}
		throw new IllegalArgumentException("Can't find factory for bean class " + beanType);
	}

}
