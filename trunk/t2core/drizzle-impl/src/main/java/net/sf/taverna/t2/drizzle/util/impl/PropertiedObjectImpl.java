/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.drizzle.bean.PropertiedObjectBean;
import net.sf.taverna.t2.drizzle.util.PropertiedObject;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectListener;
import net.sf.taverna.t2.drizzle.util.PropertyKey;
import net.sf.taverna.t2.drizzle.util.PropertyValue;

/**
 * @author alanrw
 * 
 */
/**
 * PropertiedObjectImpl is an implementation of the PropertiedObject interface.
 * 
 * Note that it is deliberate that a PropertiedObject can have more only one
 * PropertyValue for a given PropertyKey.
 * 
 * @param <O> The class of object encapsulated by the PropertiedObject.
 */
public final class PropertiedObjectImpl<O> implements PropertiedObject<O> {
	/**
	 * The properties HashMap maps the PropertyKeys to their associated
	 * PropertyValue.
	 */
	private HashMap<PropertyKey, PropertyValue> properties;

	/**
	 * object stores the Object to which the PropertiedObject corresponds.
	 */
	private O object;

	/**
	 * listeners contains the PropertiedObjectListeners that listen to the
	 * addition and removal of PropertyValues for PropertyKeys, or for a change
	 * in a PropertyValue associated with a PropertyKey,
	 */
	private HashSet<PropertiedObjectListener> listeners;

	/**
	 * Construct a PropertiedObject corresponding to the specified object.
	 * 
	 * @param object
	 */
	public PropertiedObjectImpl() {
		super();
		properties = new HashMap<PropertyKey, PropertyValue>();
		listeners = new HashSet<PropertiedObjectListener>();
		object = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addListener(final PropertiedObjectListener pol) {
		if (pol == null) {
			throw new NullPointerException("pol cannot be null");
		}
		listeners.add(pol);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasProperty(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		return properties.containsKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public O getObject() {
		return object;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PropertyKey> getPropertyKeys() {
		// Create new Set just to be on the safe side
		return new HashSet<PropertyKey>(properties.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyValue getPropertyValue(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		PropertyValue result = null;
		if (properties.containsKey(key)) {
			result = properties.get(key);
		}
		return result;
	}

	/**
	 * Notify the listeners of the addition of the PropertyValue for the
	 * specified PropertyKey.
	 * 
	 * @param key
	 * @param value
	 */
	private void notifyListenersPropertyAdded(final PropertyKey key,
			final PropertyValue value) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		PropertiedObjectListener[] copy = listeners
				.toArray(new PropertiedObjectListener[0]);
		for (PropertiedObjectListener l : copy) {
			l.propertyAdded(getObject(), key, value);
		}
	}

	/**
	 * Notify the listeners of a change in PropertyValue for the specified
	 * PropertyKey from oldValue to newValue.
	 * 
	 * @param key
	 * @param oldValue
	 * @param newValue
	 */
	private void notifyListenersPropertyChanged(final PropertyKey key,
			final PropertyValue oldValue, final PropertyValue newValue) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		if (oldValue == null) {
			throw new NullPointerException("oldValue cannot be null");
		}
		if (newValue == null) {
			throw new NullPointerException("newValue cannot be null");
		}
		PropertiedObjectListener[] copy = listeners
				.toArray(new PropertiedObjectListener[0]);
		for (PropertiedObjectListener l : copy) {
			l.propertyChanged(getObject(), key, oldValue, newValue);
		}
	}

	/**
	 * Notify the listeners of the removal of the PropertyKey and its associated
	 * PropertyValue.
	 * 
	 * @param key
	 * @param value
	 */
	private void notifyListenersPropertyRemoved(final PropertyKey key,
			final PropertyValue value) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		PropertiedObjectListener[] copy = listeners
				.toArray(new PropertiedObjectListener[0]);
		for (PropertiedObjectListener l : copy) {
			l.propertyRemoved(getObject(), key, value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListener(final PropertiedObjectListener pol) {
		if (pol == null) {
			throw new NullPointerException("pol cannot be null");
		}
		listeners.remove(pol);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeProperty(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		if (properties.containsKey(key)) {
			PropertyValue value = properties.get(key);
			properties.remove(key);
			notifyListenersPropertyRemoved(key, value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProperty(final PropertyKey key, final PropertyValue value) {
		if (key == null) {
			throw new NullPointerException("key cannot be null");
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null");
		}
		if (!properties.containsKey(key)) {
			properties.put(key, value);
			notifyListenersPropertyAdded(key, value);
		} else {
			PropertyValue oldValue = properties.get(key);
			properties.put(key, value);
			if (!oldValue.equals(value)) {
				notifyListenersPropertyChanged(key, oldValue, value);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public PropertiedObjectBean getAsBean() {
		PropertiedObjectBean result = new PropertiedObjectBean();
		HashMap<PropertyKey, PropertyValue> beanedProperties = (HashMap<PropertyKey, PropertyValue>) this.properties
				.clone();

		result.setProperties(beanedProperties);

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void setFromBean(PropertiedObjectBean bean)
			throws IllegalArgumentException {
		if ((properties.size() != 0) || (listeners.size() != 0)) {
			throw new IllegalStateException("Cannot initialize twice");
		}

		this.properties = (HashMap<PropertyKey, PropertyValue>) bean
				.getProperties().clone();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setObject(O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null");
		}
		if (this.object != null) {
			throw new IllegalStateException("Cannot initialize twice");
		}
		this.object = object;
	}

	/**
	 * {@inheritDoc}
	 */
	public void replayToListener(PropertiedObjectListener pol) {
		if (pol == null) {
			throw new NullPointerException("pol cannot be null");
		}

		for (PropertyKey key : properties.keySet()) {
			PropertyValue value = properties.get(key);
			pol.propertyAdded(this.object, key, value);
		}
	}

	public Class<PropertiedObjectBean> getBeanClass() {
		return PropertiedObjectBean.class;
	}

}
