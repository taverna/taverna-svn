/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

/**
 * @author alanrw
 *
 */
public interface ActivityPaletteModelListener {

	void subsetModelAdded(final ActivityPaletteModel activityPaletteModel,
			final ActivityRegistrySubsetModel tabModel);

	void scavengingStarted(ActivityPaletteModel model, String message);

	void scavengingDone(ActivityPaletteModel model);

}
