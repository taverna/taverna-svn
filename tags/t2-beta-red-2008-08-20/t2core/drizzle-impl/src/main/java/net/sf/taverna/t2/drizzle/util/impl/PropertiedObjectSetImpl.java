/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.bean.HashMapBean;
import net.sf.taverna.t2.drizzle.bean.HashMapEntryBean;
import net.sf.taverna.t2.drizzle.bean.PropertiedObjectBean;
import net.sf.taverna.t2.drizzle.bean.PropertiedObjectSetBean;
import net.sf.taverna.t2.drizzle.util.PropertiedObject;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectListener;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSetListener;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * PropertiedObjectSetImpl is an implementation of the PropertiedObjectSet
 * interface.
 * 
 * @author alanrw
 * 
 * @param <O> The class of object within the PropertiedObjectSet.
 */
public final class PropertiedObjectSetImpl<O extends Beanable<?>> implements PropertiedObjectSet<O> {

	/**
	 * The propertiedObjectMap maps between objects and the corresponding
	 * PropertiedObjects.
	 */
	private HashMap<O, PropertiedObject<O>> propertiedObjectMap;

	/**
	 * listeners contains the PropertiedObjectSetListeners that listen to the
	 * addition or removal of an object and its correspodning PropertiedObject
	 * from the PropertiedObjectSetImpl.
	 */
	private HashSet<PropertiedObjectSetListener> listeners;

	/**
	 * objectListeners contain the PropertiedObjectListeners that listen to all
	 * changes in the properties of the PropertiedObjects within thr
	 * PropertiedObjectSetImpl.
	 */
	private HashSet<PropertiedObjectListener> objectListeners;

	/**
	 * Construct a PropertiedObjectSetImpl.
	 */
	public PropertiedObjectSetImpl() {
		super();

		this.propertiedObjectMap = new HashMap<O, PropertiedObject<O>>();
		this.listeners = new HashSet<PropertiedObjectSetListener>();
		this.objectListeners = new HashSet<PropertiedObjectListener>();

	}

	/**
	 * {@inheritDoc}
	 */
	public void addAllObjectsListener(final PropertiedObjectListener pol) {
		if (pol == null) {
			throw new NullPointerException("pol cannot be null"); //$NON-NLS-1$
		}
		@SuppressWarnings("unchecked")
		PropertiedObject<O>[] copy = this.propertiedObjectMap.values().toArray(
				new PropertiedObject[0]);
		for (PropertiedObject<O> po : copy) {
			po.addListener(pol);
		}
		this.objectListeners.add(pol);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addListener(final PropertiedObjectSetListener posl) {
		if (posl == null) {
			throw new NullPointerException("posl cannot be null"); //$NON-NLS-1$
		}
		this.listeners.add(posl);
	}

	/**
	 * {@inheritDoc}
	 */
	public void addObject(final O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null"); //$NON-NLS-1$
		}
		PropertiedObject<O> result = null;
		if (!this.propertiedObjectMap.containsKey(object)) {
			result = new PropertiedObjectImpl<O>();
			result.setObject(object);
			this.propertiedObjectMap.put(object, result);
			notifyListenersObjectAdded(object);

			for (PropertiedObjectListener pol : this.objectListeners) {
				result.addListener(pol);
			}
		} else {
			Set<O> keySet = this.propertiedObjectMap.keySet();
			if (!keySet.contains(object)) {
				throw new IllegalStateException ("Something is very funny here"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean containsObject(final O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null"); //$NON-NLS-1$
		}
		return this.propertiedObjectMap.containsKey(object);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PropertyKey> getAllPropertyKeys() {
		HashSet<PropertyKey> result = new HashSet<PropertyKey>();
		for (PropertiedObject<O> po : this.propertiedObjectMap.values()) {
			result.addAll(po.getPropertyKeys());
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PropertyValue> getAllPropertyValues(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		Set<PropertyValue> result = new HashSet<PropertyValue>();
		for (PropertiedObject<O> po : this.propertiedObjectMap.values()) {
			if (po.hasProperty(key)) {
				result.add(po.getPropertyValue(key));
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<O> getObjects() {
		// Copy to be on the safe side
		return new HashSet<O>(this.propertiedObjectMap.keySet());
	}

	private PropertiedObject<O> getPropertiedObject(final O object) {
		if (object == null) {
			throw new NullPointerException ("object cannot be null"); //$NON-NLS-1$
		}
		return this.propertiedObjectMap.get(object);
	}

	/**
	 * Notify the listeners of the addition of an object and its corresponding
	 * PropertiedObject to the PropertiedObjectSetImpl.
	 * 
	 * It is not clear if this should appear in the PropertiedObjectSet
	 * interface.
	 * 
	 * @param object
	 */
	private void notifyListenersObjectAdded(final O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null"); //$NON-NLS-1$
		}
		PropertiedObjectSetListener[] copy = this.listeners
				.toArray(new PropertiedObjectSetListener[0]);
		for (PropertiedObjectSetListener l : copy) {
			l.objectAdded(this, object);
		}
	}

	/**
	 * Notify the listeners of the removal of an object and its corresponding
	 * PropertiedObject from the propertiedObjectSetImpl.
	 * 
	 * It is not clear if this should appear in the PropertiedObjectSet
	 * interface.
	 * 
	 * @param object
	 */
	private void notifyListenersObjectRemoved(final O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null"); //$NON-NLS-1$
		}
		PropertiedObjectSetListener[] copy = this.listeners
				.toArray(new PropertiedObjectSetListener[0]);
		for (PropertiedObjectSetListener l : copy) {
			l.objectRemoved(this, object);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeAllObjectsListener(final PropertiedObjectListener pol) {
		if (pol == null) {
			throw new NullPointerException("pol cannot be null"); //$NON-NLS-1$
		}
		if (this.objectListeners.contains(pol)) {
			@SuppressWarnings("unchecked")
			PropertiedObject<O>[] copy = this.propertiedObjectMap.values().toArray(
					new PropertiedObject[0]);
			for (PropertiedObject<O> po : copy) {
				po.removeListener(pol);
			}
			this.objectListeners.remove(pol);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListener(final PropertiedObjectSetListener posl) {
		if (posl == null) {
			throw new NullPointerException("posl cannot be null"); //$NON-NLS-1$
		}
		this.listeners.remove(posl);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeObject(final O key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		if (this.propertiedObjectMap.containsKey(key)) {
			this.propertiedObjectMap.remove(key);
			notifyListenersObjectRemoved(key);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeProperty(final O o, final PropertyKey key) {
		if (o == null) {
			throw new NullPointerException("o cannot be null"); //$NON-NLS-1$
		}
		if (key == null) {
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		addObject(o);
		PropertiedObject<O> po = getPropertiedObject(o);
		po.removeProperty(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProperty(O object, PropertyKey key, PropertyValue value) {
		if (object == null) {
			throw new NullPointerException("object cannot be null"); //$NON-NLS-1$
		}
		if (key == null) {
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null"); //$NON-NLS-1$
		}
		addObject(object);
		PropertiedObject<?> po = getPropertiedObject(object);
		po.setProperty(key, value);

	}

	/**
	 * {@inheritDoc}
	 */
	public PropertiedObjectSetBean<O> getAsBean() {
		PropertiedObjectSetBean<O> result = new PropertiedObjectSetBean<O>();
		HashMap<Object, PropertiedObjectBean> beanedPropertiedObjectMap =
			new HashMap<Object, PropertiedObjectBean> ();
		
		for (O o : getObjects()) {
			PropertiedObjectBean beanedPropertiedObject =
				getPropertiedObject(o).getAsBean();
			beanedPropertiedObjectMap.put(o.getAsBean(), beanedPropertiedObject);
		}
		HashMapBean<Object, PropertiedObjectBean> beanedHashMap =
			new HashMapBean<Object, PropertiedObjectBean>(beanedPropertiedObjectMap);
		result.setPropertiedObjectMap(beanedHashMap);
		
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void setFromBean(PropertiedObjectSetBean bean) throws IllegalArgumentException {
		if ((this.propertiedObjectMap.size() != 0) || (this.listeners.size() != 0) ||
				(this.objectListeners.size() != 0)) {
			throw new IllegalStateException("Cannot initialise twice"); //$NON-NLS-1$
		}
		HashMapBean<Object, PropertiedObjectBean> beanedPropertiedObjectMap =
			bean.getPropertiedObjectMap();
		for (HashMapEntryBean<Object, PropertiedObjectBean> entryBean: beanedPropertiedObjectMap.getEntry()) {
			Object beanObject = entryBean.getKey();
			
			// How to do this? object.setFromBean(beanObject);
//			this.addObject(object);
//			PropertiedObject<O> po = getPropertiedObject(object);
//			PropertiedObjectBean beanedPo = beanedPropertiedObjectMap.get(object);
//			po.setFromBean (beanedPo);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void replayToListener(PropertiedObjectSetListener posl) {
		if (posl == null) {
			throw new NullPointerException ("posl cannot be null"); //$NON-NLS-1$
		}
		for (O object : this.propertiedObjectMap.keySet()) {
			posl.objectAdded(this, object);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void replayToAllObjectsListener(PropertiedObjectListener pol) {
		for (PropertiedObject<O> po : this.propertiedObjectMap.values()) {
			po.replayToListener(pol);
		}
	}


	public Set<PropertyKey> getPropertyKeys(O object) {
		if (object == null) {
			throw new NullPointerException ("object cannot be null"); //$NON-NLS-1$
		}
		Set<PropertyKey> result = null;
		PropertiedObject<O> po = getPropertiedObject(object);
		if (po != null) {
			result = po.getPropertyKeys();
		}
		return result;
	}

	public PropertyValue getPropertyValue(O object, PropertyKey key) {
		if (object == null) {
			throw new NullPointerException ("object cannot be null"); //$NON-NLS-1$
		}
		if (key == null) {
			throw new NullPointerException ("key cannot be null"); //$NON-NLS-1$
		}
		PropertyValue result = null;
		PropertiedObject<O> po = getPropertiedObject(object);
		if (po != null) {
			result = po.getPropertyValue(key);
		}
		return result;
	}

	public boolean hasProperty(O object, PropertyKey key) {
		if (object == null) {
			throw new NullPointerException ("object cannot be null"); //$NON-NLS-1$
		}
		if (key == null) {
			throw new NullPointerException ("key cannot be null"); //$NON-NLS-1$
		}
		PropertiedObject<O> po = getPropertiedObject(object);
		boolean result = false;
		if (po != null) {
			result = po.hasProperty(key);
		}
		return result;
	}

}
