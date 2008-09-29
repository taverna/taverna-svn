/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import org.embl.ebi.escience.scufl.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.datatransfer.*;
import org.embl.ebi.escience.treetable.*;


/**
 * An extension of the TreeModelView to implement the TreeTable
 * interfaces.
 * @author Tom Oinn
 */
public class TreeTableModelView extends TreeModelView implements TreeTableModel {

    private static String[] columnNames = {"Workflow object", "Retries", "Delay", "Backoff"};
    private static Class[] columnClasses = { TreeTableModel.class,
					     Integer.class,
					     Integer.class,
					     Double.class };
    public int getColumnCount() {
	return columnNames.length;
    }
    public String getColumnName(int column) {
	return columnNames[column];
    }
    public Class getColumnClass(int column) {
	return columnClasses[column];
    }
    /**
     * The interesting bit
     */
    public Object getValueAt(Object nodeObject, int column) {
	if (nodeObject == null) {
	    return null;
	}
	DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodeObject;
	Processor p = null;
	if (node.getUserObject() instanceof Processor) {
	    p = (Processor)node.getUserObject();
	}
	else if (node.getUserObject() instanceof AlternateProcessor) {
	    p = ((AlternateProcessor)node.getUserObject()).getProcessor();
	}
	switch(column) {
	case 0:
	    return node.getUserObject().toString();
	case 1:
	    if (p != null) {
		return new Integer(p.getRetries());
	    }
	case 2:
	    if (p != null) {
		return new Integer(p.getRetryDelay());
	    }
	case 3:
	    if (p != null) {
		return new Double(p.getBackoff());
	    }
	}
	return null;
    }
    public boolean isCellEditable(Object nodeObject, int column) {	
	DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodeObject;
	if (node == null) {
	    return false;
	}
	Processor p = null;
	if (node.getUserObject() instanceof Processor) {
	    p = (Processor)node.getUserObject();
	}
	else if (node.getUserObject() instanceof AlternateProcessor) {
	    p = ((AlternateProcessor)node.getUserObject()).getProcessor();
	}
	// Always allow 'edits' on column 0, passes mouse events
	// through to be handled by the tree
	if (column == 0) {
	    // Check whether this is a processor with a non null
	    // model, i.e. not an alternate
	    if (node.getUserObject() instanceof Processor) {
		return true;
	    }
	    else if (node.getUserObject() instanceof Port) {
		// Check whether this is a top level workflow source or
		// sink port
		Port port = (Port)node.getUserObject();
		if (port.isSource() || port.isSink()) {
		    return true;
		}
	    }
	    else {
		return false;
	    }
	}
	if (p == null) {
	    return false;
	}
	return true;
    }
    public void setValueAt(Object value, Object nodeObject, int column) {
	//System.out.println("Setting value at column "+column+" to "+value.toString());
	DefaultMutableTreeNode node = (DefaultMutableTreeNode)nodeObject;
	Processor p = null;
	if (node.getUserObject() instanceof Processor) {
	    p = (Processor)node.getUserObject();
	}
	else if (node.getUserObject() instanceof AlternateProcessor) {
	    p = ((AlternateProcessor)node.getUserObject()).getProcessor();
	}
	if (p!=null) {
	    switch (column) {
	    case 0:
		p.setName((String)value);
		return;
	    case 1:
		Integer retries = (Integer)value;
		p.setRetries(retries.intValue());
		return;
	    case 2:
		Integer retryDelay = (Integer)value;
		p.setRetryDelay(retryDelay.intValue());
		return;
	    case 3:
		Double backoff = (Double)value;
		p.setBackoff(backoff.doubleValue());
		return;
	    }
	}
	else if (node.getUserObject() instanceof Port) {
	    Port thePort = (Port)node.getUserObject();
	    Processor theProcessor = thePort.getProcessor();
	    ScuflModel model = theProcessor.getModel();
	    // Check that the port is a workflow input or output
	    if (thePort.isSource() || thePort.isSink()) {
		thePort.setName((String)value);
	    }
	}
    }
    public void receiveModelEvent(ScuflModelEvent event) {
	super.receiveModelEvent(event);
    }
    
}
