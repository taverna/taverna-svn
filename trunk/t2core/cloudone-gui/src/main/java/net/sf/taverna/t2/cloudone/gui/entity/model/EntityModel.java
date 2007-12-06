package net.sf.taverna.t2.cloudone.gui.entity.model;

public class EntityModel {

	public static final int UNKNOWN_DEPTH = -1;

	private final EntityListModel parentModel;
	private boolean removable = true;

	public EntityModel(EntityListModel parentModel) {
		this.parentModel = parentModel;
	}

	public EntityListModel getParentModel() {
		return parentModel;
	}

	public void remove() {
		if (isRemovable()) {
			getParentModel().removeEntityModel(this);
		}
	}

	public boolean isRemovable() {
		return removable && getParentModel() != null;
	}

	public void setRemovable(boolean removable) {
		this.removable = removable;
	}

	public int getDepth() {
		return 0;
	}

}
