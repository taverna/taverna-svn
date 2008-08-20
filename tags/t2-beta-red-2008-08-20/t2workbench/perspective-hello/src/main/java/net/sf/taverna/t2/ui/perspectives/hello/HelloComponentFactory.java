package net.sf.taverna.t2.ui.perspectives.hello;


import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class HelloComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return new HelloComponent();
	}

	public ImageIcon getIcon() {
		return WorkbenchIcons.databaseIcon;
	}

	public String getName() {
		return "The hello";
	}

}
