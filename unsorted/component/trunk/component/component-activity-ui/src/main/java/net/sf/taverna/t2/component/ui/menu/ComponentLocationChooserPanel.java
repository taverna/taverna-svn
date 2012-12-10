/**
 *
 */
package net.sf.taverna.t2.component.ui.menu;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.sf.taverna.t2.component.ComponentActivityConfigurationBean;
import net.sf.taverna.t2.component.registry.ComponentFamily;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.registry.ComponentVersion;
import net.sf.taverna.t2.component.registry.ComponentVersionIdentification;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentFamilyChooserPanel;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.commons.lang.StringUtils;

/**
 * @author alanrw
 *
 */
public class ComponentLocationChooserPanel extends ComponentFamilyChooserPanel {

	private static final String T2FLOW = ".t2flow";

	private JTextField componentNameField = new JTextField(20);

	public ComponentLocationChooserPanel(boolean editableFamily) {
		super(editableFamily);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridy = 2;
		this.add(new JLabel("Component name"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1;
		this.add(componentNameField, gbc);
	}

	public static void main(String[] args) {
		System.out.println("starting");
		JFrame frame = new JFrame();
		frame.add(new ComponentLocationChooserPanel(true));
		frame.setVisible(true);
		System.out.println("done");
	}

	public String getComponentName() {
		return componentNameField.getText();
	}

	public void setComponentName(String name) {
		componentNameField.setText(name);
	}

	public ComponentActivityConfigurationBean saveComponent(Dataflow d) throws ComponentRegistryException {
		String componentName = StringUtils.remove(getComponentName(), T2FLOW);

			ComponentFamily familyChoice = getFamilyChoice();
			ComponentVersion version = familyChoice.createComponentBasedOn(componentName, d);
			
			ComponentRegistry registry = familyChoice.getComponentRegistry();
			
			ComponentVersionIdentification ident = new ComponentVersionIdentification(registry.getRegistryBase(), familyChoice.getName(), version.getComponent().getName(), version.getVersionNumber());
			
		return new ComponentActivityConfigurationBean(ident);
	}
}
