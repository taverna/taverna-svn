package net.sf.taverna.zaria;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.tree.TreeNode;

import org.jdom.Element;

/**
 * Abstract superclass of all Zaria node components,
 * extends JComponent and adds basic tree traversal
 * functionality.
 * @author Tom Oinn
 */
public abstract class ZPane extends JComponent implements ZTreeNode {

	protected boolean editable = false;
	protected JToolBar toolBar = new JToolBar();
	
	protected ZPane() {
		super();
		toolBar.setFloatable(false);
		setLayout(new BorderLayout());
	}
	
	protected class ReplaceWithBlankAction extends AbstractAction {
		public ReplaceWithBlankAction() {
			super();
			putValue(Action.NAME,"Clear");
		}
		public void actionPerformed(ActionEvent arg0) {
			replaceWith(new ZBlankComponent());
		}
	}
	
	/**
	 * Traverse up the swing container heirarchy looking for the
	 * first parent implementing ZTreeNode, or null if we fall off 
	 * the top of the container heirarchy.
	 */
	public ZTreeNode getZParent() {
		Component c = this;
		while (c != null) {
			c = c.getParent();
			if (c instanceof ZTreeNode) {
				return (ZTreeNode)c;
			}
		}
		return null;
	}

	public int getZChildCount() {
		return getZChildren().size();
	}

	public boolean isZRoot() {
		return (getZParent() == null);
	}

	public boolean isZLeaf() {
		return (getZChildren().isEmpty());
	}
	
	/**
	 * If setting editable from false to true
	 * generates the toolbar from the getActions
	 * method of the subclass and displays it
	 * otherwise hides the toolbar.
	 */
	public void setEditable(boolean b) {
		if (b!=editable) {
			editable = b;
			if (editable) {
				toolBar.removeAll();
				for (Action a : getActions()) {
					toolBar.add(a);
				}
				add(toolBar,BorderLayout.NORTH);
			}
			else {
				remove(toolBar);
			}
		}
	}
	
	/**
	 * Replace this component with the specified new one
	 */
	protected void replaceWith(ZTreeNode newComponent) {
		if (this.isZRoot()) {
			// Do nothing, we're the root
			// component and can't be swapped out
			return;
		}
		else {
			ZTreeNode parent = getZParent();
			parent.swap(this, newComponent);
			((Component)parent).repaint();
		}
	}

}
