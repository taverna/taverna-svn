package net.sf.taverna.t2.cloudone.gui.entity.view;

import javax.swing.JComponent;

/**
 * Extend this by sub views and implement the {@link #setEdit(boolean)}
 * appropriate to that implementation
 * 
 * @author Stian Soiland
 * @author Ian Dunlop
 * 
 */
public abstract class RefSchemeView extends JComponent {
	/**
	 * How the view should handle changes of state in its components eg.
	 * disable/enable buttons
	 * 
	 * @param editable
	 *            True if the view is to be editable, false if it is no longer
	 *            to be editable.
	 * @throws IllegalStateException
	 *             If the current fields were illegal for the model. The view
	 *             will remain editable.
	 */
	public abstract void setEdit(boolean editable) throws IllegalStateException;

}
