package net.sf.taverna.t2.workbench.ui.views.contextualviews;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class DragActivitiesToHereComponentFactory implements UIComponentFactorySPI{

	public UIComponentSPI getComponent() {
		// TODO Auto-generated method stub
		return new DragActivitiesToHereComponent();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Drag Activities";
	}

}
