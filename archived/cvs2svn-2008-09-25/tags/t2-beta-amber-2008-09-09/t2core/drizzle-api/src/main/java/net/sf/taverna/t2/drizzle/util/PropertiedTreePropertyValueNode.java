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
 * @author alanrw
 * 
 */
/**
 * @author alanrw
 * 
 * A PropertiedTreePropertyValueNode encapsulates a PropertyKey + PropertyValue
 * pair of an object or the absence of a PropertyValue for a PropertyKey within
 * a PropertiedTreeModel.
 * @param <O>
 *            The class of Object to which the PropertyKey + PropertyValue
 *            apply.
 */
public interface PropertiedTreePropertyValueNode<O> extends
		PropertiedTreeNode<O> {

	/**
	 * Return the PropertyKey of the node.
	 * 
	 * @return
	 */
	PropertyKey getKey();

	/**
	 * Set the PropertyKey of the node.
	 * 
	 * @param key
	 */
	void setKey(final PropertyKey key);

	/**
	 * Return the PropertyValue, if any, of the node. null is returned if the
	 * leaf objects do not have a value for the specified PropertyKey.
	 * 
	 * @return
	 */
	PropertyValue getValue();

	/**
	 * Set the PropertyValue of the node.
	 * 
	 * @param value
	 */
	void setValue(final PropertyValue value);

}
