/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.treeview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.shared.ScuflContextMenuFactory;
import org.embl.ebi.escience.scufl.view.TreeModelView;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.TemplateEditor;
import org.embl.ebi.escience.scuflui.shared.NoContextMenuFoundException;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.shared.UIUtils;

/**
 * A class to handle popup menus on nodes on the ScuflModelExplorer tree
 * 
 * @author Tom Oinn
 */
public class ScuflModelExplorerPopupHandler extends MouseAdapter {

	JTree explorer;

	TreeModelView model;

	JComponent owner;

	public ScuflModelExplorerPopupHandler(ScuflModelExplorer theExplorer) {
		this.explorer = theExplorer;
		this.model = theExplorer.treeModel;
		this.owner = theExplorer;
	}

	public ScuflModelExplorerPopupHandler(ScuflModelTreeTable theTable) {
		explorer = theTable.getTree();
		model = theTable.treeModel;
		this.owner = theTable;
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
	 * If the event was a trigger for a popup then use the
	 * ScuflContextMenuFactory to find a suitable JPopupMenu for the node that
	 * was clicked on and display it. If we couldn't find anything suitable do
	 * nothing, all it means is that there wasn't a menu available for that type
	 * of node.
	 */
	void doEvent(MouseEvent e) {
		DefaultMutableTreeNode node = null;
		try {
			node = (DefaultMutableTreeNode) (explorer.getPathForLocation(e
					.getX(), e.getY()).getLastPathComponent());
		} catch (NullPointerException npe) {
			return;
		}
		Object scuflObject = node.getUserObject();
		if (scuflObject != null) {
			try {
				final MouseEvent theMouseEvent = e;
				JPopupMenu theMenu = ScuflContextMenuFactory.getMenuForObject(
						node, scuflObject, model.getModel());
				if (scuflObject instanceof Processor) {
					final Processor theProcessor = (Processor) scuflObject;
					theMenu.addSeparator();
					theMenu.add(new ShadedLabel("Annotations",
							ShadedLabel.TAVERNA_BLUE));
					theMenu.addSeparator();
					JMenuItem editTemplates = new JMenuItem(
							"Edit templates...", TavernaIcons.editIcon);
					editTemplates.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent a) {
							UIUtils.createFrame(theProcessor.getModel(),
									new TemplateEditor(theProcessor), 100, 100,
									300, 300);
						}
					});
					theMenu.add(editTemplates);
				}
				theMenu.show(owner, e.getX(), e.getY());
			} catch (NoContextMenuFoundException ncmfe) {
				// just means that there wasn't a suitable menu for the selected
				// node.
			}
		}
	}

}
