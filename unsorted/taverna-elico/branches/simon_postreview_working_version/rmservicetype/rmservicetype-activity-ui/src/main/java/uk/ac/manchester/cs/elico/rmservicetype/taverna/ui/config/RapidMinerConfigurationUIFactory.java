package uk.ac.manchester.cs.elico.rmservicetype.taverna.ui.config;

import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;
import uk.ac.manchester.cs.elico.rmservicetype.taverna.config.RapidMinerPluginConfiguration;

public class RapidMinerConfigurationUIFactory implements ConfigurationUIFactory {

	public boolean canHandle(String uuid) {
		return uuid.equals(getConfigurable().getUUID());	}

	public Configurable getConfigurable() {
		return RapidMinerPluginConfiguration.getInstance();
	}

	public JPanel getConfigurationPanel() {
		return new RapidMinerPluginPreferencesPlugin();
	}

}
