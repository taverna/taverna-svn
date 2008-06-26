package net.sf.taverna.t2.cloudone.gui.entity.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import net.sf.taverna.t2.cloudone.gui.entity.model.EntityListModelEvent;
import net.sf.taverna.t2.cloudone.gui.entity.model.SingletonListModel;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;

import org.apache.log4j.Logger;

public class RunSingletonListView {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(RunSingletonListView.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SingletonListModel model = new SingletonListModel(0);
		SingletonListView view = new SingletonListView(model);
		model.addObserver(new Observer<EntityListModelEvent>() {

			public void notify(Observable<EntityListModelEvent> sender,
					EntityListModelEvent message) {
				System.out.println(message.getEventType() + " "
						+ message.getModel() + " in " + sender);
			}
		});
		
		JFrame frame = new JFrame("Singleton entity list view");
		frame.setLayout(new GridBagLayout());

		JPanel scrollPane = new JPanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.1;
		c.weighty = 0.1;
		frame.add(new JScrollPane(scrollPane), c);
		
		scrollPane.setLayout(new GridBagLayout());
		GridBagConstraints paneC = new GridBagConstraints();
		paneC.gridx = 0;
		paneC.gridy = 0;
		scrollPane.add(view, paneC);
		
		paneC.gridx = 1;
		paneC.gridy = 1;
		paneC.weightx = 0.1;
		paneC.weighty = 0.1;
		paneC.fill = GridBagConstraints.BOTH;
		JPanel filler = new JPanel();
		//filler.setBackground(Color.BLUE);
		scrollPane.add(filler, paneC); 
		
		frame.setSize(new Dimension(500, 400));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);

	}
}
