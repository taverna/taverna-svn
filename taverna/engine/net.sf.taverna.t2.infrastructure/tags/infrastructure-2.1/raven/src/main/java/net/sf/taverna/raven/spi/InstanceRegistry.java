/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.raven.spi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.raven.log.Log;

/**
 * Instance registry to sit on top of the SpiRegistry object. When changes occur
 * within the list of available SPI classes this object is responsible for
 * creating new instances of those classes and notifying any interested
 * listeners that this has changed.
 * <p>
 * Instantiation is lazy (as is the underlying scan for Classes in the
 * SpiRegistry) so may incur a delay the first time the getInstances() method is
 * called. Once this method has been called any subsequent events from the
 * SpiRegistry will cause a regeneration of the instance list although this is
 * conservative and will keep any instances of existing unchanged Class objects
 * rather than creating new ones.
 * 
 * @author Tom Oinn
 * 
 * @param <IType>
 *            The interface type implemented by all instances of the SPI to be
 *            tracked by this InstanceRegistry. This must be the type
 *            corresponding to the interface name specified in the SpiRegistry
 *            to which this binds.
 */
public class InstanceRegistry<IType> implements Iterable<IType>,
		RegistryListener {

	private static Log logger = Log.getLogger(InstanceRegistry.class);

	private List<InstanceRegistryListener> listeners = new ArrayList<InstanceRegistryListener>();
	private List<IType> instances = null;
	private Object[] cArgs;
	@SuppressWarnings("unchecked")
	private Class[] cArgTypes;
	private SpiRegistry registry;

	/**
	 * Build a new InstanceRegistry which will listen to events from an
	 * underlying SpiRegistry and automatically construct a single instance of
	 * each class found by that registry using the specified arguments to the
	 * constructor.
	 * 
	 * @param registry
	 *            The SpiRegistry which provides Class objects implementing the
	 *            generic type defined here.
	 * @param cArgs
	 *            Arguments for the constructors of the new objects
	 * @throws ClassCastException
	 *             if the underlying classname for the SpiRegistry and the
	 *             declared generic type for this object don't match.
	 */
	public InstanceRegistry(SpiRegistry registry, Object[] cArgs) {
		/**
		 * try { //Class spiClass = Class.forName(registry.getClassName());
		 * Class genericClass =
		 * Class.class.cast(this.getClass().getTypeParameters()[0]);
		 * 
		 * if (! genericClass.equals(spiClass)) { ClassCastException cce = new
		 * ClassCastException("Generic type doesn't match SPI classname"); throw
		 * cce; } } catch (ClassNotFoundException e) { ClassCastException cce =
		 * new ClassCastException("Unable to locate SPI Class");
		 * cce.initCause(e); throw cce; }
		 */
		this.cArgs = cArgs;
		this.registry = registry;
		this.cArgTypes = new Class[cArgs.length];
		for (int i = 0; i < cArgs.length; i++) {
			cArgTypes[i] = cArgs[i].getClass();
		}
		registry.addRegistryListener(this);
	}

	/**
	 * Add a new registry listener to be notified of any updates to this
	 * SpiRegistry
	 * 
	 * @param l
	 */
	public void addRegistryListener(InstanceRegistryListener l) {
		synchronized (listeners) {
			if (!listeners.contains(l)) {
				listeners.add(l);
			}
		}
	}

	@Override
	public void finalize() {
		registry.removeRegistryListener(this);
	}

	/**
	 * Return a copy of the List of instances of classes within the underlying
	 * SpiRegistry
	 * 
	 * @return instance List
	 */
	public synchronized List<IType> getInstances() {
		if (instances == null) {
			update(registry.getClasses());
		}
		logger.debug("getInstances called, contains " + instances.size()
				+ " instances of " + registry.getClassName());
		return new ArrayList<IType>(instances);
	}

	/**
	 * Returns an iterator over a copy of the instance list to avoid potential
	 * concurrent modification exceptions when update events occur in the
	 * underlying registry.
	 */
	public Iterator<IType> iterator() {
		return getInstances().iterator();
	}

	/**
	 * Remove a listener from this SpiRegistry
	 * 
	 * @param l
	 */
	public void removeRegistryListener(InstanceRegistryListener l) {
		synchronized (listeners) {
			listeners.remove(l);
		}
	}

	/**
	 * If the instance list exists then update it, if set to null then we don't
	 * need to as it will be updated automatically when the list is first
	 * accessed through the getInstances method
	 */
	public void spiRegistryUpdated(SpiRegistry registry) {
		if (instances != null) {
			update(registry.getClasses());
		}
	}

	private void notifyListeners() {
		synchronized (listeners) {
			for (InstanceRegistryListener rl : listeners) {
				rl.instanceRegistryUpdated(this);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private synchronized void update(List<Class> classList) {
		boolean changed = false;
		if (instances == null) {
			instances = new ArrayList<IType>();
			changed = true;
		}
		// First copy any existing instances we have for a Class
		// in the list to the temp array, removing the Class
		// for that instance from the classes List.
		List<IType> temp = new ArrayList<IType>();
		List<Class> classes = new ArrayList<Class>(classList);
		for (IType o : instances) {
			if (classes.contains(o.getClass())) {
				temp.add(o);
				classes.remove(o.getClass());
			} else {
				// Instance not found in the new list so
				// the registry state must change
				changed = true;
			}
		}
		// Iterate over any remaining entries in the classes List
		// and attempt to construct new instances from them.
		for (Class c : classes) {
			try {
				try {
					Method m = c.getMethod("getInstance", new Class[] {});
					if (m.getDeclaringClass().equals(c)) {
						logger.info("Invoking static getInstance() for " + c);
						IType newInstance = (IType) m.invoke(null,
								new Object[] {});
						temp.add(newInstance);
						changed = true;
						// Continue, don't try constructor
						continue;
					}
				} catch (NoSuchMethodException e) {
					// OK, we'll try the normal constructor
				}
				Constructor con = c.getConstructor(cArgTypes);
				IType newInstance = (IType) con.newInstance(cArgs);
				temp.add(newInstance);
				changed = true;
			} catch (NoSuchMethodException e) {
				logger.warn("Could not instantiate, no constructor " + c, e);
			} catch (SecurityException e) {
				logger.warn("Could not instantiate, security restriction " + c,
						e);
			} catch (IllegalArgumentException e) {
				logger.warn("Could not instantiate, invalid argument " + c, e);
			} catch (InstantiationException e) {
				logger.warn("Could not instantiate " + c, e);
			} catch (IllegalAccessException e) {
				logger.warn("Could not instantiate, no access " + c, e);
			} catch (InvocationTargetException e) {
				logger.warn("Failed while instantiating "
						+ "through constructor " + c, e.getCause());
			} catch (ClassCastException e) {
				TypeVariable spi = getClass().getTypeParameters()[0];
				logger.error("Declared as implementation of the SPI "
						+ spi.getName() + ", but was not: " + c, e);
			} catch (LinkageError e) {
				logger.error("Could not instantiate " + c, e);
			} catch (RuntimeException e) {
				logger.warn("Could not instantiate " + c, e);
			}
		}
		if (changed) {
			instances = temp;
			notifyListeners();
		}

	}

}
