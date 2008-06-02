package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.beanshell;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactoryRegistry;

public class BeanshellActivityMouseListener implements MouseListener {

	private JComponent component;

	/**
	 * Associate a component with what should happen when it is clicked on. In
	 * this case it pops up the StringConstantActivity view
	 * 
	 * @param component clicking on this triggers this action
	 */
	public BeanshellActivityMouseListener(JComponent component) {
		this.component = component;

	}

	/**
	 * When the component is clicked on pop up the correct type of Activity view
	 * using the {@link ActivityViewFactory}
	 */
	public void mouseClicked(MouseEvent e) {
		BeanshellActivityTransferHandler transferHandler = (BeanshellActivityTransferHandler) (component)
				.getTransferHandler();
		ActivityViewFactory viewFactoryForBeanType = ActivityViewFactoryRegistry
				.getInstance().getViewFactoryForBeanType(
						transferHandler.getActivity());
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
