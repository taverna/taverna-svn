/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

/**
 * @author alanrw
 *
 */
public class StringObject implements Comparable {
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
		return objectString;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals (Object o) {
		if (o instanceof StringObject) {
			return ((StringObject)o).getString().equals(objectString);
		} else {
			return false;
		}
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
			throw new ClassCastException ("Argument is not a StringObject");
		}
		return result;	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return getString();
	}
}
