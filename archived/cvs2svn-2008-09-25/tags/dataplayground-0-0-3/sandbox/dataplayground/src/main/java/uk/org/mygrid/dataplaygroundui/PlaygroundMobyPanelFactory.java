package uk.org.mygrid.dataplaygroundui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class PlaygroundMobyPanelFactory implements UIComponentFactorySPI {

	public String getName() {		
		return "Moby Panel";
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public UIComponentSPI getComponent() {
		return PlaygroundMobyPanel.getInstance();
	}

}
