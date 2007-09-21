/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import net.sf.taverna.t2.drizzle.bean.PropertiedObjectBean;
import net.sf.taverna.t2.drizzle.bean.PropertiedObjectSetBean;
import net.sf.taverna.t2.drizzle.util.PropertiedObject;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectListener;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSetListener;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * PropertiedObjectSetImpl is an implementation of the PropertiedObjectSet
 * interface.
 * 
 * @author alanrw
 * 
 */
public final class PropertiedObjectSetImpl<O> implements PropertiedObjectSet<O> {

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
	private TreeSet<PropertiedObjectSetListener> listeners;

	/**
	 * objectListeners contain the PropertiedObjectListeners that listen to all
	 * changes in the properties of the PropertiedObjects within thr
	 * PropertiedObjectSetImpl.
	 */
	private TreeSet<PropertiedObjectListener> objectListeners;

	/**
	 * Construct a PropertiedObjectSetImpl.
	 */
	public PropertiedObjectSetImpl() {
		super();

		propertiedObjectMap = new HashMap<O, PropertiedObject<O>>();
		listeners = new TreeSet<PropertiedObjectSetListener>();
		objectListeners = new TreeSet<PropertiedObjectListener>();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#addAllObjectsListener(net.sf.taverna.t2.service.util.PropertiedObjectListener)
	 */
	public void addAllObjectsListener(final PropertiedObjectListener pol) {
		if (pol == null) {
			throw new NullPointerException("pol cannot be null");
		}
		PropertiedObject[] copy = propertiedObjectMap.values().toArray(
				new PropertiedObject[0]);
		for (PropertiedObject po : copy) {
			po.addListener(pol);
		}
		objectListeners.add(pol);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#addListener(net.sf.taverna.t2.service.util.PropertiedObjectSetListener)
	 */
	public void addListener(final PropertiedObjectSetListener posl) {
		if (posl == null) {
			throw new NullPointerException("posl cannot be null");
		}
		listeners.add(posl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#addObject(java.lang.Object)
	 */
	public PropertiedObject addObject(final O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null");
		}
		PropertiedObject<O> result = null;
		if (!propertiedObjectMap.containsKey(object)) {
			result = new PropertiedObjectImpl<O>();
			result.setObject(object);
			propertiedObjectMap.put(object, result);
			notifyListenersObjectAdded(object);

			for (PropertiedObjectListener pol : objectListeners) {
				result.addListener(pol);
			}
		} else {
			result = propertiedObjectMap.get(object);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#containsObject(java.lang.Object)
	 */
	public boolean containsObject(final O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null");
		}
		return propertiedObjectMap.containsKey(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#containsPropertiedObject(net.sf.taverna.t2.service.util.PropertiedObject)
	 */
	public boolean containsPropertiedObject(final PropertiedObject po) {
		if (po == null) {
			throw new NullPointerException("po cannot be null");
		}
		return propertiedObjectMap.containsValue(po);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#getAllPropertyKeys()
	 */
	public Set<PropertyKey> getAllPropertyKeys() {
		TreeSet<PropertyKey> result = new TreeSet<PropertyKey>();
		for (PropertiedObject po : getPropertiedObjects()) {
			result.addAll(po.getPropertyKeys());
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#getAllPropertyValues(net.sf.taverna.t2.service.util.PropertyKey)
	 */
	public Set<PropertyValue> getAllPropertyValues(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		Set<PropertyValue> result = new TreeSet<PropertyValue>();
		for (PropertiedObject po : getPropertiedObjects()) {
			if (po.hasProperty(key)) {
				result.add(po.getPropertyValue(key));
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#getObjects()
	 */
	public Set<O> getObjects() {
		// Copy to be on the safe side
		return new TreeSet<O>(propertiedObjectMap.keySet());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#getPropertiedObject(java.lang.Object)
	 */
	public PropertiedObject getPropertiedObject(final O object) {
		return propertiedObjectMap.get(object);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#getPropertiedObjects()
	 */
	public Set<PropertiedObject> getPropertiedObjects() {
		// Copy to be on the safe side
		return new TreeSet<PropertiedObject>(propertiedObjectMap.values());
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
			throw new NullPointerException("object cannot be null");
		}
		PropertiedObjectSetListener[] copy = listeners
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
			throw new NullPointerException("object cannot be null");
		}
		PropertiedObjectSetListener[] copy = listeners
				.toArray(new PropertiedObjectSetListener[0]);
		for (PropertiedObjectSetListener l : copy) {
			l.objectRemoved(this, object);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#removeAllObjectsListener(net.sf.taverna.t2.service.util.PropertiedObjectListener)
	 */
	public void removeAllObjectsListener(final PropertiedObjectListener pol) {
		if (pol == null) {
			throw new NullPointerException("pol cannot be null");
		}
		if (objectListeners.contains(pol)) {
			PropertiedObject[] copy = propertiedObjectMap.values().toArray(
					new PropertiedObject[0]);
			for (PropertiedObject po : copy) {
				po.removeListener(pol);
			}
			objectListeners.remove(pol);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#removeListener(net.sf.taverna.t2.service.util.PropertiedObjectSetListener)
	 */
	public void removeListener(final PropertiedObjectSetListener posl) {
		if (posl == null) {
			throw new NullPointerException("posl cannot be null");
		}
		listeners.remove(posl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#removeObject(java.lang.Object)
	 */
	public void removeObject(final O key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		if (propertiedObjectMap.containsKey(key)) {
			propertiedObjectMap.remove(key);
			notifyListenersObjectRemoved(key);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#removeProperty(java.lang.Object,
	 *      net.sf.taverna.t2.service.util.PropertyKey)
	 */
	public void removeProperty(final O o, final PropertyKey key) {
		if (o == null) {
			throw new NullPointerException("o cannot be null");
		}
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		PropertiedObject po = addObject(o);
		po.removeProperty(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.taverna.t2.service.util.PropertiedObjectSet#setProperty(java.lang.Object,
	 *      net.sf.taverna.t2.service.util.PropertyKey,
	 *      net.sf.taverna.t2.service.util.PropertyValue)
	 */
	public void setProperty(O object, PropertyKey key, PropertyValue value) {
		if (object == null) {
			throw new NullPointerException("object cannot be null");
		}
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		PropertiedObject po = addObject(object);
		po.setProperty(key, value);

	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.cloudone.bean.Beanable#getAsBean()
	 */
	public PropertiedObjectSetBean getAsBean() {
		PropertiedObjectSetBean result = new PropertiedObjectSetBean();
		HashMap<Object, PropertiedObjectBean> beanedPropertiedObjectMap =
			new HashMap<Object, PropertiedObjectBean> ();
		
		for (O o : getObjects()) {
			PropertiedObjectBean beanedPropertiedObject =
				(PropertiedObjectBean) getPropertiedObject(o).getAsBean();
			beanedPropertiedObjectMap.put(o, beanedPropertiedObject);
		}
		result.setPropertiedObjectMap(beanedPropertiedObjectMap);
		
		return result;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.cloudone.bean.Beanable#setFromBean(java.lang.Object)
	 */
	public void setFromBean(PropertiedObjectSetBean bean) throws IllegalArgumentException {
		if ((propertiedObjectMap.size() != 0) || (listeners.size() != 0) ||
				(objectListeners.size() != 0)) {
			throw new IllegalStateException("Cannot initialise twice");
		}
		HashMap<O, PropertiedObjectBean> beanedPropertiedObjectMap =
			bean.getPropertiedObjectMap();
		for (O object : beanedPropertiedObjectMap.keySet()) {
			PropertiedObject po = this.addObject(object);
			PropertiedObjectBean beanedPo = beanedPropertiedObjectMap.get(object);
			po.setFromBean (beanedPo);
		}
	}

}
