package net.sf.taverna.t2.workbench.ui.impl.configuration;

import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;

public class WorkbenchConfigurationUIFactory implements ConfigurationUIFactory {
	
	public boolean canHandle(String uuid) {
		return uuid.equals(WorkbenchConfiguration.uuid);
	}

	public JPanel getConfigurationPanel() {
		return new WorkbenchConfigurationPanel();
	}

	public Configurable getConfigurable() {
		return WorkbenchConfiguration.getInstance();
	}
	
}
