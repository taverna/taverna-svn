package net.sf.taverna.t2.platform.util.reflect.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;

import net.sf.taverna.t2.platform.plugin.PluginManager;
import net.sf.taverna.t2.platform.plugin.generated.PluginDescription;
import net.sf.taverna.t2.platform.util.reflect.ReflectionException;
import net.sf.taverna.t2.platform.util.reflect.ReflectionHelper;

/**
 * Implementation of ReflectionHelper, must be connected to a PluginManager
 * through the pluginManager property before use.
 * 
 * @author Tom Oinn
 * 
 */
public class ReflectionHelperImpl implements ReflectionHelper {

	private PluginManager manager;

	/**
	 * Default constructor for use with spring, remember to call the
	 * setPluginManager method if you're calling this directly from your own
	 * code (but don't, use Spring!)
	 */
	public ReflectionHelperImpl() {
		//
	}

	/**
	 * Set the plug-in manager this reflection helper will use when looking for
	 * active plug-in class loaders
	 * 
	 * @param manager
	 */
	public void setPluginManager(PluginManager manager) {
		this.manager = manager;
	}

	public void setProperty(Object target, String propertyName, Object value)
			throws ReflectionException {
		try {
			Method m = target.getClass().getMethod("set" + propertyName,
					value.getClass());
			m.invoke(target, value);
		} catch (InvocationTargetException ite) {
			throw new ReflectionException(ite);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException(e);
		} catch (IllegalAccessException e) {
			throw new ReflectionException(e);
		} catch (SecurityException e) {
			throw new ReflectionException(e);
		} catch (NoSuchMethodException e) {
			throw new ReflectionException(e);
		}
	}

	public Object getProperty(Object target, String propertyName)
			throws ReflectionException {
		try {
			Method m = target.getClass().getMethod("get" + propertyName);
			return m.invoke(target);
		} catch (InvocationTargetException ite) {
			throw new ReflectionException(ite);
		} catch (IllegalArgumentException e) {
			throw new ReflectionException(e);
		} catch (IllegalAccessException e) {
			throw new ReflectionException(e);
		} catch (SecurityException e) {
			throw new ReflectionException(e);
		} catch (NoSuchMethodException e) {
			throw new ReflectionException(e);
		}
	}

	public Object construct(String className, Object... args)
			throws ReflectionException {
		for (PluginDescription desc : manager.getActivePluginList()) {
			try {
				ClassLoader loader = manager.getPluginClassLoader(desc.getId(),
						new ArrayList<URL>());
				Class<?> c = loader.loadClass(className);
				Class<?>[] constructorTypes = new Class<?>[args.length];
				for (int i = 0; i < args.length; i++) {
					constructorTypes[i] = args[i].getClass();
				}
				Constructor<?> cons = c.getConstructor(constructorTypes);
				Object o = cons.newInstance(args);
				return o;
			} catch (Exception ex) {
				// 
			}
		}
		throw new ReflectionException("Unable to construct a '" + className
				+ "'");
	}

	public Object createAndConfigure(String className,
			String[] propertyStrings, Object... constructorArgs)
			throws ReflectionException {
		Object target = construct(className, constructorArgs);
		for (String propertyString : propertyStrings) {
			String[] propertyStringArray = propertyString.split("=");
			if (propertyStringArray.length != 2) {
				throw new ReflectionException("Invalid property specification "
						+ propertyString);
			}
			String propertyName = propertyStringArray[0];
			String propertyValueString = propertyStringArray[1];
			Object propertyValue;
			Method method = null;
			for (Method m : target.getClass().getMethods()) {
				if (m.getName().equalsIgnoreCase("set" + propertyName)) {
					method = m;
					break;
				}
			}
			if (method == null) {
				throw new ReflectionException(
						"Unable to find a set method for property '"
								+ propertyName + "'");
			}
			if (method.getParameterTypes().length != 1) {
				throw new ReflectionException(
						"Setter method must have one argument, has "
								+ method.getParameterTypes().length + " in '"
								+ className + ":" + propertyName);
			}
			Class<?> propertyClass = method.getParameterTypes()[0];
			try {
				Constructor<?> propertyConstructor = propertyClass
						.getConstructor(new Class<?>[] { String.class });
				propertyValue = propertyConstructor
						.newInstance(propertyValueString);
			} catch (NoSuchMethodException nsme) {
				throw new ReflectionException(
						"No suitable constructor for property value as object for type "
								+ propertyClass.getCanonicalName());
			} catch (IllegalArgumentException e) {
				throw new ReflectionException(
						"Unable to construct property value", e);
			} catch (InstantiationException e) {
				throw new ReflectionException(
						"Unable to construct property value", e);
			} catch (IllegalAccessException e) {
				throw new ReflectionException(
						"Unable to construct property value", e);
			} catch (InvocationTargetException e) {
				throw new ReflectionException(
						"Unable to construct property value", e);
			}
			try {
				method.invoke(target, propertyValue);
			} catch (IllegalArgumentException e) {
				throw new ReflectionException("Unable to set property value", e);
			} catch (IllegalAccessException e) {
				throw new ReflectionException("Unable to set property value", e);
			} catch (InvocationTargetException e) {
				throw new ReflectionException("Unable to set property value", e);
			}
		}
		return target;
	}

}
