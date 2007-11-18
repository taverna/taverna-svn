/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

/**
 * @author alanrw
 *
 */
// TODO should this be forced to be a singleton?
public final class ActivityRegistry {
	private PropertiedObjectSet<ProcessorFactory> registry;
	
	public ActivityRegistry() {
		this.registry = ObjectFactory.getInstance(PropertiedObjectSet.class);
	}

	public synchronized ActivityQueryRunIdentification addImmediateQuery(ActivityQuery<?> query) {
		ActivityQueryRunIdentification ident =
			query.runQuery(this.registry);
		return ident;
	}
}
