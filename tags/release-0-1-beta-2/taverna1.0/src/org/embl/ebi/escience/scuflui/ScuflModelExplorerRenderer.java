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
import org.embl.ebi.escience.scufl.*;

import org.embl.ebi.escience.scuflui.ScuflIcons;
import java.lang.Object;



/**
 * A cell renderer that paints the appropriate icons depending on the
 * component of the model being displayed.
 * @author Tom Oinn
 */
public class ScuflModelExplorerRenderer extends DefaultTreeCellRenderer {
    
    /**
     * Return a custom renderer to draw the cell correctly for each node type
     */
    public Component getTreeCellRendererComponent(JTree tree,
						  Object value,
						  boolean sel,
						  boolean expanded,
						  boolean leaf,
						  int row,
						  boolean hasFocus) {
	super.getTreeCellRendererComponent(tree, value, sel,
					   expanded, leaf, row,
					   hasFocus);
	Object userObject = ((DefaultMutableTreeNode)value).getUserObject();
	if (userObject instanceof Processor) {
	    if (userObject instanceof WSDLBasedProcessor) {
		setIcon(ScuflIcons.wsdlIcon);
	    }
	    else if (userObject instanceof TalismanProcessor) {
		setIcon(ScuflIcons.talismanIcon);
	    }
	    else if (userObject instanceof SoaplabProcessor) {
		setIcon(ScuflIcons.soaplabIcon);
	    }
	}
	else if (userObject instanceof ConcurrencyConstraint) {
	    setIcon(ScuflIcons.constraintIcon);
	}
	else if (userObject instanceof Port) {
	    Port thePort = (Port)userObject;
	    Processor theProcessor = thePort.getProcessor();
	    ScuflModel model = theProcessor.getModel();
	    if (theProcessor == model.getWorkflowSourceProcessor()) {
		// Workflow source port
		setIcon(ScuflIcons.inputIcon);
	    }
	    else if (theProcessor == model.getWorkflowSinkProcessor()) {
		// Workflow sink port
		setIcon(ScuflIcons.outputIcon);
	    }
	    else {
		// Normal port
		if (thePort instanceof InputPort) {
		    setIcon(ScuflIcons.inputPortIcon);
		}
		else if (thePort instanceof OutputPort) {
		    setIcon(ScuflIcons.outputPortIcon);
		}
	    }
	}
	else if (userObject instanceof DataConstraint) {
	    setIcon(ScuflIcons.dataLinkIcon);
	}
	return this;
    }
}
