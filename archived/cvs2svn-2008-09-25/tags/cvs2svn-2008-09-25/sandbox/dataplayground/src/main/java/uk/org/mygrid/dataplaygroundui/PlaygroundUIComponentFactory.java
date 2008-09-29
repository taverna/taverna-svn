package uk.org.mygrid.dataplaygroundui;

import java.net.URL;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class PlaygroundUIComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return PlaygroundPanel.getInstance();
	}

	public ImageIcon getIcon() {
		URL iconURL = PlaygroundUIComponentFactory.class
				.getResource("user-desktop.gif");
		System.out.println("URL = " + iconURL);
		if (iconURL == null) {
			return null;
		} else {
			return new ImageIcon(iconURL);
		}
	}

	public String getName() {
		return "DataPlayground";
	}

}
