package net.sf.taverna.t2.cloudone.gui.entity.model;

/**
 * Event generated when the {@link DataDocumentModel} has been modified.
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
@SuppressWarnings("unchecked")
public class DataDocumentModelEvent extends ModelEvent<ReferenceSchemeModel> {

	/**
	 * The {@link EventType} and the {@link ReferenceSchemeModel} that caused it
	 * to be sent
	 * 
	 * @param type
	 *            ADDED/REMOVED
	 * @param refSchemeModel
	 *            what has been added or removed
	 */
	public DataDocumentModelEvent(EventType type,
			ReferenceSchemeModel refSchemeModel) {
		super(type, refSchemeModel);
	}
}
