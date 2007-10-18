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
	
	public ExampleObject() {
		i = objectCount++;
	}
	
	public int getI() {
		return i;
	}
	
	public boolean equals (Object o) {
		if (o instanceof ExampleObject) {
			return ((ExampleObject)o).getI() == this.i;
		} else {
			return false;
		}
	}

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
	
	public String toString() {
		return Integer.toString(getI());
	}
}
