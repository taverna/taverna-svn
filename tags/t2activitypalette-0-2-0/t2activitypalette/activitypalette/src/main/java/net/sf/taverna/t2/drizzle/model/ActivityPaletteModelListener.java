/**
 * 
 */
package net.sf.taverna.t2.drizzle.model;


/**
 * @author alanrw
 *
 */
public interface ActivityPaletteModelListener {

	/**
	 * @param activityPaletteModel
	 * @param tabModel
	 */
	void subsetModelAdded(final ActivityPaletteModel activityPaletteModel,
			final ActivitySubsetModel tabModel);

	/**
	 * @param model
	 * @param message
	 */
	void scavengingStarted(ActivityPaletteModel model, String message);

	/**
	 * @param model
	 */
	void scavengingDone(ActivityPaletteModel model);

}
