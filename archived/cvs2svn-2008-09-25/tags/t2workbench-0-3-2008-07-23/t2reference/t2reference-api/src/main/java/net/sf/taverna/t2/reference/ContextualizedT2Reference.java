package net.sf.taverna.t2.reference;

/**
 * Used by the {@link ReferenceService#traverseFrom(T2Reference, int)} when
 * traversing a collection structure. Each contextualized t2reference contains
 * the {@link T2Reference} along with an integer array index representing the
 * position of that reference within the traversal structure. The index [i<sub>0</sub>,i<sub>1</sub>,i<sub>2</sub>
 * ... i<sub>n</sub>] is interpreted such that the reference is located at
 * parent.get(i<sub>0</sub>).get(i<sub>1</sub>).get(i<sub>2</sub>)....get(i<sub>n</sub>).
 * If the index is empty then the T2Reference <em>is</em> the original
 * reference supplied to the {@link ReferenceService#traverseFrom(T2Reference, int) traverseFrom} method.
 * 
 * @author Tom Oinn
 * 
 */
public interface ContextualizedT2Reference {

	/**
	 * @return the T2Reference to which the associated index applies.
	 */
	public T2Reference getReference();
	
	/**
	 * @return the index of this T2Reference
	 */
	public int[] getIndex();
	
}
