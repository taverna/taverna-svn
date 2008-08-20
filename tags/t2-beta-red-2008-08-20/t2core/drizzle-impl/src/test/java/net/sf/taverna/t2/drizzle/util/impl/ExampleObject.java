/**
 * 
 */
package net.sf.taverna.t2.drizzle.util.impl;

import net.sf.taverna.t2.util.beanable.Beanable;

/**
 * @author alanrw
 *
 */
public class ExampleObject implements Comparable<Object>, Beanable<ExampleObject>{
	private static int objectCount = 0;
	
	private int i;
	
	/**
	 * Construct an ExampleObject
	 */
	public ExampleObject() {
		this.i = objectCount++;
	}
	
	/**
	 * Return the integer identifying the ExampleObject
	 * 
	 * @return
	 */
	public int getI() {
		return this.i;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals (Object o) {
		if (o instanceof ExampleObject) {
			return ((ExampleObject)o).getI() == this.i;
		}
		return false;
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
			throw new ClassCastException ("Argument is not an ExampleObject"); //$NON-NLS-1$
		}
		return result;	
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "ExampleObject" + Integer.toString(getI()); //$NON-NLS-1$
	}

	public ExampleObject getAsBean() {
		return this;
	}

	public void setFromBean(ExampleObject bean) throws IllegalArgumentException {
		this.i = bean.getI();
	}
}
