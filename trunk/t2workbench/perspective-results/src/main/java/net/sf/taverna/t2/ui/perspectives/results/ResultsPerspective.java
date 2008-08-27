package net.sf.taverna.t2.ui.perspectives.results;

import java.io.InputStream;

import javax.swing.ImageIcon;

import org.jdom.Element;

import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;

public class ResultsPerspective implements PerspectiveSPI{

	private boolean visible = true;
	
	public ImageIcon getButtonIcon() {
		return WorkbenchIcons.resultsPerspectiveIcon;
	}

	public InputStream getLayoutInputStream() {
		return getClass().getResourceAsStream("results-perspective.xml");
	}

	public String getText() {
		// TODO Auto-generated method stub
		return "Results";
	}

	public boolean isVisible() {
		// TODO Auto-generated method stub
		return visible;
	}

	public int positionHint() {
		// TODO Auto-generated method stub
		return 20;
	}

	public void setVisible(boolean visible) {
		// TODO Auto-generated method stub
		this.visible = visible;
	}

	public void update(Element layoutElement) {
		// TODO Auto-generated method stub
		
	}

}
