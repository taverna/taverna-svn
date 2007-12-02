/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.Set;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

/**
 * @author alanrw
 *
 */
public final class ActivityQueryRunIdentification {
	private String name;
	private PropertiedObjectFilter<ProcessorFactory> objectFilter;
	private long timeOfRun;
	private Set<PropertyKey> propertyKeyProfile;
	/**
	 * @return the name
	 */
	public synchronized final String getName() {
		return this.name;
	}
	/**
	 * @param name the name to set
	 */
	public synchronized final void setName(String name) {
		if (name == null) {
			throw new NullPointerException("name cannot be null"); //$NON-NLS-1$
		}
		this.name = name;
	}
	/**
	 * @return the objectFilter
	 */
	public synchronized final PropertiedObjectFilter<ProcessorFactory> getObjectFilter() {
		return this.objectFilter;
	}
	/**
	 * @param objectFilter the objectFilter to set
	 */
	public synchronized final void setObjectFilter(
			PropertiedObjectFilter<ProcessorFactory> objectFilter) {
		if (objectFilter == null) {
			throw new NullPointerException("objectFilter cannot be null"); //$NON-NLS-1$
		}
		this.objectFilter = objectFilter;
	}
	/**
	 * @return the propertyKeyProfile
	 */
	public synchronized final Set<PropertyKey> getPropertyKeyProfile() {
		return this.propertyKeyProfile;
	}
	/**
	 * @param propertyKeyProfile the propertyKeyProfile to set
	 */
	public synchronized final void setPropertyKeyProfile(
			Set<PropertyKey> propertyKeyProfile) {
		if (propertyKeyProfile == null) {
			throw new NullPointerException("propertyKeyProfile cannot be null"); //$NON-NLS-1$
		}
		this.propertyKeyProfile = propertyKeyProfile;
	}
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
}
