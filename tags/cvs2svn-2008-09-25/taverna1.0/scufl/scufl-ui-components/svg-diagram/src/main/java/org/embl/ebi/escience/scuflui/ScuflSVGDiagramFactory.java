package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class ScuflSVGDiagramFactory implements UIComponentFactorySPI {

	public String getName() {
		return "Simple workflow diagram";
	}
	
	public ImageIcon getIcon() {
		return TavernaIcons.windowDiagram;
	}

	public UIComponentSPI getComponent() {
		return new ScuflSVGDiagram(false, false);
	}
	
}
