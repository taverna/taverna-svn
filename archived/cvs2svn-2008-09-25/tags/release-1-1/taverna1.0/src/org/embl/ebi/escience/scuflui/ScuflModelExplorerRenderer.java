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
public class ScuflModelExplorerRenderer extends NodeColouringRenderer {
    
    /**
     * Create a new explorer renderer with no regular expression based
     * highlight operation
     */
    public ScuflModelExplorerRenderer() {
	super();
    }
    
    /**
     * Create a new renderer which marks nodes that have text matching
     * the regular expression in red
     */
    public ScuflModelExplorerRenderer(String pattern) {
	super(pattern);
    }

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
	if (userObject instanceof AlternateProcessor) {
	    userObject = ((AlternateProcessor)userObject).getProcessor();
	}
	if (userObject instanceof Processor) {
	    setIcon(org.embl.ebi.escience.scuflworkers.ProcessorHelper.getPreferredIcon((Processor)userObject));
	}
	else if (userObject instanceof ConcurrencyConstraint) {
	    setIcon(ScuflIcons.constraintIcon);
	}
	else if (userObject instanceof Port) {
	    Port thePort = (Port)userObject;
	    //Processor theProcessor = thePort.getProcessor();
	    ScuflModel model = thePort.getProcessor().getModel();
	    if (thePort.isSource()) {
		// Workflow source port
		setIcon(ScuflIcons.inputIcon);
	    }
	    else if (thePort.isSink()) {
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
		// Check whether the port is part of an alternate processor
		if (model == null) {
		    // Fetch the alternate processor itself
		    DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
		    DefaultMutableTreeNode alternateProcessorNode = (DefaultMutableTreeNode)node.getParent();
		    AlternateProcessor theAlternate = (AlternateProcessor)alternateProcessorNode.getUserObject();
		    String originalPortName = theAlternate.getPortTranslation(thePort.getName());
		    if (originalPortName == null) {
			originalPortName = "<NO MAPPING>";
		    }
		    setText(thePort.getName()+" == "+originalPortName);
		}
		else {
		    setText("<html>"+thePort.toString()+" <font color=\"#666666\">"+thePort.getSyntacticType()+"</font></html");
		}
	    }
	}
	else if (userObject instanceof DataConstraint) {
	    setIcon(ScuflIcons.dataLinkIcon);
	}
	else if (((DefaultMutableTreeNode)value).isLeaf()) {
	    setIcon(ScuflIcons.folderClosedIcon);
	}
	return this;
    }
}
