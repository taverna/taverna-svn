package net.sf.taverna.t2.workbench.views.inputs;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class InputViewComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return new InputViewComponent();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Inputs View";
	}

}
