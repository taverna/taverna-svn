package net.sf.taverna.t2.reference.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ui.tree.T2ReferenceTreeCellRenderer;
import net.sf.taverna.t2.reference.ui.tree.T2ReferenceTreeModel;

/**
 * A panel containing a T2ReferenceTreeModel in a JTree along with a control to
 * discard the current reference and return to the edit view. Used by the
 * InputConstructionPanel
 * 
 * @author Tom Oinn
 * 
 */
public abstract class T2ReferencePanel extends JPanel {

	private final ImageIcon editIcon = new ImageIcon(getClass().getResource(
			"/icons/write_obj.gif"));

	// Return to the edit view
	private final Action editAction;

	@SuppressWarnings("serial")
	public T2ReferencePanel(ReferenceService referenceService,
			T2Reference reference) {
		super(new BorderLayout());

		// Construct new tree view of specified T2Reference
		JTree referenceTree = new JTree(new T2ReferenceTreeModel(reference,
				referenceService));
		expandAll(referenceTree, true);
		referenceTree.setCellRenderer(new T2ReferenceTreeCellRenderer());
		add(new JScrollPane(referenceTree), BorderLayout.CENTER);

		// Construct 'return to edit view' action
		editAction = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				handleEdit();
			}
		};
		editAction.putValue(Action.NAME, "Edit");
		editAction.putValue(Action.SMALL_ICON, editIcon);

		// Construct tool bar
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar
				.add(new JLabel("Database view, 'edit' to return to edit view."));
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(new JButton(editAction));
		add(toolBar, BorderLayout.NORTH);
	}

	private static void expandAll(JTree tree, boolean expand) {
		Object root = tree.getModel().getRoot();
		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	private static void expandAll(JTree tree, TreePath parent, boolean expand) {
		// Traverse children
		Object node = parent.getLastPathComponent();
		if (tree.getModel().getChildCount(node) >= 0) {
			for (int i = 0; i < tree.getModel().getChildCount(node); i++) {
				TreePath path = parent.pathByAddingChild(tree.getModel()
						.getChild(node, i));
				expandAll(tree, path, expand);
			}
		}
		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	/**
	 * Called when the user invokes the 'return to edit view' or similar with
	 * the intention of scrapping this existing T2Reference and constructing a
	 * new one through the PreRegistrationPanel
	 */
	public abstract void handleEdit();

}
