/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * @author alanrw
 *
 */
@XmlRootElement(namespace = "http://taverna.sf.net/t2/drizzle/util/", name = "qNameValue")
@XmlType(namespace = "http://taverna.sf.net/t2/drizzle/util/", name = "qNameValue")
public class QNameValue implements PropertyValue, Comparable<Object> {
	private QName value;
	
	/**
	 * Construct a new StringValue
	 * 
	 * @param value
	 */
	public QNameValue(final QName value) {
		this.value = value;
	}
	
	/**
	 * Return the String that identifies the QNameValue
	 * 
	 * @return
	 */
	public QName getValue() {
		return this.value;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals (Object o) {
		if (o instanceof QNameValue) {
			return ((QNameValue)o).getValue().equals(this.value);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object arg0) {
		int result = 0;
		if (arg0 instanceof QNameValue) {
			QNameValue exampleArg = (QNameValue) arg0;
			result = getValue().getLocalPart().compareTo(exampleArg.getValue().getLocalPart());
		}
		else {
			throw new ClassCastException ("Argument is not a QNameValue"); //$NON-NLS-1$
		}
		return result;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getValue().getLocalPart();
	}
}
