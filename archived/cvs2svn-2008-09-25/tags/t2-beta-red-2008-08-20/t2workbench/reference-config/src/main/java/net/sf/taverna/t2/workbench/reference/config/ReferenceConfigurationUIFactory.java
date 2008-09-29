package net.sf.taverna.t2.workbench.reference.config;

import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;

public class ReferenceConfigurationUIFactory implements ConfigurationUIFactory {
	
	public boolean canHandle(String uuid) {
		return uuid.equals(getConfigurable().getUUID());
	}

	public JPanel getConfigurationPanel() {
		return new ReferenceConfigurationPanel();
	}

	public Configurable getConfigurable() {
		return ReferenceConfiguration.getInstance();
	}
	
}
