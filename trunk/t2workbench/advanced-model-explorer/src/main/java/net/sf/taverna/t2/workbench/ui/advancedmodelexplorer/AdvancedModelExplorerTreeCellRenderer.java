package net.sf.taverna.t2.workbench.ui.advancedmodelexplorer;

import java.awt.Component;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.query.BeanshellActivityItem;
import net.sf.taverna.t2.activities.biomart.BiomartActivity;
import net.sf.taverna.t2.activities.biomart.query.BiomartActivityItem;
import net.sf.taverna.t2.activities.biomoby.BiomobyActivity;
import net.sf.taverna.t2.activities.biomoby.query.BiomobyActivityItem;
import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
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
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.ProcessorInputPort;
import net.sf.taverna.t2.workflowmodel.ProcessorOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;


public class AdvancedModelExplorerTreeCellRenderer extends
		DefaultTreeCellRenderer {

	private static final long serialVersionUID = -1326663036193567147L;
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		
		Component result = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
		
		if (userObject instanceof DataflowInputPort){
			((JLabel) result).setIcon(WorkbenchIcons.inputIcon);
			((JLabel) result).setText(((DataflowInputPort) userObject)
					.getName());
		}
		else if (userObject instanceof DataflowOutputPort){
			((JLabel) result).setIcon(WorkbenchIcons.outputIcon);
			((JLabel) result).setText(((DataflowOutputPort) userObject)
					.getName());
		}
		else if (userObject instanceof Processor) {
			
			// Get the activity associated with the procesor - currently only the first one in the list gets displayed
			List<? extends Activity<?>> activityList = ((Processor) userObject).getActivityList();
			Activity<?> activity = activityList.get(0);
			
			if (activity instanceof BeanshellActivity){
				((JLabel) result).setIcon(new ImageIcon(BeanshellActivityItem.class.getResource("/beanshell.png")));
			}
			else if (activity instanceof BiomartActivity){
				((JLabel) result).setIcon(new ImageIcon(BiomartActivityItem.class.getResource("/biomart.png")));
			}
			else if (activity instanceof BiomobyActivity){
				((JLabel) result).setIcon(new ImageIcon(BiomobyActivityItem.class.getResource("/registry.gif")));
			}
			else if (activity instanceof DataflowActivity){
				((JLabel) result).setIcon(new ImageIcon(DataflowActivityItem.class.getResource("/dataflow.png")));
			}
			else if (activity instanceof LocalworkerActivity){
				((JLabel) result).setIcon(new ImageIcon(LocalworkerActivityItem.class.getResource("/localworker.png")));
			}
			else if (activity instanceof RshellActivity){
				((JLabel) result).setIcon(new ImageIcon(RshellActivityItem.class.getResource("/rshell.png")));
			}
			else if (activity instanceof SoaplabActivity){
				((JLabel) result).setIcon(new ImageIcon(SoaplabActivityItem.class.getResource("/soaplab.png")));
			}
			else if (activity instanceof StringConstantActivity){
				((JLabel) result).setIcon(new ImageIcon(StringConstantActivityItem.class.getResource("/stringconstant.png")));
			}
			else if (activity instanceof WSDLActivity){
				((JLabel) result).setIcon(new ImageIcon(WSDLActivityItem.class.getResource("/wsdl.png")));
			}
			
			((JLabel) result).setText(((Processor) userObject).getLocalName());
		}
		// A child of a processor node under 'Workflow processors'
		else if(userObject instanceof ProcessorInputPort){
			((JLabel) result).setIcon(WorkbenchIcons.inputPortIcon);
			((JLabel) result).setText(((ProcessorInputPort) userObject)
					.getName());
		}
		// A child of a processor node under 'Workflow processors'
		else if(userObject instanceof ProcessorOutputPort){
			((JLabel) result).setIcon(WorkbenchIcons.outputPortIcon);
			((JLabel) result).setText(((ProcessorOutputPort) userObject)
					.getName());
		}
		else if (userObject instanceof Datalink){
			((JLabel) result).setIcon(WorkbenchIcons.outputPortIcon);
			
		}
		/*else if (((AdvancedModelExplorerTreeModel) tree.getModel()).getRoot().equals(value)){
			// If root
			((JLabel) result).setIcon(WorkbenchIcons.advancedModelExplorerIcon);
		}*/
		else{
			// If one of the main nodes - inputs, outputs, processors or
			// datalinks then for a non-expanded node always show the closedIcon
			// regardless of whether it has children or not
			if (expanded) {
				((JLabel) result).setIcon(WorkbenchIcons.folderOpenIcon);
			} else{
				((JLabel) result).setIcon(WorkbenchIcons.folderClosedIcon);
			}
		}
		
		return result;
	}

}
