package net.sf.taverna.t2.cloudone.gui.entity.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.taverna.t2.cloudone.gui.entity.model.ModelEvent.EventType;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.junit.Before;
import org.junit.Test;

public class DataDocumentModelTest {

	DataDocObserver observer = new DataDocObserver();

	DataDocumentModel model = new DataDocumentModel(null);

	@SuppressWarnings("unchecked")
	@Test
	public void add() throws Exception {
		ReferenceSchemeModel refSchemeMod = new DummyRefSchemeModel();
		model.addReferenceScheme(refSchemeMod);
		assertEquals(1, model.getReferenceSchemeModels().size());
		assertEquals(refSchemeMod, model.getReferenceSchemeModels().get(0));
		assertEquals(EventType.ADDED, observer.lastEvent.getEventType());
		assertEquals(refSchemeMod, observer.lastEvent.getModel());
	}

	@Before
	public void attachObserver() {
		model.addObserver(observer);
	}

	@Test
	public void empty() throws Exception {
		assertTrue("Initial models not empty", model.getReferenceSchemeModels()
				.isEmpty());
		assertNull(observer.lastEvent);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void remove() throws Exception {
		ReferenceSchemeModel otherRefScheme = new DummyRefSchemeModel();
		ReferenceSchemeModel refSchemeMod = otherRefScheme;
		model.addReferenceScheme(refSchemeMod);
		model.addReferenceScheme(otherRefScheme); // another
		assertEquals(2, model.getReferenceSchemeModels().size());
		model.removeReferenceScheme(refSchemeMod);
		assertEquals(1, model.getReferenceSchemeModels().size());
		assertEquals(otherRefScheme, model.getReferenceSchemeModels().get(0));
		assertEquals(EventType.REMOVED, observer.lastEvent.getEventType());
		assertEquals(refSchemeMod, observer.lastEvent.getModel());
	}

	private final class DummyRefSchemeModel extends ReferenceSchemeModel<String> {
		@Override
		public void remove() {
		}
		@Override
		public String getStringRepresentation() {
			return null;
		}
		public void addObserver(Observer<String> observer) {
		}
		public void removeObserver(Observer<String> observer) {	
		}
		public List<Observer<String>> getObservers() {
			return null;
		}
	}

	public class DataDocObserver implements Observer<DataDocumentModelEvent> {

		public DataDocumentModelEvent lastEvent = null;

		public void notify(Observable<DataDocumentModelEvent> sender,
				DataDocumentModelEvent message) {
			lastEvent = message;
		}
	}

}
