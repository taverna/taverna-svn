/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import java.util.HashSet;

import net.sf.taverna.t2.drizzle.util.FalseFilter;
import net.sf.taverna.t2.drizzle.util.ObjectAndFilter;
import net.sf.taverna.t2.drizzle.util.ObjectOrFilter;
import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;

/**
 * @author alanrw
 *
 */
public final class ActivitySubsetSelectionIdentification extends
		ActivitySubsetIdentification {
	

	/**
	 * @see net.sf.taverna.t2.drizzle.model.ActivitySubsetIdentification#clearSubset()
	 */
	@Override
	public void clearSubset() {
		this.setObjectFilter(new FalseFilter<ProcessorFactoryAdapter>());
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.model.ActivitySubsetIdentification#addOredFilter(net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter)
	 */
	@Override
	public void addOredFilter(PropertiedObjectFilter<ProcessorFactoryAdapter> additionalFilter) {
		PropertiedObjectFilter<ProcessorFactoryAdapter> oldFilter = this.getObjectFilter();
		HashSet<PropertiedObjectFilter<ProcessorFactoryAdapter>> filters = new HashSet<PropertiedObjectFilter<ProcessorFactoryAdapter>>();
		filters.add(oldFilter);
		filters.add(additionalFilter);
		ObjectOrFilter<ProcessorFactoryAdapter> newFilter = new ObjectOrFilter<ProcessorFactoryAdapter>(filters);
		this.setObjectFilter(newFilter);
	}

	/**
	 * @see net.sf.taverna.t2.drizzle.model.ActivitySubsetIdentification#addAndedFilter(net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter)
	 */
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
