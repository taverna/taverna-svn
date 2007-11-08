package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.Dimension;
import java.net.MalformedURLException;

import javax.swing.WindowConstants;

import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.HttpRefSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.view.DataDocumentView;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

public class RunDocumentView {

	public static void main(String[] args) throws InterruptedException,
			MalformedURLException {
		DataDocumentModel model = new DataDocumentModel();
		DataDocumentView view = new DataDocumentView(model);

		model.registerObserver(new Observer<DataDocumentModelEvent>() {
			public void notify(Observable<DataDocumentModelEvent> sender,
					DataDocumentModelEvent message) {
				System.out.println(message.getEventType() + " "
						+ message.getRefSchemeModel() + " in " + sender);
			}
		});

		view.setSize(new Dimension(500, 350));
		view.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		view.setVisible(true);

		Thread.sleep(5 * 1000);
		HttpRefSchemeModel httpModel = new HttpRefSchemeModel(model);
		model.addReferenceScheme(httpModel);
		Thread.sleep(5 * 1000);
		httpModel.setURL("http://www.google.co.uk/");

		Thread.sleep(2 * 60 * 1000);
		System.exit(0);
	}
}
