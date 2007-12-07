package net.sf.taverna.t2.cloudone.gui.entity.model;

public class EntityListModelEvent extends ModelEvent<EntityModel> {
	/**
	 * Details of what happened and to what {@link EntityListModel}
	 * 
	 * @param eventType
	 * @param entityModel
	 */
	public EntityListModelEvent(EventType eventType, EntityModel entityModel) {
		super(eventType, entityModel);
	}

}
