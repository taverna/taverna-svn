/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * PropertyKey is an interface that all classes used as the key for property
 * values should implement.
 * 
 * It is possible that PropertyKey may be abandoned and Object used instead.
 * 
 * Classes that implement the PropertyKey interface must conform to the Bean
 * standard.
 * 
 * @author alanrw
 * 
 */
public interface PropertyKey {
	// Nothing in common
}
