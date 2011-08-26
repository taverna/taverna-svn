/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * A cell renderer that has a highlight field which will colour nodes in red if
 * they match the regular expression supplied.
 * 
 * @author Tom Oinn
 */
public class NodeColouringRenderer extends DefaultTreeCellRenderer {

	private String pattern = null;

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public NodeColouringRenderer() {
		super();
	}

	private boolean plain = false;

	public void setPlain(boolean plain) {
		this.plain = plain;
	}

	/**
	 * Create a new renderer which marks nodes that have text matching the
	 * regular expression in red
	 */
	public NodeColouringRenderer(String pattern) {
		super();
		this.pattern = pattern;
	}

	/**
	 * Return a custom renderer to draw the cell correctly for each node type
	 */
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
		if (!leaf) {
			if (expanded) {
				setIcon(ScuflIcons.folderOpenIcon);
			} else {
				setIcon(ScuflIcons.folderClosedIcon);
			}
		}
		// Do highlight
		String stringRepresentation = userObject.toString();
		// Strip out any <font></font><html></html> tags
		String stripped = stringRepresentation.replaceAll("</{0,1}font[^>]*>",
				"").replaceAll("</{0,1}html>", "");
		if (plain) {
			setText(stripped);
		} else {
			if (pattern != null && stripped.toLowerCase().matches(pattern)) {
				setText("<html><font color=\"red\">" + stripped
						+ "</font></html>");
			} else if (userObject.toString().indexOf("<") > -1
					&& userObject.toString().indexOf("<html>") == -1) {
				setText("<html><font color=\"black\">" + userObject.toString()
						+ "</font></html>");
			} else {
				setText(userObject.toString());
			}
		}
		return this;

	}
}
