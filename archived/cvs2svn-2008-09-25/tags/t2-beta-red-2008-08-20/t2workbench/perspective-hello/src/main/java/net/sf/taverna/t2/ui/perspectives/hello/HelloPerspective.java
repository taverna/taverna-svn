package net.sf.taverna.t2.ui.perspectives.hello;

import java.io.InputStream;

import javax.swing.ImageIcon;

import org.jdom.Element;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

public class HelloPerspective implements PerspectiveSPI {

	private boolean visible = true;

	public ImageIcon getButtonIcon() {
		return WorkbenchIcons.findIcon;
	}

	public InputStream getLayoutInputStream() {
		return getClass().getResourceAsStream("hello-perspective.xml");
	}

	public String getText() {
		return "Hello there";
	}

	public boolean isVisible() {
		return visible;
	}

	public int positionHint() {
		return 10;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		
	}

	public void update(Element layoutElement) {
		// TODO Auto-generated method stub
		
		// Not sure what to do here
	}

}
