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
