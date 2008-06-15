package net.sf.taverna.t2.reference;

/**
 * Interface for any object that has an associated T2Reference
 * 
 * @author Tom Oinn
 * 
 */
public interface Identified {

	/**
	 * Return an appropriately configured instance of T2Reference for this
	 * identified object.
	 * 
	 * @return the id of this object in the form of a T2Reference
	 */
	public T2Reference getId();

}
