/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.shared;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.TavernaIcons;

/**
 * A cell renderer that paints the appropriate icons depending on the component
 * of the model being displayed.
 * 
 * @author Tom Oinn
 */
public class ScuflModelExplorerRenderer extends NodeColouringRenderer {

	private Object significantObject = null;

	public void setSignificant(Object o) {
		if (o == null) {
			significantObject = null;
			return;
		}
		if (o instanceof Port) {
			significantObject = o;
			return;
		} else if (o instanceof Processor && ((Processor) o).getModel() != null) {
			significantObject = o;
			return;
		}
		significantObject = null;
	}

	/**
	 * Create a new explorer renderer with no regular expression based highlight
	 * operation
	 */
	public ScuflModelExplorerRenderer() {
		super();
	}

	/**
	 * Create a new renderer which marks nodes that have text matching the
	 * regular expression in red
	 */
	public ScuflModelExplorerRenderer(String pattern) {
		super(pattern);
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
		if (userObject instanceof AlternateProcessor) {
			userObject = ((AlternateProcessor) userObject).getProcessor();
		}
		if (userObject instanceof Processor) {
			setIcon(org.embl.ebi.escience.scuflworkers.ProcessorHelper
					.getPreferredIcon((Processor) userObject));
		} else if (userObject instanceof ConcurrencyConstraint) {
			setIcon(TavernaIcons.constraintIcon);
		} else if (userObject instanceof DataConstraint) {
			DataConstraint dc = (DataConstraint) userObject;
			if (significantObject != null) {
				String colour = null;
				if (significantObject instanceof Port) {
					Port p = (Port) significantObject;
					if (dc.getSource() == p) {
						colour = "#ff44bb";
					} else if (dc.getSink() == p) {
						colour = "#dd9922";
					}
				} else if (significantObject instanceof Processor) {
					Processor p = (Processor) significantObject;
					if (dc.getSource().getProcessor() == p) {
						colour = "#ff44bb";
					} else if (dc.getSink().getProcessor() == p) {
						colour = "#dd9922";
					}
				}
				if (colour != null) {
					setText("<html><font color=\"" + colour + "\">"
							+ dc.getName() + "</font></html>");
				}
			} else {
				setText(dc.getName());
			}
			setIcon(TavernaIcons.dataLinkIcon);
		} else if (userObject instanceof Port) {
			Port thePort = (Port) userObject;
			// Processor theProcessor = thePort.getProcessor();
			ScuflModel model = thePort.getProcessor().getModel();
			if (thePort.isSource()) {
				// Workflow source port
				setIcon(TavernaIcons.inputIcon);
			} else if (thePort.isSink()) {
				// Workflow sink port
				setIcon(TavernaIcons.outputIcon);
			} else {
				// Normal port
				if (thePort instanceof InputPort) {
					setIcon(TavernaIcons.inputPortIcon);
				} else if (thePort instanceof OutputPort) {
					setIcon(TavernaIcons.outputPortIcon);
				}
				// Check whether the port is part of an alternate processor
				if (model == null) {
					// Fetch the alternate processor itself
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
					DefaultMutableTreeNode alternateProcessorNode = (DefaultMutableTreeNode) node
							.getParent();
					AlternateProcessor theAlternate = (AlternateProcessor) alternateProcessorNode
							.getUserObject();
					String originalPortName = theAlternate
							.getPortTranslation(thePort.getName());
					if (originalPortName == null) {
						originalPortName = "<NO MAPPING>";
					}
					setText(thePort.getName() + " == " + originalPortName);
				} else {
					String toString = thePort.toString();
					String colour = null;
					String defaultText = "";
					if (thePort instanceof InputPort) {
						InputPort ip = (InputPort) thePort;
						boolean bound = ip.isBound();
						boolean optional = ip.isOptional();
						boolean hasdefault = ip.hasDefaultValue();
						if (bound == false && hasdefault == false
								&& optional == false) {
							colour = "red";
						} else if (bound == false && hasdefault == true) {
							colour = "green";
						} else if (bound == true && hasdefault == true) {
							colour = "purple";
						} else if (bound == false) {
							colour = "#888888";
						}
						if (colour != null) {
							toString = "<font color=\"" + colour + "\">"
									+ toString + "</font>";
						}
					}
					setText("<html>" + toString + " <font color=\"#666666\">"
							+ thePort.getSyntacticType() + "</font>"
							+ defaultText + "</html>");
				}
			}
		} else if (userObject instanceof ScuflModel) {
			setIcon(TavernaIcons.folderClosedIcon);
			setText(((ScuflModel) userObject).getDescription().getTitle());
		} else if (((DefaultMutableTreeNode) value).isLeaf()) {
			setIcon(TavernaIcons.folderClosedIcon);
		}
		return this;
	}
}
