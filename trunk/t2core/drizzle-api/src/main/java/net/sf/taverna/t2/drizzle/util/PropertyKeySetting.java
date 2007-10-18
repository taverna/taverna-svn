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
	PropertyKey getPropertyKey();
	void setPropertyKey (final PropertyKey propertyKey);
	
	Comparator<PropertyValue> getComparator();
	void setComparator(final Comparator<PropertyValue> comparator);
}
