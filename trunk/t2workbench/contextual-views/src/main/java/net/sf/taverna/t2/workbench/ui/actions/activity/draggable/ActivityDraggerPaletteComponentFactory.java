package net.sf.taverna.t2.workbench.ui.actions.activity.draggable;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class ActivityDraggerPaletteComponentFactory implements
		UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return new ActivityDraggerPaletteComponent();
	}

	public ImageIcon getIcon() {
		return WorkbenchIcons.databaseIcon;
	}

	public String getName() {
		return "Draggable Activity Pallette";
	}

}
