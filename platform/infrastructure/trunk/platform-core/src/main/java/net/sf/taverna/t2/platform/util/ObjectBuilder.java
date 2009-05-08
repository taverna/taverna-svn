package net.sf.taverna.t2.platform.util;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;

import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.plugin.generated.PluginDescription;

/**
 * Used to construct an instance of a particular class by searching all
 * currently active plug-in packages from the plug-in manager and trying to
 * locate an appropriate class and constructor
 * 
 * @author Tom Oinn
 */
public class ObjectBuilder {

	private PluginManager manager;

	/**
	 * Wrap the ObjectBuilder around a PluginManager from which it will find its
	 * class loaders
	 * 
	 * @param manager
	 *            the PluginManager to use
	 */
	public ObjectBuilder(PluginManager manager) {
		this.manager = manager;
	}

	/**
	 * Attempt to build an object given a class name and array of constructor
	 * arguments
	 * 
	 * @param className
	 *            fully qualified class name to build an instance of
	 * @param objects
	 *            arguments to the constructor to use
	 * @return a newly constructed object of the specified type from the first
	 *         successfully invoked constructor method
	 * @throws RuntimeException
	 *             if we can't find an appropriate class and constructor or if
	 *             any error occurs during object construction
	 */
	public Object buildObject(String className, Object... objects) {
		Class<?>[] types = new Class<?>[objects.length];
		for (int i = 0; i < objects.length; i++) {
			types[i] = objects[i].getClass();
		}
		// Try to locate the class first
		for (PluginDescription plugin : manager.getActivePluginList()) {
			ClassLoader cl = manager.getPluginClassLoader(plugin.getId(),
					new ArrayList<URL>());
			try {
				Class<?> c = cl.loadClass(className);
				Constructor<?> cons = c.getConstructor(types);
				return cons.newInstance(objects);
			} catch (Exception ex) {
				//
			}
		}
		throw new RuntimeException(
				"Unable to build object from current plug-in set");
	}
}
