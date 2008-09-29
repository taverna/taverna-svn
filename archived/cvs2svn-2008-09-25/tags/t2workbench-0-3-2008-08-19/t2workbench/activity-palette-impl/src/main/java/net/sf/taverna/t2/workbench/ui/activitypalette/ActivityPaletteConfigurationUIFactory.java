package net.sf.taverna.t2.workbench.ui.activitypalette;

import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;

public class ActivityPaletteConfigurationUIFactory implements
		ConfigurationUIFactory {

	public boolean canHandle(String uuid) {
		return (uuid!=null && uuid.equals(getConfigurable().getUUID()));
	}

	public Configurable getConfigurable() {
		return ActivityPaletteConfiguration.getInstance();
	}

	public JPanel getConfigurationPanel() {
		return new ActivityPaletteConfigurationPanel();
	}

}
