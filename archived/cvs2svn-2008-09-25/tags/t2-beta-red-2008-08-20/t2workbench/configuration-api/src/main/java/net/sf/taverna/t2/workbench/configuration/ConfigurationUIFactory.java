package net.sf.taverna.t2.workbench.configuration;

import javax.swing.JPanel;

public interface ConfigurationUIFactory {
	public boolean canHandle(String uuid);
	public JPanel getConfigurationPanel();
	public Configurable getConfigurable();
}
