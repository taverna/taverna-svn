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
