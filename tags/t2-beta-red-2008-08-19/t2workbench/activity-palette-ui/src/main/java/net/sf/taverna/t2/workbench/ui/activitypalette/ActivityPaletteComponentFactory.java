package net.sf.taverna.t2.workbench.ui.activitypalette;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.workbench.ui.zaria.UIComponentFactorySPI;
import net.sf.taverna.t2.workbench.ui.zaria.UIComponentSPI;

/**
 * Used to get the {@link ActivityPaletteComponent} containing the
 * {@link ActivityTree}
 * 
 * @author Ian Dunlop
 * 
 */
public class ActivityPaletteComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return ActivityPaletteComponent.getInstance();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Activity Palette";
	}

}
