/**
 * 
 */
package net.sf.taverna.t2.drizzle.query;

import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertyKey;

/**
 * @author alanrw
 *
 */
/**
 * @author alanrw
 *
 * @param <O>
 */
public final class DecodeRunIdentification<O> {
	private Set<O> affectedObjects;
	private long timeOfRun;
	private Set<PropertyKey> propertyKeyProfile;
	/**
	 * @return the affectedObjects
	 */
	public synchronized final Set<O> getAffectedObjects() {
		return this.affectedObjects;
	}
	/**
	 * @param affectedObjects the affectedObjects to set
	 */
	public synchronized final void setAffectedObjects(Set<O> affectedObjects) {
		this.affectedObjects = affectedObjects;
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
