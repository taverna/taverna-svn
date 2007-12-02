/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public final class ActivityRegistrySubsetModel {
	private ActivityRegistry parentRegistry = null;
	
	private PropertiedObjectFilter<ProcessorFactory> filter = null;
	private String name;
	private Set<PropertyKey> propertyKeyProfile = null;

	/**
	 * @return the name
	 */
	public synchronized final String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public synchronized final void setName(final String name) {
		if (name == null) {
			throw new NullPointerException("name cannot be null"); //$NON-NLS-1$
		}
		this.name = name;
	}

	/**
	 * @return the filter
	 */
	public synchronized final PropertiedObjectFilter<ProcessorFactory> getFilter() {
		return this.filter;
	}

	/**
	 * @param filter the filter to set
	 */
	public synchronized final void setFilter(final PropertiedObjectFilter<ProcessorFactory> filter) {
		if (filter == null) {
			throw new NullPointerException("filter cannot be null"); //$NON-NLS-1$
		}
		this.filter = filter;
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
	public synchronized final void setPropertyKeyProfile(final 
			Set<PropertyKey> propertyKeyProfile) {
		if (propertyKeyProfile == null) {
			throw new NullPointerException("propertyKeyProfile cannot be null"); //$NON-NLS-1$
		}
		this.propertyKeyProfile = propertyKeyProfile;
	}

	/**
	 * @return the parentRegistry
	 */
	public synchronized final ActivityRegistry getParentRegistry() {
		return this.parentRegistry;
	}

	/**
	 * @param parentRegistry the parentRegistry to set
	 */
	public synchronized final void setParentRegistry(ActivityRegistry parentRegistry) {
		if (parentRegistry == null) {
			throw new NullPointerException("parentRegistry cannot be null"); //$NON-NLS-1$
		}
		this.parentRegistry = parentRegistry;
	}

}
