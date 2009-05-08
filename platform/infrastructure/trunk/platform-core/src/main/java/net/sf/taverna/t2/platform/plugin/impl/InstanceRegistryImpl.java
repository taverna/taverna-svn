package net.sf.taverna.t2.platform.plugin.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.platform.plugin.InstanceInitializer;
import net.sf.taverna.t2.platform.plugin.InstanceRegistry;
import net.sf.taverna.t2.platform.plugin.InstanceRegistryListener;
import net.sf.taverna.t2.platform.plugin.SPIRegistry;
import net.sf.taverna.t2.platform.plugin.SPIRegistryListener;

/**
 * Implementation of {@link InstanceRegistry}.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 * @see InstanceRegistry
 * @param <T>
 *            the type of object held in this registry
 */
public final class InstanceRegistryImpl<T> implements InstanceRegistry<T> {

	private Map<Class<T>, T> instances;
	private List<InstanceRegistryListener<T>> listeners = new ArrayList<InstanceRegistryListener<T>>();
	private final InstanceInitializer<T> instanceInitializer;
	private final SPIRegistry<T> spiRegistry;
	private Class<?>[] constructorTypes;
	private Object[] constructorArgs;

	public SPIRegistry<T> getSpiRegistry() {
		return spiRegistry;
	}
	
	private boolean firstUpdateDone = false;

	public InstanceRegistryImpl(final SPIRegistry<T> spiRegistry,
			final List<Object> constructorArgList) {
		this(spiRegistry, constructorArgList, null);
	}

	public InstanceRegistryImpl(final SPIRegistry<T> spiRegistry,
			final List<Object> constructorArgList,
			InstanceInitializer<T> instanceInitializer) {
		this.spiRegistry = spiRegistry;
		this.instanceInitializer = instanceInitializer;
		this.instances = new HashMap<Class<T>, T>();
		constructorTypes = new Class<?>[constructorArgList
				.size()];
		constructorArgs = new Object[constructorArgList.size()];
		for (int i = 0; i < constructorArgList.size(); i++) {
			constructorTypes[i] = constructorArgList.get(i).getClass();
			constructorArgs[i] = constructorArgList.get(i);
		}
		synchronized (this) {
			spiRegistry.addSPIRegistryListener(new SPIListener());
			// firstUpdate() done on demand
		}
	}

	private synchronized void firstUpdate() {
		if (firstUpdateDone) {
			return;
		}
		Set<Class<T>> currentMembership = new HashSet<Class<T>>();
		for (Class<T> spiClass : spiRegistry) {
			currentMembership.add(spiClass);
		}
		// Poke the update method in case there were already entries there
		update(new HashSet<Class<T>>(), new HashSet<Class<T>>(),
				currentMembership);
		firstUpdateDone = true;
	}

	public final synchronized void addInstanceRegistryListener(
			InstanceRegistryListener<T> listener) {
		listeners.add(listener);
	}

	public final synchronized void removeInstanceRegistryListener(
			InstanceRegistryListener<T> listener) {
		listeners.remove(listener);
	}

	private final synchronized void update(Set<Class<T>> entriesAdded,
			Set<Class<T>> entriesRemoved, Set<Class<T>> currentMembership) {

		// Remove any instances that corresponded to classes in the
		// removed set, keeping track in sets for instances removed and
		// added
		Set<T> instancesRemoved = new HashSet<T>();
		for (Class<T> removedClass : entriesRemoved) {
			T instanceToRemove = instances.get(removedClass);
			if (instanceToRemove != null) {
				instances.remove(removedClass);
				instancesRemoved.add(instanceToRemove);
			}
		}
		// Now find any new classes, constructing new instances
		// appropriately. We always check the current membership set
		// here rather than the 'entries added' because we might be
		// initialized when there are already entries in the SPI and we
		// want to pick up the current set not necessarily the changes
		Set<T> instancesAdded = new HashSet<T>();
		for (Class<T> currentClass : currentMembership) {
			// Check whether we already have an instance for this class
			if (!instances.containsKey(currentClass)) {
				try {
					Constructor<T> constructor = currentClass
							.getConstructor(constructorTypes);
					T newInstance = constructor.newInstance(constructorArgs);
					if (instanceInitializer != null) {
						try {
							instanceInitializer.initialize(newInstance);
						} catch (RuntimeException ex) {
							ex.printStackTrace();
							continue;
						}
					}
					instances.put(currentClass, newInstance);
					instancesAdded.add(newInstance);
				} catch (NoSuchMethodException e) {
					// Thrown if we can't locate an appropriate
					// constructor for an SPI class entry
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// Unable to call constructor
					e.printStackTrace();
				} catch (InstantiationException e) {
					// Unable to call constructor
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// Unable to call constructor due to security
					// violation
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// Unable to call constructor
					e.printStackTrace();
				}
			}
		}
		firstUpdateDone = true;
		notifyListeners(instancesAdded, instancesRemoved, new HashSet<T>(
				instances.values()));
	}

	private void notifyListeners(Set<T> instancesAdded,
			Set<T> instancesRemoved, Set<T> currentInstances) {
		for (InstanceRegistryListener<T> listener : listeners) {
			listener.spiMembershipChanged(instancesAdded, instancesRemoved,
					currentInstances);
		}

	}

	public Iterator<T> iterator() {
		if (! firstUpdateDone) {
			firstUpdate();
		}
		return new HashSet<T>(instances.values()).iterator();
	}

	private final class SPIListener implements SPIRegistryListener<T> {
		public synchronized void spiMembershipChanged(
				Set<Class<T>> entriesAdded, Set<Class<T>> entriesRemoved,
				Set<Class<T>> currentMembership) {
			if (! firstUpdateDone) {
				firstUpdate();
			}
			update(entriesAdded, entriesRemoved, currentMembership);
		}
	}

}
