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
package net.sf.taverna.t2.drizzle.util;

/**
 * A PropertiedObjectListener listens for changes in the PropertyKey +
 * PropertyValue pairs associated with a PropertiedObject.
 * 
 * At the moment the methods take the Object rather than the PropertiedObject.
 * This decision will be reviewed when listeners have been tried in practice.
 * 
 * @author alanrw
 * 
 */
public interface PropertiedObjectListener {
	/**
	 * A PropertyValue has been added for the given PropertyKey to a
	 * PropertiedObject corresponding to the specified Object.
	 * 
	 * @param o
	 * @param key
	 * @param value
	 */
	void propertyAdded(Object o, PropertyKey key, PropertyValue value);

	/**
	 * The PropertyKey and its associated PropertyValue have been removed from a
	 * PropertiedObject corresponding to the specified Object.
	 * 
	 * @param o
	 * @param key
	 * @param value
	 */
	void propertyRemoved(Object o, PropertyKey key, PropertyValue value);

	/**
	 * The PropertyValue associated with the PropertyKey for the
	 * PropertiedObject corresponding to the specified Object has changed from
	 * the old to the new PropertyValue.
	 * 
	 * @param o
	 * @param key
	 * @param oldValue
	 * @param newValue
	 */
	void propertyChanged(final Object o, PropertyKey key,
			PropertyValue oldValue, PropertyValue newValue);
}
