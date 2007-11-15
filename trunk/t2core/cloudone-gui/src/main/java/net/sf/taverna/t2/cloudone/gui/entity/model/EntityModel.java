package net.sf.taverna.t2.cloudone.gui.entity.model;

public class EntityModel {

	private final EntityListModel parentModel;

	public EntityModel(EntityListModel parentModel) {
		this.parentModel = parentModel;
	}

	public EntityListModel getParentModel() {
		return parentModel;
	}

	public void remove() {
		EntityListModel parent = getParentModel();
		if (parent != null) {
			parent.removeEntityModel(this);
		}
	}

}
