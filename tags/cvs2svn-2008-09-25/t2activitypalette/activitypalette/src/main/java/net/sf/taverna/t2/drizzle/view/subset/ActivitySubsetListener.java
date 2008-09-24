/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.drizzle.model.ProcessorFactoryAdapter;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * @author alanrw
 *
 */
public class ActivitySubsetListener extends MouseAdapter {
	
	private ActivitySubsetPanel parent;
	
	/**
	 * @param parent
	 */
	public ActivitySubsetListener(final ActivitySubsetPanel parent) {
		if (parent == null) {
			throw new NullPointerException("parent cannot be null"); //$NON-NLS-1$
		}
		this.parent = parent;
	}
	/**
	 * Handle the mouse pressed event in case this is the platform specific
	 * trigger for a popup menu
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null"); //$NON-NLS-1$
		}
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	/**
	 * Similarly handle the mouse released event
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (e == null) {
			throw new NullPointerException("e cannot be null"); //$NON-NLS-1$
		}
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
		if (e == null) {
			throw new NullPointerException("e cannot be null"); //$NON-NLS-1$
		}
		Component source = (Component) e.getSource();
		ProcessorFactory pf = null;
		if (source instanceof JTree) {
			JTree sourceTree = (JTree) source;
			TreePath path = sourceTree.getPathForLocation(e.getX(), e.getY());
			if (path != null) {
				final PropertiedTreeNode<ProcessorFactoryAdapter> node = (PropertiedTreeNode<ProcessorFactoryAdapter>) (path.getLastPathComponent());
				if ((node != null) && (node instanceof PropertiedTreeObjectNode)) {
					PropertiedTreeObjectNode<ProcessorFactoryAdapter> objectNode = (PropertiedTreeObjectNode<ProcessorFactoryAdapter>) node;
					pf = objectNode.getObject().getTheFactory();
				}
			}
		} else if (source instanceof JTable) {
			JTable sourceTable = (JTable) source;
			int row = sourceTable.rowAtPoint(e.getPoint());
			ActivitySubsetTableModel tableModel = (ActivitySubsetTableModel) sourceTable.getModel();
			pf = tableModel.getRowObject(row).getTheFactory();
		}
		if (pf != null) {
			JPopupMenu menu = new ActivitySelectionPopupMenu(pf, this.parent);
			menu.show(((Component)e.getSource()), e.getX(), e.getY());
		}
	}
}
