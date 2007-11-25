/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public class ActivityTreeListener extends MouseAdapter {
	
	private ActivityTabPanel parent;
	
	public ActivityTreeListener(final ActivityTabPanel parent) {
		this.parent = parent;
	}
	/**
	 * Handle the mouse pressed event in case this is the platform specific
	 * trigger for a popup menu
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	/**
	 * Similarly handle the mouse released event
	 */
	@Override
	public void mouseReleased(final MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	/**
	 * If the popup was over a ProcessorFactory implementation then present the
	 * 'add' option to the user
	 */
	@SuppressWarnings("unchecked")
	void doEvent(MouseEvent e) {
		TreePath path = this.parent.getCurrentTree().getPathForLocation(e.getX(), e.getY());
		if (path != null) {
			final PropertiedTreeNode<ProcessorFactory> node = (PropertiedTreeNode<ProcessorFactory>) (path.getLastPathComponent());
			if ((node != null) && (node instanceof PropertiedTreeObjectNode)) {
				PropertiedTreeObjectNode<ProcessorFactory> objectNode = (PropertiedTreeObjectNode<ProcessorFactory>) node;
				final ProcessorFactory pf = objectNode.getObject();
				JPopupMenu menu = new ActivitySelectionPopupMenu(pf, this.parent);
				menu.show(this.parent.getCurrentTree(), e.getX(), e.getY());
			}
		}
	}
}
