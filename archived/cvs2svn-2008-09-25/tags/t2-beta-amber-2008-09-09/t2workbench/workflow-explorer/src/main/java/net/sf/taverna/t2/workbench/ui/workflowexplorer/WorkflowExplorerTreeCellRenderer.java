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
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.query.BeanshellActivityItem;
import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.activities.biomart.query.BiomartActivityItem;
import net.sf.taverna.t2.activities.biomoby.BiomobyActivity;
import net.sf.taverna.t2.activities.biomoby.query.BiomobyActivityItem;
import net.sf.taverna.t2.activities.dataflow.query.DataflowActivityItem;
import net.sf.taverna.t2.activities.localworker.LocalworkerActivity;
import net.sf.taverna.t2.activities.localworker.query.LocalworkerActivityItem;
import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.query.RshellActivityItem;
import net.sf.taverna.t2.activities.soaplab.SoaplabActivity;
import net.sf.taverna.t2.activities.soaplab.query.SoaplabActivityItem;
import net.sf.taverna.t2.activities.stringconstant.StringConstantActivity;
import net.sf.taverna.t2.activities.stringconstant.query.StringConstantActivityItem;
import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.query.WSDLActivityItem;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflow;

/**
 * Cell renderer for Workflow Explorer tree.
 * 
 * @author Alex Nenadic
 *
 */

public class WorkflowExplorerTreeCellRenderer extends
		DefaultTreeCellRenderer {

	private static final long serialVersionUID = -1326663036193567147L;
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		
		Component result = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
		
		if (userObject instanceof DataflowInputPort){
			((WorkflowExplorerTreeCellRenderer) result).setIcon(WorkbenchIcons.inputIcon);
			((WorkflowExplorerTreeCellRenderer) result).setText(((DataflowInputPort) userObject)
					.getName());
		}
		else if (userObject instanceof DataflowOutputPort){
			((WorkflowExplorerTreeCellRenderer) result).setIcon(WorkbenchIcons.outputIcon);
			((WorkflowExplorerTreeCellRenderer) result).setText(((DataflowOutputPort) userObject)
					.getName());
		}
		else if (userObject instanceof Processor) {
			
			// Get the activity associated with the procesor - currently only the first one in the list gets displayed
			List<? extends Activity<?>> activityList = ((Processor) userObject).getActivityList();
			Activity<?> activity = activityList.get(0);
			
			if (activity instanceof BeanshellActivity){
				((WorkflowExplorerTreeCellRenderer) result).setIcon(new ImageIcon(BeanshellActivityItem.class.getResource("/beanshell.png")));
			}
			else if (activity instanceof BiomartActivity){
				((WorkflowExplorerTreeCellRenderer) result).setIcon(new ImageIcon(BiomartActivityItem.class.getResource("/biomart.png")));
			}
			else if (activity instanceof BiomobyActivity){
				((WorkflowExplorerTreeCellRenderer) result).setIcon(new ImageIcon(BiomobyActivityItem.class.getResource("/registry.gif")));
			}
			else if (activity instanceof NestedDataflow){
				((WorkflowExplorerTreeCellRenderer) result).setIcon(new ImageIcon(DataflowActivityItem.class.getResource("/dataflow.png")));
			}
			else if (activity instanceof LocalworkerActivity){
				((WorkflowExplorerTreeCellRenderer) result).setIcon(new ImageIcon(LocalworkerActivityItem.class.getResource("/localworker.png")));
			}
			else if (activity instanceof RshellActivity){
				((WorkflowExplorerTreeCellRenderer) result).setIcon(new ImageIcon(RshellActivityItem.class.getResource("/rshell.png")));
			}
			else if (activity instanceof SoaplabActivity){
				((WorkflowExplorerTreeCellRenderer) result).setIcon(new ImageIcon(SoaplabActivityItem.class.getResource("/soaplab.png")));
			}
			else if (activity instanceof StringConstantActivity){
				((WorkflowExplorerTreeCellRenderer) result).setIcon(new ImageIcon(StringConstantActivityItem.class.getResource("/stringconstant.png")));
			}
			else if (activity instanceof WSDLActivity){
				((WorkflowExplorerTreeCellRenderer) result).setIcon(new ImageIcon(WSDLActivityItem.class.getResource("/wsdl.png")));
			}
			
			((WorkflowExplorerTreeCellRenderer) result).setText(((Processor) userObject).getLocalName());
		}
		// Processor's child input port (from the associated activity)
		else if(userObject instanceof ActivityInputPort){
			((WorkflowExplorerTreeCellRenderer) result).setIcon(WorkbenchIcons.inputPortIcon);
			((WorkflowExplorerTreeCellRenderer) result).setText(((ActivityInputPort) userObject)
					.getName());
		}
		// Processor's child output port (from the associated activity)
		else if(userObject instanceof OutputPort){
			((WorkflowExplorerTreeCellRenderer) result).setIcon(WorkbenchIcons.outputPortIcon);
			((WorkflowExplorerTreeCellRenderer) result).setText(((OutputPort) userObject)
					.getName());
		}
		else if (userObject instanceof Datalink){
			((WorkflowExplorerTreeCellRenderer) result).setIcon(WorkbenchIcons.datalinkIcon);
			((WorkflowExplorerTreeCellRenderer) result)
					.setText(((Datalink) userObject).getSource().getName()
							+ " -> " + ((Datalink) userObject).getSink().getName());
		}
		else{
			// It is the root or one of the main nodes (inputs, outputs, 
			// processors or datalinks)
			if (expanded) {
				((WorkflowExplorerTreeCellRenderer) result).setIcon(WorkbenchIcons.folderOpenIcon);
			} else{
				((WorkflowExplorerTreeCellRenderer) result).setIcon(WorkbenchIcons.folderClosedIcon);
			}
		}
		
		return result;
	}

}
