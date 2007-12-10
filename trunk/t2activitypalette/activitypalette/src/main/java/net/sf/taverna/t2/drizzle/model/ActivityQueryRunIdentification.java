/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;




/**
 * @author alanrw
 *
 */
public final class ActivityQueryRunIdentification extends ActivityRegistrySubsetIdentification {
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
	@Override
	public void clearSubset() {
		throw new UnsupportedOperationException();
	}
	@Override
	public void addOredFilter(PropertiedObjectFilter<ProcessorFactoryAdapter> additionalFilter) {
		throw new UnsupportedOperationException();
	}
	@Override
	public void addAndedFilter(PropertiedObjectFilter<ProcessorFactoryAdapter> additionalFilter) {
		throw new UnsupportedOperationException();
	}
}
