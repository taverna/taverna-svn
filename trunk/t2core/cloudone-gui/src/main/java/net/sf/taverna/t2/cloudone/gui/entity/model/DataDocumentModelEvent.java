package net.sf.taverna.t2.cloudone.gui.entity.model;

/**
 * Event generated when the {@link DataDocumentModel} has been modified.
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
public class DataDocumentModelEvent {

	private final ReferenceSchemeModel refSchemeModel;
	private final EventType eventType;

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
		this.eventType = type;
		this.refSchemeModel = refSchemeModel;
	}

	/**
	 * What actually happened to trigger the event
	 * 
	 * @return
	 */
	public EventType getEventType() {
		return eventType;
	}

	/**
	 * The {@link ReferenceSchemeModel} that the {@link DataDocumentModel} is
	 * about
	 * 
	 * @return
	 */
	public ReferenceSchemeModel getRefSchemeModel() {
		return refSchemeModel;
	}

	/**
	 * What is the type of event
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public enum EventType {
		ADDED, REMOVED
	}

}
