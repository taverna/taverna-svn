package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class ScavengerTreePanelFactory implements UIComponentFactorySPI {

	public String getName() {
		return "Services palette";
	}

	public ImageIcon getIcon() {
		return TavernaIcons.windowScavenger;
	}

	public UIComponentSPI getComponent() {
		return new ScavengerTreePanel();
	}

}
