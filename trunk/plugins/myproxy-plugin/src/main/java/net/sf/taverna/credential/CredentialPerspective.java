package net.sf.taverna.credential;

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import net.sf.taverna.perspectives.AbstractPerspective;

public class CredentialPerspective extends AbstractPerspective {

	public ImageIcon getButtonIcon() {
		URL iconURL = CredentialPerspective.class
				.getResource("/t2cogs_16x16.png");
		if (iconURL == null) {
			return null;
		} else {
			return new ImageIcon(iconURL);
		}
	}

	public InputStream getLayoutResourceStream() {
		return CredentialPerspective.class
				.getResourceAsStream("/credential-perspective.xml");
	}

	public String getText() {
		return "Proxy Manager";
	}

}
