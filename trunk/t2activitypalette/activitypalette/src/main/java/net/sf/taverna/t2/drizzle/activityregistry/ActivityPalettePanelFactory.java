package net.sf.taverna.t2.drizzle.activityregistry;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class ActivityPalettePanelFactory implements UIComponentFactorySPI {

	public String getName() {
		return "Services palette"; //$NON-NLS-1$
	}

	public ImageIcon getIcon() {
		return TavernaIcons.windowScavenger;
	}

	public UIComponentSPI getComponent() {
		return new ActivityPalettePanel();
	}

}
