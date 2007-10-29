/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

/**
 * @author alanrw
 *
 */
public class ExampleObject implements Comparable {
	private static int objectCount = 0;
	
	private int i;
	
	/**
	 * Construct an ExampleObject
	 */
	public ExampleObject() {
		i = objectCount++;
	}
	
	/**
	 * Return the integer identifying the ExampleObject
	 * 
	 * @return
	 */
	public int getI() {
		return i;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean equals (Object o) {
		if (o instanceof ExampleObject) {
			return ((ExampleObject)o).getI() == this.i;
		} else {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int compareTo(Object o) {
		int result = 0;
		if (o instanceof ExampleObject) {
			ExampleObject exampleArg = (ExampleObject) o;
			result = getI() - exampleArg.getI();
		}
		else {
			throw new ClassCastException ("Argument is not an ExampleObject");
		}
		return result;	
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return "ExampleObject" + Integer.toString(getI());
	}
}
