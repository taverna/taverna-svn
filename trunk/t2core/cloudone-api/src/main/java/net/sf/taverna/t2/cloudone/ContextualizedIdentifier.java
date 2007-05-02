package net.sf.taverna.t2.cloudone;

/**
 * Bean holding a data reference and an index path denoting its context within
 * the traversal requested from the data manager
 * 
 * @author Tom Oinn
 * 
 */
public class ContextualizedIdentifier {

	private EntityIdentifier dataRef;
	private int[] index;
	
	public ContextualizedIdentifier(EntityIdentifier dataRef, int[] index) {
		this.dataRef = dataRef;
		this.index = index;
	}
	
	public EntityIdentifier getDataRef() {
		return this.dataRef;
	}
	
	public int[] getIndex() {
		return this.index;
	}
	
}
