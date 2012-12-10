/**
 * 
 */
package net.sf.taverna.t2.component.preference;

import java.util.Map;

import javax.swing.JPanel;

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

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory#canHandle(java.lang.String)
	 */
	@Override
	public boolean canHandle(String uuid) {
		return (uuid.equals(pref.getUUID()));
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

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getCategory()
	 */
	@Override
	public String getCategory() {
		return "general";
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getDefaultPropertyMap()
	 */
	@Override
	public Map<String, String> getDefaultPropertyMap() {
		return null;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getFilePrefix()
	 */
	@Override
	public String getFilePrefix() {
		return pref.getFilePrefix();
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.workbench.configuration.Configurable#getUUID()
	 */
	@Override
	public String getUUID() {
		return pref.getUUID();
	}

}
