package net.sf.taverna.t2.plugin;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import net.sf.taverna.perspectives.AbstractPerspective;

public class T2Perspective extends AbstractPerspective {

	public ImageIcon getButtonIcon() {
		URL iconURL = T2Perspective.class.getResource("/t2cogs_16x16.png");
		if (iconURL == null) {
			return null;
		} else {
			return new ImageIcon(iconURL);
		}
	}

	public InputStream getLayoutResourceStream() {
		return T2Perspective.class.getResourceAsStream("/t2-perspective.xml");
	}

	public String getText() {
		return "Taverna 2 preview";
	}

}
