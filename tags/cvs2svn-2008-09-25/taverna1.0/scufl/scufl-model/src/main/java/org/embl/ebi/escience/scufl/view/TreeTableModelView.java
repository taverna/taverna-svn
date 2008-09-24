/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.view;

import javax.swing.tree.DefaultMutableTreeNode;

import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.treetable.TreeTableModel;

/**
 * An extension of the TreeModelView to implement the TreeTable interfaces.
 * 
 * @author Tom Oinn
 */
public class TreeTableModelView extends TreeModelView implements TreeTableModel {

	private static String[] columnNames = { "Workflow object", "Retries",
			"Delay", "Backoff", "Threads", "Critical" };

	private static Class[] columnClasses = { TreeTableModel.class,
			Integer.class, Integer.class, Double.class, Integer.class,
			Boolean.class };

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int column) {
		return columnNames[column];
	}

	public Class getColumnClass(int column) {
		return columnClasses[column];
	}
	 
	public Object getValueAt(Object nodeObject, int column) {
		if (nodeObject == null) {
			return null;
		}
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeObject;
		if (column == 0) {
			return node.getUserObject().toString();
		}

		// OK,  handle the other columns.. only valid for Processor nodes
		Processor p = null;
		if (node.getUserObject() instanceof Processor) {
			p = (Processor) node.getUserObject();
		} else if (node.getUserObject() instanceof AlternateProcessor) {
			p = ((AlternateProcessor) node.getUserObject()).getProcessor();
		}
		if (p == null) {
			return null;
		}

		switch (column) {
		case 1:
			return new Integer(p.getRetries());
		case 2:
			return new Integer(p.getRetryDelay());
		case 3:
			return new Double(p.getBackoff());
		case 4:
			return new Integer(p.getWorkers());
		case 5:
			return new Boolean(p.getCritical());
		}
		return null;
	}

	public boolean isCellEditable(Object nodeObject, int column) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeObject;
		if (node == null) {
			return false;
		}
		// Always allow 'edits' on column 0, passes mouse events
		// through to be handled by the tree
		if (column == 0) {
			// Check whether this is a processor with a non null
			// model, i.e. not an alternate
			if (node.getUserObject() instanceof Processor) {
				return true;
            } else if (node.getUserObject() instanceof AlternateProcessor) {
				return true;
			} else if (node.getUserObject() instanceof Port) {

				Port port = (Port) node.getUserObject();
				if (port.isNameEditable()) {
					// It will be possible to edit the actual name of the port
					return true;
				} else if (node.getUserObject() instanceof InputPort) {
					// Allow setting of default value
					// FIXME: Should not edit default values in this nasty way
					InputPort ip = (InputPort) node.getUserObject();
					// FIXME: (Why would getModel return null here?)
					return (ip.getProcessor().getModel() != null);
				}
			}
			return false;
		}
		// OK to edit if we have some processor
		Processor p = null;
		if (node.getUserObject() instanceof Processor) {
			p = (Processor) node.getUserObject();
		} else if (node.getUserObject() instanceof AlternateProcessor) {
			p = ((AlternateProcessor) node.getUserObject()).getProcessor();
		}
		return (p != null);
	}

	public void setValueAt(Object value, Object nodeObject, int column) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodeObject;
		// Is it a Port?
		if (column == 0 && node.getUserObject() instanceof Port) {
			Port port = (Port) node.getUserObject();
			// Typical sinks and workflow inputs
			if (port.isNameEditable()) {
				port.setName((String) value);
			} else if (node.getUserObject() instanceof InputPort) {
				// FIXME: This is a confusing way to set default values
				((InputPort) port).setDefaultValue((String) value);
			}
			return;
		}

		// OK, then it must be a Processor
		Processor p = null;
		if (node.getUserObject() instanceof Processor) {
			p = (Processor) node.getUserObject();
		} else if (node.getUserObject() instanceof AlternateProcessor) {
			p = ((AlternateProcessor) node.getUserObject()).getProcessor();
		}
		if (p == null) {
			// No? Then we have no clue what to do but exit
			return;
		}

		switch (column) {
		case 0:
			p.setName((String) value);
			break;
		case 1:
			Integer retries = (Integer) value;
			p.setRetries(retries.intValue());
			break;
		case 2:
			Integer retryDelay = (Integer) value;
			p.setRetryDelay(retryDelay.intValue());
			break;
		case 3:
			Double backoff = (Double) value;
			p.setBackoff(backoff.doubleValue());
			break;
		case 4:
			Integer threads = (Integer) value;
			p.setWorkers(threads.intValue());
			break;
		case 5:
			p.setCritical(((Boolean) value).booleanValue());
			break;
		}
	}

	public void receiveModelEvent(ScuflModelEvent event) {
		super.receiveModelEvent(event);
	}
}
