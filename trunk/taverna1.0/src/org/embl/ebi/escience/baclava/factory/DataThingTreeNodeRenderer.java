/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.factory;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.embl.ebi.escience.baclava.DataThing;




/**
* A default renderer implementation for the tree
* nodes generated by this factory
* @author Tom Oinn
*/
public class DataThingTreeNodeRenderer extends DefaultTreeCellRenderer {
    static ImageIcon textIcon, binaryIcon, imageIcon;
    static {
	try {
	    textIcon = new ImageIcon(ClassLoader.getSystemResource("org/embl/ebi/escience/baclava/icons/text.png"));
	    imageIcon = new ImageIcon(ClassLoader.getSystemResource("org/embl/ebi/escience/baclava/icons/image.png"));
	    binaryIcon = new ImageIcon(ClassLoader.getSystemResource("org/embl/ebi/escience/baclava/icons/application.png"));
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}
    }
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
	super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	DataThingTreeNode theNode = (DataThingTreeNode)value;
	Object userObject = theNode.getUserObject();
	DataThing theDataThing = theNode.getDataThing();
	// If a leaf then do magic...
	if (theNode.isLeaf()) {
	    String syntacticType = theDataThing.getSyntacticTypeForObject(userObject);
	    String mimeTypes = syntacticType.split("'")[1].toLowerCase();
	    setText(mimeTypes);
	    if (mimeTypes.matches(".*text/.*")) {
		setIcon(textIcon);
		// If possible then show a textual representation as well as just the
		// mime type
		if (userObject instanceof String) {
		    String summaryText = (String)userObject;
		    if (summaryText.length() > 100) {
			summaryText = "<em>Click to view...</em>";
		    }
		    setText("<html><font color=\"#666666\">"+mimeTypes+"</font><br>"+
			    summaryText+"</html>");
		}
	    }
	    else if (mimeTypes.matches(".*image/.*")) {
		setIcon(imageIcon);
	    }
	    else if (mimeTypes.matches(".*application/.*")) {
		setIcon(binaryIcon);
	    }
	}
	return this;
    }
}
