package net.sf.taverna.t2.reference.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.reference.ReferenceContext;

/**
 * A trivial implementation of ReferenceContext, used if the context parameter
 * to any service method is null.
 * 
 * @author Tom Oinn
 * 
 */
public class EmptyReferenceContext implements ReferenceContext {

	/**
	 * Return an empty entity set for all queries.
	 */
	public <T> List<? extends T> getEntities(Class<T> arg0) {
		return new ArrayList<T>();
	}

}
