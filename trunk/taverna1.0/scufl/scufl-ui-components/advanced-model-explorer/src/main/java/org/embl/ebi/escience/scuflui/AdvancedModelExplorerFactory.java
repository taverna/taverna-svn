package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class AdvancedModelExplorerFactory implements UIComponentFactorySPI {

	public String getName() {
		return "Advanced Model Explorer";
	}

	public ImageIcon getIcon() {
		return TavernaIcons.windowExplorer;
	}

	public UIComponentSPI getComponent() {
		return new AdvancedModelExplorer();
	}

}
