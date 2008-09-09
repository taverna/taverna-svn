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
package net.sf.taverna.t2.drizzle.util;

import java.util.Set;

import net.sf.taverna.t2.drizzle.bean.PropertiedObjectBean;
import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * A PropertiedObject is the association of an Object with a set of PropertyKey +
 * PropertyValue pairs within the context of a PropertiedObjectSet.
 * 
 * It is not clear if PropertiedObject should be exposed or be hidden within an
 * implementation package or even within the PropertiedObjectsSet. It is
 * possible that it may be moved later on.
 * 
 * @author alanrw
 * 
 * @param <O>
 */
public interface PropertiedObject<O> extends Beanable<PropertiedObjectBean> {

	/**
	 * Add a listener to the PropertiedObject. The PropertiedObjectListener
	 * listens for the addition or removal of a PropertyKey + PropertyValue
	 * pair, or for a change in a PropertyValue.
	 * 
	 * @param pol
	 */
	void addListener(final PropertiedObjectListener pol);

	/**
	 * Check if the Object corresponding to the PropertiedObject has a
	 * PropertyValue for the given PropertyKey.
	 * 
	 * @param key
	 * @return
	 */
	boolean hasProperty(final PropertyKey key);

	/**
	 * Return the Object to which the PropertiedObject corresponds.
	 * 
	 * @return the object
	 */
	O getObject();

	/**
	 * Return the Set of PropertyKeys for which the PropertiedObject has
	 * PropertyValues.
	 * 
	 * @return
	 */
	Set<PropertyKey> getPropertyKeys();

	/**
	 * Return the PropertyValue corresponding to the PropertyKey. If the
	 * PropertiedObject does not have a PropertyValue for the specified
	 * PropertyKey then null is returned.
	 * 
	 * @param key
	 * @return
	 */
	PropertyValue getPropertyValue(final PropertyKey key);

	/**
	 * Remove the PropertiedObjectListener from listening to the
	 * PropertiedObject.
	 * 
	 * @param pol
	 */
	void removeListener(final PropertiedObjectListener pol);

	/**
	 * Replay all the calls necessary to acquant a listener with the current
	 * state of the PropertiedObject.
	 * 
	 * Note that the calls are not necessarily the same as created the state.
	 */
	void replayToListener(final PropertiedObjectListener pol);
	
	/**
	 * Remove the PropertyKey and its associated PropertyValue from the
	 * PropertiedObject. It is not an error if the PropertiedObject does not
	 * have the specified PropertyKey.
	 * 
	 * If the PropertiedObject had a PropertiedValue for the PropertyKey, then
	 * its listeners are notified of the removal of the property.
	 * 
	 * @param key
	 */
	void removeProperty(final PropertyKey key);

	/**
	 * Set the object to which the PropertiedObject corresponds.
	 * 
	 * The object cannot be null.
	 * 
	 * The PropertiedObject can only have its object set once.
	 * 
	 * @param object
	 */
	void setObject(O object);

	/**
	 * Set the PropertyValue for the specified PropertyKey. It is not an error
	 * if the PropertiedObject already has a PropertyValue for the PropertyKey.
	 * 
	 * If the PropertiedObject did not already have a PropertyValue for the
	 * specified PropertyKey then its listeners are notified of the addition of
	 * the property.
	 * 
	 * If the PropertiedObject already had a PropertyValue for the specified
	 * PropertyKey then, unless the new and old PropertyValues are equal, its
	 * listeners are notified of the change in the PropertyValue
	 * 
	 * @param key
	 * @param value
	 */
	void setProperty(final PropertyKey key, final PropertyValue value);

}
