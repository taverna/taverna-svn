/*
 * FetaResultRenderer.java
 *
 * Created on January 15, 2005, 3:03 PM
 */

package uk.ac.man.cs.img.fetaClient.queryGUI.taverna;

/**
 * 
 * @author alperp
 */

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

import uk.ac.man.cs.img.fetaClient.resource.FetaResources;

public class FetaResultRenderer extends DefaultTreeCellRenderer {

	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);
		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
		if (userObject instanceof BasicServiceModel)
		// for operations set their Taverna compliant icons
		{
			String tagName = ((BasicServiceModel) userObject)
					.getTavernaProcessorTag();
			ImageIcon icon = ProcessorHelper.getIconForTagName(tagName);
			if (icon != null) {
				setIcon(icon);
			}
		} else
		// for others (i.e. the root node for now) use the Feta icon
		{
			setIcon(FetaResources.getFetaIcon());
		}
		return this;
	}
}
