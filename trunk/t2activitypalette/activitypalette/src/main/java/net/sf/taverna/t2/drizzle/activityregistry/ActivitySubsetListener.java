/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflWorkflowProcessor;
import org.embl.ebi.escience.scufl.ScuflWorkflowProcessorFactory;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.WorkflowInputPanelFactory;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTreePopupHandler;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jdom.Document;
import org.jdom.Element;

import net.sf.taverna.t2.drizzle.util.PropertiedTreeNode;
import net.sf.taverna.t2.drizzle.util.PropertiedTreeObjectNode;

/**
 * @author alanrw
 *
 */
public class ActivitySubsetListener extends MouseAdapter {
	
	private ActivitySubsetPanel parent;
	
	public ActivitySubsetListener(final ActivitySubsetPanel parent) {
		this.parent = parent;
	}
	/**
	 * Handle the mouse pressed event in case this is the platform specific
	 * trigger for a popup menu
	 */
	public void mousePressed(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	/**
	 * Similarly handle the mouse released event
	 */
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			doEvent(e);
		}
	}

	/**
	 * If the popup was over a ProcessorFactory implementation then present the
	 * 'add' option to the user
	 */
	void doEvent(MouseEvent e) {
		Component source = (Component) e.getSource();
		ProcessorFactory pf = null;
		if (source instanceof JTree) {
			JTree sourceTree = (JTree) source;
			TreePath path = sourceTree.getPathForLocation(e.getX(), e.getY());
			if (path != null) {
				final PropertiedTreeNode node = (PropertiedTreeNode) (path.getLastPathComponent());
				if ((node != null) && (node instanceof PropertiedTreeObjectNode)) {
					PropertiedTreeObjectNode<ProcessorFactory> objectNode = (PropertiedTreeObjectNode) node;
					pf = objectNode.getObject();
				}
			}
		} else if (source instanceof JTable) {
			JTable sourceTable = (JTable) source;
			int row = sourceTable.rowAtPoint(e.getPoint());
			ActivitySubsetTableModel tableModel = (ActivitySubsetTableModel) sourceTable.getModel();
			pf = tableModel.getRowObject(row);
		}
		if (pf != null) {
			JPopupMenu menu = new ActivitySelectionPopupMenu(pf, parent);
			menu.show(((Component)e.getSource()), e.getX(), e.getY());
		}
	}
}
