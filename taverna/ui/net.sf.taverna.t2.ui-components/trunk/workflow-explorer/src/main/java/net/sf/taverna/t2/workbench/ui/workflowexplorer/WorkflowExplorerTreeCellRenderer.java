/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.ui.workflowexplorer;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.lang.ui.icons.Icons;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.report.ReportManager;

import org.apache.commons.beanutils.BeanUtils;

import uk.org.taverna.scufl2.api.activity.Activity;
import uk.org.taverna.scufl2.api.common.Scufl2Tools;
import uk.org.taverna.scufl2.api.core.BlockingControlLink;
import uk.org.taverna.scufl2.api.core.DataLink;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.port.InputProcessorPort;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputProcessorPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.api.port.Port;
import uk.org.taverna.scufl2.api.port.ProcessorPort;
import uk.org.taverna.scufl2.api.port.ReceiverPort;
import uk.org.taverna.scufl2.api.port.SenderPort;
import uk.org.taverna.scufl2.api.profiles.ProcessorBinding;

/**
 * Cell renderer for Workflow Explorer tree.
 *
 * @author Alex Nenadic
 * @author David Withers
 */
public class WorkflowExplorerTreeCellRenderer extends DefaultTreeCellRenderer {

	private static final long serialVersionUID = -1326663036193567147L;

	private final ActivityIconManager activityIconManager;

	private final String RUNS_AFTER = " runs after ";

	private Workflow workflow = null;
	private final ReportManager reportManager;

	private Scufl2Tools scufl2Tools = new Scufl2Tools();

	public WorkflowExplorerTreeCellRenderer(Workflow workflow, ReportManager reportManager,
			ActivityIconManager activityIconManager) {
		super();
		this.workflow = workflow;
		this.reportManager = reportManager;
		this.activityIconManager = activityIconManager;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
			boolean expanded, boolean leaf, int row, boolean hasFocus) {

		Component result = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);

		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
		// TODO rewrite report manager to use scufl2 validation
		// Status status = reportManager.getStatus(workflow, userObject);
		Status status = Status.OK;
		WorkflowExplorerTreeCellRenderer renderer = (WorkflowExplorerTreeCellRenderer) result;

		if (userObject instanceof Workflow) { // the root node
			if (!hasGrandChildren((DefaultMutableTreeNode) value)) {
				renderer.setIcon(WorkbenchIcons.workflowExplorerIcon);
			} else {
				renderer.setIcon(chooseIcon(WorkbenchIcons.workflowExplorerIcon, status));
			}
			renderer.setText(((Workflow) userObject).getName());
		} else if (userObject instanceof InputWorkflowPort) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.inputIcon, status));
			renderer.setText(((InputWorkflowPort) userObject).getName());
		} else if (userObject instanceof OutputWorkflowPort) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.outputIcon, status));
			renderer.setText(((OutputWorkflowPort) userObject).getName());
		} else if (userObject instanceof Processor) {
			Processor p = (Processor) userObject;
			// Get the activity associated with the processor - currently only
			// the first one in the list gets displayed
			List<ProcessorBinding> processorbindings = scufl2Tools.processorBindingsForProcessor(p, p.getParent().getParent().getMainProfile());
			String text = p.getName();
			if (!processorbindings.isEmpty()) {
				Activity activity = processorbindings.get(0).getBoundActivity();
				Icon basicIcon = activityIconManager.iconForActivity(activity);
				renderer.setIcon(chooseIcon(basicIcon, status));

				String extraDescription;
				try {
					extraDescription = BeanUtils.getProperty(activity, "extraDescription");
					text += " - " + extraDescription;
				} catch (IllegalAccessException e) {
					// no problem
				} catch (InvocationTargetException e) {
					// no problem
				} catch (NoSuchMethodException e) {
					// no problem;
				}
			}
			renderer.setText(text);
		}
		// Processor's child input port
		else if (userObject instanceof InputProcessorPort) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.inputPortIcon, status));
			renderer.setText(((InputProcessorPort) userObject).getName());
		}
		// Processor's child output port
		else if (userObject instanceof OutputProcessorPort) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.outputPortIcon, status));
			renderer.setText(((OutputProcessorPort) userObject).getName());
		} else if (userObject instanceof DataLink) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.datalinkIcon, status));
			SenderPort source = ((DataLink) userObject).getReceivesFrom();
			String sourceName = findName(source);
			ReceiverPort sink = ((DataLink) userObject).getSendsTo();
			String sinkName = findName(sink);
			renderer.setText(sourceName + " -> " + sinkName);
		} else if (userObject instanceof BlockingControlLink) {
			renderer.setIcon(chooseIcon(WorkbenchIcons.controlLinkIcon, status));
			String htmlText = "<html><head></head><body>"
					+ ((BlockingControlLink) userObject).getBlock().getName() + " " + RUNS_AFTER + " "
					+ ((BlockingControlLink) userObject).getUntilFinished().getName() + "</body></html>";
			renderer.setText(htmlText);

		} else {
			// It one of the main container nodes (inputs, outputs,
			// processors, datalinks) or a nested workflow node
			if (expanded) {
				renderer.setIcon(WorkbenchIcons.folderOpenIcon);
			} else {
				renderer.setIcon(WorkbenchIcons.folderClosedIcon);
			}
		}

		return result;
	}

	private static Icon chooseIcon(final Icon basicIcon, Status status) {
		if (status == null) {
			return basicIcon;
		}
		if (status == Status.OK) {
			return basicIcon;
		} else if (status == Status.WARNING) {
			return Icons.warningIcon;
		} else if (status == Status.SEVERE) {
			return Icons.severeIcon;
		}
		return basicIcon;
	}

	private static boolean hasGrandChildren(DefaultMutableTreeNode node) {
		int childCount = node.getChildCount();
		for (int i = 0; i < childCount; i++) {
			if (node.getChildAt(i).getChildCount() > 0) {
				return true;
			}
		}
		return false;
	}

	private String findName(Port port) {
		if (port instanceof ProcessorPort) {
			String sourceProcessorName = ((ProcessorPort) port).getParent().getName();
			return sourceProcessorName + ":" + port.getName();
		} else {
			return port.getName();
		}
	}
}
