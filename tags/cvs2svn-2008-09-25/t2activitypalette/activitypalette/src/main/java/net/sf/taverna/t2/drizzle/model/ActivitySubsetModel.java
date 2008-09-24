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
public final class ActivitySubsetModel {
	private ActivitySetModel parentActivitySubsetModel = null;
	
	private ActivitySubsetIdentification ident = null;
	
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
	 * @return the parentActivitySubsetModel
	 */
	public synchronized final ActivitySetModel getParentActivitySubsetModel() {
		return this.parentActivitySubsetModel;
	}

	/**
	 * @param parentActivitySetModel the parentActivitySetModel to set
	 */
	public synchronized final void setParentActivitySetModel(ActivitySetModel parentActivitySetModel) {
		if (parentActivitySetModel == null) {
			throw new NullPointerException("parentActivitySetModel cannot be null"); //$NON-NLS-1$
		}
		this.parentActivitySubsetModel = parentActivitySetModel;
	}

	/**
	 * @return the ident
	 */
	public synchronized final ActivitySubsetIdentification getIdent() {
		return this.ident;
	}

	/**
	 * @param ident the ident to set
	 */
	public synchronized final void setIdent(
			ActivitySubsetIdentification ident) {
		this.ident = ident;
	}

	/**
	 * @param additionalFilter
	 */
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

	/**
	 * 
	 */
	public void clearSubset() {
		this.ident.clearSubset();
	}

	/**
	 * @param additionalFilter
	 */
	public final void addAndedFilter(PropertiedObjectFilter<ProcessorFactoryAdapter> additionalFilter) {
		this.ident.addAndedFilter(additionalFilter);
		setUpdated(true);
	}

	/**
	 * @param filter
	 */
	public void setFilter(PropertiedObjectFilter<ProcessorFactoryAdapter> filter) {
		this.ident.setObjectFilter(filter);
		setUpdated(true);
	}

}
