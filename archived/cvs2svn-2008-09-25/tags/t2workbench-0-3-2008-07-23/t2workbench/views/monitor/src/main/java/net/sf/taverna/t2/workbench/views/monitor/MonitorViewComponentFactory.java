package net.sf.taverna.t2.workbench.views.monitor;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class MonitorViewComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return new MonitorViewComponent();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Monitor View";
	}

}
