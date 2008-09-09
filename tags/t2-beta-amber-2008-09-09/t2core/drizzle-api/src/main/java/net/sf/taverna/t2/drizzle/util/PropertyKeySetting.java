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

import java.util.Comparator;

/**
 * @author alanrw
 * 
 */
public interface PropertyKeySetting {
	/**
	 * Return the PropertyKey used to order a given level of
	 * PropertiedTreePropertyValueNode within a PropertiedTreeModel.
	 * 
	 * @return
	 */
	PropertyKey getPropertyKey();

	/**
	 * Specify the PropertyKey used to order a given level of
	 * PropertiedTreePropertyValueNode within a PropertiedTreeModel.
	 * 
	 * @param propertyKey
	 */
	void setPropertyKey(final PropertyKey propertyKey);

	/**
	 * Return the Comparator, if any, used to collate
	 * PropertyTreePropertyValueNodes. null indicates that the natural ordering
	 * of the nodes is used.
	 * 
	 * @return
	 */
	Comparator<PropertyValue> getComparator();

	/**
	 * Specify the Comparator to be used to collate
	 * PropertyTreePropertyValueNodes.
	 * 
	 * @param comparator
	 */
	void setComparator(final Comparator<PropertyValue> comparator);
}
