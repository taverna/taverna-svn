package ${packageName};

import java.io.InputStream;
import java.net.URL;

import javax.swing.ImageIcon;

import net.sf.taverna.perspectives.AbstractPerspective;

public class ExamplePerspective extends AbstractPerspective {

	public ImageIcon getButtonIcon() {
		URL iconURL = ExamplePerspective.class.getResource("/example.png");
		if (iconURL == null) {
			return null;
		} else {
			return new ImageIcon(iconURL);
		}
	}

	public InputStream getLayoutResourceStream() {
		return ExamplePerspective.class.getResourceAsStream("/example-perspective.xml");
	}

	public String getText() {
		return "Example";
	}

}
