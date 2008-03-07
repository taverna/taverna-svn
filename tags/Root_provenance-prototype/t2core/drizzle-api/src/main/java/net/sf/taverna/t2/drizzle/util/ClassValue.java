/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * @author alanrw
 *
 */
@XmlRootElement(namespace = "http://taverna.sf.net/t2/drizzle/util/", name = "classValue")
@XmlType(namespace = "http://taverna.sf.net/t2/drizzle/util/", name = "classValue")
public class ClassValue implements PropertyValue, Comparable<Object> {
	private Class<?> value;
	
	/**
	 * Construct a new StringValue
	 * 
	 * @param value
	 */
	public ClassValue(final Class<?> value) {
		this.value = value;
	}
	
	/**
	 * Return the String that identifies the ClassValue
	 * 
	 * @return
	 */
	public Class<?> getValue() {
		return this.value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals (Object o) {
		if (o instanceof ClassValue) {
			return ((ClassValue)o).getValue().equals(this.value);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object arg0) {
		int result = 0;
		if (arg0 instanceof ClassValue) {
			ClassValue exampleArg = (ClassValue) arg0;
			result = getValue().getName().compareTo(exampleArg.getValue().getName());
		}
		else {
			throw new ClassCastException ("Argument is not a ClassValue"); //$NON-NLS-1$
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getValue().getName();
	}
}
