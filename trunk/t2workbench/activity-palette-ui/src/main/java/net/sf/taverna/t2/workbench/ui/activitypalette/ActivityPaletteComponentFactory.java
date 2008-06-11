package net.sf.taverna.t2.workbench.ui.activitypalette;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

public class ActivityPaletteComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return new ActivityPaletteComponent();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
