/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;




/**
 * @author alanrw
 *
 */
public final class ActivityQueryRunIdentification extends ActivitySubsetIdentification {
	private long timeOfRun;
	/**
	 * @return the timeOfRun
	 */
	public synchronized final long getTimeOfRun() {
		return this.timeOfRun;
	}
	/**
	 * @param timeOfRun the timeOfRun to set
	 */
	public synchronized final void setTimeOfRun(long timeOfRun) {
		this.timeOfRun = timeOfRun;
	}
	
	/**
	 * @see net.sf.taverna.t2.drizzle.model.ActivitySubsetIdentification#clearSubset()
	 */
	@Override
	public void clearSubset() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see net.sf.taverna.t2.drizzle.model.ActivitySubsetIdentification#addOredFilter(net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter)
	 */
	@Override
	public void addOredFilter(PropertiedObjectFilter<ProcessorFactoryAdapter> additionalFilter) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @see net.sf.taverna.t2.drizzle.model.ActivitySubsetIdentification#addAndedFilter(net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter)
	 */
	@Override
	public void addAndedFilter(PropertiedObjectFilter<ProcessorFactoryAdapter> additionalFilter) {
		throw new UnsupportedOperationException();
	}
}
