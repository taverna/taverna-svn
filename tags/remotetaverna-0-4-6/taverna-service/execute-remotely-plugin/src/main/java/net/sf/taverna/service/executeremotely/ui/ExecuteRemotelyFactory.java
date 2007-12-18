package net.sf.taverna.service.executeremotely.ui;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.spi.UIComponentFactorySPI;
import org.embl.ebi.escience.scuflui.spi.UIComponentSPI;

public class ExecuteRemotelyFactory implements UIComponentFactorySPI {

	static ImageIcon ICON =	ExecuteRemotelyPerspective.ICON;

	private static Logger logger = Logger
			.getLogger(ExecuteRemotelyFactory.class);

	public UIComponentSPI getComponent() {
		logger.info("Making new component " + this);
		return new ExecuteRemotelyPanel();
	}

	// FIXME: Replace with a "Run remotely" icon
	public ImageIcon getIcon() {
		return ICON;
	}

	public String getName() {
		return ExecuteRemotelyPerspective.NAME;
	}

}
