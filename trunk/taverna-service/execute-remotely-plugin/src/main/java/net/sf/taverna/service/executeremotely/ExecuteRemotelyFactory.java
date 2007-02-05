package net.sf.taverna.service.executeremotely;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class ExecuteRemotelyFactory implements UIComponentFactorySPI {

	private static Logger logger = Logger.getLogger(ExecuteRemotelyFactory.class);
	
	public UIComponentSPI getComponent() {
		logger.info("Making new component " + this);
		return new ExecuteRemotelyPanel();
	}

	// FIXME: Replace with a "Run remotely" icon
	public ImageIcon getIcon() {
		return TavernaIcons.runIcon;
	}

	public String getName() {
		logger.info("Found mr. name");
		return "Execute remotely";
	}

}
