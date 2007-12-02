/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.util.ObjectFactory;
import net.sf.taverna.t2.drizzle.util.PropertiedGraphView;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectSet;

/**
 * @author alanrw
 *
 */
// TODO should this be forced to be a singleton?
public final class ActivityRegistry {
	private PropertiedObjectSet<ProcessorFactory> registry;
	private PropertiedGraphView<ProcessorFactory> graphView;
	
	@SuppressWarnings("unchecked")
	public ActivityRegistry() {
		this.registry = ObjectFactory.getInstance(PropertiedObjectSet.class);
		this.graphView = ObjectFactory.getInstance(PropertiedGraphView.class);
		this.graphView.setPropertiedObjectSet(this.registry);
	}

	public synchronized ActivityQueryRunIdentification addImmediateQuery(ActivityQuery<?> query) {
		if (query == null) {
			throw new NullPointerException("query cannot be null"); //$NON-NLS-1$
		}
		ActivityQueryRunIdentification ident =
			query.runQuery(this.registry);
		return ident;
	}

	/**
	 * @return the registry
	 */
	public synchronized final PropertiedObjectSet<ProcessorFactory> getRegistry() {
		return this.registry;
	}

	/**
	 * @return the graphView
	 */
	public synchronized final PropertiedGraphView<ProcessorFactory> getGraphView() {
		return this.graphView;
	}
}
