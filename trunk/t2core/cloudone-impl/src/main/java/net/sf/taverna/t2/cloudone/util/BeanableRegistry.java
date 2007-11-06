package net.sf.taverna.t2.cloudone.util;

import java.util.Map;

import net.sf.taverna.t2.cloudone.bean.Beanable;
import net.sf.taverna.t2.spi.SPIRegistry;

@SuppressWarnings("unchecked")
public class BeanableRegistry extends SPIRegistry<Beanable>{

	private static BeanableRegistry instance = null;
	
	public BeanableRegistry() {
		super(Beanable.class);
	}
	
	public static synchronized BeanableRegistry getInstance() {
		if (instance == null) {
			instance = new BeanableRegistry();
			return instance;
		}
		return instance;
	}
	
	public Beanable getBeanable (String className) {
		Map<String, Class<? extends Beanable>> map = getClasses();
		Class<? extends Beanable> beanableClass = map.get(className);
		if (beanableClass == null) {
			throw new IllegalArgumentException("Can't find Beanable class " + className);
		}
		try {
			return beanableClass.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException(
					"Can't instantiate Beanable class " + className, e);
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException(
					"Can't access Beanable class " + className, e);
		}
	}

}
