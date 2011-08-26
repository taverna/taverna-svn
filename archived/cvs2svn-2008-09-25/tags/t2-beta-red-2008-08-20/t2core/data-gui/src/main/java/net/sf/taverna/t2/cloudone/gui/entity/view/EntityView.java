package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.cloudone.gui.entity.model.ModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.ReferenceSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.ModelEvent.EventType;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

/**
 * Extend this class in other parent Views.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 * @param <MyModelType>
 *            the model which the view represents
 * @param <ChildModelType>
 *            the type of models which can be added to MyModelType
 * @param <Event>
 *            the event representing changes to MyModelType
 */
public abstract class EntityView<MyModelType extends Observable<Event>, ChildModelType, Event extends ModelEvent<ChildModelType>>
		extends JPanel {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(EntityView.class);

	/**
	 * A {@link Map} of {@link ReferenceSchemeModel} to {@link RefSchemeView} to
	 * allow tracking of view state and what model is associated with what view
	 */
	protected Map<ChildModelType, JComponent> modelViews = new HashMap<ChildModelType, JComponent>();

	private final MyModelType myModel;

	protected final ModelObserver modelObserver;

	@SuppressWarnings("unused")
	private int depth;

	private EntityListView parentView;

	/**
	 * Create the view and register MyModelType with the ChildType
	 * {@link MultiCaster} for observing changes
	 * 
	 * @param myModel
	 * @param parentView
	 */
	public EntityView(MyModelType myModel, EntityListView parentView) {
		this.parentView = parentView;
		this.myModel = myModel;
		this.modelObserver = makeModelObserver();
		// TODO: removeObserver on window close
		myModel.addObserver(modelObserver);
	}

	/**
	 * What type of model represents the view
	 * 
	 * @return the model which represents this view
	 */
	public MyModelType getModel() {
		return myModel;
	}

	protected void addModelView(ChildModelType childModel) {
		JComponent view = createModelView(childModel);
		if (view == null) {
			return;
		}
		view.setOpaque(false);
		modelViews.put(childModel, view);
		placeViewComponent(view);
	}

	protected abstract void placeViewComponent(JComponent view);

	protected abstract void removeViewComponent(JComponent view);

	protected abstract JComponent createModelView(ChildModelType model);

	protected ModelObserver makeModelObserver() {
		return new ModelObserver();
	}

	protected void removeModelView(ChildModelType refModel) {
		JComponent view = modelViews.get(refModel);
		if (view != null) {
			removeViewComponent(view);
		}
	}

	/**
	 * The parent (MyModelType) uses this to observe events on the ChildType
	 * 
	 * @author Ian Dunlop
	 * @author Stian Soiland
	 * 
	 */
	public class ModelObserver implements Observer<Event> {
		public void notify(Observable<Event> sender, Event event) {
			EventType eventType = event.getEventType();
			if (eventType.equals(EventType.ADDED)) {
				ChildModelType refModel = event.getModel();
				addModelView(refModel);
			} else if (eventType.equals(EventType.REMOVED)) {
				ChildModelType refModel = event.getModel();
				removeModelView(refModel);
			} else {
				logger.warn("Unsupported event type " + eventType);
			}
		}
	}

	public abstract void setEdit(boolean editable);

	public EntityListView getParentView() {
		return parentView;
	}
}