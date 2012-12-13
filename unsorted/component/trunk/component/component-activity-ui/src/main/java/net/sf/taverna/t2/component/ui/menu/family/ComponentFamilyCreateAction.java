/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu.family;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.sf.taverna.t2.component.profile.ComponentProfile;
import net.sf.taverna.t2.component.registry.ComponentRegistry;
import net.sf.taverna.t2.component.registry.ComponentRegistryException;
import net.sf.taverna.t2.component.ui.panel.ProfileChooserPanel;
import net.sf.taverna.t2.component.ui.panel.RegistryChooserPanel;
import net.sf.taverna.t2.component.ui.serviceprovider.ComponentServiceIcon;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public class ComponentFamilyCreateAction extends AbstractAction {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7780471499146286881L;



	private static Logger logger = Logger.getLogger(ComponentFamilyCreateAction.class);
	

	
	private static final String CREATE_FAMILY = "Create family...";
	
	public ComponentFamilyCreateAction() {
		super (CREATE_FAMILY, ComponentServiceIcon.getIcon());
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		JPanel overallPanel = new JPanel();
		overallPanel.setLayout(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		RegistryChooserPanel registryPanel = new RegistryChooserPanel();
		
		gbc.insets = new Insets(0, 5, 0, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		overallPanel.add(registryPanel, gbc);
		
		ProfileChooserPanel profilePanel = new ProfileChooserPanel();
		registryPanel.addObserver(profilePanel);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weighty = 1;
		overallPanel.add(profilePanel, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		overallPanel.add(new JLabel("Component family name:"), gbc);
		
		gbc.gridx = 1;
		gbc.weightx = 1;
		JTextField familyNameField = new JTextField(80);
		overallPanel.add(familyNameField, gbc);
		
		int answer = JOptionPane.showConfirmDialog(null, overallPanel, "Create Component Family", JOptionPane.OK_CANCEL_OPTION);
		if (answer == JOptionPane.OK_OPTION) {
			ComponentRegistry chosenRegistry = registryPanel.getChosenRegistry();
			if (chosenRegistry == null) {
				JOptionPane.showMessageDialog(null, "Unable to determine registry", "Component Registry Problem", JOptionPane.ERROR_MESSAGE);
				return;
			}
			ComponentProfile chosenProfile = profilePanel.getChosenProfile();
			if (chosenProfile == null) {
				JOptionPane.showMessageDialog(null, "Unable to determine profile", "Component Profile Problem", JOptionPane.ERROR_MESSAGE);
				return;
			}
			String newName = familyNameField.getText();
			
			try {
				boolean alreadyUsed = (chosenRegistry.getComponentFamily(newName) != null);
				if (alreadyUsed) {
					JOptionPane.showMessageDialog(null, newName + " is already used", "Duplicate component family name", JOptionPane.ERROR_MESSAGE);
				} else {
					chosenRegistry.createComponentFamily(newName, chosenProfile);
				}
			} catch (ComponentRegistryException e) {
				logger.error(e);
			}
		}

	}

}
