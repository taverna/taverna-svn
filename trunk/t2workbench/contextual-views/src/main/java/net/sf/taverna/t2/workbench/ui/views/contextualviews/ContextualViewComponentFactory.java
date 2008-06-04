package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class ContextualViewComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		// TODO Auto-generated method stub
		return new ContextualViewComponent();
	}

	public ImageIcon getIcon() {
		return WorkbenchIcons.databaseIcon;
	}

	public String getName() {
		return "Contextual View";
	}

}
