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
public final class ActivityRegistrySubsetModel {
	private ActivityRegistry parentRegistry = null;
	
	private ActivityRegistrySubsetIdentification ident = null;
	
	private boolean editable = false;
	
	/**
	 * @return the name
	 */
	public synchronized final String getName() {
		return ident.getName();
	}

	/**
	 * @return the filter
	 */
	public synchronized final PropertiedObjectFilter<ProcessorFactory> getFilter() {
		return ident.getObjectFilter();
	}

	/**
	 * @return the propertyKeyProfile
	 */
	public synchronized final Set<PropertyKey> getPropertyKeyProfile() {
		return ident.getPropertyKeyProfile();
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
		return ident;
	}

	/**
	 * @param ident the ident to set
	 */
	public synchronized final void setIdent(
			ActivityRegistrySubsetIdentification ident) {
		this.ident = ident;
	}

	public void addOredFilter(PropertiedObjectFilter<ProcessorFactory> additionalFilter) {
		ident.addOredFilter(additionalFilter);
	}

	/**
	 * @return the editable
	 */
	public synchronized final boolean isEditable() {
		return editable;
	}

	/**
	 * @param editable the editable to set
	 */
	public synchronized final void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void clearSubset() {
		ident.clearSubset();
	}

	public final void addAndedFilter(PropertiedObjectFilter<ProcessorFactory> additionalFilter) {
		ident.addAndedFilter(additionalFilter);
	}

}
