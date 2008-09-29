package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.cloudone.gui.entity.view.DataDocumentEditView;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

/**
 * Acts as the Model in the Model-View-Controller pattern for the
 * {@link DataDocumentEditView}. Contains a list of {@link ReferenceSchemeModel}s
 * which it delegates add/remove responsibilities to a {@link MultiCaster}
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
@SuppressWarnings("unchecked")
public class DataDocumentModel extends EntityModel implements Observable<DataDocumentModelEvent> {

	private List<ReferenceSchemeModel> refSchemeModels = new ArrayList<ReferenceSchemeModel>();

	private MultiCaster<DataDocumentModelEvent> multiCaster = new MultiCaster<DataDocumentModelEvent>(
			this);

	public DataDocumentModel(EntityListModel entityListModel) {
		super(entityListModel);
	}

	/**
	 * Add a {@link ReferenceSchemeModel} to the model. Use the
	 * {@link MultiCaster} to inform registered observers (usually the
	 * {@link DataDocumentEditView} that something has been added by sending a
	 * {@link DataDocumentModelEvent}
	 * 
	 * @param refSchemeModel
	 *            {@link ReferenceSchemeModel}
	 */
	public void addReferenceScheme(ReferenceSchemeModel refSchemeModel) {
		refSchemeModels.add(refSchemeModel);
		multiCaster.notify(new DataDocumentModelEvent(
				DataDocumentModelEvent.EventType.ADDED, refSchemeModel));
	}

	/**
	 * The {@link List} of {@link ReferenceSchemeModel}s
	 * 
	 * @return
	 */
	public List<ReferenceSchemeModel> getReferenceSchemeModels() {
		return new ArrayList<ReferenceSchemeModel>(refSchemeModels);
	}

	/**
	 * Register with the {@link DataDocumentModel} to receive notifications when
	 * a {@link ReferenceSchemeModel} is added or removed
	 */
	public void addObserver(Observer<DataDocumentModelEvent> observer) {
		multiCaster.addObserver(observer);
	}

	/**
	 * Ask the {@link DataDocumentModel} to no longer inform you of changes
	 */
	public void removeObserver(Observer<DataDocumentModelEvent> observer) {
		multiCaster.removeObserver(observer);
	}

	/**
	 * Remove a {@link ReferenceSchemeModel} from the model. Use the
	 * {@link MultiCaster} to inform registered observers (probably the
	 * {@link DataDocumentEditView} that something has been removed by sending a
	 * {@link DataDocumentModelEvent}
	 * 
	 * @param refSchemeModel
	 *            {@link ReferenceSchemeModel}
	 */
	public void removeReferenceScheme(ReferenceSchemeModel refSchemeModel) {
		refSchemeModels.remove(refSchemeModel);
		multiCaster.notify(new DataDocumentModelEvent(
				DataDocumentModelEvent.EventType.REMOVED, refSchemeModel));
	}

	@Override
	public void remove() {
		for (ReferenceSchemeModel refModel : getReferenceSchemeModels()) {
			refModel.remove();
		}
		super.remove();
	}

	public List<Observer<DataDocumentModelEvent>> getObservers() {
		return multiCaster.getObservers();
	}
	
}
