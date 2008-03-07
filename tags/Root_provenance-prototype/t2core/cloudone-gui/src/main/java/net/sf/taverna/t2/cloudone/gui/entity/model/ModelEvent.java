package net.sf.taverna.t2.cloudone.gui.entity.model;

import org.apache.log4j.Logger;

/**
 * Defines the type of events what can happen to an {@link EntityModel}.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 * @param <Model>
 *            What type of {@link EntityModel} the event will represent
 */
public class ModelEvent<Model> {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(ModelEvent.class);

	protected final Model entityModel;
	protected final EventType eventType;

	/**
	 * What happened and to who
	 * 
	 * @param eventType
	 *            ADDED or REMOVED
	 * @param entityModel
	 *            the model which has the event
	 */
	public ModelEvent(EventType eventType, Model entityModel) {
		this.eventType = eventType;
		this.entityModel = entityModel;
	}

	public Model getModel() {
		return entityModel;
	}

	/**
	 * What actually happened to trigger the event
	 * 
	 * @return
	 */
	public EventType getEventType() {
		return eventType;
	}

	@Override
	public String toString() {
		return getEventType() + ": " + getModel();
	}

	/**
	 * What is the type of event
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public static enum EventType {
		ADDED, REMOVED
	}

}