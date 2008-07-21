/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * @author alanrw
 *
 */
public class StringObject implements Comparable<Object>, Beanable<StringObjectBean> {
	private String objectString;
	
	/**
	 * Construct a new StringObject
	 * 
	 * @param objectString
	 */
	public StringObject(final String objectString) {
		this.objectString = objectString;
	}
	
	/**
	 * Return the String that identifies the StringObject
	 * 
	 * @return
	 */
	public String getString() {
		return this.objectString;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals (Object o) {
		if (o instanceof StringObject) {
			return ((StringObject)o).getString().equals(this.objectString);
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object o) {
		int result = 0;
		if (o instanceof StringObject) {
			StringObject exampleArg = (StringObject) o;
			result = getString().compareTo(exampleArg.getString());
		}
		else {
			throw new ClassCastException ("Argument is not a StringObject"); //$NON-NLS-1$
		}
		return result;	
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return getString();
	}

	public StringObjectBean getAsBean() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFromBean(StringObjectBean bean)
			throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}
}
