/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public abstract class ActivityRegistrySubsetIdentification {

	private String name;
	private PropertiedObjectFilter<ProcessorFactory> objectFilter;
	private Set<PropertyKey> propertyKeyProfile;
	private String kind;

	/**
	 * @return the kind
	 */
	public synchronized final String getKind() {
		return kind;
	}

	/**
	 * @param kind the kind to set
	 */
	public synchronized final void setKind(String kind) {
		this.kind = kind;
	}

	/**
	 * @return the name
	 */
	public final synchronized String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public final synchronized void setName(String name) {
		if (name == null) {
			throw new NullPointerException("name cannot be null"); //$NON-NLS-1$
		}
		this.name = name;
	}

	/**
	 * @return the objectFilter
	 */
	public final synchronized PropertiedObjectFilter<ProcessorFactory> getObjectFilter() {
		return this.objectFilter;
	}

	/**
	 * @param objectFilter the objectFilter to set
	 */
	public final synchronized void setObjectFilter(PropertiedObjectFilter<ProcessorFactory> objectFilter) {
		if (objectFilter == null) {
			throw new NullPointerException("objectFilter cannot be null"); //$NON-NLS-1$
		}
		this.objectFilter = objectFilter;
	}

	/**
	 * @return the propertyKeyProfile
	 */
	public final synchronized Set<PropertyKey> getPropertyKeyProfile() {
		return this.propertyKeyProfile;
	}

	/**
	 * @param propertyKeyProfile the propertyKeyProfile to set
	 */
	public final synchronized void setPropertyKeyProfile(Set<PropertyKey> propertyKeyProfile) {
		if (propertyKeyProfile == null) {
			throw new NullPointerException("propertyKeyProfile cannot be null"); //$NON-NLS-1$
		}
		this.propertyKeyProfile = propertyKeyProfile;
	}

	public abstract void clearSubset();

	public abstract void addOredFilter(PropertiedObjectFilter<ProcessorFactory> additionalFilter);

	public abstract void addAndedFilter(PropertiedObjectFilter<ProcessorFactory> additionalFilter);

}
