package uk.org.mygrid.dataplaygroundui;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import net.sf.taverna.perspectives.AbstractPerspective;

public class PlaygroundPerspective extends AbstractPerspective {


	public ImageIcon getButtonIcon() {
		URL iconURL = PlaygroundUIComponentFactory.class.getResource("user-desktop.gif");
		System.out.println("URL = " + iconURL);
		if (iconURL == null) {
			return null;
		} else {
			return new ImageIcon(iconURL);
		}
	}

	public InputStream getLayoutResourceStream() {
		return PlaygroundPerspective.class.getResourceAsStream("/playground-perspective.xml");
	}

	public String getText() {
		return "Playground";
	}


}
