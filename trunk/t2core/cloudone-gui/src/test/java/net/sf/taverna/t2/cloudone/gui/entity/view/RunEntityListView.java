package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModel;
import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.ModelEvent;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

public class RunEntityListView {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RunEntityListView.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		EntityListModel model = new EntityListModel(null);
		EntityListView view = new EntityListView(model);
		model.registerObserver(new Observer<EntityListModelEvent>() {

			public void notify(Observable<EntityListModelEvent> sender,
					EntityListModelEvent message) {
				System.out.println(message.getEventType() + " "
						+ message.getModel() + " in " + sender);
			}
		});
		
		JFrame frame = new JFrame("Entity List view");
		frame.add(new JPanel(), BorderLayout.LINE_START);
		frame.add(new JPanel(), BorderLayout.PAGE_START);
		frame.add(view, BorderLayout.CENTER);
		
		frame.setSize(new Dimension(500, 400));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
}
