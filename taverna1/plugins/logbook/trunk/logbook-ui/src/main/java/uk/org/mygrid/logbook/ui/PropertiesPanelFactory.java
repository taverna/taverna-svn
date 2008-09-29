package uk.org.mygrid.logbook.ui;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class PropertiesPanelFactory implements UIComponentFactorySPI {

	public String getName() {
		return PropertiesPanel.LOG_BOOK_SETTINGS;
	}

	public ImageIcon getIcon() {
		return TavernaIcons.databaseIcon;
	}

	public UIComponentSPI getComponent() {
		return new PropertiesPanel();
	}

}
