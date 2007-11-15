/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.util.HashSet;
import java.util.Set;

/**
 * @author alanrw
 *
 */
public final class ActivityPaletteModel {
	private ActivityRegistry registry;
	
	private Set<ActivityTabModel> tabModels;
	
	public ActivityPaletteModel () {
		tabModels = new HashSet<ActivityTabModel> ();
	}

	/**
	 * @param registry the registry to set
	 */
	public synchronized void setRegistry(final ActivityRegistry registry) {
		if (registry == null) {
			throw new NullPointerException("registry cannot be null");
		}
		this.registry = registry;
	}
	
	public ActivityTabModel addImmediateQuery(ActivityQuery<?> query) {
		if (query == null) {
			throw new NullPointerException("query cannot be null");
		}
		ActivityQueryRunIdentification ident = registry.addImmediateQuery(query);
		ActivityTabModel tabModel = new ActivityTabModel();
		tabModel.setFilter(ident.getObjectFilter());
		tabModel.setName(ident.getName());
		tabModels.add(tabModel);
		return tabModel;
	}
	
	public void removeTabModel(final ActivityTabModel tabModel) {
		if (tabModel == null) {
			throw new NullPointerException ("tabModel cannot be null");
		}
		tabModels.remove(tabModel);
	}
}
