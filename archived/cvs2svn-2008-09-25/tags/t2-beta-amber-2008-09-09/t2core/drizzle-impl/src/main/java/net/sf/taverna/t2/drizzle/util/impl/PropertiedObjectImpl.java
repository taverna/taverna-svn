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
		this.properties = new HashMap<PropertyKey, PropertyValue>();
		this.listeners = new HashSet<PropertiedObjectListener>();
		this.object = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void addListener(final PropertiedObjectListener pol) {
		if (pol == null) {
			throw new NullPointerException("pol cannot be null"); //$NON-NLS-1$
		}
		this.listeners.add(pol);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasProperty(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		return this.properties.containsKey(key);
	}

	/**
	 * {@inheritDoc}
	 */
	public O getObject() {
		return this.object;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<PropertyKey> getPropertyKeys() {
		// Create new Set just to be on the safe side
		return new HashSet<PropertyKey>(this.properties.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	public PropertyValue getPropertyValue(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		PropertyValue result = null;
		if (this.properties.containsKey(key)) {
			result = this.properties.get(key);
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
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null"); //$NON-NLS-1$
		}
		PropertiedObjectListener[] copy = this.listeners
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
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		if (oldValue == null) {
			throw new NullPointerException("oldValue cannot be null"); //$NON-NLS-1$
		}
		if (newValue == null) {
			throw new NullPointerException("newValue cannot be null"); //$NON-NLS-1$
		}
		PropertiedObjectListener[] copy = this.listeners
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
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null"); //$NON-NLS-1$
		}
		PropertiedObjectListener[] copy = this.listeners
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
			throw new NullPointerException("pol cannot be null"); //$NON-NLS-1$
		}
		this.listeners.remove(pol);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeProperty(final PropertyKey key) {
		if (key == null) {
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		if (this.properties.containsKey(key)) {
			PropertyValue value = this.properties.get(key);
			this.properties.remove(key);
			notifyListenersPropertyRemoved(key, value);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProperty(final PropertyKey key, final PropertyValue value) {
		if (key == null) {
			throw new NullPointerException("key cannot be null"); //$NON-NLS-1$
		}
		if (value == null) {
			throw new NullPointerException("value cannot be null"); //$NON-NLS-1$
		}
		if (!this.properties.containsKey(key)) {
			this.properties.put(key, value);
			notifyListenersPropertyAdded(key, value);
		} else {
			PropertyValue oldValue = this.properties.get(key);
			this.properties.put(key, value);
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
		HashMapBean<PropertyKey, PropertyValue> beanedProperties =
			new HashMapBean((HashMap<PropertyKey, PropertyValue>) this.properties
				.clone());

		result.setProperties(beanedProperties);

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public void setFromBean(PropertiedObjectBean bean)
			throws IllegalArgumentException {
		if ((this.properties.size() != 0) || (this.listeners.size() != 0)) {
			throw new IllegalStateException("Cannot initialize twice"); //$NON-NLS-1$
		}

		this.properties = new HashMap<PropertyKey, PropertyValue>();
		HashMapBean<PropertyKey, PropertyValue> beanedProperties = bean.getProperties();
		for (HashMapEntryBean<PropertyKey, PropertyValue> entryBean : beanedProperties.getEntry()) {
			this.properties.put(entryBean.getKey(), entryBean.getValue());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setObject(O object) {
		if (object == null) {
			throw new NullPointerException("object cannot be null"); //$NON-NLS-1$
		}
		if (this.object != null) {
			throw new IllegalStateException("Cannot initialize twice"); //$NON-NLS-1$
		}
		this.object = object;
	}

	/**
	 * {@inheritDoc}
	 */
	public void replayToListener(PropertiedObjectListener pol) {
		if (pol == null) {
			throw new NullPointerException("pol cannot be null"); //$NON-NLS-1$
		}

		for (PropertyKey key : this.properties.keySet()) {
			PropertyValue value = this.properties.get(key);
			pol.propertyAdded(this.object, key, value);
		}
	}


	@SuppressWarnings("unchecked")
	@Override
	public boolean equals (Object o) {
		if (o instanceof PropertiedObject) {
			return ((PropertiedObject)o).getObject().equals(this.getObject());
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(Object arg0) {
		int result = 0;
		if (arg0 instanceof PropertiedObject) {
			PropertiedObject<?> argPo = (PropertiedObject) arg0;
			Object arg0Object = argPo.getObject();
			if (getObject() instanceof Comparable) {
				Comparable<Object> cObject = (Comparable<Object>) getObject();
			result = cObject.compareTo(arg0Object);
			}
		}
		else {
			throw new ClassCastException ("Argument is not a PropertiedObject"); //$NON-NLS-1$
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return getObject().hashCode();
	}
}
