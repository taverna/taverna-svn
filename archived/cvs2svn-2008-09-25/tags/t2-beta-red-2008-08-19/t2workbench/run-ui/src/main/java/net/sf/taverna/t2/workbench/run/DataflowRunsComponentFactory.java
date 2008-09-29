package net.sf.taverna.t2.workbench.run;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class DataflowRunsComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return DataflowRunsComponent.getInstance();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Monitor View";
	}

}
