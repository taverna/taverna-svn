package net.sf.taverna.t2.cloudone.gui.entity.model;

public class EntityModel {

	public static final int UNKNOWN_DEPTH = -1;

	private int depth = UNKNOWN_DEPTH;

	private final EntityListModel parentModel;
	private boolean removable = true;

	public EntityModel(EntityListModel parentModel) {
		this.parentModel = parentModel;
		if (parentModel != null) {
			int parentDepth = parentModel.getDepth();
			if (parentDepth != UNKNOWN_DEPTH) {
				setDepth(parentDepth - 1);
			}
		}
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
		return depth;
	}

	public void setDepth(int depth) {
		if (depth < UNKNOWN_DEPTH) {
			throw new IllegalArgumentException("Invalid depth: " + depth);
		}
		this.depth = depth;
	}

}
