package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.io.File;

import net.sf.taverna.t2.cloudone.gui.entity.model.FileRefSchemeModel;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

public class FileRefSchemeView extends RefSchemeView implements Observer<File> {

	private static final long serialVersionUID = 1L;
	private FileRefSchemeModel model;
	private DataDocumentView parentView;

	public FileRefSchemeView(FileRefSchemeModel model,
			DataDocumentView parentView) {
		this.model = model;
		this.parentView = parentView;
		model.registerObserver(this);
		initialise();
	}

	private void initialise() {
		// GUI stuff
	}

	@Override
	public void setEdit(boolean editable) throws IllegalStateException {
		// set the edit state of the components

	}

	public void notify(Observable<File> sender, File message) {
		// the model informs us that something has happened, change the GUI
		// accordingly

	}

}
