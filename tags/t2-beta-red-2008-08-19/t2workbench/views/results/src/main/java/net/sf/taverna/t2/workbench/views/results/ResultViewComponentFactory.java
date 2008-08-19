package net.sf.taverna.t2.workbench.views.results;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class ResultViewComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return new ResultViewComponent();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Results View";
	}

}
