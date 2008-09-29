/**
 * 
 */
package net.sf.taverna.t2.drizzle.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author alanrw
 *
 */
public final class ObjectMembershipFilter<O> implements PropertiedObjectFilter<O> {
	
	Set<O> members = null;
	
	public ObjectMembershipFilter (final Set<O> members) {
		this.members = new HashSet<O> (members);
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter#acceptObject(java.lang.Object)
	 */
	public boolean acceptObject(O object) {
		return this.members.contains(object);
	}

}
