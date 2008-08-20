package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.gui.entity.view.EntityListView;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

/**
 * Acts as the Model in the Model-View-Controller pattern for the
 * {@link EntityListView}. Contains a list of {@link EntityModel}s which it
 * delegates add/remove responsibilities for to a {@link MultiCaster}
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class EntityListModel extends EntityModel implements
		Observable<EntityListModelEvent> {
	/*
	 * Responsible for sending event notifications to registered observers
	 */
	private MultiCaster<EntityListModelEvent> multiCaster = new MultiCaster<EntityListModelEvent>(
			this);
	/*
	 * The models which are contained inside the list
	 */
	private List<EntityModel> entityModels = new ArrayList<EntityModel>();
	/*
	 * The depth of the list ie. how many siblings it has
	 */
	private int depth = UNKNOWN_DEPTH;

	/**
	 * The depth is set to one less than the parent (if it has one)
	 * 
	 * @param parentModel
	 */
	public EntityListModel(EntityListModel parentModel) {
		super(parentModel);
		if (parentModel != null) {
			int parentDepth = parentModel.getDepth();
			if (parentDepth != UNKNOWN_DEPTH) {
				setDepth(parentDepth - 1);
			}
		}
	}

	/**
	 * If you want to be notified of changes to this model then register with
	 * this method. Uses the {@link MultiCaster}
	 */
	public void addObserver(Observer<EntityListModelEvent> observer) {
		multiCaster.addObserver(observer);
	}

	/**
	 * If you no longer wish to be notified then inform this model. Uses the
	 * {@link MultiCaster}
	 */
	public void removeObserver(Observer<EntityListModelEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	/**
	 * Add a sibling to the list
	 * 
	 * @param entityModel
	 *            The child model to be added
	 */
	public void addEntityModel(EntityModel entityModel) {
		synchronized (this) {
			entityModels.add(entityModel);
		}
		multiCaster.notify(new EntityListModelEvent(ModelEvent.EventType.ADDED,
				entityModel));
	}

	/**
	 * Remove a child model from this list. Uses the {@link MultiCaster} to send
	 * out a notification that the model has been removed
	 * 
	 * @param entityModel
	 */
	public void removeEntityModel(EntityModel entityModel) {
		synchronized (this) {
			entityModels.remove(entityModel);
		}
		multiCaster.notify(new EntityListModelEvent(
				ModelEvent.EventType.REMOVED, entityModel));
	}

	/**
	 * Return all the child models contained within the top level of this list
	 * 
	 * @return a {@link ArrayList} containing all the child models
	 */
	public List<EntityModel> getEntityModels() {
		synchronized (this) {
			return new ArrayList<EntityModel>(entityModels);
		}
	}

	@Override
	public void remove() {
		for (EntityModel entModel : getEntityModels()) {
			entModel.remove();
		}
		super.remove();
	}

	/**
	 * How deep should the list be. It can't be zero or less than the
	 * UNKNOWN_DEPTH (set to -1)
	 * 
	 * @param depth
	 */
	public void setDepth(int depth) {
		if (depth == 0) {
			throw new IllegalArgumentException("List depth can't be zero");
		}
		if (depth < UNKNOWN_DEPTH) {
			throw new IllegalArgumentException("Invalid depth: " + depth);
		}
		this.depth = depth;
	}

	@Override
	public int getDepth() {
		return depth;
	}

	public List<Observer<EntityListModelEvent>> getObservers() {
		return multiCaster.getObservers();
	}

}