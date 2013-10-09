/**
 *
 */
package net.sf.taverna.t2.component.ui.panel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.component.api.Family;
import net.sf.taverna.t2.component.api.Registry;
import net.sf.taverna.t2.component.api.Version;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;

import org.apache.commons.lang.StringUtils;

/**
 * @author alanrw
 * 
 */
public class RegisteryAndFamilyChooserComponentEntryPanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = -6675545311458594678L;

	private static final String T2FLOW = ".t2flow";

	private JTextField componentNameField = new JTextField(20);

	private RegistryAndFamilyChooserPanel registryAndFamilyChooserPanel = new RegistryAndFamilyChooserPanel();

	public RegisteryAndFamilyChooserComponentEntryPanel() {

		this.setLayout(new GridBagLayout());

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		this.add(registryAndFamilyChooserPanel, gbc);
		gbc.gridy = 1;

		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		this.add(new JLabel("Component name:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		this.add(componentNameField, gbc);
	}

	public String getComponentName() {
		return componentNameField.getText();
	}

	public void setComponentName(String name) {
		componentNameField.setText(name);
	}

	public Version.ID getComponentVersionIdentification() {
		String componentName = getComponentName();

		Family familyChoice = registryAndFamilyChooserPanel.getChosenFamily();

		Registry registry = registryAndFamilyChooserPanel.getChosenRegistry();

		if ((familyChoice == null) || (registry == null)
				|| (componentName == null) || componentName.isEmpty()) {
			return null;
		}

		componentName = StringUtils.remove(componentName, T2FLOW);
		Version.ID ident = new ComponentVersionIdentification(
				registry.getRegistryBase(), familyChoice.getName(),
				componentName, -1);
		return ident;
	}
}
