package uk.org.mygrid.logbook.ui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class LogBookUIFactory implements UIComponentFactorySPI {

	public String getName() {
		return "LogBook";
	}

	public ImageIcon getIcon() {
		return LogBookIcons.logBookIcon;
	}

	public UIComponentSPI getComponent() {
		return new LogBookUI();
	}

}
