/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import java.util.HashSet;

import net.sf.taverna.t2.drizzle.util.FalseFilter;
import net.sf.taverna.t2.drizzle.util.ObjectAndFilter;
import net.sf.taverna.t2.drizzle.util.ObjectOrFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class ActivityRegistrySubsetSelectionIdentification extends
		ActivityRegistrySubsetIdentification {
	

	@Override
	public void clearSubset() {
		this.setObjectFilter(new FalseFilter<ProcessorFactory>());
	}

	@Override
	public void addOredFilter(PropertiedObjectFilter<ProcessorFactory> additionalFilter) {
		PropertiedObjectFilter<ProcessorFactory> oldFilter = this.getObjectFilter();
		HashSet<PropertiedObjectFilter<ProcessorFactory>> filters = new HashSet<PropertiedObjectFilter<ProcessorFactory>>();
		filters.add(oldFilter);
		filters.add(additionalFilter);
		ObjectOrFilter<ProcessorFactory> newFilter = new ObjectOrFilter<ProcessorFactory>(filters);
		this.setObjectFilter(newFilter);
	}

	@Override
	public void addAndedFilter(PropertiedObjectFilter<ProcessorFactory> additionalFilter) {
		PropertiedObjectFilter<ProcessorFactory> oldFilter = this.getObjectFilter();
		HashSet<PropertiedObjectFilter<ProcessorFactory>> filters = new HashSet<PropertiedObjectFilter<ProcessorFactory>>();
		filters.add(oldFilter);
		filters.add(additionalFilter);
		ObjectAndFilter<ProcessorFactory> newFilter = new ObjectAndFilter<ProcessorFactory>(filters);
		this.setObjectFilter(newFilter);
	}

}
