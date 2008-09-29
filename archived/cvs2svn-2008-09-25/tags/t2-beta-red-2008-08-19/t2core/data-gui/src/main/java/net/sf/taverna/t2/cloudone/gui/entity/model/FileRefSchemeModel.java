package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.io.File;
import java.util.List;

import net.sf.taverna.t2.cloudone.gui.entity.view.FileRefSchemeView;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observer;

/**
*  Model (in MVC terms) for the {@link FileReferenceScheme} being added or
 * removed from a {@link FileRefSchemeView}. Interested parties can register
 * with it (delegated to the {@link MultiCaster}) to receive notifications when
 * this model changes
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class FileRefSchemeModel extends ReferenceSchemeModel<File> {
	/*
	 * Proxy for sending notifications about events
	 */
	private MultiCaster<File> multiCaster = new MultiCaster<File>(this);
	private File file = null;
	/*
	 * The parent DataDocumentModel
	 */
	private final DataDocumentModel parentModel;

	public FileRefSchemeModel(DataDocumentModel parentModel) {
		this.parentModel = parentModel;
	}

	/**
	 * If you want to be notified about events happening to this
	 * {@link FileRefSchemeModel} then register. Uses the {@link MultiCaster}
	 */
	public void addObserver(Observer<File> observer) {
		multiCaster.addObserver(observer);
	}

	/**
	 * If you no longer want to be notified about events then remove yourself.
	 * Uses the {@link MultiCaster}
	 */
	public void removeObserver(Observer<File> observer) {
		multiCaster.removeObserver(observer);
	}

	/**
	 * Remove this {@link FileRefSchemeModel} from the model and inform the
	 * parent model that it has happened
	 */
	@Override
	public void remove() {
		parentModel.removeReferenceScheme(this);
	}

	/**
	 * The {@link File} represented by this {@link FileRefSchemeModel}
	 * 
	 * @return
	 */
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
		multiCaster.notify(file);
	}

	@Override
	public String getStringRepresentation() {
		if (file == null) {
			return "(none)";
		}
		return file.toString();
	}

	public List<Observer<File>> getObservers() {
		return multiCaster.getObservers();
	}

}
