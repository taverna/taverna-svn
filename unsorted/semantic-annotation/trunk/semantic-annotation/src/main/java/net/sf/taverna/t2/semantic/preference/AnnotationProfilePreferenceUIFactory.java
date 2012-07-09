/**
 * 
 */
package net.sf.taverna.t2.semantic.preference;

import javax.swing.JPanel;

import net.sf.taverna.t2.semantic.configuration.DefaultAnnotationProfileConfiguration;
import net.sf.taverna.t2.workbench.configuration.Configurable;
import net.sf.taverna.t2.workbench.configuration.ConfigurationUIFactory;

/**
 * @author alanrw
 *
 */
public class AnnotationProfilePreferenceUIFactory implements ConfigurationUIFactory {

	private JPanel configPanel;

	public AnnotationProfilePreferenceUIFactory() {
		super();
		configPanel = new AnnotationProfilePreferencePanel();
	}

	
	public JPanel getConfigurationPanel() {
		return configPanel;
	}

	public boolean canHandle(String uuid) {
		return uuid.equals(DefaultAnnotationProfileConfiguration.getConfigurationUuid());
	}

	public Configurable getConfigurable() {
		return DefaultAnnotationProfileConfiguration.getINSTANCE();
	}

}
