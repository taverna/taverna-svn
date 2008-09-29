package uk.org.mygrid.dataplaygroundui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class PlaygroundRendererPanelFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return PlaygroundRendererPanel.getInstance();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		return "Data Viewer";
	}

}
