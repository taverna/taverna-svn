/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.EclipseRepository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;
import net.sf.taverna.raven.spi.SpiRegistry;

/**
 * @author alanrw
 *
 */
public final class ObjectFactory {

	private static Map<Class<?>,SpiRegistry> registryMap = new HashMap<Class<?>,SpiRegistry>();
	
	@SuppressWarnings("unchecked")
	public static <C> C getInstance(Class<C> objectClass) {
		if (objectClass == null) {
			throw new NullPointerException ("objectClass cannot be null"); //$NON-NLS-1$
		}
		C result = null;
		SpiRegistry registry = registryMap.get(objectClass);
		if (registry == null) {
			registry = new SpiRegistry (getRepository(), objectClass.getName(),
					getClassLoader());
			registryMap.put(objectClass, registry);
		}
		List<Class> classes = registry.getClasses();
		if (classes.size() > 0) {
			try {
				result = (C) classes.get(0).newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	private static Repository getRepository() {
		if (getClassLoader() instanceof LocalArtifactClassLoader) {
			return ((LocalArtifactClassLoader) getClassLoader())
					.getRepository();
		}
		// Do the ugly hack in one place
		System.setProperty("raven.eclipse", "true");  //$NON-NLS-1$//$NON-NLS-2$
		return new EclipseRepository();
	}

	private static ClassLoader getClassLoader() {
		return ObjectFactory.class.getClassLoader();
	}

}
