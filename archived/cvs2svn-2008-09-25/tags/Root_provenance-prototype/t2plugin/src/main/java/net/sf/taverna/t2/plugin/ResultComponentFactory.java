package net.sf.taverna.t2.plugin;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class ResultComponentFactory implements UIComponentFactorySPI {

	private static UIComponentSPI component;
	
	public UIComponentSPI getComponent() {
		if (component == null) {
			component = new ResultComponent();
		}
		return component;
	}

	public ImageIcon getIcon() {
		return null;
	}

	public String getName() {
		return "Results Panel";
	}

}
