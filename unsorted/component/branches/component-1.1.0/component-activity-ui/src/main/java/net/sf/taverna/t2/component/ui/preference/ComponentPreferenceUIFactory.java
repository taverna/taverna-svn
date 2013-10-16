/**
 * 
 */
package net.sf.taverna.t2.component.ui.preference;

import java.util.Map;

import javax.swing.JPanel;

import net.sf.taverna.t2.component.preference.ComponentPreference;
import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;
import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;

/**
 * @author alanrw
 * 
 */
public class ComponentPreferenceUIFactory extends AbstractConfigurable
		implements ConfigurationUIFactory {

	public static final String DISPLAY_NAME = "Components";
	private final JPanel configPanel;
	private static ComponentPreference pref = ComponentPreference.getInstance();

	public ComponentPreferenceUIFactory() {
		super();
		this.configPanel = new ComponentPreferencePanel();
	}

	@Override
	public boolean canHandle(String uuid) {
		return (uuid.equals(pref.getUUID()));
	}

	@Override
	public Configurable getConfigurable() {
		return this;
	}

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
		return DISPLAY_NAME;
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
