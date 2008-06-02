package net.sf.taverna.t2.workbench.ui.actions.activity.draggable.stringconstant;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ActivityViewFactoryRegistry;

/**
 * When a component which displays a StringConstant is clicked on it needs to
 * pop up the appropriate view. Use this MouseListener to handle that action. It
 * uses the {@link ActivityViewFactory} to get the view for the
 * StringConsantActivity
 * 
 * @author Ian Dunlop
 * 
 */
public class StringConstantActivityMouseListener implements MouseListener {

	private JComponent component;

	/**
	 * Associate a component with what should happen when it is clicked on. In
	 * this case it pops up the StringConstantActivity view
	 * 
	 * @param component clicking on this triggers this action
	 */
	public StringConstantActivityMouseListener(JComponent component) {
		this.component = component;

	}

	/**
	 * When the component is clicked on pop up the correct type of Activity view
	 * using the {@link ActivityViewFactory}
	 */
	public void mouseClicked(MouseEvent e) {
		StringConstantTransferHandler transferHandler = (StringConstantTransferHandler) (component)
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
