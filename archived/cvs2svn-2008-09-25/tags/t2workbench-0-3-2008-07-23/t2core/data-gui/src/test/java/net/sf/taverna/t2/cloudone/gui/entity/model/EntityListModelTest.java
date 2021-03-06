package net.sf.taverna.t2.cloudone.gui.entity.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.cloudone.gui.entity.model.ModelEvent.EventType;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.junit.Before;
import org.junit.Test;

public class EntityListModelTest {

	EntityListModelObserver observer = new EntityListModelObserver();

	EntityListModel model = new EntityListModel(null);

	@Test
	public void add() throws Exception {
		EntityModel entityModel = new EntityModel(null);
		model.addEntityModel(entityModel);
		assertEquals(1, model.getEntityModels().size());
		assertEquals(entityModel, model.getEntityModels().get(0));
		assertEquals(EventType.ADDED, observer.lastEvent.getEventType());
		assertEquals(entityModel, observer.lastEvent.getModel());
	}

	@Before
	public void attachObserver() {
		model.addObserver(observer);
	}

	@Test
	public void empty() throws Exception {
		assertTrue("Initial models not empty", model.getEntityModels()
				.isEmpty());
		assertNull(observer.lastEvent);
	}

	@Test
	public void remove() throws Exception {
		EntityModel entityModel = new EntityModel(null);
		EntityModel otherEntityModel = new EntityModel(null);
		model.addEntityModel(entityModel);
		model.addEntityModel(otherEntityModel);
		assertEquals(2, model.getEntityModels().size());
		model.removeEntityModel(entityModel);
		assertEquals(1, model.getEntityModels().size());
		assertEquals(otherEntityModel, model.getEntityModels().get(0));
		assertEquals(EventType.REMOVED, observer.lastEvent.getEventType());
		assertEquals(entityModel, observer.lastEvent.getModel());
	}

	public class EntityListModelObserver implements Observer<EntityListModelEvent> {

		public EntityListModelEvent lastEvent = null;

		public void notify(Observable<EntityListModelEvent> sender,
				EntityListModelEvent message) {
			lastEvent = message;
		}
	}

}
