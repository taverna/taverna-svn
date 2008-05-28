package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * An abstract class defining the base container to hold a contextual view over
 * Dataflow element.
 * <p>
 * The specific implementation of this class to support a given dataflow element
 * needs to implement the getMainFrame() and getViewTitle().
 * </p>
 * <p>
 * If a view is associated with an action handler to configure this component,
 * then the getConfigureAction handler must be over-ridden. If this returns null
 * then the configure button is left disabled and it is not possible to
 * configure the element.
 * </p>
 * 
 * @author Stuart Owen
 * @author Ian Dunlop
 * 
 */
public abstract class ContextualView extends JFrame {

	/**
	 * When implemented, this method should define the main frame that is placed
	 * in this container, and provides a static view of the Dataflow element.
	 * 
	 * @return a JComponent that represents the dataflow element.
	 */
	protected abstract JComponent getMainFrame();

	/**
	 * @return a String providing a title for the view
	 */
	protected abstract String getViewTitle();

	/**
	 * Allows the item to be configured, but returning an action handler that
	 * will be invoked when selecting to configure. By default this is provided
	 * by a button.
	 * <p>
	 * If there is no ability to configure the given item, then this should
	 * return null.
	 * </p>
	 * 
	 * @return an action that allows the element being viewed to be configured.
	 */
	public Action getConfigureAction() {
		return null;
	}

	/**
	 * This MUST be called by any sub classes after they have initialised their
	 * own view since it gets their main panel and adds it to the main
	 * contextual view. If you don't do this you will get a very empty frame
	 * popping up!
	 */
	protected void initView() {
		setSize(800, 500);
		setLayout(new BorderLayout());
		add(getMainFrame(), BorderLayout.CENTER);
		setTitle(getViewTitle());
		JPanel buttonFrame = new JPanel();
		add(buttonFrame, BorderLayout.SOUTH);
		buttonFrame.setLayout(new BorderLayout());

		buttonFrame.add(createButtonPanel(), BorderLayout.EAST);
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());

		JButton configureButton = new JButton(getConfigureAction());
		configureButton.setEnabled(false);

		if (getConfigureAction() != null) {
			configureButton.setAction(getConfigureAction());
			configureButton.setEnabled(true);
		}
		configureButton.setText("Configure");
		buttonPanel.add(configureButton);

		return buttonPanel;
	}

}
