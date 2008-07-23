package net.sf.taverna.t2.plugin;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class T2ComponentFactory implements UIComponentFactorySPI {

	public UIComponentSPI getComponent() {
		return new T2Component();
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Taverna 2 Component";
	}

}
