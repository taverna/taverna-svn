/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.embl.ebi.escience.scufl.*;

import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.Object;



/**
 * A cell renderer that paints the appropriate icons depending on the
 * component of the model being displayed.
 * @author Tom Oinn
 */
public class ScuflModelExplorerRenderer extends DefaultTreeCellRenderer {
    
    public static ImageIcon wsdlIcon, soaplabIcon, talismanIcon, inputIcon, outputIcon, inputPortIcon, outputPortIcon, dataLinkIcon, constraintIcon;
    
    static {
	// Load the image files found in this package into the class.
	try {
	    Class c = Class.forName("org.embl.ebi.escience.scuflui.ScuflModelExplorerRenderer");
	    wsdlIcon = new ImageIcon(c.getResource("wsdl.gif"));
	    talismanIcon = new ImageIcon(c.getResource("talisman.gif"));
	    soaplabIcon = new ImageIcon(c.getResource("soaplab.gif"));
	    inputPortIcon = new ImageIcon(c.getResource("inputport.gif"));
	    outputPortIcon = new ImageIcon(c.getResource("outputport.gif"));
	    dataLinkIcon = new ImageIcon(c.getResource("datalink.gif"));
	    inputIcon = new ImageIcon(c.getResource("input.gif"));
	    outputIcon = new ImageIcon(c.getResource("output.gif"));
	    constraintIcon = new ImageIcon(c.getResource("constraint.gif"));
	}
	catch (ClassNotFoundException cnfe) {
	    //
	}
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
	if (userObject instanceof Processor) {
	    if (userObject instanceof WSDLBasedProcessor) {
		setIcon(wsdlIcon);
	    }
	    else if (userObject instanceof TalismanProcessor) {
		setIcon(talismanIcon);
	    }
	    else if (userObject instanceof SoaplabProcessor) {
		setIcon(soaplabIcon);
	    }
	}
	else if (userObject instanceof ConcurrencyConstraint) {
	    setIcon(constraintIcon);
	}
	else if (userObject instanceof Port) {
	    Port thePort = (Port)userObject;
	    Processor theProcessor = thePort.getProcessor();
	    ScuflModel model = theProcessor.getModel();
	    if (theProcessor == model.getWorkflowSourceProcessor()) {
		// Workflow source port
		setIcon(inputIcon);
	    }
	    else if (theProcessor == model.getWorkflowSinkProcessor()) {
		// Workflow sink port
		setIcon(outputIcon);
	    }
	    else {
		// Normal port
		if (thePort instanceof InputPort) {
		    setIcon(inputPortIcon);
		}
		else if (thePort instanceof OutputPort) {
		    setIcon(outputPortIcon);
		}
	    }
	}
	else if (userObject instanceof DataConstraint) {
	    setIcon(dataLinkIcon);
	}
	return this;
    }
}
