package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class DotTextAreaFactory implements UIComponentFactorySPI {

	public String getName() {
		return "Dot text area";
	}

	public ImageIcon getIcon() {
		return TavernaIcons.inputValueIcon;
	}

	public UIComponentSPI getComponent() {
		return new DotTextArea();
	}

}
