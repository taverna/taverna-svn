package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class XScuflTreeFactory implements UIComponentFactorySPI {

	public String getName() {
		return "XScufl tree view";
	}

	public ImageIcon getIcon() {
		return TavernaIcons.xmlNodeIcon;
	}

	public UIComponentSPI getComponent() {
		return new XScuflTree();
	}

}
