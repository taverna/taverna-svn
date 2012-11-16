/**
 *
 */
package net.sf.taverna.t2.activities.interaction.preference;

import java.util.Map;

import javax.swing.JPanel;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;
import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;

/**
 * @author alanrw
 *
 */
public class InteractionPreferenceUIFactory extends AbstractConfigurable implements ConfigurationUIFactory  {

	private final JPanel configPanel;
	private static InteractionPreference pref = InteractionPreference.getInstance();

	public InteractionPreferenceUIFactory() {
		super();
		this.configPanel = new InteractionPreferencePanel();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory#canHandle(java.lang.String)
	 */
	@Override
	public boolean canHandle(final String uuid) {
		return uuid.equals(pref.getUUID());
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory#getConfigurable()
	 */
	@Override
	public Configurable getConfigurable() {
		return this;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory#getConfigurationPanel()
	 */
	@Override
	public JPanel getConfigurationPanel() {
		return this.configPanel;
	}

	@Override
	public String getCategory() {
		return "general";
	}

	@Override
	public Map<String, String> getDefaultPropertyMap() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return "Interaction";
	}

	@Override
	public String getFilePrefix() {
		return pref.getFilePrefix();
	}

	@Override
	public String getUUID() {
		return pref.getUUID();
	}

}
