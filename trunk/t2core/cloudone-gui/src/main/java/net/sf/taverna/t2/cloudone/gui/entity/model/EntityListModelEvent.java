package net.sf.taverna.t2.cloudone.gui.entity.model;

public class EntityListModelEvent {
	
	private final EventType eventType;
	private final EntityModel entityModel;
	
	public EntityListModelEvent(EventType eventType, EntityModel entityModel) {
		this.eventType = eventType;
		this.entityModel = entityModel;
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
	 * What is the type of event
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public enum EventType {
		ADDED, REMOVED
	}

	public EntityModel getEntityModel() {
		return entityModel;
	}


}
