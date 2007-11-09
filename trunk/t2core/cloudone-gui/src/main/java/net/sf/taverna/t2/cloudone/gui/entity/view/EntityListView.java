package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModelEvent.EventType;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;


public class EntityListView extends EntityView implements Observer<EntityListModelEvent> {
	
	private Map<EntityModel, EntityView> modelViews = new HashMap<EntityModel, EntityView>();

	private static Logger logger = Logger.getLogger(EntityListView.class);

	private final EntityListModel entityListModel;
	
	private JPanel entityViews;

	private EntityView lastEditedView;

	private ModelObserver observer;
	
	public EntityListView(EntityListModel entityListModel) {
		this.entityListModel = entityListModel;
		initialise();
		observer = new ModelObserver();
		entityListModel.registerObserver(observer);
	}

	private void initialise() {
		entityViews = new JPanel();
		
	}
	
	public void addEntityModel(EntityModel entityModel) {
		EntityView view;
		if (entityModel instanceof DataDocumentModel) {
			view = new DataDocumentView((DataDocumentModel)entityModel);
		} else if (entityModel instanceof EntityListModel) {
			view = new EntityListView((EntityListModel) entityModel);
		} else {
			throw new IllegalArgumentException("Unsupported model type " + entityModel);
		}
		modelViews.put(entityModel, view);
		// TODO: Strings and literals
	}
	
	public void removeEntityModel(EntityModel entityModel) {
		EntityView view = modelViews.remove(entityModel);
		entityViews.remove(view);
		if (lastEditedView == view) {
			lastEditedView = null;
		}
		entityViews.revalidate();
	}

	public void notify(Observable<EntityListModelEvent> sender,
			EntityListModelEvent event) {
		if (event.getEventType().equals(EntityListModelEvent.EventType.ADDED)) {
			addEntityModel(event.getEntityModel());
		} else if (event.getEventType().equals(EntityListModelEvent.EventType.REMOVED)) {
			removeEntityModel(event.getEntityModel());
		} else {
			logger.warn("Unsupported event type " + event.getEventType());
		}
		
	}
	
	/**
	 * Observes the changes in a {@link DataDocumentModel} and is notified by
	 * the {@link DataDocumentModel} whenit wants to inform it of a change to
	 * the underlying data
	 * 
	 * @author Stian Soiland
	 * @author Ian Dunlop
	 * 
	 */
	private final class ModelObserver implements
			Observer<EntityListModelEvent> {

		public void notify(Observable<EntityListModelEvent> sender,
				EntityListModelEvent event) {
			EventType eventType = event.getEventType();
			EntityModel entityModel = event.getEntityModel();
			if (eventType.equals(EventType.ADDED)) {
				addEntityModel(entityModel);
			} else if (eventType.equals(EventType.REMOVED)) {
				removeEntityModel(entityModel);
			} else {
				logger.warn("Unsupported event type " + eventType);
			}
		}
	}

}
