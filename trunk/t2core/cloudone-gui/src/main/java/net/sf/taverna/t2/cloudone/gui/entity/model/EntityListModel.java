package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

public class EntityListModel extends EntityModel implements
		Observable<EntityListModelEvent> {

	private MultiCaster<EntityListModelEvent> multiCaster = new MultiCaster<EntityListModelEvent>(
			this);

	private List<EntityModel> entityModels = new ArrayList<EntityModel>();

	public EntityListModel(EntityListModel parentModel) {
		super(parentModel);
	}

	public void registerObserver(Observer<EntityListModelEvent> observer) {
		multiCaster.registerObserver(observer);
	}

	public void removeObserver(Observer<EntityListModelEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	public void addEntityModel(EntityModel entityModel) {
		synchronized (this) {
			entityModels.add(entityModel);
		}
		multiCaster.notify(new EntityListModelEvent(ModelEvent.EventType.ADDED,
				entityModel));
	}

	public void removeEntityModel(EntityModel entityModel) {
		synchronized (this) {
			entityModels.remove(entityModel);
		}
		multiCaster.notify(new EntityListModelEvent(
				ModelEvent.EventType.REMOVED, entityModel));
	}

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

	@Override
	public void setDepth(int depth) {
		if (depth == 0) {
			throw new IllegalArgumentException("List depth can't be zero");
		}
		super.setDepth(depth);
	}

}