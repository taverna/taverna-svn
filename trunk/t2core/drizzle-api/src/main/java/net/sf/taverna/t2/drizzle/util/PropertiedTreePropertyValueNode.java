/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

/**
 * @author alanrw
 *
 */
public interface PropertiedTreePropertyValueNode<O> extends PropertiedTreeNode<O> {
	
	PropertyKey getKey();
	void setKey (final PropertyKey key);
	PropertyValue getValue();
	void setValue (final PropertyValue value);

}
