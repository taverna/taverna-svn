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
		this.setObjectFilter(new FalseFilter<ProcessorFactoryAdapter>());
	}

	@Override
	public void addOredFilter(PropertiedObjectFilter<ProcessorFactoryAdapter> additionalFilter) {
		PropertiedObjectFilter<ProcessorFactoryAdapter> oldFilter = this.getObjectFilter();
		HashSet<PropertiedObjectFilter<ProcessorFactoryAdapter>> filters = new HashSet<PropertiedObjectFilter<ProcessorFactoryAdapter>>();
		filters.add(oldFilter);
		filters.add(additionalFilter);
		ObjectOrFilter<ProcessorFactoryAdapter> newFilter = new ObjectOrFilter<ProcessorFactoryAdapter>(filters);
		this.setObjectFilter(newFilter);
	}

	@Override
	public void addAndedFilter(PropertiedObjectFilter<ProcessorFactoryAdapter> additionalFilter) {
		PropertiedObjectFilter<ProcessorFactoryAdapter> oldFilter = this.getObjectFilter();
		HashSet<PropertiedObjectFilter<ProcessorFactoryAdapter>> filters = new HashSet<PropertiedObjectFilter<ProcessorFactoryAdapter>>();
		filters.add(oldFilter);
		filters.add(additionalFilter);
		ObjectAndFilter<ProcessorFactoryAdapter> newFilter = new ObjectAndFilter<ProcessorFactoryAdapter>(filters);
		this.setObjectFilter(newFilter);
	}

}
