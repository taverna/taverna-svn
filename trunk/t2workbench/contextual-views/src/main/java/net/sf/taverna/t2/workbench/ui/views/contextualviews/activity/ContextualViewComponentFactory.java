package net.sf.taverna.t2.workbench.ui.views.contextualviews.activity;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class ContextualViewComponentFactory implements UIComponentFactorySPI{

	public UIComponentSPI getComponent() {
		return new ContextualViewComponent();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Contextual View";
	}

}
