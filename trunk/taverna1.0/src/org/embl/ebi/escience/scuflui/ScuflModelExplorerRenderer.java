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
    
    static ImageIcon wsdlicon, soaplabicon, talismanicon, inputicon, outputicon, inputporticon, outputporticon, datalinkicon, constrainticon;
    
    static {
	// Load the image files found in this package into the class.
	try {
	    Class c = Class.forName("org.embl.ebi.escience.scuflui.ScuflModelExplorerRenderer");
	    wsdlicon = new ImageIcon(c.getResource("wsdl.gif"));
	    talismanicon = new ImageIcon(c.getResource("talisman.gif"));
	    soaplabicon = new ImageIcon(c.getResource("soaplab.gif"));
	    inputporticon = new ImageIcon(c.getResource("inputport.gif"));
	    outputporticon = new ImageIcon(c.getResource("outputport.gif"));
	    datalinkicon = new ImageIcon(c.getResource("datalink.gif"));
	    inputicon = new ImageIcon(c.getResource("input.gif"));
	    outputicon = new ImageIcon(c.getResource("output.gif"));
	    constrainticon = new ImageIcon(c.getResource("constraint.gif"));
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
		setIcon(wsdlicon);
	    }
	    else if (userObject instanceof TalismanProcessor) {
		setIcon(talismanicon);
	    }
	    else if (userObject instanceof SoaplabProcessor) {
		setIcon(soaplabicon);
	    }
	}
	else if (userObject instanceof ConcurrencyConstraint) {
	    setIcon(constrainticon);
	}
	else if (userObject instanceof Port) {
	    Port thePort = (Port)userObject;
	    Processor theProcessor = thePort.getProcessor();
	    ScuflModel model = theProcessor.getModel();
	    if (theProcessor == model.getWorkflowSourceProcessor()) {
		// Workflow source port
		setIcon(inputicon);
	    }
	    else if (theProcessor == model.getWorkflowSinkProcessor()) {
		// Workflow sink port
		setIcon(outputicon);
	    }
	    else {
		// Normal port
		if (thePort instanceof InputPort) {
		    setIcon(inputporticon);
		}
		else if (thePort instanceof OutputPort) {
		    setIcon(outputporticon);
		}
	    }
	}
	else if (userObject instanceof DataConstraint) {
	    setIcon(datalinkicon);
	}
	return this;
    }
}
