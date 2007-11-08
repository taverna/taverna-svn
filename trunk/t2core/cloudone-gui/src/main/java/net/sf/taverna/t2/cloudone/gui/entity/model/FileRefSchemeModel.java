package net.sf.taverna.t2.cloudone.gui.entity.model;

import java.io.File;

import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

public class FileRefSchemeModel extends ReferenceSchemeModel implements
Observable<File>{
	
	private MultiCaster<File> multiCaster = new MultiCaster<File>(this);
	private File file = null;
	private final DataDocumentModel parentModel;
	
	public FileRefSchemeModel(DataDocumentModel parentModel) {
		this.parentModel = parentModel;
	}

	public void registerObserver(Observer<File> observer) {
		multiCaster.registerObserver(observer);
	}

	public void removeObserver(Observer<File> observer) {
		multiCaster.removeObserver(observer);		
	}
	
	/**
	 * Remove this {@link FileRefSchemeModel} from the model and inform the
	 * parent model that it has happened
	 */
	public void remove() {
		parentModel.removeReferenceScheme(this);
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
		multiCaster.notify(file);
	}

}
