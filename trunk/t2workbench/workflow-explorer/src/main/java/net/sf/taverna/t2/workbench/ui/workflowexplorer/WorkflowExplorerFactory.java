package net.sf.taverna.t2.workbench.ui.workflowexplorer;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

/**
 * Workflow Explorer factory.
 * 
 * @author Alex Nenadic
 *
 */
public class WorkflowExplorerFactory implements UIComponentFactorySPI{

	public UIComponentSPI getComponent() {
		return WorkflowExplorer.getInstance();
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Workflow Explorer";
	}

}