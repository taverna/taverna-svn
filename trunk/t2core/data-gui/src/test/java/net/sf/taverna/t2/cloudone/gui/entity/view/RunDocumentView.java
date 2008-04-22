package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.DataDocumentModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.FileRefSchemeModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.HttpRefSchemeModel;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

public class RunDocumentView {

	public static void main(String[] args) throws InterruptedException,
			IOException {
		DataDocumentModel model = new DataDocumentModel(null);
		DataDocumentEditView view = new DataDocumentEditView(model, null);

		model.addObserver(new Observer<DataDocumentModelEvent>() {
			public void notify(Observable<DataDocumentModelEvent> sender,
					DataDocumentModelEvent message) {
				System.out.println(message.getEventType() + " "
						+ message.getModel() + " in " + sender);
			}
		});

		JFrame frame = new JFrame("Document view");
		frame.add(new JPanel(), BorderLayout.LINE_START);
		frame.add(new JPanel(), BorderLayout.PAGE_START);
		frame.add(view, BorderLayout.CENTER);
		
		frame.setSize(new Dimension(500, 400));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

		Thread.sleep(3 * 1000);
		FileRefSchemeModel fileModel = new FileRefSchemeModel(model);
		model.addReferenceScheme(fileModel);

		Thread.sleep(2 * 1000);

		HttpRefSchemeModel httpModel = new HttpRefSchemeModel(model);
		model.addReferenceScheme(httpModel);
		Thread.sleep(2 * 1000);
		fileModel.setFile(File.createTempFile("some", "test"));
		Thread.sleep(2 * 1000);
		httpModel.setURL("http://www.google.co.uk/");
		
		Thread.sleep(2 * 60 * 1000);
		System.exit(0);
	}
}
