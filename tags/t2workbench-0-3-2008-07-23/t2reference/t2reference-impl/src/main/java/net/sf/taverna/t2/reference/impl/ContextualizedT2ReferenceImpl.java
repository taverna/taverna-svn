package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.ContextualizedT2Reference;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * Simple implementation of ContextualizedT2Reference
 * 
 * @author Tom Oinn
 * 
 */
public class ContextualizedT2ReferenceImpl implements ContextualizedT2Reference {

	private T2Reference reference;
	private int[] index;

	public ContextualizedT2ReferenceImpl(T2Reference ref, int[] context) {
		this.reference = ref;
		this.index = context;
	}

	public int[] getIndex() {
		return this.index;
	}

	public T2Reference getReference() {
		return this.reference;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		boolean doneFirst = false;
		for (int i = 0; i < index.length; i++) {
			if (doneFirst) {
				sb.append(",");
			}
			doneFirst = true;
			sb.append(index[i]);
		}
		sb.append("]");
		sb.append(reference.toString());		
		return sb.toString();
	}
	
}
