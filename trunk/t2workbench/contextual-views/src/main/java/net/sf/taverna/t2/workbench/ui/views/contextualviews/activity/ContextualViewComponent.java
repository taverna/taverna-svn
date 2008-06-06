package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class ContextualViewComponent extends JPanel implements UIComponentSPI,
		Observer<ModelMapEvent> {

	private JPanel panel;
	private JComponent view;
	/** Keep list of views in case you want to go back or forward between them */
	private List<JPanel> views = new ArrayList<JPanel>();

	public ContextualViewComponent() {
		ModelMap.getInstance().addObserver(this);
		initialise();
	}

	private void initialise() {
		add(new JLabel("this is a contextual view!"));
		panel = new JPanel();
		add(panel);
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Contextual View";
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		// TODO Auto-generated method stub

	}

	public void updateContextualView(JPanel view) {
		remove(this.view);
		views.add(view);
		add(view);
		revalidate();
	}

	/**
	 * Change the view if the {@link ModelMapEvent} is for the appropriate
	 * object
	 */
	public void notify(Observable<ModelMapEvent> sender, ModelMapEvent message)
			throws Exception {

		if (message.modelName.equalsIgnoreCase("activity")) {
			Object newModel = message.newModel;
			ActivityViewFactory viewFactoryForBeanType = ActivityViewFactoryRegistry
					.getInstance().getViewFactoryForBeanType(
							(Activity<?>) newModel);
			ActivityContextualView viewType = viewFactoryForBeanType
					.getView(newModel);
			updateContextualView(viewType);
		}
	}

}
