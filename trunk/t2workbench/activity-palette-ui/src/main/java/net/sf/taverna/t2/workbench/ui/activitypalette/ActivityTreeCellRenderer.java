package net.sf.taverna.t2.workbench.ui.activitypalette;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import net.sf.taverna.t2.partition.ActivityItem;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

public class ActivityTreeCellRenderer extends DefaultTreeCellRenderer implements
		TreeCellRenderer {

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		
		Component result = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		if (leaf && result instanceof ActivityTreeCellRenderer && value instanceof ActivityItem) {
			ActivityItem item = (ActivityItem)value;
			((ActivityTreeCellRenderer)result).setIcon(item.getIcon());
		}
		else{
			if (expanded) {
				((ActivityTreeCellRenderer)result).setIcon(WorkbenchIcons.folderOpenIcon);
			} else{
				((ActivityTreeCellRenderer)result).setIcon(WorkbenchIcons.folderClosedIcon);
			}
		}
		return result;
	}

}
