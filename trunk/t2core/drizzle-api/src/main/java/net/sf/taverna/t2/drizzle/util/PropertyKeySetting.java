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
