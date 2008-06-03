package net.sf.taverna.t2.workbench.ui.actions.activity.draggable;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactoryRegistry;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/**
 * When a component is clicked on pop up the appropriate {@link ContextualView}
 * based on the {@link ActivityTransferHandler} which is inside the component
 * 
 * @author Ian Dunlop
 * 
 */
public class ActivityMouseListener implements MouseListener {

	private JComponent component;

	public ActivityMouseListener(JComponent component) {
		this.component = component;
	}

	/**
	 * Get the {@link ActivityTransferHandler} from the component and use the
	 * {@link ActivityViewFactory} to show the appropriate
	 * {@link ContextualView}
	 */
	public void mouseClicked(MouseEvent e) {
		ActivityTransferHandler transferHandler = (ActivityTransferHandler) (component)
				.getTransferHandler();
		ActivityViewFactory viewFactoryForBeanType = ActivityViewFactoryRegistry
				.getInstance().getViewFactoryForBeanType(
						(Activity<?>) transferHandler.getActivity());
		ActivityContextualView viewType = viewFactoryForBeanType
				.getView(transferHandler.getActivity());
		viewType.setVisible(true);
	}

	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

}
