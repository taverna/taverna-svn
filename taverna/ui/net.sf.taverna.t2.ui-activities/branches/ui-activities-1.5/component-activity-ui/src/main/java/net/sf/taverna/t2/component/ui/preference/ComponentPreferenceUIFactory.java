/**
 * 
 */
package net.sf.taverna.t2.component.ui.preference;

import javax.swing.JPanel;

import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;

/**
 * @author alanrw
 * 
 */
public class ComponentPreferenceUIFactory
		implements ConfigurationUIFactory {

	public static final String DISPLAY_NAME = "Components";
	private final JPanel configPanel;
	private static ComponentPreference pref = ComponentPreference.getInstance();

	public ComponentPreferenceUIFactory() {
		super();
		configPanel = new ComponentPreferencePanel();
	}

	@Override
	public boolean canHandle(String uuid) {
		return uuid.equals(pref.getUUID());
	}

	@Override
	public Configurable getConfigurable() {
		return pref;
	}

	@Override
	public JPanel getConfigurationPanel() {
		return this.configPanel;
	}

}
