package uk.org.mygrid.dataplaygroundui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class PlaygroundInputPanelFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return PlaygroundInputPanel.getInstance();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Data Input";
	}

}
