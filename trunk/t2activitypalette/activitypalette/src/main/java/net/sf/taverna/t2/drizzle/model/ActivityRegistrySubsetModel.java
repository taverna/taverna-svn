/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;

import java.util.Set;

import net.sf.taverna.t2.drizzle.util.PropertiedObjectFilter;
import net.sf.taverna.t2.drizzle.util.PropertyKey;

/**
 * @author alanrw
 *
 */
public final class ActivityRegistrySubsetModel {
	private ActivityRegistry parentRegistry = null;
	
	private ActivityRegistrySubsetIdentification ident = null;
	
	private boolean editable = false;
	
	private boolean updated = false;
	
	/**
	 * @return the updated
	 */
	public synchronized final boolean isUpdated() {
		return this.updated;
	}

	/**
	 * @param updated the updated to set
	 */
	public synchronized final void setUpdated(boolean updated) {
		this.updated = updated;
	}

	/**
	 * @return the name
	 */
	public synchronized final String getName() {
		return this.ident.getName();
	}

	/**
	 * @return the filter
	 */
	public synchronized final PropertiedObjectFilter<ProcessorFactoryAdapter> getFilter() {
		return this.ident.getObjectFilter();
	}

	/**
	 * @return the propertyKeyProfile
	 */
	public synchronized final Set<PropertyKey> getPropertyKeyProfile() {
		return this.ident.getPropertyKeyProfile();
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

	/**
	 * @return the ident
	 */
	public synchronized final ActivityRegistrySubsetIdentification getIdent() {
		return this.ident;
	}

	/**
	 * @param ident the ident to set
	 */
	public synchronized final void setIdent(
			ActivityRegistrySubsetIdentification ident) {
		this.ident = ident;
	}

	public void addOredFilter(PropertiedObjectFilter<ProcessorFactoryAdapter> additionalFilter) {
		this.ident.addOredFilter(additionalFilter);
		setUpdated(true);
	}

	/**
	 * @return the editable
	 */
	public synchronized final boolean isEditable() {
		return this.editable;
	}

	/**
	 * @param editable the editable to set
	 */
	public synchronized final void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void clearSubset() {
		this.ident.clearSubset();
	}

	public final void addAndedFilter(PropertiedObjectFilter<ProcessorFactoryAdapter> additionalFilter) {
		this.ident.addAndedFilter(additionalFilter);
		setUpdated(true);
	}

}
