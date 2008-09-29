package net.sf.taverna.t2.cloudone.gui.entity.model;

/**
 * Parent type for most entities which can be added to the model
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
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

	/**
	 * If an entity {@link #isRemovable()} then remove it from the
	 * {@link #parentModel}
	 */
	public void remove() {
		if (isRemovable()) {
			getParentModel().removeEntityModel(this);
		}
	}

	/**
	 * Can this model be removed (usually depends on whether it is the base
	 * model)
	 * 
	 * @return
	 */
	public boolean isRemovable() {
		return removable && getParentModel() != null;
	}

	/**
	 * Defaults to true
	 * 
	 * @param removable
	 */
	public void setRemovable(boolean removable) {
		this.removable = removable;
	}

	/**
	 * Depth is zero unless overriden in a sub-class and re-set
	 * 
	 * @return
	 */
	public int getDepth() {
		return 0;
	}

}
