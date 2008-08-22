package net.sf.taverna.t2.workbench.ui.advancedmodelexplorer;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class AdvancedModelExplorerFactory implements UIComponentFactorySPI{

	public UIComponentSPI getComponent() {
		return AdvancedModelExplorer.getInstance();
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Advanced Model Explorer";
	}

}